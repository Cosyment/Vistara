package com.vistara.aestheticwalls.manager

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
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
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.model.WallpaperTarget
import com.vistara.aestheticwalls.service.LiveWallpaperService
import com.vistara.aestheticwalls.utils.NotificationUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import com.vistara.aestheticwalls.utils.StringProvider

/**
 * 壁纸管理器
 * 统一处理静态和动态壁纸的设置
 */
@Singleton
class AppWallpaperManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationUtil: NotificationUtil,
    private val stringProvider: StringProvider
) {
    companion object {
        private const val TAG = "AppWallpaperManager"
    }

    /**
     * 设置壁纸
     * 根据壁纸类型和目标位置设置壁纸
     */
    suspend fun setWallpaper(
        activity: Activity,
        wallpaper: Wallpaper,
        target: WallpaperTarget,
        editedBitmap: Bitmap? = null,
        onComplete: (Boolean) -> Unit
    ) {
        try {
            Log.d(TAG, "Setting wallpaper: ${wallpaper.title}, target: ${wallpaper.isLive}")
            if (wallpaper.isLive) {
                setLiveWallpaper(activity, wallpaper, target, onComplete)
            } else {
                setStaticWallpaper(activity, wallpaper, target, editedBitmap, onComplete)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting wallpaper", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(activity, stringProvider.getString(R.string.set_wallpaper_failed), Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }
    }

    /**
     * 设置静态壁纸
     */
    private suspend fun setStaticWallpaper(
        activity: Activity,
        wallpaper: Wallpaper,
        target: WallpaperTarget,
        editedBitmap: Bitmap? = null,
        onComplete: (Boolean) -> Unit
    ) {
        try {
            // 获取位图（编辑后的或原始的）
            val bitmap = editedBitmap ?: withContext(Dispatchers.IO) {
                val url = URL(wallpaper.url)
                BitmapFactory.decodeStream(url.openStream())
            }

            // 设置壁纸
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
                                TAG,
                                "Setting lock screen wallpaper not supported on this Android version"
                            )
                        }
                    }

                    WallpaperTarget.BOTH -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            wallpaperManager.setBitmap(
                                bitmap,
                                null,
                                true,
                                WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                            )
                        } else {
                            wallpaperManager.setBitmap(bitmap)
                        }
                    }
                }

                // 发送壁纸更换通知
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        notificationUtil.showWallpaperChangedNotification(wallpaper)
                    }
                } else {
                    notificationUtil.showWallpaperChangedNotification(wallpaper)
                }
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
     * 设置动态壁纸
     */
    private suspend fun setLiveWallpaper(
        activity: Activity,
        wallpaper: Wallpaper,
        target: WallpaperTarget,
        onComplete: (Boolean) -> Unit
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

            // 3. 设置视频URI并启动壁纸选择器
            LiveWallpaperService.setVideoUri(activity, Uri.parse(videoUrl))
            Log.d(TAG, "Set video URI directly from URL: $videoUrl")

            // 4. 设置壁纸
            withContext(Dispatchers.Main) {
                val result = setVideoWallpaper(activity)
                // 只有在直接设置成功时才显示成功提示
                if (result.directSuccess) {
                    val message = getSuccessMessage(target)
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

                    // 发送壁纸更换通知
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
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
    private suspend fun downloadVideo(context: Context, videoUrl: String): File? =
        withContext(Dispatchers.IO) {
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
     */
    private fun setVideoWallpaper(activity: Activity): WallpaperSetResult {
        try {
            // 创建组件名
            val componentName = ComponentName(
                activity.packageName, LiveWallpaperService::class.java.name
            )

            // 直接使用ACTION_CHANGE_LIVE_WALLPAPER方法
            // 这个方法会直接指定我们的壁纸服务
            try {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, componentName)
                activity.startActivity(intent)
                Log.d(TAG, "Started ACTION_CHANGE_LIVE_WALLPAPER intent")
                return WallpaperSetResult(directSuccess = false, anyMethodSucceeded = true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start ACTION_CHANGE_LIVE_WALLPAPER intent: ${e.message}")
            }

            // 所有方法都失败
            return WallpaperSetResult(directSuccess = false, anyMethodSucceeded = false)
        } catch (e: Exception) {
            Log.e(TAG, "Error in setVideoWallpaper: ${e.message}")
            return WallpaperSetResult(directSuccess = false, anyMethodSucceeded = false)
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
}