package com.vistara.aestheticwalls.ui.screens.lives

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.billing.BillingConnectionState
import com.vistara.aestheticwalls.billing.BillingManager
import com.vistara.aestheticwalls.billing.PurchaseState
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.repository.UserRepository
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 动态壁纸库ViewModel
 * 管理动态壁纸数据和状态
 */
@HiltViewModel
class LiveLibraryViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
    private val userRepository: UserRepository,
    private val billingManager: BillingManager
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 20
        private const val TAG = "LiveLibraryViewModel"
    }

    // 壁纸数据状态
    private val _wallpapersState = MutableStateFlow<UiState<List<Wallpaper>>>(UiState.Loading)
    val wallpapersState: StateFlow<UiState<List<Wallpaper>>> = _wallpapersState.asStateFlow()

    // 分类数据
    val categories = listOf(
        "全部", "抽象", "科技感", "自然", "赛博朋克", "粒子", "流体", "风景", "人像"
    )

    // 当前选中的分类
    private val _selectedCategory = MutableStateFlow("全部")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // 分页加载相关状态
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _canLoadMore = MutableStateFlow(true)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // 会员状态
    private val _isPremiumUser = MutableStateFlow(false)
    val isPremiumUser: StateFlow<Boolean> = _isPremiumUser.asStateFlow()

    // 计费连接状态
    private val _billingConnectionState = MutableStateFlow(BillingConnectionState.DISCONNECTED)
    val billingConnectionState: StateFlow<BillingConnectionState> = _billingConnectionState.asStateFlow()

    // 升级结果
    private val _upgradeResult = MutableStateFlow<UpgradeResult?>(null)
    val upgradeResult: StateFlow<UpgradeResult?> = _upgradeResult.asStateFlow()

    // 当前已加载的壁纸列表
    private val _wallpapers = MutableStateFlow<List<Wallpaper>>(emptyList())

    init {
        loadWallpapers()
        checkPremiumStatus()
        observeBillingState()
        observePurchaseState()
    }

    /**
     * 观察计费状态
     */
    private fun observeBillingState() {
        viewModelScope.launch {
            billingManager.connectionState.collectLatest { state ->
                _billingConnectionState.value = state
                Log.d(TAG, "Billing connection state: $state")
            }
        }
    }

    /**
     * 观察购买状态
     */
    private fun observePurchaseState() {
        viewModelScope.launch {
            billingManager.purchaseState.collectLatest { state ->
                when (state) {
                    is PurchaseState.Pending -> {
                        // 处理购买进行中状态
                    }
                    is PurchaseState.Completed -> {
                        _isPremiumUser.value = true
                        _upgradeResult.value = UpgradeResult.Success("升级成功！感谢您的支持")
                    }
                    is PurchaseState.Failed -> {
                        _upgradeResult.value = UpgradeResult.Error("升级失败: ${state.message}")
                    }
                    is PurchaseState.Cancelled -> {
                        _upgradeResult.value = UpgradeResult.Error("升级已取消")
                    }
                    else -> {
                        // 其他状态不处理
                    }
                }
            }
        }
    }

    /**
     * 检查用户会员状态
     */
    private fun checkPremiumStatus() {
        viewModelScope.launch {
            try {
                val isPremium = userRepository.isPremiumUser.first()
                _isPremiumUser.value = isPremium
                Log.d(TAG, "Premium status: $isPremium")
            } catch (e: Exception) {
                Log.e(TAG, "Error checking premium status: ${e.message}")
                _isPremiumUser.value = false
            }
        }
    }

    /**
     * 加载壁纸数据
     * @param isRefresh 是否是刷新操作，如果是则重置分页参数
     * @param categoryFilter 分类筛选，如果不为null则按分类加载
     */
    fun loadWallpapers(isRefresh: Boolean = false, categoryFilter: String? = null) {
        viewModelScope.launch {
            // 如果是刷新操作，设置刷新状态并重置分页
            if (isRefresh) {
                Log.d(TAG, "Refreshing, resetting page to 1")
                _isRefreshing.value = true
                _currentPage.value = 1
                _wallpapers.value = emptyList()
            } else if (_currentPage.value == 1) {
                // 首次加载或切换分类后的加载，显示加载状态
                _wallpapersState.value = UiState.Loading
            } else {
                // 加载更多时，设置加载更多状态
                Log.d(TAG, "Loading more, page: ${_currentPage.value}")
                _isLoadingMore.value = true
            }

            try {
                // 根据是否有分类筛选决定调用哪个API
                val result = if (categoryFilter != null && categoryFilter != "全部") {
                    // 使用分类ID格式："pexels_分类名称"
                    val categoryId = "pexels_${categoryFilter.lowercase()}"
                    wallpaperRepository.getWallpapersByCategory(
                        categoryId,
                        _currentPage.value,
                        PAGE_SIZE
                    )
                } else {
                    wallpaperRepository.getWallpapers(
                        "live",
                        _currentPage.value,
                        PAGE_SIZE
                    )
                }

                when (result) {
                    is ApiResult.Success -> {
                        // 如果返回的数据少于页面大小，说明没有更多数据了
                        _canLoadMore.value = result.data.size >= PAGE_SIZE

                        // 如果是刷新或首次加载，直接设置数据
                        // 否则将新数据添加到现有数据中
                        val newWallpapers = if (isRefresh || _currentPage.value == 1) {
                            result.data
                        } else {
                            _wallpapers.value + result.data
                        }

                        _wallpapers.value = newWallpapers
                        _wallpapersState.value = UiState.Success(newWallpapers)

                        // 如果不是刷新操作且有数据返回，增加页码
                        if (!isRefresh && result.data.isNotEmpty()) {
                            _currentPage.value = _currentPage.value + 1
                        }
                    }
                    is ApiResult.Error -> {
                        _wallpapersState.value = UiState.Error(result.message)
                    }
                    is ApiResult.Loading -> {
                        // 已经设置了Loading状态，不需要额外处理
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during loading: ${e.message}", e)
                _wallpapersState.value = UiState.Error(e.message ?: "加载壁纸失败")
            } finally {
                // 无论成功失败，都重置加载状态
                _isRefreshing.value = false
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * 根据分类筛选壁纸
     * @param category 分类名称
     */
    fun filterByCategory(category: String) {
        // 如果当前已经是这个分类，不需要重复筛选
        if (_selectedCategory.value == category) return

        Log.d(TAG, "Filtering by category: $category")

        // 先更新选中的分类，这样UI可以立即响应
        _selectedCategory.value = category

        // 在单独的协程中处理数据加载，避免阻塞UI线程
        viewModelScope.launch {
            // 重置分页参数
            _currentPage.value = 1
            _canLoadMore.value = true

            // 如果选择"全部"，则重新加载所有壁纸
            if (category == "全部") {
                // 先设置加载状态，然后再清空列表，避免闪烁
                _wallpapersState.value = UiState.Loading
                _wallpapers.value = emptyList()
                loadWallpapers(true, null)
                return@launch
            }

            // 否则，根据分类筛选
            try {
                // 使用现有数据进行筛选，避免重复网络请求
                val allWallpapers = _wallpapers.value

                if (allWallpapers.isNotEmpty()) {
                    // 先设置加载状态
                    _wallpapersState.value = UiState.Loading

                    // 在后台进行筛选，不会阻塞UI
                    val filtered = allWallpapers.filter { wallpaper ->
                        wallpaper.tags.any { it.contains(category, ignoreCase = true) }
                    }

                    if (filtered.isNotEmpty()) {
                        Log.d(TAG, "Filtered wallpapers: ${filtered.size}")
                        _wallpapersState.value = UiState.Success(filtered)
                    } else {
                        // 如果筛选后没有数据，尝试从服务器按分类加载
                        Log.d(TAG, "No filtered results, loading from server with category: $category")
                        _wallpapers.value = emptyList()
                        loadWallpapers(true, category)
                    }
                } else {
                    // 如果没有数据，按分类从服务器加载
                    Log.d(TAG, "No existing data, loading from server with category: $category")
                    _wallpapersState.value = UiState.Loading
                    _wallpapers.value = emptyList()
                    loadWallpapers(true, category)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error filtering by category: ${e.message}", e)
                _wallpapersState.value = UiState.Error(e.message ?: "筛选壁纸失败")
            }
        }
    }

    /**
     * 刷新壁纸数据
     */
    fun refresh() {
        Log.d(TAG, "refresh called")
        // 使用当前选中的分类进行刷新
        val currentCategory = _selectedCategory.value
        val categoryFilter = if (currentCategory != "全部") currentCategory else null
        loadWallpapers(true, categoryFilter)
    }

    /**
     * 加载更多壁纸
     */
    fun loadMore() {
        Log.d(TAG, "loadMore called, isLoadingMore: ${_isLoadingMore.value}, canLoadMore: ${_canLoadMore.value}")
        if (_isLoadingMore.value || !_canLoadMore.value) {
            Log.d(TAG, "loadMore aborted due to isLoadingMore: ${_isLoadingMore.value} or !canLoadMore: ${!_canLoadMore.value}")
            return
        }
        Log.d(TAG, "loadMore executing loadWallpapers(false)")
        // 使用当前选中的分类加载更多
        val currentCategory = _selectedCategory.value
        val categoryFilter = if (currentCategory != "全部") currentCategory else null
        loadWallpapers(false, categoryFilter)
    }

    /**
     * 升级到高级版
     */
    fun upgradeToPremium(activity: Activity?) {
        if (_isPremiumUser.value) {
            _upgradeResult.value = UpgradeResult.Error("您已经是高级用户")
            return
        }

        if (_billingConnectionState.value != BillingConnectionState.CONNECTED) {
            _upgradeResult.value = UpgradeResult.Error("支付服务未连接，请稍后再试")
            return
        }

        // 默认使用月度订阅
        billingManager.launchBillingFlow(activity, BillingManager.SUBSCRIPTION_MONTHLY)
    }

    /**
     * 清除升级结果
     */
    fun clearUpgradeResult() {
        _upgradeResult.value = null
    }

    /**
     * 升级结果
     */
    sealed class UpgradeResult {
        data class Success(val message: String) : UpgradeResult()
        data class Error(val message: String) : UpgradeResult()
    }
}
