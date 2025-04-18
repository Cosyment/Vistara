package com.vistara.aestheticwalls.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.Banner
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.model.WallpaperCategory
import com.vistara.aestheticwalls.data.repository.BannerRepository
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import com.vistara.aestheticwalls.data.remote.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.vistara.aestheticwalls.utils.StringProvider

/**
 * 首页ViewModel
 * 管理首页数据和状态
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
    private val bannerRepository: BannerRepository,
    private val stringProvider: StringProvider
) : ViewModel() {

    // Banner数据
    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners: StateFlow<List<Banner>> = _banners.asStateFlow()

    // 推荐壁纸
    private val _featuredWallpapers = MutableStateFlow<List<Wallpaper>>(emptyList())
    val featuredWallpapers: StateFlow<List<Wallpaper>> = _featuredWallpapers.asStateFlow()

    // 热门静态壁纸
    private val _staticWallpapers = MutableStateFlow<List<Wallpaper>>(emptyList())
    val staticWallpapers: StateFlow<List<Wallpaper>> = _staticWallpapers.asStateFlow()

    // 动态壁纸
    private val _liveWallpapers = MutableStateFlow<List<Wallpaper>>(emptyList())
    val liveWallpapers: StateFlow<List<Wallpaper>> = _liveWallpapers.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 错误状态
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadInitialData()
    }

    /**
     * 加载初始数据
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            Log.d(TAG, "开始加载初始数据")
            _isLoading.value = true
            _error.value = null

            try {
                // 加载轮播图数据
                Log.d(TAG, "开始加载轮播图数据")
                try {
                    when (val result = bannerRepository.getHomeBanners()) {
                        is ApiResult.Success -> {
                            Log.d(TAG, "轮播图数据加载成功: 获取到${result.data.size}个Banner")
                            _banners.value = result.data
                        }
                        is ApiResult.Error -> {
                            Log.e(TAG, "轮播图数据加载失败: ${result.message}")
                            // 仅记录错误，但不中断加载过程
                        }
                        is ApiResult.Loading -> {
                            // 忽略加载状态
                            Log.d(TAG, "轮播图数据正在加载...")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "轮播图数据加载异常", e)
                    // 仅记录错误，但不中断加载过程
                }
                Log.d(TAG, "轮播图数据加载完成")

                // 加载精选壁纸
                Log.d(TAG, "开始加载精选壁纸")
                try {
                    when (val result = wallpaperRepository.getFeaturedWallpapers(1, 10)) {
                        is ApiResult.Success -> {
                            Log.d(TAG, "精选壁纸加载成功: 获取到${result.data.size}个壁纸")
                            _featuredWallpapers.value = result.data
                        }
                        is ApiResult.Error -> {
                            Log.e(TAG, "精选壁纸加载失败: ${result.message}")
                            // 仅记录错误，但不中断加载过程
                            // _error.value = result.message
                        }
                        is ApiResult.Loading -> {
                            // 忽略加载状态
                            Log.d(TAG, "精选壁纸正在加载...")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "精选壁纸加载异常", e)
                    // 仅记录错误，但不中断加载过程
                    // _error.value = e.message ?: "加载精选壁纸时发生未知错误"
                }

                // 加载静态壁纸
                Log.d(TAG, "开始加载静态壁纸")
                try {
                    when (val result = wallpaperRepository.getWallpapers("static", 1, 10)) {
                        is ApiResult.Success -> {
                            Log.d(TAG, "静态壁纸加载成功: 获取到${result.data.size}个壁纸")
                            _staticWallpapers.value = result.data
                        }
                        is ApiResult.Error -> {
                            Log.e(TAG, "静态壁纸加载失败: ${result.message}")
                            // 仅记录错误，但不中断加载过程
                            // _error.value = result.message
                        }
                        is ApiResult.Loading -> {
                            // 忽略加载状态
                            Log.d(TAG, "静态壁纸正在加载...")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "静态壁纸加载异常", e)
                    // 仅记录错误，但不中断加载过程
                    // _error.value = e.message ?: "加载静态壁纸时发生未知错误"
                }

                // 加载动态壁纸
                Log.d(TAG, "开始加载动态壁纸")
                try {
                    when (val result = wallpaperRepository.getWallpapers("live", 1, 10)) {
                        is ApiResult.Success -> {
                            Log.d(TAG, "动态壁纸加载成功: 获取到${result.data.size}个壁纸")
                            _liveWallpapers.value = result.data
                        }
                        is ApiResult.Error -> {
                            Log.e(TAG, "动态壁纸加载失败: ${result.message}")
                            // 仅记录错误，但不中断加载过程
                            // _error.value = result.message
                        }
                        is ApiResult.Loading -> {
                            // 忽略加载状态
                            Log.d(TAG, "动态壁纸正在加载...")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "动态壁纸加载异常", e)
                    // 仅记录错误，但不中断加载过程
                    // _error.value = e.message ?: "加载动态壁纸时发生未知错误"
                }

            } catch (e: Exception) {
                Log.e(TAG, "加载数据时发生异常", e)
                _error.value = e.message ?: stringProvider.getString(R.string.error_loading_data)
            } finally {
                _isLoading.value = false
                Log.d(TAG, "数据加载完成")
            }
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        loadInitialData()
    }

    /**
     * 加载更多壁纸
     */
    fun loadMoreWallpapers() {
        // TODO: 实现加载更多壁纸的逻辑
    }

    // 当前选中的分类
    private val _selectedCategory = MutableStateFlow<WallpaperCategory?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    // 分类壁纸
    private val _categoryWallpapers = MutableStateFlow<List<Wallpaper>>(emptyList())
    val categoryWallpapers = _categoryWallpapers.asStateFlow()

    // 分类加载状态
    private val _isCategoryLoading = MutableStateFlow(false)
    val isCategoryLoading = _isCategoryLoading.asStateFlow()

    /**
     * 按分类加载壁纸（使用枚举类型）
     */
    fun loadWallpapersByCategory(category: WallpaperCategory) {
        _selectedCategory.value = category

        viewModelScope.launch {
            _isCategoryLoading.value = true

            try {
                // 根据分类加载壁纸，使用正确的分类 ID 格式
                val categoryId = if (category == WallpaperCategory.ALL) {
                    "all"
                } else {
                    "unsplash_${category.apiValue}"
                }
                Log.d(TAG, "开始加载分类壁纸: $categoryId (${category.titleRes})")

                when (val result = wallpaperRepository.getWallpapersByCategory(categoryId, 1, 10)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "分类壁纸加载成功: 获取到${result.data.size}个壁纸")
                        _categoryWallpapers.value = result.data
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "分类壁纸加载失败: ${result.message}")
                        _error.value = result.message
                    }
                    is ApiResult.Loading -> {
                        // 忽略加载状态
                        Log.d(TAG, "分类壁纸正在加载...")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "分类壁纸加载异常", e)
                _error.value = e.message ?: stringProvider.getString(R.string.error_loading_category_wallpapers)
            } finally {
                _isCategoryLoading.value = false
            }
        }
    }

    /**
     * 按分类加载壁纸（兼容字符串参数）
     */
    fun loadWallpapersByCategory(category: String) {
        // 将字符串转换为枚举类型
        val categoryEnum = WallpaperCategory.values().find {
            it.apiValue.equals(category, ignoreCase = true)
        } ?: WallpaperCategory.ALL

        loadWallpapersByCategory(categoryEnum)
    }
}