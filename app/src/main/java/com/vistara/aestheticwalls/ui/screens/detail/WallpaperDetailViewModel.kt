package com.vistara.aestheticwalls.ui.screens.detail

import android.Manifest
import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
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
    @ApplicationContext private val context: Context,
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

    // 是否需要请求存储权限
    private val _needStoragePermission = mutableStateOf(false)
    val needStoragePermission: State<Boolean> = _needStoragePermission
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
                // 在IO线程上下载和处理图片
                val bitmap = withContext(Dispatchers.IO) {
                    val url = URL(currentWallpaper.url)
                    BitmapFactory.decodeStream(url.openStream())
                }

                // 设置壁纸操作也可能是耗时操作，使用IO线程
                withContext(Dispatchers.IO) {
                    val wallpaperManager = WallpaperManager.getInstance(context)
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
                }

                // 隐藏选项对话框 - 这个操作在主线程上执行
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

        // 检查存储权限
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            // Android 10 及以下需要显式请求存储权限
            val hasStoragePermission = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED

            if (!hasStoragePermission) {
                // 需要请求权限，设置状态并返回
                _needStoragePermission.value = true
                return
            }
        }

        // 开始实际下载
        startDownload(currentWallpaper)
    }

    /**
     * 权限授予后继续下载
     */
    fun continueDownloadAfterPermissionGranted() {
        _needStoragePermission.value = false
        val currentWallpaper = (_wallpaperState.value as? UiState.Success)?.data ?: return
        startDownload(currentWallpaper)
    }

    /**
     * 重置权限请求状态
     */
    fun resetPermissionRequest() {
        _needStoragePermission.value = false
    }

    /**
     * 实际开始下载过程
     */
    private fun startDownload(wallpaper: Wallpaper) {
        // 开始下载壁纸
        _isDownloading.value = true
        _downloadProgress.value = 0f

        viewModelScope.launch {
            try {
                // 实际下载图片
                val bitmap = withContext(Dispatchers.IO) {
                    val url = URL(wallpaper.url)
                    BitmapFactory.decodeStream(url.openStream())
                }

                // 保存图片到相册
                val fileName = "Vistara_${System.currentTimeMillis()}.jpg"
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

                val imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                imageUri?.let { uri ->
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            // 模拟下载进度
                            val buffer = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, buffer)
                            val byteArray = buffer.toByteArray()

                            // 写入文件并更新进度
                            val chunkSize = byteArray.size / 10
                            for (i in 0 until 10) {
                                val start = i * chunkSize
                                val end = if (i == 9) byteArray.size else (i + 1) * chunkSize
                                outputStream.write(byteArray, start, end - start)
                                outputStream.flush()

                                // 更新进度
                                _downloadProgress.value = (i + 1) / 10f
                                delay(100) // 稍微延迟以显示进度
                            }
                        }

                        // 如果是Android Q及以上，需要更新IS_PENDING状态
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val updateValues = ContentValues().apply {
                                put(MediaStore.Images.Media.IS_PENDING, 0)
                            }
                            context.contentResolver.update(uri, updateValues, null, null)
                        }

                        // 通知媒体扫描器更新相册
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            mediaScanIntent.data = uri
                            context.sendBroadcast(mediaScanIntent)
                        }
                    }
                }

                // 记录下载历史
                wallpaperRepository.trackWallpaperDownload(wallpaperId)
                _isDownloading.value = false
                _downloadProgress.value = 1f
            } catch (e: Exception) {
                e.printStackTrace()
                _isDownloading.value = false
                _downloadProgress.value = 0f
            }
        }
    }

    /**
     * 分享壁纸
     */
    fun shareWallpaper() {
        viewModelScope.launch {
            val currentWallpaper = (_wallpaperState.value as? UiState.Success)?.data ?: return@launch

            // 分享壁纸信息
            val shareText = "\u6211发现了一张精美的壁纸\n" +
                    "\u6807题: ${currentWallpaper.title ?: "未命名壁纸"}\n" +
                    "\u4f5c者: ${currentWallpaper.author}\n" +
                    "\u6765源: ${currentWallpaper.source}\n" +
                    "\u5206辨率: ${currentWallpaper.resolution?.width} x ${currentWallpaper.resolution?.height}\n" +
                    "\u4e0b载 Vistara 壁纸应用以获取更多精美壁纸!"

            // 创建分享意图
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"

                // 如果有壁纸URL，添加到分享内容
                if (currentWallpaper.sourceUrl != null) {
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(currentWallpaper.sourceUrl))
                    type = "image/*"
                }
            }

            // 创建选择器对话框
            val chooserIntent = Intent.createChooser(shareIntent, "分享壁纸")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // 启动分享选择器 - 在主线程上执行
            try {
                context.startActivity(chooserIntent)
            } catch (e: Exception) {
                // 处理异常情况
            }
        }
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
        viewModelScope.launch {
            val currentWallpaper = (_wallpaperState.value as? UiState.Success)?.data ?: return@launch

            // 检查是否为高级壁纸且用户不是高级用户
            if (currentWallpaper.isPremium && !_isPremiumUser.value) {
                _showPremiumPrompt.value = true
                return@launch
            }

            // 创建编辑意图
            try {
                // 这里可以启动编辑器或其他图片处理应用
                val editIntent = Intent(Intent.ACTION_EDIT).apply {
                    setDataAndType(Uri.parse(currentWallpaper.url), "image/*")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                // 检查是否有应用可以处理这个意图
                if (editIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(editIntent)
                } else {
                    // 如果没有应用可以处理，尝试使用查看意图
                    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(Uri.parse(currentWallpaper.url), "image/*")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(viewIntent)
                }
            } catch (e: Exception) {
                // 处理异常情况
            }
        }
    }
}
