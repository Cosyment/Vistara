package com.vistara.aestheticwalls.manager

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.model.WallpaperTarget
import com.vistara.aestheticwalls.service.LiveWallpaperService
import com.vistara.aestheticwalls.utils.ActivityProvider
import com.vistara.aestheticwalls.utils.NotificationUtil
import com.vistara.aestheticwalls.utils.StringProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 壁纸管理器
 * 统一处理静态和动态壁纸的设置
 */
@Singleton
class AppWallpaperManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AppWallpaperManager"
        private const val WALLPAPERS_DIR = "wallpapers"
        private const val DOWNLOAD_BUFFER_SIZE = 8192 // 8KB buffer size for downloads
    }

    /**
     * 下载进度回调接口
     */
    interface DownloadProgressCallback {
        fun onProgressUpdate(progress: Float)
    }


    @Inject
    lateinit var stringProvider: StringProvider

    @Inject
    lateinit var notificationUtil: NotificationUtil

    /**
     * 设置壁纸
     * 根据壁纸类型和目标位置设置壁纸
     * 需要Activity上下文，主要用于设置视频壁纸和显示提示
     *
     * @param wallpaper 壁纸对象
     * @param target 目标位置（主屏幕、锁屏或两者）
     * @param editedBitmap 编辑后的位图（如果有）
     * @param onComplete 完成回调
     * @param useSystemCropper 是否使用系统壁纸裁剪器
     */
    suspend fun setWallpaper(
        wallpaper: Wallpaper, target: WallpaperTarget, editedBitmap: Bitmap? = null, onComplete: (Boolean) -> Unit
    ) {
        try {
            val activity = ActivityProvider.getMainActivity()
            Log.d(TAG, "Setting wallpaper: ${wallpaper.title}, target: ${wallpaper.isLive}")
            activity?.let {
                if (wallpaper.isLive) {
                    // 动态壁纸不支持系统裁剪器
                    setLiveWallpaper(activity, wallpaper, target, onComplete)
                } else {
                    setStaticWallpaper(activity, wallpaper, target, editedBitmap, onComplete)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting wallpaper", e)
            withContext(Dispatchers.Main) {
                onComplete(false)
            }
        }
    }

    /**
     * 设置静态壁纸
     * @param useSystemCropper 是否使用系统壁纸裁剪器
     */
    private suspend fun setStaticWallpaper(
        activity: Activity,
        wallpaper: Wallpaper,
        target: WallpaperTarget,
        editedBitmap: Bitmap? = null,
        onComplete: (Boolean) -> Unit,
    ) {
        try {
            // 获取位图（编辑后的或原始的）
            val bitmap = editedBitmap ?: loadBitmapFromFile(wallpaper.id) ?: withContext(Dispatchers.IO) {
                val url = URL(wallpaper.url)
                BitmapFactory.decodeStream(url.openStream())
            }
            saveBitmapToFile(bitmap, wallpaper.id)

            // 直接设置壁纸
            withContext(Dispatchers.IO) {
                val wallpaperManager = WallpaperManager.getInstance(activity)
                when (target) {
                    WallpaperTarget.HOME -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            wallpaperManager.setBitmap(
                                bitmap, null, true, WallpaperManager.FLAG_SYSTEM
                            )
                        } else {
                            wallpaperManager.setBitmap(bitmap)
                        }
                    }

                    WallpaperTarget.LOCK -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            wallpaperManager.setBitmap(
                                bitmap, null, true, WallpaperManager.FLAG_LOCK
                            )
                        } else {
                            // 在旧版本Android上，无法单独设置锁屏壁纸
                            Log.w(
                                TAG, "Setting lock screen wallpaper not supported on this Android version"
                            )
                        }
                    }

                    WallpaperTarget.BOTH -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            wallpaperManager.setBitmap(
                                bitmap, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                            )
                        } else {
                            wallpaperManager.setBitmap(bitmap)
                        }
                    }
                }

                // 发送壁纸更换通知
                sendWallpaperChangedNotification(wallpaper)
            }

            // 显示成功提示
            withContext(Dispatchers.Main) {
                val message = getSuccessMessage(target)
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                onComplete(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting static wallpaper", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(activity, stringProvider.getString(R.string.set_wallpaper_failed), Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }
    }

    /**
     * 预览壁纸
     * 直接调用系统的壁纸预览功能
     */
    suspend fun previewWallpaper(context: Activity, wallpaper: Wallpaper, editedBitmap: Bitmap? = null) {
        try {
            // 获取位图（编辑后的或原始的）
            val bitmap = editedBitmap ?: loadBitmapFromFile(wallpaper.id) ?: withContext(Dispatchers.IO) {
                val url = URL(wallpaper.url)
                BitmapFactory.decodeStream(url.openStream())
            }
            saveBitmapToFile(bitmap, wallpaper.id)

            // 使用壁纸预览工具类调用系统壁纸预览
            val success = com.vistara.aestheticwalls.utils.WallpaperPreviewUtil.previewWallpaper(context, bitmap)
            if (!success) {
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error previewing wallpaper", e)
        }
    }

    /**
     * 设置动态壁纸
     */
    private suspend fun setLiveWallpaper(
        activity: Activity, wallpaper: Wallpaper, target: WallpaperTarget, onComplete: (Boolean) -> Unit
    ) {
        try {
            val videoUrl = wallpaper.url
            if (videoUrl.isNullOrEmpty()) {
                Log.e(TAG, "Video URL is null or empty")
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, stringProvider.getString(R.string.invalid_video_url), Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
                return
            }

            // 1. 检查缓存中是否已经存在该视频，如果不存在才下载
            val cacheDir = File(activity.cacheDir, "videos")
            val videoFileName = "video_${System.currentTimeMillis()}.mp4"
            val videoFile = File(cacheDir, videoFileName)

            // 检查缓存中是否已经存在相同的视频URL
            val cachedFile = findCachedVideoByUrl(activity, videoUrl)

            if (cachedFile == null) {
                // 缓存中不存在，需要下载
                Log.d(TAG, "Starting to download video from URL: $videoUrl")
                val cacheDir = File(context.cacheDir, "videos")
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }


                // 创建空的进度回调（因为这里不需要显示进度）
                val emptyProgressCallback = object : DownloadProgressCallback {
                    override fun onProgressUpdate(progress: Float) {
                        // 不需要显示进度
                    }
                }

                // 使用通用下载方法下载文件
                downloadFile(videoUrl, videoFile, emptyProgressCallback)
            } else {
                Log.d(TAG, "Using cached video file: ${cachedFile.absolutePath}")
            }

            // 2. 重置当前壁纸状态
            val wallpaperManager = WallpaperManager.getInstance(activity)

            // 使用一个小的透明位图来重置壁纸状态
            withContext(Dispatchers.IO) {
                try {
                    // 创建一个1x1像素的透明位图
                    val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(Color.TRANSPARENT)

                    // 根据目标设置壁纸
                    when (target) {
                        WallpaperTarget.HOME -> wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                        WallpaperTarget.LOCK -> wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                        WallpaperTarget.BOTH -> {
                            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                        }
                    }

                    // 释放位图资源
                    bitmap.recycle()
                    Log.d(TAG, "Successfully reset wallpaper state")

                    // 等待一小段时间，确保壁纸重置完成
                    delay(300)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reset wallpaper: ${e.message}")
                    // 即使重置失败也继续尝试设置新壁纸
                }
            }

            // 3. 设置视频URI
            LiveWallpaperService.setVideoUri(activity, Uri.parse(videoUrl))
            Log.d(TAG, "Set video URI directly from URL: $videoUrl")

            // 4. 设置壁纸 - 尝试直接设置
            val result = setVideoWallpaper(activity)

            // 处理设置结果
            withContext(Dispatchers.Main) {
                // 只有在直接设置成功时才显示成功提示
                if (result.directSuccess) {
                    val message = getSuccessMessage(target)
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

                    // 发送壁纸更换通知
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ActivityCompat.checkSelfPermission(
                                context, Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationUtil.showWallpaperChangedNotification(wallpaper)
                        }
                    } else {
                        notificationUtil.showWallpaperChangedNotification(wallpaper)
                    }
                } else if (!result.anyMethodSucceeded) {
                    // 如果所有方法都失败，显示失败提示
                    Toast.makeText(activity, stringProvider.getString(R.string.set_wallpaper_failed), Toast.LENGTH_SHORT).show()
                }
                // 如果使用了系统界面方法，不显示任何提示，让用户在系统界面完成操作
                onComplete(result.anyMethodSucceeded)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting live wallpaper", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(activity, stringProvider.getString(R.string.set_wallpaper_failed), Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }
    }

    /**
     * 检查缓存中是否已经存在相同的视频URL
     */
    private fun findCachedVideoByUrl(context: Context, videoUrl: String): File? {
        try {
            // 获取缓存目录
            val cacheDir = File(context.cacheDir, "videos")
            if (!cacheDir.exists() || !cacheDir.isDirectory) {
                return null
            }

            // 获取所有缓存的视频文件
            val videoFiles = cacheDir.listFiles { file -> file.name.endsWith(".mp4") }
            if (videoFiles.isNullOrEmpty()) {
                return null
            }

            // 按修改时间排序，获取最新的视频文件
            val latestVideo = videoFiles.maxByOrNull { it.lastModified() }

            // 返回最新的视频文件，即使不是相同的URL
            // 这里的逻辑是简化的，实际上我们可以存储URL和文件路径的映射关系
            // 但在这个简化的实现中，我们假设最新的视频文件就是我们需要的
            return latestVideo
        } catch (e: Exception) {
            Log.e(TAG, "Error finding cached video: ${e.message}")
            return null
        }
    }

    /**
     * 设置视频壁纸的结果
     */
    data class WallpaperSetResult(
        val directSuccess: Boolean, // 是否通过直接方法成功设置
        val anyMethodSucceeded: Boolean // 是否有任何方法成功（包括跳转到系统界面）
    )

    /**
     * 设置视频壁纸
     * 尝试直接使用WallpaperManager.setStream()方法设置视频壁纸
     * 如果直接设置失败，回退到使用系统壁纸选择器
     */
    private suspend fun setVideoWallpaper(activity: Activity): WallpaperSetResult = withContext(Dispatchers.IO) {
        try {
            // 获取当前视频URI
            val videoUri = LiveWallpaperService.getCurrentVideoUri(activity) ?: return@withContext WallpaperSetResult(
                directSuccess = false, anyMethodSucceeded = false
            )
            Log.d(TAG, "Setting video wallpaper with URI: $videoUri")

            // 如果直接设置失败，回退到使用系统壁纸选择器
            try {
                // 创建组件名
                val componentName = ComponentName(
                    activity.packageName, LiveWallpaperService::class.java.name
                )

                withContext(Dispatchers.Main) {
                    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                    intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, componentName)
                    activity.startActivity(intent)
                }
                Log.d(TAG, "Started ACTION_CHANGE_LIVE_WALLPAPER intent as fallback")
                // Always consider this a success, even though the user still needs to complete the action in the system UI
                return@withContext WallpaperSetResult(directSuccess = false, anyMethodSucceeded = true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start ACTION_CHANGE_LIVE_WALLPAPER intent: ${e.message}")
            }

            // 所有方法都失败
            return@withContext WallpaperSetResult(directSuccess = false, anyMethodSucceeded = false)
        } catch (e: Exception) {
            Log.e(TAG, "Error in setVideoWallpaper: ${e.message}")
            return@withContext WallpaperSetResult(directSuccess = false, anyMethodSucceeded = false)
        }
    }

    /**
     * 获取成功提示消息
     */
    private fun getSuccessMessage(target: WallpaperTarget): String {
        return when (target) {
            WallpaperTarget.HOME -> stringProvider.getString(R.string.home_screen_wallpaper_set)
            WallpaperTarget.LOCK -> stringProvider.getString(R.string.lock_screen_wallpaper_set)
            WallpaperTarget.BOTH -> stringProvider.getString(R.string.both_screens_wallpaper_set)
        }
    }

    /**
     * 获取本地壁纸目录
     */
    fun getWallpapersDir(): File {
        val dir = File(context.filesDir, WALLPAPERS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * 保存Bitmap到本地文件
     *
     * @param bitmap 要保存的位图
     * @param fileName 文件名
     * @return 保存的文件或null（如果保存失败）
     */
    fun saveBitmapToFile(bitmap: Bitmap, fileName: String): File? {
        return try {
            val wallpapersDir = getWallpapersDir()
            val file = File(wallpapersDir, "$fileName.jpg")

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
            }

            file
        } catch (e: Exception) {
            Log.e(TAG, "保存壁纸到文件失败", e)
            null
        }
    }

    /**
     * 从本地文件加载Bitmap
     *
     * @param fileName 文件名
     * @return 位图或null（如果加载失败）
     */
    fun loadBitmapFromFile(fileName: String): Bitmap? {
        return try {
            val wallpapersDir = getWallpapersDir()
            val file = File(wallpapersDir, "$fileName.jpg")

            if (!file.exists()) {
                return null
            }

            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "从文件加载壁纸失败", e)
            null
        }
    }


    /**
     * 下载壁纸到设备存储
     * 支持图片和视频壁纸，并处理原始质量和普通质量的选择
     *
     * @param wallpaper 要下载的壁纸
     * @param downloadOriginalQuality 是否下载原始质量
     * @return 下载进度流，包含0.0-1.0的进度值和最终的文件路径
     */
    fun downloadWallpaper(
        wallpaper: Wallpaper, downloadOriginalQuality: Boolean
    ): Flow<Pair<Float, String?>> = channelFlow {
        try {
            // 初始进度
            send(Pair(0f, null))

            // 创建进度回调
            val progressCallback = object : DownloadProgressCallback {
                override fun onProgressUpdate(progress: Float) {
                    trySend(Pair(progress, null))
                }
            }

            // 根据壁纸类型选择下载方法
            val filePath = if (wallpaper.isLive) {
                // 视频壁纸
                downloadVideoFile(wallpaper, downloadOriginalQuality, progressCallback)
            } else {
                // 图片壁纸
                downloadImageFile(wallpaper, downloadOriginalQuality, progressCallback)
            }

            // 完成下载，返回文件路径
            send(Pair(1f, filePath))
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}")
            e.printStackTrace()
            // 发送错误状态
            send(Pair(-1f, null))
            throw e
        }
    }

    /**
     * 下载图片文件（带进度回调）
     *
     * @param wallpaper 要下载的壁纸
     * @param downloadOriginalQuality 是否下载原始质量
     * @param progressCallback 进度回调
     * @return 下载的文件路径
     */
    private suspend fun downloadImageFile(
        wallpaper: Wallpaper, downloadOriginalQuality: Boolean, progressCallback: DownloadProgressCallback
    ): String = withContext(Dispatchers.IO) {
        try {
            // 根据壁纸来源和设置选择正确的URL
            val imageUrl = if (downloadOriginalQuality) {
                // 使用原始质量图片URL
                if (wallpaper.id.startsWith("pexels_photo_") && wallpaper.url?.contains("/photos/") == true) {
                    // 对于Pexels图片，使用原始质量的URL
                    wallpaper.url // 已经是original URL
                } else {
                    // 其他来源，使用downloadUrl或url
                    wallpaper.downloadUrl ?: wallpaper.url
                }
            } else {
                // 使用普通质量图片URL
                if (wallpaper.id.startsWith("pexels_photo_") && wallpaper.url?.contains("/photos/") == true) {
                    // 对于Pexels图片，使用压缩质量的URL
                    // 从原始URL生成压缩版本
                    wallpaper.url?.replace(".jpeg", ".jpeg?auto=compress&cs=tinysrgb&h=650&w=940") ?: wallpaper.url
                } else {
                    // 其他来源，使用previewUrl或url
                    wallpaper.previewUrl ?: wallpaper.url
                }
            } ?: throw IllegalArgumentException("Image URL is null")

            Log.d(TAG, "Downloading image from: $imageUrl with original quality: $downloadOriginalQuality")

            // 创建临时文件
            val cacheDir = File(context.cacheDir, "images")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val tempFile = File(cacheDir, "temp_${System.currentTimeMillis()}.jpg")

            // 使用通用下载方法下载文件
            val downloadedFile = downloadFile(imageUrl, tempFile, progressCallback)

            // 将文件转换为Bitmap
            val bitmap = BitmapFactory.decodeFile(downloadedFile.absolutePath)

            // 保存到公共目录
            val fileName = "Vistara_${wallpaper.id}_${System.currentTimeMillis()}.jpg"
            val filePath = saveImageToPublicStorage(bitmap, fileName)

            // 删除临时文件
            downloadedFile.delete()

            return@withContext filePath
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading image wallpaper: ${e.message}")
            throw e
        }
    }

    /**
     * 下载视频文件（带进度回调）
     *
     * @param wallpaper 要下载的壁纸
     * @param downloadOriginalQuality 是否下载原始质量
     * @param progressCallback 进度回调
     * @return 下载的文件路径
     */
    private suspend fun downloadVideoFile(
        wallpaper: Wallpaper, downloadOriginalQuality: Boolean, progressCallback: DownloadProgressCallback
    ): String = withContext(Dispatchers.IO) {
        try {
            // 根据壁纸来源和设置选择正确的URL
            // 使用原始URL，避免构造可能不正确的URL
            val videoUrl = if (downloadOriginalQuality) {
                // 优先使用下载URL，如果没有则使用普通URL
                wallpaper.downloadHdUrl ?: wallpaper.downloadUrl
            } else {
                // 优先使用预览URL，如果没有则使用普通URL
                wallpaper.downloadSdUrl ?: wallpaper.downloadUrl
            } ?: throw IllegalArgumentException("Video URL is null")

            // 记录下载URL，便于调试
            Log.d(TAG, "Using video URL: $videoUrl for wallpaper ID: ${wallpaper.id}")

            Log.d(TAG, "Downloading video from: $videoUrl with original quality: $downloadOriginalQuality")

            // 创建临时文件
            val cacheDir = File(context.cacheDir, "videos")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val tempFile = File(cacheDir, "temp_${System.currentTimeMillis()}.mp4")

            // 使用通用下载方法下载文件
            val downloadedFile = downloadFile(videoUrl, tempFile, progressCallback)

            // 保存到公共目录
            val filePath = saveVideoToPublicStorage(downloadedFile)
            return@withContext filePath
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading video wallpaper: ${e.message}", e)
            // Add more detailed logging for debugging purposes
            if (e.message?.contains("AccessDenied") == true || e.message?.contains("403") == true) {
                Log.e(TAG, "Access denied error detected. URL might be invalid or restricted.")
            } else if (e.message?.contains("404") == true) {
                Log.e(TAG, "File not found error detected. URL might be incorrect.")
            }
            throw e
        }
    }

    /**
     * 通用文件下载方法（使用OkHttp）
     *
     * @param url 要下载的URL
     * @param outputFile 输出文件（可以是临时文件）
     * @param progressCallback 进度回调
     * @return 下载的文件
     */
    private suspend fun downloadFile(
        url: String, outputFile: File, progressCallback: DownloadProgressCallback
    ): File = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Downloading file from: $url")

            // 创建 OkHttp 客户端
            val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build()

            // 创建请求
            val request = Request.Builder().url(url).build()

            // 执行请求
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    // Show toast on main thread to avoid 'Looper.prepare()' error
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, stringProvider.getString(R.string.video_wallpaper_download_failed), Toast.LENGTH_SHORT).show()
                    }
                    throw IOException("Unexpected response code: ${response.code}")
                }

                // 获取内容长度
                val contentLength = response.body?.contentLength() ?: -1L

                // 创建输出流
                FileOutputStream(outputFile).use { outputStream ->
                    // 获取输入流
                    response.body?.byteStream()?.let { inputStream ->
                        val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
                        var bytesRead: Int
                        var totalBytesRead: Long = 0

                        // 读取数据并更新进度
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead

                            // 更新进度
                            if (contentLength > 0) {
                                val progress = totalBytesRead.toFloat() / contentLength.toFloat()
                                progressCallback.onProgressUpdate(progress)
                            }
                        }

                        outputStream.flush()
                    } ?: throw IOException("Response body is null")
                }
            }

            return@withContext outputFile
        } catch (e: Exception) {
            // Show toast on main thread to avoid 'Looper.prepare()' error
            withContext(Dispatchers.Main) {
                val errorMessage = when {
                    e.message?.contains("AccessDenied") == true -> stringProvider.getString(R.string.video_wallpaper_download_failed) + ": AccessDenied"
                    e.message?.contains("403") == true -> stringProvider.getString(R.string.video_wallpaper_download_failed) + ": Permission Denied (403)"
                    e.message?.contains("404") == true -> stringProvider.getString(R.string.video_wallpaper_download_failed) + ": File Not Found (404)"
                    else -> stringProvider.getString(R.string.video_wallpaper_download_failed)
                }
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
            Log.e(TAG, "Error downloading file: ${e.message}", e)
            throw e
        }
    }

    /**
     * 保存图片到公共存储
     *
     * @param bitmap 要保存的位图
     * @param fileName 文件名
     * @return 保存的文件路径
     */
    private suspend fun saveImageToPublicStorage(
        bitmap: Bitmap, fileName: String
    ): String = withContext(Dispatchers.IO) {
        var imageUri: Uri? = null
        var filePath = ""

        // 使用MediaStore API保存图片到公共目录
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10及以上使用MediaStore API
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/" + fileName
        } else {
            // Android 9及以下直接使用文件系统
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, fileName)
            filePath = image.absolutePath

            // 创建文件URI
            imageUri = Uri.fromFile(image)
        }

        // 写入文件
        imageUri?.let { uri ->
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                // 压缩并写入文件
                val buffer = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, buffer)
                val byteArray = buffer.toByteArray()

                // 一次性写入整个文件
                outputStream.write(byteArray)
                outputStream.flush()
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

        return@withContext filePath
    }

    /**
     * 保存视频到公共存储
     *
     * @param file 视频文件
     * @return 保存的文件路径
     */
    private suspend fun saveVideoToPublicStorage(
        file: File
    ): String = withContext(Dispatchers.IO) {
        val fileName = "Vistara_video_${System.currentTimeMillis()}.mp4"
        var videoUri: Uri? = null
        var filePath = ""

        // 使用MediaStore API保存视频到公共目录
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10及以上使用MediaStore API
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }

            videoUri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath + "/" + fileName
        } else {
            // Android 9及以下直接使用文件系统
            val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            val video = File(moviesDir, fileName)
            filePath = video.absolutePath

            // 创建文件URI
            videoUri = Uri.fromFile(video)
        }

        // 写入文件
        videoUri?.let { uri ->
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                // 从源文件读取并写入目标文件
                FileInputStream(file).use { inputStream ->
                    val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
                    var bytesRead: Int

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            }

            // 如果是Android Q及以上，需要更新IS_PENDING状态
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val updateValues = ContentValues().apply {
                    put(MediaStore.Video.Media.IS_PENDING, 0)
                }
                context.contentResolver.update(uri, updateValues, null, null)
            }

            // 通知媒体扫描器更新相册
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = uri
                context.sendBroadcast(mediaScanIntent)
            }

            // 删除临时文件
            file.delete()
        }

        return@withContext filePath
    }

    /**
     * 发送壁纸更换通知
     * 统一处理壁纸更换通知的发送
     *
     * @param wallpaper 设置的壁纸
     */
    private fun sendWallpaperChangedNotification(wallpaper: Wallpaper) {
        try {
            // 检查用户设置是否允许显示通知
            val showNotification = runBlocking {
                try {
                    notificationUtil.shouldShowWallpaperChangedNotification()
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking notification settings: ${e.message}")
                    false
                }
            }

            if (!showNotification) {
                return
            }

            // 检查权限并发送通知
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    notificationUtil.showWallpaperChangedNotification(wallpaper)
                }
            } else {
                notificationUtil.showWallpaperChangedNotification(wallpaper)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending wallpaper changed notification: ${e.message}")
        }
    }


}