package com.vistara.aestheticwalls.ui.screens.detail

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.billing.BillingConnectionState
import com.vistara.aestheticwalls.billing.BillingManager
import com.vistara.aestheticwalls.billing.PurchaseState
import com.vistara.aestheticwalls.data.EditedImageCache
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.model.WallpaperTarget
import com.vistara.aestheticwalls.data.repository.UserPrefsRepository
import com.vistara.aestheticwalls.data.repository.UserRepository
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import com.vistara.aestheticwalls.manager.AppWallpaperManager
import com.vistara.aestheticwalls.utils.NotificationUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * 壁纸详情页面的ViewModel
 */
@HiltViewModel
class WallpaperDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wallpaperRepository: WallpaperRepository,
    private val userPrefsRepository: UserPrefsRepository,
    private val userRepository: UserRepository,
    private val billingManager: BillingManager,
    private val wallpaperManager: AppWallpaperManager,
    private val notificationUtil: NotificationUtil,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "WallpaperDetailViewModel"
    }

    // 壁纸ID
    private val wallpaperId: String = checkNotNull(savedStateHandle["wallpaperId"])

    // 壁纸详情状态
    private val _wallpaperState = MutableStateFlow<UiState<Wallpaper>>(UiState.Loading)
    val wallpaperState: StateFlow<UiState<Wallpaper>> = _wallpaperState.asStateFlow()

    // 登录状态
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // 需要登录的操作类型
    private val _needLoginAction = MutableStateFlow<LoginAction?>(null)
    val needLoginAction: StateFlow<LoginAction?> = _needLoginAction.asStateFlow()

    // 编辑后的图片
    private val _editedBitmap = mutableStateOf<Bitmap?>(null)
    val editedBitmap: State<Bitmap?> = _editedBitmap

    // 当前壁纸ID
    private var currentWallpaperId: String = ""

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

    // 导航到升级页面的状态
    private val _navigateToUpgrade = mutableStateOf(false)
    val navigateToUpgrade: State<Boolean> = _navigateToUpgrade

    // 下载状态
    private val _isDownloading = mutableStateOf(false)
    val isDownloading: State<Boolean> = _isDownloading

    // 下载进度
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    // 壁纸处理状态，避免重复操作
    private val _isProcessingWallpaper = mutableStateOf(false)
    val isProcessingWallpaper: State<Boolean> = _isProcessingWallpaper

    // 壁纸设置成功状态
    private val _wallpaperSetSuccess = MutableStateFlow<String?>(null)
    val wallpaperSetSuccess: StateFlow<String?> = _wallpaperSetSuccess.asStateFlow()

    // 壁纸信息展开状态
    private val _isInfoExpanded = mutableStateOf(false)
    val isInfoExpanded: State<Boolean> = _isInfoExpanded

    // 计费连接状态
    private val _billingConnectionState = MutableStateFlow(BillingConnectionState.DISCONNECTED)
    val billingConnectionState: StateFlow<BillingConnectionState> = _billingConnectionState.asStateFlow()

    // 升级结果
    private val _upgradeResult = MutableStateFlow<UpgradeResult?>(null)
    val upgradeResult: StateFlow<UpgradeResult?> = _upgradeResult.asStateFlow()

    init {
        // 从SavedStateHandle获取壁纸ID
        val wallpaperId = savedStateHandle.get<String>("wallpaperId") ?: ""
        if (wallpaperId.isNotEmpty()) {
            currentWallpaperId = wallpaperId
            loadWallpaper()
            // 检查是否有编辑后的图片
            checkForEditedImage(wallpaperId)
        }
        checkPremiumStatus()
        checkLoginStatus()
        observeBillingState()
        observePurchaseState()
    }

    /**
     * 检查登录状态
     */
    private fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                _isLoggedIn.value = userRepository.checkUserLoggedIn()
                Log.d(TAG, "User login status: ${_isLoggedIn.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error checking login status", e)
                _isLoggedIn.value = false
            }
        }
    }

    /**
     * 观察计费状态
     */
    private fun observeBillingState() {
        viewModelScope.launch {
            billingManager.connectionState.collectLatest { state ->
                _billingConnectionState.value = state
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
                    is PurchaseState.Completed -> {
                        _isPremiumUser.value = true
                        _upgradeResult.value = UpgradeResult.Success(context.getString(R.string.upgrade_success))
                    }

                    is PurchaseState.Failed -> {
                        _upgradeResult.value = UpgradeResult.Error(context.getString(R.string.upgrade_failed, state.message))
                    }

                    is PurchaseState.Cancelled -> {
                        _upgradeResult.value = UpgradeResult.Error(context.getString(R.string.upgrade_cancelled))
                    }

                    else -> {
                        // 其他状态不处理
                    }
                }
            }
        }
    }

    /**
     * 检查是否有编辑后的图片
     */
    private fun checkForEditedImage(wallpaperId: String) {
        val editedImage = EditedImageCache.getEditedImage(wallpaperId)
        if (editedImage != null) {
            _editedBitmap.value = editedImage
        }
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
                    _wallpaperState.value = UiState.Error(context.getString(R.string.wallpaper_not_exist))
                }
            } catch (e: Exception) {
                _wallpaperState.value = UiState.Error(e.message ?: context.getString(R.string.load_wallpaper_failed))
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
     * 使用userRepository而不是userPrefsRepository获取高级用户状态
     */
    private fun checkPremiumStatus() {
        viewModelScope.launch {
            // 使用userRepository而不是userPrefsRepository
            _isPremiumUser.value = userRepository.checkPremiumStatus()
        }
    }

    /**
     * 切换收藏状态
     */
    fun toggleFavorite() {
        viewModelScope.launch {
            // 检查登录状态
            if (!_isLoggedIn.value) {
                _needLoginAction.value = LoginAction.FAVORITE
                return@launch
            }

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
     * 清除需要登录的操作
     */
    fun clearNeedLoginAction() {
        _needLoginAction.value = null
    }

    /**
     * 设置需要登录的操作
     */
    fun setNeedLoginAction(action: LoginAction) {
        _needLoginAction.value = action
    }

    /**
     * 显示设置壁纸选项
     * @param activity 当前活动实例，用于设置动态壁纸
     */
    fun showSetWallpaperOptions(activity: Activity? = null) {
        val currentWallpaper = (_wallpaperState.value as? UiState.Success)?.data ?: return

        // 检查是否为高级壁纸且用户不是高级用户
        if (currentWallpaper.isPremium && !_isPremiumUser.value) {
            // 如果是高级壁纸且用户不是高级用户，触发导航到升级页面
            _navigateToUpgrade.value = true
            return
        }

        // 对于动态壁纸，需要高级用户才能设置
        if (currentWallpaper.isLive) {
            Log.d("WallpaperDetailViewModel", "Checking premium status for live wallpaper")
            // 检查是否为高级用户
            if (!_isPremiumUser.value) {
                // 如果不是高级用户，触发导航到升级页面
                _navigateToUpgrade.value = true
                return
            }

            // 是高级用户，可以设置动态壁纸
            if (activity != null) {
                // 直接设置为两者（系统会显示选择界面）
                setWallpaper(activity, WallpaperTarget.BOTH)
            } else {
                Log.e("WallpaperDetailViewModel", "Cannot set live wallpaper: activity is null")
                // 如果没有Activity，也显示选项弹框，让用户选择
                _showSetWallpaperOptions.value = true
            }
        } else {
            // 静态壁纸显示设置壁纸选项
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
     * 显示高级提示
     * 现在直接触发导航到升级页面
     */
    fun showPremiumPrompt() {
        // 不再显示弹框，而是触发导航到升级页面
        _navigateToUpgrade.value = true
    }

    /**
     * 隐藏高级提示
     */
    fun hidePremiumPrompt() {
        _showPremiumPrompt.value = false
    }

    /**
     * 重置导航到升级页面的状态
     */
    fun resetNavigateToUpgrade() {
        _navigateToUpgrade.value = false
    }

    /**
     * 设置壁纸
     * 根据壁纸类型和目标位置设置壁纸
     */
    fun setWallpaper(context: Activity?, target: WallpaperTarget) {
        // 检查登录状态
        if (!_isLoggedIn.value) {
            _needLoginAction.value = LoginAction.SET_WALLPAPER
            return
        }

        // 检查上下文是否为空
        if (context == null) {
            Log.e("WallpaperDetailViewModel", "Context is null")
            return
        }

        viewModelScope.launch {
            Log.d("WallpaperDetailViewModel", "Setting wallpaper for target: $target")
            val currentWallpaper = (_wallpaperState.value as? UiState.Success)?.data ?: return@launch

            // 立即隐藏选项对话框，提供即时反馈
            _showSetWallpaperOptions.value = false

            // 设置正在处理状态，避免重复操作
            _isProcessingWallpaper.value = true

            // 使用统一的WallpaperManager设置壁纸
            wallpaperManager.setWallpaper(
                activity = context, wallpaper = currentWallpaper, target = target, editedBitmap = _editedBitmap.value
            ) { success ->
                _isProcessingWallpaper.value = false
                if (success) {
                    // 设置成功，更新成功消息
                    val message = when (target) {
                        WallpaperTarget.HOME -> context.getString(R.string.home_screen_wallpaper_set)
                        WallpaperTarget.LOCK -> context.getString(R.string.lock_screen_wallpaper_set)
                        WallpaperTarget.BOTH -> context.getString(R.string.both_screens_wallpaper_set)
                    }
                    _wallpaperSetSuccess.value = message
                }
            }
        }
    }

    // 已移除handleLiveWallpaper和handleStaticWallpaper方法
    // 现在使用统一的WallpaperManager类处理壁纸设置

    /**
     * 下载壁纸
     */
    fun downloadWallpaper() {
        // 检查登录状态
        if (!_isLoggedIn.value) {
            _needLoginAction.value = LoginAction.DOWNLOAD
            return
        }

        val currentWallpaper = (_wallpaperState.value as? UiState.Success)?.data ?: return

        // 检查是否为高级壁纸或动态壁纸，且用户不是高级用户
        if ((currentWallpaper.isPremium || currentWallpaper.isLive) && !_isPremiumUser.value) {
            // 如果是高级壁纸或动态壁纸且用户不是高级用户，触发导航到升级页面
            _navigateToUpgrade.value = true
            return
        }

        // 检查存储权限
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            // Android 10 及以下需要显式请求存储权限
            val hasStoragePermission = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

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
                // 获取用户设置中的下载原始质量设置
                val userSettings = userPrefsRepository.getUserSettings()
                val downloadOriginalQuality = userSettings.downloadOriginalQuality

                Log.d("WallpaperDetailViewModel", "Download with original quality: $downloadOriginalQuality")

                // 显示下载进度通知
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        notificationUtil.showDownloadProgressNotification(wallpaper, 0)
                    }
                } else {
                    notificationUtil.showDownloadProgressNotification(wallpaper, 0)
                }

                if (wallpaper.isLive) {
                    // 如果是动态壁纸（视频），使用不同的下载逻辑
                    downloadVideo(wallpaper)
                } else {
                    // 静态壁纸下载逻辑
                    downloadImage(wallpaper, downloadOriginalQuality)
                }

                // 记录下载历史
                wallpaperRepository.trackWallpaperDownload(wallpaperId)
                _isDownloading.value = false
                _downloadProgress.value = 1f

                // 显示下载完成通知
                val filePath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/Vistara_${System.currentTimeMillis()}.jpg"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        notificationUtil.showDownloadCompleteNotification(wallpaper, filePath)
                    }
                } else {
                    notificationUtil.showDownloadCompleteNotification(wallpaper, filePath)
                }
            } catch (e: Exception) {
                Log.e("WallpaperDetailViewModel", "Download failed: ${e.message}")
                e.printStackTrace()
                _isDownloading.value = false
                _downloadProgress.value = 0f
            }
        }
    }

    /**
     * 下载静态壁纸（图片）
     * @param downloadOriginalQuality 是否下载原始质量
     */
    private suspend fun downloadImage(wallpaper: Wallpaper, downloadOriginalQuality: Boolean) {
        // 实际下载图片
        val bitmap = withContext(Dispatchers.IO) {
            // 根据设置选择下载原始质量或压缩质量
            val imageUrl = if (downloadOriginalQuality) {
                // 使用原始质量图片URL
                // 如果有downloadUrl字段，优先使用，否则使用普通的url
                wallpaper.downloadUrl ?: wallpaper.url
            } else {
                // 使用普通质量图片URL
                wallpaper.url
            }

            Log.d("WallpaperDetailViewModel", "Downloading image from: $imageUrl")
            val url = URL(imageUrl)
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

        val imageUri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        )
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
                        val progress = (i + 1) / 10f
                        _downloadProgress.value = progress

                        // 更新通知进度
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ActivityCompat.checkSelfPermission(
                                    context, Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationUtil.showDownloadProgressNotification(wallpaper, (progress * 100).toInt())
                            }
                        } else {
                            notificationUtil.showDownloadProgressNotification(wallpaper, (progress * 100).toInt())
                        }

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
    }

    /**
     * 下载动态壁纸（视频）
     */
    private suspend fun downloadVideo(wallpaper: Wallpaper) {
        val videoUrl = wallpaper.url ?: throw IllegalArgumentException("Video URL is null")
        Log.d("WallpaperDetailViewModel", "Downloading video from: $videoUrl")

        // 使用OkHttp下载视频文件
        val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build()

        val request = Request.Builder().url(videoUrl).build()

        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }

        if (!response.isSuccessful) {
            throw IOException("Failed to download video: ${response.code}")
        }

        // 准备保存视频的ContentValues
        val fileName = "Vistara_${System.currentTimeMillis()}.mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        // 插入到视频媒体库
        val videoUri = context.contentResolver.insert(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues
        )
        videoUri?.let { uri ->
            withContext(Dispatchers.IO) {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    response.body?.let { responseBody ->
                        // 获取总大小用于计算进度
                        val contentLength = responseBody.contentLength()
                        val buffer = ByteArray(8192) // 8KB buffer
                        var bytesRead: Int
                        var totalBytesRead: Long = 0

                        // 使用输入流读取数据
                        responseBody.byteStream().use { inputStream ->
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead

                                // 更新进度
                                if (contentLength > 0) {
                                    val progress = totalBytesRead.toFloat() / contentLength.toFloat()
                                    _downloadProgress.value = progress
                                }
                            }
                            outputStream.flush()
                        }
                    }
                }

                // 如果是Android Q及以上，需要更新IS_PENDING状态
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val updateValues = ContentValues().apply {
                        put(MediaStore.Video.Media.IS_PENDING, 0)
                    }
                    context.contentResolver.update(uri, updateValues, null, null)
                }

                // 通知媒体扫描器更新视频库
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    mediaScanIntent.data = uri
                    context.sendBroadcast(mediaScanIntent)
                }

                Log.d("WallpaperDetailViewModel", "Video download completed and saved to: $uri")
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
            val shareText =
                context.getString(
                    R.string.share_wallpaper_text,
                    currentWallpaper.title ?: context.getString(R.string.unnamed_wallpaper),
                    currentWallpaper.author,
                    currentWallpaper.source,
                    currentWallpaper.resolution?.width ?: 0,
                    currentWallpaper.resolution?.height ?: 0
                )

            try {
                // 下载图片并生成分享图
                val bitmap = withContext(Dispatchers.IO) {
                    try {
                        // 下载原始图片
                        val url = URL(currentWallpaper.url)
                        val originalBitmap = BitmapFactory.decodeStream(url.openStream())

                        // 生成带水印的分享图
                        createShareImage(originalBitmap, currentWallpaper)
                    } catch (e: Exception) {
                        null // 如果下载失败，返回null
                    }
                }

                // 创建分享意图
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)

                    if (bitmap != null) {
                        // 将位图保存到缓存目录并分享
                        val cachePath = File(context.cacheDir, "shared_images")
                        cachePath.mkdirs()

                        val shareImageFile = File(cachePath, "share_${System.currentTimeMillis()}.jpg")
                        val outputStream = FileOutputStream(shareImageFile)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        outputStream.close()

                        // 使用FileProvider获取URI
                        val contentUri = FileProvider.getUriForFile(
                            context, "${context.packageName}.fileprovider", shareImageFile
                        )

                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        type = "image/jpeg"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } else {
                        // 如果无法生成分享图，则只分享文本
                        type = "text/plain"
                    }
                }

                // 创建选择器对话框
                val chooserIntent = Intent.createChooser(shareIntent, context.getString(R.string.share_wallpaper))
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                // 启动分享选择器
                context.startActivity(chooserIntent)
            } catch (e: Exception) {
                // 如果分享失败，尝试简单的文本分享
                try {
                    val simpleShareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    val chooserIntent = Intent.createChooser(simpleShareIntent, context.getString(R.string.share_wallpaper))
                    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooserIntent)
                } catch (e2: Exception) {
                    // 处理异常情况
                }
            }
        }
    }

    /**
     * 创建带水印的分享图片
     */
    private fun createShareImage(originalBitmap: Bitmap, wallpaper: Wallpaper): Bitmap {
        // 创建一个新的位图，底部添加水印区域
        val watermarkHeight = 150 // 水印区域高度

        // 计算新图片尺寸，保持原始宽度，增加水印高度
        val width = originalBitmap.width
        val height = originalBitmap.height

        // 如果原图太大，进行缩放
        val maxWidth = 1080
        val scale = if (width > maxWidth) maxWidth.toFloat() / width else 1f

        val scaledWidth = (width * scale).toInt()
        val scaledHeight = (height * scale).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true)

        // 创建最终图片，包含水印区域
        val result = Bitmap.createBitmap(
            scaledWidth, scaledHeight + watermarkHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(result)

        // 绘制原始图片
        canvas.drawBitmap(scaledBitmap, 0f, 0f, null)

        // 绘制水印背景
        val paint = Paint()
        paint.color = Color.WHITE
        canvas.drawRect(
            0f, scaledHeight.toFloat(), scaledWidth.toFloat(), (scaledHeight + watermarkHeight).toFloat(), paint
        )

        // 绘制水印文字
        paint.color = Color.BLACK
        paint.textSize = 30f
        paint.isFakeBoldText = true

        // 绘制壁纸标题
        val title = wallpaper.title ?: context.getString(R.string.beautiful_wallpaper)
        canvas.drawText(title, 20f, scaledHeight + 40f, paint)

        // 绘制来源信息
        paint.isFakeBoldText = false
        paint.textSize = 25f
        val sourceText = "${wallpaper.author} | ${wallpaper.source}"
        canvas.drawText(sourceText, 20f, scaledHeight + 80f, paint)

        // 绘制应用名称
        val appText = "Vistara壁纸应用"
        canvas.drawText(appText, 20f, scaledHeight + 120f, paint)

        return result
    }

    /**
     * 切换壁纸信息展开状态
     */
    fun toggleInfoExpanded() {
        _isInfoExpanded.value = !_isInfoExpanded.value
    }

    /**
     * 刷新编辑后的图片
     */
    fun refreshEditedImage() {
        if (currentWallpaperId.isNotEmpty()) {
            val editedImage = EditedImageCache.getEditedImage(currentWallpaperId)
            _editedBitmap.value = editedImage
        }
    }

    /**
     * 清除升级结果
     */
    fun clearUpgradeResult() {
        _upgradeResult.value = null
    }

    /**
     * 清除壁纸设置成功消息
     */
    fun clearWallpaperSetSuccess() {
        _wallpaperSetSuccess.value = null
    }

    /**
     * 升级结果
     */
    sealed class UpgradeResult {
        data class Success(val message: String) : UpgradeResult()
        data class Error(val message: String) : UpgradeResult()
    }

    /**
     * 需要登录的操作类型
     */
    enum class LoginAction {
        FAVORITE, DOWNLOAD, SET_WALLPAPER, EDIT
    }
}
