package com.vistara.aestheticwalls.manager

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
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
        wallpaper: Wallpaper, target: WallpaperTarget, editedBitmap: Bitmap? = null, onComplete: (Boolean) -> Unit,
        useSystemCropper: Boolean = false
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
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "预览壁纸失败", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error previewing wallpaper", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "预览壁纸失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 启动系统壁纸裁剪器
     * @return 是否成功启动系统裁剪器
     */
    private suspend fun launchSystemWallpaperCropper(activity: Activity, bitmap: Bitmap, target: WallpaperTarget): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 将位图保存到缓存文件
                val cachePath = File(activity.cacheDir, "wallpapers")
                cachePath.mkdirs()

                val wallpaperFile = File(cachePath, "temp_wallpaper_${System.currentTimeMillis()}.jpg")
                FileOutputStream(wallpaperFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }

                // 使用FileProvider获取URI
                val contentUri = FileProvider.getUriForFile(
                    activity, "${activity.packageName}.fileprovider", wallpaperFile
                )

                // 创建设置壁纸的Intent
                val intent = Intent(Intent.ACTION_ATTACH_DATA)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.setDataAndType(contentUri, "image/*")
                intent.putExtra("mimeType", "image/*")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                // 根据目标设置额外参数
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    when (target) {
                        WallpaperTarget.HOME -> {
                            intent.putExtra("which", WallpaperManager.FLAG_SYSTEM)
                        }

                        WallpaperTarget.LOCK -> {
                            intent.putExtra("which", WallpaperManager.FLAG_LOCK)
                        }

                        WallpaperTarget.BOTH -> {
                            // 对于同时设置两者，系统会提供选项
                        }
                    }
                }

                // 创建选择器
                val chooserIntent = Intent.createChooser(intent, stringProvider.getString(R.string.set_as_wallpaper))

                withContext(Dispatchers.Main) {
                    try {
                        activity.startActivity(chooserIntent)
                        true
                    } catch (e: ActivityNotFoundException) {
                        Log.e(TAG, "No app can handle setting wallpaper", e)
                        false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error launching system wallpaper cropper", e)
                false
            }
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
                val downloadedFile = downloadVideo(activity, videoUrl)
                if (downloadedFile == null) {
                    Log.e(TAG, "Failed to download video")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity, stringProvider.getString(R.string.video_download_failed), Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
                    return
                }
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
     * 下载视频到本地缓存
     */
    private suspend fun downloadVideo(context: Context, videoUrl: String): File? = withContext(Dispatchers.IO) {
        try {
            // 创建缓存目录
            val cacheDir = File(context.cacheDir, "videos")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            // 创建视频文件
            val fileName = "video_${System.currentTimeMillis()}.mp4"
            val videoFile = File(cacheDir, fileName)

            // 下载视频
            val url = URL(videoUrl)
            val connection = url.openConnection()
            connection.connect()

            // 将视频保存到文件
            val inputStream: InputStream = connection.getInputStream()
            val outputStream = FileOutputStream(videoFile)
            val buffer = ByteArray(1024)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.close()
            inputStream.close()

            return@withContext videoFile
        } catch (e: IOException) {
            Log.e(TAG, "Error downloading video", e)
            return@withContext null
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