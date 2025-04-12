package com.vistara.aestheticwalls.ui.screens.detail

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.model.WallpaperTarget
import com.vistara.aestheticwalls.data.repository.UserPrefsRepository
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import com.vistara.aestheticwalls.data.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.URL
import javax.inject.Inject

/**
 * 壁纸详情页面的ViewModel
 */
@HiltViewModel
class WallpaperDetailViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
    private val userPrefsRepository: UserPrefsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 壁纸ID
    private val wallpaperId: String = checkNotNull(savedStateHandle["wallpaperId"])

    // 壁纸详情状态
    private val _wallpaperState = MutableStateFlow<UiState<Wallpaper>>(UiState.Loading)
    val wallpaperState: StateFlow<UiState<Wallpaper>> = _wallpaperState.asStateFlow()

    // 收藏状态
    private val _isFavorite = mutableStateOf(false)
    val isFavorite: State<Boolean> = _isFavorite

    // 是否为高级用户
    private val _isPremiumUser = mutableStateOf(false)
    val isPremiumUser: State<Boolean> = _isPremiumUser

    // 设置壁纸选项对话框状态
    private val _showSetWallpaperOptions = mutableStateOf(false)
    val showSetWallpaperOptions: State<Boolean> = _showSetWallpaperOptions

    // 高级壁纸提示对话框状态
    private val _showPremiumPrompt = mutableStateOf(false)
    val showPremiumPrompt: State<Boolean> = _showPremiumPrompt

    // 下载状态
    private val _isDownloading = mutableStateOf(false)
    val isDownloading: State<Boolean> = _isDownloading

    init {
        loadWallpaper()
        checkPremiumStatus()
    }

    /**
     * 加载壁纸详情
     */
    private fun loadWallpaper() {
        viewModelScope.launch {
            _wallpaperState.value = UiState.Loading

            try {
                val wallpaper = wallpaperRepository.getWallpaperById(wallpaperId)
                if (wallpaper != null) {
                    _wallpaperState.value = UiState.Success(wallpaper)
                    checkFavoriteStatus()
                } else {
                    _wallpaperState.value = UiState.Error("壁纸不存在")
                }
            } catch (e: Exception) {
                _wallpaperState.value = UiState.Error(e.message ?: "加载壁纸失败")
            }
        }
    }

    /**
     * 检查收藏状态
     */
    private fun checkFavoriteStatus() {
        viewModelScope.launch {
            _isFavorite.value = wallpaperRepository.isWallpaperFavorited(wallpaperId)
        }
    }

    /**
     * 检查高级用户状态
     */
    private fun checkPremiumStatus() {
        viewModelScope.launch {
            val userSettings = userPrefsRepository.getUserSettings()
            _isPremiumUser.value = userSettings.isPremiumUser
        }
    }

    /**
     * 切换收藏状态
     */
    fun toggleFavorite() {
        viewModelScope.launch {
            val currentWallpaper = (_wallpaperState.value as? UiState.Success)?.data ?: return@launch

            if (_isFavorite.value) {
                // 取消收藏
                wallpaperRepository.unfavoriteWallpaper(wallpaperId)
            } else {
                // 添加收藏
                wallpaperRepository.favoriteWallpaper(currentWallpaper)
            }

            // 更新状态
            _isFavorite.value = !_isFavorite.value
        }
    }

    /**
     * 显示设置壁纸选项
     */
    fun showSetWallpaperOptions() {
        val currentWallpaper = (_wallpaperState.value as? UiState.Success)?.data ?: return

        if (currentWallpaper.isPremium && !_isPremiumUser.value) {
            // 如果是高级壁纸且用户不是高级用户，显示高级提示
            _showPremiumPrompt.value = true
        } else {
            // 否则显示设置壁纸选项
            _showSetWallpaperOptions.value = true
        }
    }

    /**
     * 隐藏设置壁纸选项
     */
    fun hideSetWallpaperOptions() {
        _showSetWallpaperOptions.value = false
    }

    /**
     * 隐藏高级提示
     */
    fun hidePremiumPrompt() {
        _showPremiumPrompt.value = false
    }

    /**
     * 设置壁纸
     */
    fun setWallpaper(context: Context, target: WallpaperTarget) {
        viewModelScope.launch {
            val currentWallpaper = (_wallpaperState.value as? UiState.Success)?.data ?: return@launch

            try {
                val wallpaperManager = WallpaperManager.getInstance(context)
                val url = URL(currentWallpaper.url)
                val bitmap = BitmapFactory.decodeStream(url.openStream())

                when (target) {
                    WallpaperTarget.HOME -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                        } else {
                            wallpaperManager.setBitmap(bitmap)
                        }
                    }
                    WallpaperTarget.LOCK -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                        }
                    }
                    WallpaperTarget.BOTH -> {
                        wallpaperManager.setBitmap(bitmap)
                    }
                }

                // 隐藏选项对话框
                _showSetWallpaperOptions.value = false

            } catch (e: IOException) {
                // 处理错误
            }
        }
    }

    /**
     * 下载壁纸
     */
    fun downloadWallpaper() {
        val currentWallpaper = (_wallpaperState.value as? UiState.Success)?.data ?: return

        if (currentWallpaper.isPremium && !_isPremiumUser.value) {
            // 如果是高级壁纸且用户不是高级用户，显示高级提示
            _showPremiumPrompt.value = true
            return
        }

        viewModelScope.launch {
            _isDownloading.value = true

            try {
                // 记录下载
                wallpaperRepository.trackWallpaperDownload(wallpaperId)

                // TODO: 实现实际的下载逻辑

                _isDownloading.value = false
            } catch (e: Exception) {
                _isDownloading.value = false
            }
        }
    }

    /**
     * 分享壁纸
     */
    fun shareWallpaper() {
        // TODO: 实现分享逻辑
    }

    /**
     * 编辑壁纸
     */
    fun editWallpaper() {
        // TODO: 实现编辑逻辑
    }
}
