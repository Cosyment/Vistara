package com.vistara.aestheticwalls.ui.screens.favorites

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.repository.UserRepository
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 收藏页面的ViewModel
 * 管理收藏壁纸的数据和状态
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    companion object {
        private const val TAG = "FavoritesViewModel"
    }

    // 收藏壁纸状态
    private val _favoritesState = MutableStateFlow<UiState<List<Wallpaper>>>(UiState.Loading)
    val favoritesState: StateFlow<UiState<List<Wallpaper>>> = _favoritesState.asStateFlow()

    // 登录状态
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        checkLoginStatus()
    }

    /**
     * 检查登录状态
     */
    private fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                _isLoggedIn.value = userRepository.checkUserLoggedIn()
                Log.d(TAG, "User login status: ${_isLoggedIn.value}")

                if (_isLoggedIn.value) {
                    loadFavorites()
                } else {
                    _favoritesState.value = UiState.Error("需要登录才能查看收藏列表")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking login status", e)
                _isLoggedIn.value = false
                _favoritesState.value = UiState.Error("检查登录状态失败")
            }
        }
    }

    /**
     * 加载收藏壁纸
     */
    private fun loadFavorites() {
        viewModelScope.launch {
            _favoritesState.value = UiState.Loading

            wallpaperRepository.getFavoriteWallpapers()
                .catch { e ->
                    _favoritesState.value = UiState.Error(e.message ?: "加载收藏壁纸失败")
                }
                .collectLatest { wallpapers ->
                    if (wallpapers.isEmpty()) {
                        _favoritesState.value = UiState.Success(emptyList())
                    } else {
                        _favoritesState.value = UiState.Success(wallpapers)
                    }
                }
        }
    }

    /**
     * 取消收藏壁纸
     */
    fun unfavoriteWallpaper(wallpaperId: String) {
        viewModelScope.launch {
            val success = wallpaperRepository.unfavoriteWallpaper(wallpaperId)
            if (success) {
                // 取消收藏成功后，更新当前列表
                // 由于使用了Flow，数据会自动更新，不需要手动刷新
            }
        }
    }

    /**
     * 刷新收藏列表
     */
    fun refresh() {
        checkLoginStatus()
    }
}
