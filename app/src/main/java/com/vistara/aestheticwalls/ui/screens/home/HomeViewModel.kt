package com.vistara.aestheticwalls.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.data.model.Banner
import com.vistara.aestheticwalls.data.model.BannerActionType
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import com.vistara.aestheticwalls.data.remote.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 首页ViewModel
 * 管理首页数据和状态
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository
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
            _isLoading.value = true
            _error.value = null

            try {
                // 加载轮播图数据
                val banners = listOf(
                    Banner(
                        id = "1",
                        imageUrl = "https://example.com/banner1.jpg",
                        title = "精选壁纸",
                        subtitle = "发现最新最美壁纸",
                        actionType = BannerActionType.COLLECTION,
                        actionTarget = "featured"
                    ),
                    Banner(
                        id = "2",
                        imageUrl = "https://example.com/banner2.jpg",
                        title = "高级会员",
                        subtitle = "解锁所有高清壁纸",
                        actionType = BannerActionType.PREMIUM,
                        actionTarget = "premium"
                    )
                )
                _banners.value = banners

                // 加载精选壁纸
                when (val result = wallpaperRepository.getFeaturedWallpapers(1, 10)) {
                    is ApiResult.Success -> {
                        _featuredWallpapers.value = result.data
                    }
                    is ApiResult.Error -> {
                        _error.value = result.message
                    }
                    is ApiResult.Loading -> {
                        // 忽略加载状态
                    }
                }

                // 加载静态壁纸
                when (val result = wallpaperRepository.getWallpapers("static", 1, 10)) {
                    is ApiResult.Success -> {
                        _staticWallpapers.value = result.data
                    }
                    is ApiResult.Error -> {
                        _error.value = result.message
                    }
                    is ApiResult.Loading -> {
                        // 忽略加载状态
                    }
                }

                // 加载动态壁纸
                when (val result = wallpaperRepository.getWallpapers("live", 1, 10)) {
                    is ApiResult.Success -> {
                        _liveWallpapers.value = result.data
                    }
                    is ApiResult.Error -> {
                        _error.value = result.message
                    }
                    is ApiResult.Loading -> {
                        // 忽略加载状态
                    }
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "加载数据时发生未知错误"
            } finally {
                _isLoading.value = false
            }
        }
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
}