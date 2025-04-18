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
import com.vistara.aestheticwalls.utils.ImageUtil
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

    // 模糊背景图片
    private val _blurredBackgroundBitmap = mutableStateOf<Bitmap?>(null)
    val blurredBackgroundBitmap: State<Bitmap?> = _blurredBackgroundBitmap

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
                // 先从本地数据库查询
                var wallpaper: Wallpaper? = wallpaperRepository.getWallpaperById(wallpaperId)

                // 如果本地数据库有这个壁纸，直接显示
                if (wallpaper != null) {
                    Log.d(TAG, "从本地数据库找到壁纸: $wallpaperId")
                    _wallpaperState.value = UiState.Success(wallpaper)
                    checkFavoriteStatus()

                    // 同时在后台尝试从服务器获取最新数据更新本地缓存
                    try {
                        val updatedWallpaper = wallpaperRepository.getWallpaperById(wallpaperId)
                        if (updatedWallpaper != null && updatedWallpaper != wallpaper) {
                            _wallpaperState.value = UiState.Success(updatedWallpaper)
                        }
                    } catch (e: Exception) {
                        // 忽略后台更新错误，不影响用户体验
                        Log.w(TAG, "后台更新壁纸数据失败: ${e.message}")
                    }
                    return@launch
                }

                // 如果本地数据库没有，尝试多次从服务器获取
                var retryCount = 0
                val maxRetries = 3
                var isRateLimitError = false

                while (wallpaper == null && retryCount < maxRetries) {
                    try {
                        wallpaper = wallpaperRepository.getWallpaperById(wallpaperId)
                        if (wallpaper != null) {
                            break
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "获取壁纸详情失败，重试第${retryCount + 1}次: ${e.message}")
                        // 检查是否是速率限制错误
                        if (e.message?.contains("Rate Limit") == true || e.message?.contains("403") == true) {
                            isRateLimitError = true
                            // 如果是速率限制错误，等待时间更长
                            delay(2000) // 等待2秒
                        }
                    }
                    retryCount++
                    if (!isRateLimitError) {
                        delay(500) // 正常重试等待500毫秒
                    }
                }

                if (wallpaper != null) {
                    _wallpaperState.value = UiState.Success(wallpaper)
                    checkFavoriteStatus()

                    // 如果是图片壁纸，加载并模糊背景
                    if (!wallpaper.isLive) {
                        loadAndBlurBackground(wallpaper)
                    }
                } else {
                    // 如果多次重试后仍然无法获取壁纸详情，显示错误信息
                    Log.e(TAG, "多次重试后仍然无法获取壁纸详情: $wallpaperId")

                    // 如果是速率限制错误，显示更友好的错误信息
                    if (isRateLimitError) {
                        _wallpaperState.value = UiState.Error(context.getString(R.string.api_rate_limit_exceeded))
                    } else {
                        _wallpaperState.value = UiState.Error(context.getString(R.string.wallpaper_not_exist))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载壁纸详情失败: ${e.message}")

                // 检查是否是速率限制错误
                if (e.message?.contains("Rate Limit") == true || e.message?.contains("403") == true) {
                    _wallpaperState.value = UiState.Error(context.getString(R.string.api_rate_limit_exceeded))
                } else {
                    _wallpaperState.value = UiState.Error(e.message ?: context.getString(R.string.load_wallpaper_failed))
                }
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
                wallpaper = currentWallpaper, target = target, editedBitmap = _editedBitmap.value,
                onComplete = { success ->
                    _isProcessingWallpaper.value = false
                    // 设置一个成功消息，触发重新应用沉浸式效果
                    // 使用空字符串避免显示实际的Toast
                    _wallpaperSetSuccess.value = ""
                },
            )
        }
    }

    /**
     * 预览壁纸
     * 直接调用系统的壁纸预览功能
     */
    fun previewWallpaper(context: Activity?) {
        // 检查上下文是否为空
        if (context == null) {
            Log.e("WallpaperDetailViewModel", "Context is null")
            return
        }

        viewModelScope.launch {
            Log.d("WallpaperDetailViewModel", "Previewing wallpaper")
            val currentWallpaper = (_wallpaperState.value as? UiState.Success)?.data ?: return@launch

            // 检查是否为视频壁纸
            if (currentWallpaper.isLive) {
                // 视频壁纸不支持系统预览
//                Toast.makeText(context, "视频壁纸不支持预览", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // 直接调用系统壁纸预览
            wallpaperManager.previewWallpaper(
                context = context,
                wallpaper = currentWallpaper,
                editedBitmap = _editedBitmap.value
            )

            // 设置一个成功消息，触发重新应用沉浸式效果
            // 使用空字符串避免显示实际的Toast
            _wallpaperSetSuccess.value = ""
        }
    }

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
     * 使用AppWallpaperManager的下载方法
     */
    private fun startDownload(wallpaper: Wallpaper) {
        // 开始下载壁纸
        _isDownloading.value = true
        _downloadProgress.value = 0f

        viewModelScope.launch @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS) {
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

                // 使用AppWallpaperManager下载壁纸
                var filePath: String? = null
                wallpaperManager.downloadWallpaper(wallpaper, downloadOriginalQuality).collect { (progress, path) ->
                    if (progress >= 0) {
                        // 更新进度
                        _downloadProgress.value = progress

                        // 更新通知进度
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                notificationUtil.showDownloadProgressNotification(wallpaper, (progress * 100).toInt())
                            }
                        } else {
                            notificationUtil.showDownloadProgressNotification(wallpaper, (progress * 100).toInt())
                        }

                        // 如果有路径，记录下来
                        if (path != null) {
                            filePath = path
                        }
                    }
                }

                // 记录下载历史
                wallpaperRepository.trackWallpaperDownload(wallpaperId)
                _isDownloading.value = false
                _downloadProgress.value = 1f

                // 显示下载完成通知
                if (filePath != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                            notificationUtil.showDownloadCompleteNotification(wallpaper, filePath!!)
                        }
                    } else {
                        notificationUtil.showDownloadCompleteNotification(wallpaper, filePath!!)
                    }
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
     * 加载并模糊背景图片
     */
    private fun loadAndBlurBackground(wallpaper: Wallpaper) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 使用较低分辨率的URL以提高加载速度
                val imageUrl = wallpaper.thumbnailUrl ?: wallpaper.url
                if (imageUrl != null) {
                    val url = URL(imageUrl)
                    val originalBitmap = BitmapFactory.decodeStream(url.openStream())

                    // 应用高斯模糊
                    val blurredBitmap = ImageUtil.applyGaussianBlur(
                        context,
                        originalBitmap,
                        radius = 25f,
                        scale = 0.2f
                    )

                    // 更新状态
                    withContext(Dispatchers.Main) {
                        _blurredBackgroundBitmap.value = blurredBitmap
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载并模糊背景图片失败: ${e.message}")
                // 失败时不更新状态，保持默认黑色背景
            }
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
