package com.vistara.aestheticwalls.ui.screens.detail

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import com.vistara.aestheticwalls.data.EditedImageCache
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.State
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.billing.BillingConnectionState
import com.vistara.aestheticwalls.billing.BillingManager
import com.vistara.aestheticwalls.billing.PurchaseState
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.model.WallpaperTarget
import com.vistara.aestheticwalls.data.repository.UserPrefsRepository
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
    private val billingManager: BillingManager,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 壁纸ID
    private val wallpaperId: String = checkNotNull(savedStateHandle["wallpaperId"])

    // 壁纸详情状态
    private val _wallpaperState = MutableStateFlow<UiState<Wallpaper>>(UiState.Loading)
    val wallpaperState: StateFlow<UiState<Wallpaper>> = _wallpaperState.asStateFlow()

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
        observeBillingState()
        observePurchaseState()
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
                        _upgradeResult.value = UpgradeResult.Success("升级成功！感谢您的支持")
                    }
                    is PurchaseState.Failed -> {
                        _upgradeResult.value = UpgradeResult.Error("升级失败: ${state.message}")
                    }
                    is PurchaseState.Cancelled -> {
                        _upgradeResult.value = UpgradeResult.Error("升级已取消")
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
     * 显示高级提示
     */
    fun showPremiumPrompt() {
        _showPremiumPrompt.value = true
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

            // 立即隐藏选项对话框，提供即时反馈
            _showSetWallpaperOptions.value = false

            try {
                // 检查是否有编辑后的图片
                val bitmap = if (_editedBitmap.value != null) {
                    // 使用编辑后的图片
                    _editedBitmap.value
                } else {
                    // 如果没有编辑过，则下载原始图片
                    withContext(Dispatchers.IO) {
                        val url = URL(currentWallpaper.url)
                        BitmapFactory.decodeStream(url.openStream())
                    }
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

            } catch (e: IOException) {
                // 处理错误
                e.printStackTrace()
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
                            context,
                            "${context.packageName}.fileprovider",
                            shareImageFile
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
                val chooserIntent = Intent.createChooser(shareIntent, "分享壁纸")
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
                    val chooserIntent = Intent.createChooser(simpleShareIntent, "分享壁纸")
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
        val result = Bitmap.createBitmap(scaledWidth, scaledHeight + watermarkHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // 绘制原始图片
        canvas.drawBitmap(scaledBitmap, 0f, 0f, null)

        // 绘制水印背景
        val paint = Paint()
        paint.color = Color.WHITE
        canvas.drawRect(0f, scaledHeight.toFloat(), scaledWidth.toFloat(), (scaledHeight + watermarkHeight).toFloat(), paint)

        // 绘制水印文字
        paint.color = Color.BLACK
        paint.textSize = 30f
        paint.isFakeBoldText = true

        // 绘制壁纸标题
        val title = wallpaper.title ?: "精美壁纸"
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
        }
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
     * 升级到高级版
     */
    fun upgradeToPremium(activity: Activity?) {
        if (_isPremiumUser.value) {
            _upgradeResult.value = UpgradeResult.Error("您已经是高级用户")
            return
        }

        if (_billingConnectionState.value != BillingConnectionState.CONNECTED) {
            _upgradeResult.value = UpgradeResult.Error("支付服务未连接，请稍后再试")
            return
        }

        // 默认使用月度订阅
        billingManager.launchBillingFlow(activity, BillingManager.SUBSCRIPTION_MONTHLY)
    }

    /**
     * 清除升级结果
     */
    fun clearUpgradeResult() {
        _upgradeResult.value = null
    }

    /**
     * 测试支付
     * 仅用于开发测试
     */
    fun testPayment(activity: Activity?) {
        if (_billingConnectionState.value != BillingConnectionState.CONNECTED) {
            _upgradeResult.value = UpgradeResult.Error("支付服务未连接，请稍后再试")
            return
        }

        // 测试不同的支付方式
        val productIds = listOf(
            BillingManager.SUBSCRIPTION_MONTHLY,
            BillingManager.SUBSCRIPTION_YEARLY,
            BillingManager.PREMIUM_LIFETIME
        )

        // 随机选择一种支付方式进行测试
        val randomProductId = productIds.random()
        billingManager.launchBillingFlow(activity, randomProductId)
    }

    /**
     * 升级结果
     */
    sealed class UpgradeResult {
        data class Success(val message: String) : UpgradeResult()
        data class Error(val message: String) : UpgradeResult()
    }
}
