package com.vistara.aestheticwalls.ui.screens.statics

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 20
    }

    // 壁纸数据状态
    private val _wallpapersState = MutableStateFlow<UiState<List<Wallpaper>>>(UiState.Loading)
    val wallpapersState: StateFlow<UiState<List<Wallpaper>>> = _wallpapersState.asStateFlow()

    // 分类数据
    val categories = listOf(
        R.string.category_all,
        R.string.category_nature,
        R.string.category_city,
        R.string.category_abstract,
        R.string.category_illustration,
        R.string.category_tech,
        R.string.category_minimal,
        R.string.category_animal,
        R.string.category_food
    )

    // 当前选中的分类
    private val _selectedCategory = MutableStateFlow(R.string.category_all)
    val selectedCategory: StateFlow<Int> = _selectedCategory.asStateFlow()

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
                _isRefreshing.value = true
                _currentPage.value = 1
                _wallpapers.value = emptyList()
            } else if (_currentPage.value == 1) {
                // 首次加载或切换分类后的加载，显示加载状态
                _wallpapersState.value = UiState.Loading
            } else {
                // 加载更多时，设置加载更多状态
                _isLoadingMore.value = true
            }

            try {
                // 根据是否有分类筛选决定调用哪个API
                val result = if (categoryFilter != null && categoryFilter != "全部") {
                    // 使用分类ID格式："unsplash_分类名称"
                    val categoryId = "unsplash_${categoryFilter.lowercase()}"
                    wallpaperRepository.getWallpapersByCategory(
                        categoryId, _currentPage.value, PAGE_SIZE
                    )
                } else {
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
                _wallpapersState.value = UiState.Error(e.message ?: "加载壁纸失败")
            } finally {
                // 无论成功失败，都重置加载状态
                _isRefreshing.value = false
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * 加载更多壁纸
     */
    fun loadMore() {
        if (_isLoadingMore.value || !_canLoadMore.value) {
            Log.d(
                "StaticLibraryViewModel",
                "loadMore aborted due to isLoadingMore: ${_isLoadingMore.value} or !canLoadMore: ${!_canLoadMore.value}"
            )
            return
        }
        // 使用当前选中的分类加载更多
        val currentCategoryResId = _selectedCategory.value
        val categoryFilter = if (currentCategoryResId != R.string.category_all) {
            context.getString(currentCategoryResId)
        } else null
        loadWallpapers(false, categoryFilter)
    }

    /**
     * 根据分类筛选壁纸
     * @param categoryResId 分类资源ID
     */
    fun filterByCategory(categoryResId: Int) {
        // 如果当前已经是这个分类，不需要重复筛选
        if (_selectedCategory.value == categoryResId) return

        // 先更新选中的分类，这样UI可以立即响应
        _selectedCategory.value = categoryResId

        // 在单独的协程中处理数据加载，避免阻塞UI线程
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
        }
    }

    /**
     * 刷新壁纸数据
     */
    fun refresh() {
        // 重置分页参数并加载数据
        // 使用当前选中的分类进行刷新
        val currentCategoryResId = _selectedCategory.value
        val categoryFilter = if (currentCategoryResId != R.string.category_all) {
            context.getString(currentCategoryResId)
        } else null
        loadWallpapers(true, categoryFilter)
    }
}
