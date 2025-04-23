package com.vistara.aestheticwalls.ui.screens.lives

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.model.WallpaperCategory
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import com.vistara.aestheticwalls.utils.RefreshUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 动态壁纸库ViewModel
 * 管理动态壁纸数据和状态
 */
@HiltViewModel
class LiveLibraryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wallpaperRepository: WallpaperRepository,
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 20
        private const val TAG = "LiveLibraryViewModel"
    }

    // 壁纸数据状态
    private val _wallpapersState = MutableStateFlow<UiState<List<Wallpaper>>>(UiState.Loading)
    val wallpapersState: StateFlow<UiState<List<Wallpaper>>> = _wallpapersState.asStateFlow()

    // 分类数据
    val categories = WallpaperCategory.getAllCategories()

    // 当前选中的分类
    private val _selectedCategory = MutableStateFlow(WallpaperCategory.ALL)
    val selectedCategory: StateFlow<WallpaperCategory> = _selectedCategory.asStateFlow()

    // 分页加载相关状态
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _canLoadMore = MutableStateFlow(true)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // 当前已加载的壁纸列表
    private val _wallpapers = MutableStateFlow<List<Wallpaper>>(emptyList())

    init {
        loadWallpapers()
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
                val result = if (categoryFilter != null) {
                    // 对于动态壁纸，我们需要直接使用搜索视频的API
                    // 因为getWallpapersByCategory方法只会返回静态壁纸
                    Log.d(TAG, "Searching videos with category filter: $categoryFilter")

                    // 使用类型为"live"的API获取动态壁纸，并传入分类参数
                    // 这会调用WallpaperRepositoryImpl中的getWallpapers方法，该方法会根据类型返回不同的壁纸
                    // 但是我们需要修改WallpaperRepositoryImpl中的getWallpapers方法，使其能够处理分类参数

                    // 对于动态壁纸，我们需要使用分类参数搜索视频
                    Log.d(TAG, "Searching videos with category filter: $categoryFilter")

                    // 使用类型为"live"的API获取动态壁纸，并传入分类参数
                    // 这里我们使用一个特殊的参数格式来传递分类信息
                    // 在WallpaperRepositoryImpl中的getWallpapers方法中处理这个参数
                    val liveType = if (categoryFilter != null) {
                        "live:$categoryFilter"
                    } else {
                        "live"
                    }

                    Log.d(TAG, "Using live type with category: $liveType")
                    wallpaperRepository.getWallpapers(
                        liveType, _currentPage.value, PAGE_SIZE
                    )
                } else {
                    // 如果没有分类筛选，直接使用"live"类型的API
                    Log.d(TAG, "No category filter, using live API")
                    wallpaperRepository.getWallpapers(
                        "live", _currentPage.value, PAGE_SIZE
                    )
                }

                when (result) {
                    is ApiResult.Success -> {
                        // 如果返回的数据少于页面大小，说明没有更多数据了
                        _canLoadMore.value = result.data.size >= PAGE_SIZE

                        // 如果是刷新或首次加载，直接设置数据
                        // 否则将新数据添加到现有数据中
                        val newWallpapers = if (isRefresh || _currentPage.value == 1) {
                            Log.d(TAG, "刷新或首次加载，设置 ${result.data.size} 个壁纸")
                            // 如果是切换分类，清空当前壁纸列表
                            _wallpapers.value = emptyList()
                            result.data
                        } else {
                            Log.d(TAG, "加载更多，添加 ${result.data.size} 个壁纸，总计 ${_wallpapers.value.size + result.data.size} 个")
                            _wallpapers.value + result.data
                        }

                        // 更新壁纸列表和状态
                        _wallpapers.value = newWallpapers
                        _wallpapersState.value = UiState.Success(newWallpapers)

                        Log.d(TAG, "更新壁纸列表和状态，当前页码: ${_currentPage.value}, 壁纸数量: ${newWallpapers.size}")

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
                _wallpapersState.value =
                    UiState.Error(e.message ?: context.getString(R.string.error_loading_wallpapers))
            } finally {
                // 无论成功失败，都重置加载状态
                if (isRefresh) {
                    // 使用延迟结束刷新状态，改善用户体验
                    RefreshUtil.delayedEndRefreshing(_isRefreshing, viewModelScope)
                } else {
                    _isRefreshing.value = false
                }
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * 根据分类筛选壁纸
     * @param category 分类枚举
     */
    fun filterByCategory(category: WallpaperCategory) {
        // 如果当前已经是这个分类，不需要重复筛选
        if (_selectedCategory.value == category) return

        // 先更新选中的分类，这样UI可以立即响应
        _selectedCategory.value = category

        // 重置分页参数
        _currentPage.value = 1
        _canLoadMore.value = true
        _isLoadingMore.value = false
        _isRefreshing.value = false

        // 设置加载状态，但不清空当前壁纸列表，避免滚动位置重置
        _wallpapersState.value = UiState.Loading

        Log.d(TAG, "切换到分类: ${category.name}, 重置分页参数")

        // 加载新分类的壁纸
        val categoryFilter = if (category != WallpaperCategory.ALL) {
            category.apiValue
        } else null
        loadWallpapers(true, categoryFilter)
    }

    /**
     * 刷新壁纸数据
     */
    fun refresh() {
        Log.d(TAG, "refresh called")
        // 使用当前选中的分类进行刷新
        val currentCategory = _selectedCategory.value
        val categoryFilter = if (currentCategory != WallpaperCategory.ALL) {
            currentCategory.apiValue
        } else null
        loadWallpapers(true, categoryFilter)
    }

    /**
     * 加载更多壁纸
     */
    fun loadMore() {
        Log.d(
            TAG,
            "loadMore called, isLoadingMore: ${_isLoadingMore.value}, canLoadMore: ${_canLoadMore.value}"
        )
        if (_isLoadingMore.value || !_canLoadMore.value) {
            Log.d(
                TAG,
                "loadMore aborted due to isLoadingMore: ${_isLoadingMore.value} or !canLoadMore: ${!_canLoadMore.value}"
            )
            return
        }
        Log.d(TAG, "loadMore executing loadWallpapers(false)")
        // 使用当前选中的分类加载更多
        val currentCategory = _selectedCategory.value
        val categoryFilter = if (currentCategory != WallpaperCategory.ALL) {
            currentCategory.apiValue
        } else null
        loadWallpapers(false, categoryFilter)
    }
}
