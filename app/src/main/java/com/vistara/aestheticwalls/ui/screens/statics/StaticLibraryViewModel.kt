package com.vistara.aestheticwalls.ui.screens.statics

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.model.WallpaperCategory
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.remote.ApiUsageTracker
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
 * 静态壁纸库ViewModel
 * 管理静态壁纸数据和状态
 */
@HiltViewModel
class StaticLibraryViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
    private val apiUsageTracker: ApiUsageTracker,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 20
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
        // 防止重复请求
        if (_isLoadingMore.value || _isRefreshing.value) {
            Log.d("StaticLibraryViewModel", "正在加载中，跳过请求")
            return
        }

        viewModelScope.launch {
            // 如果是刷新操作，设置刷新状态并重置分页
            if (isRefresh) {
                _isRefreshing.value = true
                _currentPage.value = 1
                _wallpapers.value = emptyList()
                Log.d("StaticLibraryViewModel", "刷新壁纸数据，重置到第1页")
            } else if (_currentPage.value == 1) {
                // 首次加载或切换分类后的加载，显示加载状态
                _wallpapersState.value = UiState.Loading
                Log.d("StaticLibraryViewModel", "首次加载壁纸数据，第1页")
            } else {
                // 加载更多时，设置加载更多状态
                _isLoadingMore.value = true
                Log.d("StaticLibraryViewModel", "加载更多壁纸，当前页码: ${_currentPage.value}")
            }

            try {

                // 根据是否有分类筛选决定调用哪个API
                val result = if (categoryFilter != null && categoryFilter != "全部") {
                    // 优先使用Unsplash和Pexels，因为这两个API比较稳定
                    val apiSources = listOf("unsplash", "pexels")

                    // 使用固定的索引而不是随机索引，确保每次都使用相同的API源
                    // 使用分类名称的长度作为索引，确保稳定性
                    val apiSource = apiSources[categoryFilter.length % apiSources.size]
                    val categoryId = "${apiSource}_${categoryFilter.lowercase()}"

                    Log.d("StaticLibraryViewModel", "使用分类ID: $categoryId 用于筛选: $categoryFilter")

                    // 如果是刷新操作或首次加载，尝试使用分类请求
                    if (isRefresh || _currentPage.value == 1) {
                        try {
                            val categoryResult = wallpaperRepository.getWallpapersByCategory(
                                categoryId, _currentPage.value, PAGE_SIZE
                            )

                            // 如果分类请求成功且返回了数据，直接使用
                            if (categoryResult is ApiResult.Success && categoryResult.data.isNotEmpty()) {
                                Log.d("StaticLibraryViewModel", "使用 $apiSource 获取分类成功，返回 ${categoryResult.data.size} 个壁纸")
                                categoryResult
                            }
                            // 如果分类请求失败或没有数据，尝试使用默认的壁纸获取方法
                            else {
                                Log.w("StaticLibraryViewModel", "使用 $apiSource 获取分类失败或没有数据，尝试使用默认API")
                                wallpaperRepository.getWallpapers(
                                    "static", _currentPage.value, PAGE_SIZE
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("StaticLibraryViewModel", "获取分类壁纸异常: ${e.message}")
                            // 如果发生异常，尝试使用默认的壁纸获取方法
                            wallpaperRepository.getWallpapers(
                                "static", _currentPage.value, PAGE_SIZE
                            )
                        }
                    }
                    // 如果是加载更多，直接使用分类请求
                    else {
                        try {
                            wallpaperRepository.getWallpapersByCategory(
                                categoryId, _currentPage.value, PAGE_SIZE
                            )
                        } catch (e: Exception) {
                            Log.e("StaticLibraryViewModel", "加载更多分类壁纸异常: ${e.message}")
                            // 如果发生异常，返回空结果
                            ApiResult.Success(emptyList())
                        }
                    }
                }
                // 如果是全部分类，使用默认的壁纸获取方法
                else {
                    Log.d("StaticLibraryViewModel", "使用默认壁纸获取方法")
                    wallpaperRepository.getWallpapers(
                        "static", _currentPage.value, PAGE_SIZE
                    )
                }

                when (result) {
                    is ApiResult.Success -> {
                        // 如果返回的数据少于页面大小，说明没有更多数据了
                        _canLoadMore.value = result.data.size >= PAGE_SIZE

                        // 如果是刷新或首次加载，直接设置数据
                        // 否则将新数据添加到现有数据中
                        val newWallpapers = if (isRefresh || _currentPage.value == 1) {
                            Log.d("StaticLibraryViewModel", "刷新或首次加载，设置 ${result.data.size} 个壁纸")
                            // 如果是切换分类，清空当前壁纸列表
                            _wallpapers.value = emptyList()
                            result.data
                        } else {
                            Log.d("StaticLibraryViewModel", "加载更多，添加 ${result.data.size} 个壁纸，总计 ${_wallpapers.value.size + result.data.size} 个")
                            _wallpapers.value + result.data
                        }

                        // 更新壁纸列表和状态
                        _wallpapers.value = newWallpapers
                        _wallpapersState.value = UiState.Success(newWallpapers)

                        Log.d("StaticLibraryViewModel", "更新壁纸列表和状态，当前页码: ${_currentPage.value}, 壁纸数量: ${newWallpapers.size}")

                        // 如果不是刷新操作且有数据返回，增加页码
                        if (!isRefresh && result.data.isNotEmpty()) {
                            val nextPage = _currentPage.value + 1
                            Log.d("StaticLibraryViewModel", "增加页码从 ${_currentPage.value} 到 $nextPage")
                            _currentPage.value = nextPage
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
                _wallpapersState.value = UiState.Error(e.message ?: "加载壁纸失败")
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
     * 加载更多壁纸
     */
    fun loadMore() {
        // 检查是否正在加载或者没有更多数据
        if (_isLoadingMore.value || !_canLoadMore.value) {
            Log.d(
                "StaticLibraryViewModel",
                "loadMore aborted due to isLoadingMore: ${_isLoadingMore.value} or !canLoadMore: ${!_canLoadMore.value}"
            )
            return
        }

        Log.d("StaticLibraryViewModel", "加载更多壁纸，当前页码: ${_currentPage.value}")

        // 使用当前选中的分类加载更多
        val currentCategory = _selectedCategory.value
        val categoryFilter = if (currentCategory != WallpaperCategory.ALL) {
            currentCategory.apiValue
        } else null
        loadWallpapers(false, categoryFilter)
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

        Log.d("StaticLibraryViewModel", "切换到分类: ${category.name}, 重置分页参数")

        // 如果是切换到全部分类，尝试使用缓存数据
        if (category == WallpaperCategory.ALL) {
            Log.d("StaticLibraryViewModel", "切换到全部分类，使用默认数据源")
            // 对于全部分类，使用默认数据源，避免发送多个请求
            loadWallpapers(true, null)
            return
        }

        // 对于其他分类，加载新分类的壁纸
        val categoryFilter = category.apiValue
        Log.d("StaticLibraryViewModel", "切换到分类: $categoryFilter")
        loadWallpapers(true, categoryFilter)
    }

    /**
     * 根据分类筛选壁纸
     * @param categoryResId 分类资源ID
     */
    fun filterByCategory(categoryResId: Int) {
        // 将资源ID转换为枚举类型
        val category = when (categoryResId) {
            R.string.category_all -> WallpaperCategory.ALL
            R.string.category_nature -> WallpaperCategory.NATURE
            R.string.category_city -> WallpaperCategory.CITY
            R.string.category_abstract -> WallpaperCategory.ABSTRACT
            R.string.category_minimal -> WallpaperCategory.MINIMAL
            R.string.category_animals -> WallpaperCategory.ANIMALS
            R.string.category_food -> WallpaperCategory.FOOD
            R.string.category_architecture -> WallpaperCategory.ARCHITECTURE
            R.string.category_art -> WallpaperCategory.ART
            R.string.category_space -> WallpaperCategory.SPACE
            R.string.category_illustration -> WallpaperCategory.ILLUSTRATION
            R.string.category_technology -> WallpaperCategory.TECHNOLOGY
            else -> WallpaperCategory.ALL
        }

        filterByCategory(category)
        // 以下代码已经在上面的 filterByCategory(WallpaperCategory) 方法中实现，不需要重复
        /*
        viewModelScope.launch {
            // 重置分页参数
            _currentPage.value = 1
            _canLoadMore.value = true

            // 如果选择"全部"，则重新加载所有壁纸
            if (categoryResId == R.string.category_all) {
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
                    val categoryName = context.getString(categoryResId)
                    val filtered = allWallpapers.filter { wallpaper ->
                        wallpaper.tags.any { it.contains(categoryName, ignoreCase = true) }
                    }

                    if (filtered.isNotEmpty()) {
                        _wallpapersState.value = UiState.Success(filtered)
                    } else {
                        // 如果筛选后没有数据，尝试从服务器按分类加载
                        _wallpapers.value = emptyList()
                        loadWallpapers(true, categoryName)
                    }
                } else {
                    // 如果没有数据，按分类从服务器加载
                    _wallpapersState.value = UiState.Loading
                    _wallpapers.value = emptyList()
                    val categoryName = context.getString(categoryResId)
                    loadWallpapers(true, categoryName)
                }
            } catch (e: Exception) {
                _wallpapersState.value = UiState.Error(e.message ?: context.getString(R.string.error_filtering_wallpapers))
            }
        }*/
    }

    /**
     * 刷新壁纸数据
     */
    fun refresh() {
        // 重置所有API速率限制
        apiUsageTracker.resetAllRateLimits()
        Log.d("StaticLibraryViewModel", "已重置所有API速率限制")

        // 重置所有API统计数据
        apiUsageTracker.resetAllStats()
        Log.d("StaticLibraryViewModel", "已重置所有API统计数据")

        // 重置分页参数
        _currentPage.value = 1
        _canLoadMore.value = true
        _isLoadingMore.value = false
        _isRefreshing.value = true  // 设置为刷新状态
        _wallpapers.value = emptyList()
        _wallpapersState.value = UiState.Loading

        Log.d("StaticLibraryViewModel", "刷新壁纸数据，重置分页参数")

        // 使用当前选中的分类进行刷新
        val currentCategory = _selectedCategory.value
        val categoryFilter = if (currentCategory != WallpaperCategory.ALL) {
            currentCategory.apiValue
        } else null

        // 加载数据，指定为刷新操作
        loadWallpapers(true, categoryFilter)
    }
}
