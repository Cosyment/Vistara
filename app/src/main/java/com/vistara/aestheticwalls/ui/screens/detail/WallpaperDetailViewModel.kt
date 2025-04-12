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
import kotlinx.coroutines.delay
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

    // 下载进度
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    // 壁纸信息展开状态
    private val _isInfoExpanded = mutableStateOf(false)
    val isInfoExpanded: State<Boolean> = _isInfoExpanded

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

        // 开始下载壁纸
        _isDownloading.value = true
        _downloadProgress.value = 0f

        viewModelScope.launch {
            try {
                // 模拟下载过程
                for (i in 1..10) {
                    delay(300)
                    _downloadProgress.value = i / 10f
                }

                // 记录下载历史
                wallpaperRepository.trackWallpaperDownload(wallpaperId)
                _isDownloading.value = false
                _downloadProgress.value = 1f
            } catch (e: Exception) {
                _isDownloading.value = false
                _downloadProgress.value = 0f
            }
        }
    }

    /**
     * 分享壁纸
     */
    fun shareWallpaper() {
        val currentWallpaper = (_wallpaperState.value as? UiState.Success)?.data ?: return

        // 分享壁纸信息
        val shareText = "\u6211发现了一张精美的壁纸\n" +
                "\u6807题: ${currentWallpaper.title ?: "未命名壁纸"}\n" +
                "\u4f5c者: ${currentWallpaper.author}\n" +
                "\u6765源: ${currentWallpaper.source}\n" +
                "\u5206辨率: ${currentWallpaper.resolution?.width} x ${currentWallpaper.resolution?.height}\n" +
                "\u4e0b载 Vistara 壁纸应用以获取更多精美壁纸!"

        // TODO: 实现实际的分享逻辑
    }

    /**
     * 切换壁纸信息展开状态
     */
    fun toggleInfoExpanded() {
        _isInfoExpanded.value = !_isInfoExpanded.value
    }

    /**
     * 编辑壁纸
     */
    fun editWallpaper() {
        // TODO: 实现编辑逻辑
    }
}
