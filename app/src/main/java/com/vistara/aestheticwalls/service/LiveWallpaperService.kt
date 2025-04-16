package com.vistara.aestheticwalls.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import java.io.File
import java.io.FileDescriptor
import java.io.IOException

/**
 * 视频壁纸服务
 * 使用MediaPlayer播放视频作为动态壁纸
 */
class LiveWallpaperService : WallpaperService() {

    /**
     * 视频缩放模式
     */
    enum class ScaleMode {
        /**
         * 填充模式 - 视频将被缩放以填充整个屏幕，可能会发生变形
         */
        FILL,

        /**
         * 适应模式 - 视频将被缩放以适应屏幕，保持原始宽高比，可能有黑边
         */
        FIT,

        /**
         * 中心裁剪模式 - 视频将被缩放以覆盖屏幕，保持原始宽高比，可能会有部分内容被裁剪
         */
        CENTER_CROP
    }

    companion object {
        private const val TAG = "LiveWallpaperService"

        // 用于传递视频URI的Intent extra key
        const val EXTRA_VIDEO_URI = "video_uri"

        // SharedPreferences的名称和key
        private const val PREFS_NAME = "video_wallpaper_prefs"
        private const val KEY_VIDEO_URI = "video_uri"

        // 用于保存当前设置的视频URI
        private var currentVideoUri: Uri? = null

        /**
         * 设置视频URI
         */
        fun setVideoUri(context: Context, uri: Uri) {
            Log.d(TAG, "Setting video URI: $uri")

            // 生成一个唯一的标识符，确保每次设置都被视为新的壁纸
            val uniqueId = System.currentTimeMillis().toString()

            // 不清除旧的URI设置，只更新当前的URI
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            // 设置新的URI
            currentVideoUri = uri

            // 保存URI和唯一标识符到SharedPreferences
            prefs.edit()
                .putString(KEY_VIDEO_URI, uri.toString())
                .putString("unique_id", uniqueId) // 添加唯一标识符
                .apply()
            Log.d(TAG, "Video URI saved to preferences: $uri with unique ID: $uniqueId")
        }

        /**
         * 获取当前视频URI
         */
        fun getCurrentVideoUri(context: Context): Uri? {
            // 如果内存中有，直接返回
            if (currentVideoUri != null) {
                return currentVideoUri
            }

            // 否则从 SharedPreferences 中读取
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val uriString = prefs.getString(KEY_VIDEO_URI, null)

            if (uriString != null) {
                try {
                    currentVideoUri = Uri.parse(uriString)
                    Log.d(TAG, "Restored video URI from preferences: $currentVideoUri")
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing saved URI: $uriString", e)
                }
            }

            return currentVideoUri
        }
    }

    override fun onCreateEngine(): Engine {
        // 尝试从 SharedPreferences 恢复 URI
        val restoredUri = getCurrentVideoUri(applicationContext)
        Log.d(TAG, "Creating video wallpaper engine, current URI: $restoredUri")
        return VideoEngine()
    }

    /**
     * 视频壁纸引擎
     * 负责播放视频和处理生命周期事件
     */
    inner class VideoEngine : Engine() {
        private var mediaPlayer: MediaPlayer? = null
        private var isMediaPlayerPrepared = false
        private var visible = false
        private val handler = android.os.Handler(android.os.Looper.getMainLooper())

        // 屏幕尺寸
        private var screenWidth = 0
        private var screenHeight = 0

        // 视频尺寸
        private var videoWidth = 0
        private var videoHeight = 0

        // 缩放模式
        private var scaleMode = ScaleMode.CENTER_CROP

        // 屏幕关闭广播接收器
        private val screenOffReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                    pauseVideo()
                } else if (intent?.action == Intent.ACTION_SCREEN_ON) {
                    if (visible) {
                        playVideo()
                    }
                }
            }
        }

        init {
            // 注册屏幕关闭广播接收器
            val intentFilter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
            }
            registerReceiver(screenOffReceiver, intentFilter)
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)

            // 获取唯一标识符，用于调试
            val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val uniqueId = prefs.getString("unique_id", "none")
            val videoUri = prefs.getString(KEY_VIDEO_URI, "none")
            Log.d(TAG, "Engine onCreate with unique ID: $uniqueId, video URI: $videoUri")
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            Log.d(TAG, "Surface created")
            initMediaPlayer(holder)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            Log.d(TAG, "Surface changed: $width x $height")

            // 保存屏幕尺寸
            screenWidth = width
            screenHeight = height

            // 如果已经有视频尺寸信息，重新计算缩放矩阵
            if (videoWidth > 0 && videoHeight > 0) {
                updateVideoScale()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            Log.d(TAG, "Surface destroyed")
            try {
                // 先暂停播放
                pauseVideo()
                // 使用延迟释放，避免在Surface销毁过程中操作MediaPlayer
                // 注意：我们在onDestroy中会调用releaseMediaPlayer，这里只暂停播放
                // 避免重复释放资源
            } catch (e: Exception) {
                Log.e(TAG, "Error in onSurfaceDestroyed", e)
                // 即使出错也要确保标记为未准备状态
                isMediaPlayerPrepared = false
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            this.visible = visible
            Log.d(TAG, "Visibility changed: $visible")

            if (visible) {
                playVideo()
            } else {
                pauseVideo()
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            Log.d(TAG, "Engine onDestroy")

            try {
                // 先暂停播放
                pauseVideo()
                // 在主线程中安全地释放资源
                handler.post {
                    releaseMediaPlayer()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing MediaPlayer in onDestroy", e)
                // 即使出错也要确保释放资源
                mediaPlayer = null
                isMediaPlayerPrepared = false
            }

            try {
                unregisterReceiver(screenOffReceiver)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering receiver", e)
            }
        }

        /**
         * 初始化MediaPlayer
         */
        private fun initMediaPlayer(holder: SurfaceHolder) {
            try {
                // 获取视频URI，从 SharedPreferences 中恢复
                val videoUri = getCurrentVideoUri(applicationContext)
                if (videoUri == null) {
                    Log.e(TAG, "No video URI set")
                    // 如果没有URI，安排延迟重试
                    handler.postDelayed({
                        Log.d(TAG, "Retrying initMediaPlayer after delay")
                        initMediaPlayer(holder)
                    }, 1000) // 1秒后重试
                    return
                }

                // 检查URI类型并选择适当的初始化方法
                val uriString = videoUri.toString()
                try {
                    Log.d(TAG, "Initializing MediaPlayer with URI: $videoUri")
                    mediaPlayer = MediaPlayer().apply {
                        // 如果是网络 URL，直接使用字符串设置数据源
                        if (uriString.startsWith("http")) {
                            Log.d(TAG, "Using network URL directly: $uriString")
                            setDataSource(uriString)
                        } else {
                            // 如果是本地文件或内容提供者 URI，使用内容解析器
                            Log.d(TAG, "Using ContentResolver for URI: $videoUri")
                            setDataSource(applicationContext, videoUri)
                        }

                        setSurface(holder.surface)
                        setOnPreparedListener { mp ->
                            isMediaPlayerPrepared = true
                            mp.isLooping = true
                            if (visible) {
                                mp.start()
                            }
                            Log.d(TAG, "MediaPlayer prepared successfully")
                        }
                        setOnErrorListener { mp, what, extra ->
                            Log.e(TAG, "MediaPlayer error: $what, $extra")
                            false
                        }
                        prepareAsync()
                    }
                    return
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing MediaPlayer with URI: ${e.message}")
                    e.printStackTrace()
                    // 如果直接使用URI失败，尝试使用文件路径
                }

                // 直接使用ContentResolver打开文件描述符
                var fileDescriptor: FileDescriptor? = null
                try {
                    val contentResolver = applicationContext.contentResolver
                    val parcelFileDescriptor = contentResolver.openFileDescriptor(videoUri, "r")
                    if (parcelFileDescriptor != null) {
                        fileDescriptor = parcelFileDescriptor.fileDescriptor
                        Log.d(TAG, "Successfully opened file descriptor for URI: $videoUri")
                    } else {
                        Log.e(TAG, "Failed to open file descriptor for URI: $videoUri")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error opening file descriptor: ${e.message}")
                    // 如果无法使用ContentResolver，尝试使用直接文件路径
                    fileDescriptor = null
                }

                // 如果文件描述符方法失败，尝试使用直接文件路径
                val uriPath = videoUri.toString()
                val file = if (fileDescriptor == null) {
                    if (uriPath.startsWith("file://")) {
                        try {
                            val path = uriPath.replace("file://", "")
                            File(path)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing file path: $uriPath", e)
                            null
                        }
                    } else {
                        try {
                            // 尝试直接使用URI路径
                            val path = videoUri.path
                            if (path != null) {
                                File(path)
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error creating file from URI path: $videoUri", e)
                            null
                        }
                    }
                } else {
                    null // 如果有文件描述符，就不需要文件对象
                }

                // 如果有文件描述符，直接使用文件描述符初始化MediaPlayer
                if (fileDescriptor != null) {
                    Log.d(TAG, "Using file descriptor to initialize MediaPlayer")
                    initMediaPlayerWithFileDescriptor(holder, fileDescriptor)
                    return
                }

                // 如果没有文件描述符，检查文件是否存在
                if (file == null || !file.exists() || !file.canRead()) {
                    Log.e(TAG, "Video file does not exist or cannot be read: $videoUri")
                    // 如果文件不存在，尝试清除当前的URI设置
                    val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit().remove(KEY_VIDEO_URI).apply()
                    currentVideoUri = null

                    // 尝试从缓存目录查找最新的视频文件
                    val cacheDir = File(applicationContext.cacheDir, "videos")
                    if (cacheDir.exists() && cacheDir.isDirectory) {
                        val videoFiles = cacheDir.listFiles { file -> file.name.endsWith(".mp4") }
                        if (videoFiles != null && videoFiles.isNotEmpty()) {
                            // 按修改时间排序，获取最新的视频文件
                            val latestVideo = videoFiles.maxByOrNull { it.lastModified() }
                            if (latestVideo != null && latestVideo.exists() && latestVideo.canRead()) {
                                val newUri = Uri.fromFile(latestVideo)
                                Log.d(TAG, "Found latest video file: ${latestVideo.absolutePath}")
                                // 保存新的URI
                                prefs.edit().putString(KEY_VIDEO_URI, newUri.toString()).apply()
                                currentVideoUri = newUri
                                // 使用新找到的文件初始化MediaPlayer
                                Log.d(TAG, "Video file exists and is readable: ${latestVideo.absolutePath}")
                                Log.d(TAG, "Initializing MediaPlayer with URI: $newUri")
                                initMediaPlayerWithFile(holder, latestVideo)
                                return
                            }
                        }
                    }

                    // 如果没有找到可用的视频文件，显示黑屏
                    return
                }
                Log.d(TAG, "Video file exists and is readable: ${file.absolutePath}")

                // 使用已验证的文件初始化MediaPlayer
                initMediaPlayerWithFile(holder, file)
            } catch (e: IOException) {
                Log.e(TAG, "Error initializing MediaPlayer", e)
            }
        }

        /**
         * 播放视频
         */
        private fun playVideo() {
            if (isMediaPlayerPrepared && mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                Log.d(TAG, "Starting video playback")
                mediaPlayer?.start()
            }
        }

        /**
         * 暂停视频
         */
        private fun pauseVideo() {
            if (isMediaPlayerPrepared && mediaPlayer != null && mediaPlayer!!.isPlaying) {
                Log.d(TAG, "Pausing video playback")
                mediaPlayer?.pause()
            }
        }

        /**
         * 更新视频缩放
         * 根据当前的缩放模式计算视频的显示区域
         */
        private fun updateVideoScale() {
            if (videoWidth <= 0 || videoHeight <= 0 || screenWidth <= 0 || screenHeight <= 0) {
                Log.e(TAG, "Cannot update video scale, invalid dimensions")
                return
            }

            try {
                val mp = mediaPlayer ?: return

                // 计算视频和屏幕的宽高比
                val videoRatio = videoWidth.toFloat() / videoHeight.toFloat()
                val screenRatio = screenWidth.toFloat() / screenHeight.toFloat()

                Log.d(TAG, "Video ratio: $videoRatio, Screen ratio: $screenRatio, Scale mode: $scaleMode")

                when (scaleMode) {
                    ScaleMode.FILL -> {
                        // 填充模式 - 直接拉伸视频以填充屏幕
                        // 不需要设置特殊参数，默认就是填充
                    }

                    ScaleMode.FIT -> {
                        // 适应模式 - 保持宽高比缩放视频以适应屏幕
                        if (videoRatio > screenRatio) {
                            // 视频更宽，需要上下黑边
                            val scaledHeight = screenWidth / videoRatio
                            val yOffset = (screenHeight - scaledHeight) / 2

                            // 设置视频显示区域
                            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                        } else {
                            // 视频更高，需要左右黑边
                            val scaledWidth = screenHeight * videoRatio
                            val xOffset = (screenWidth - scaledWidth) / 2

                            // 设置视频显示区域
                            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                        }
                    }

                    ScaleMode.CENTER_CROP -> {
                        // 中心裁剪模式 - 保持宽高比缩放视频以覆盖屏幕
                        if (videoRatio > screenRatio) {
                            // 视频更宽，需要左右裁剪
                            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                        } else {
                            // 视频更高，需要上下裁剪
                            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                        }
                    }
                }

                Log.d(TAG, "Video scaling updated for mode: $scaleMode")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating video scale: ${e.message}")
            }
        }

        /**
         * 使用文件描述符初始化MediaPlayer
         */
        private fun initMediaPlayerWithFileDescriptor(holder: SurfaceHolder, fileDescriptor: FileDescriptor) {
            try {
                Log.d(TAG, "Initializing MediaPlayer with file descriptor")

                // 创建MediaPlayer
                mediaPlayer = MediaPlayer().apply {
                    // 使用文件描述符设置数据源
                    setDataSource(fileDescriptor)
                    Log.d(TAG, "MediaPlayer data source set to file descriptor")

                    // 设置错误监听器
                    setOnErrorListener { mp, what, extra ->
                        Log.e(TAG, "MediaPlayer error: $what, $extra")
                        isMediaPlayerPrepared = false
                        releaseMediaPlayer()
                        // 尝试重新初始化
                        handler.postDelayed({
                            initMediaPlayer(holder)
                        }, 1000)
                        true
                    }

                    // 设置Surface
                    try {
                        val surface = holder.surface
                        if (surface.isValid) {
                            setSurface(surface)
                            Log.d(TAG, "Set surface successfully")
                        } else {
                            Log.e(TAG, "Surface is not valid")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error setting surface: ${e.message}")
                    }

                    // 设置视频尺寸获取监听器
                    setOnVideoSizeChangedListener { mp, width, height ->
                        Log.d(TAG, "Video size changed: $width x $height")
                        this@VideoEngine.videoWidth = width
                        this@VideoEngine.videoHeight = height

                        // 如果已经有屏幕尺寸信息，计算缩放矩阵
                        if (screenWidth > 0 && screenHeight > 0) {
                            updateVideoScale()
                        }
                    }

                    // 设置准备完成监听器
                    setOnPreparedListener { mp ->
                        isMediaPlayerPrepared = true
                        Log.d(TAG, "MediaPlayer prepared")

                        try {
                            // 设置循环播放
                            mp.isLooping = true

                            // 获取视频尺寸
                            this@VideoEngine.videoWidth = mp.videoWidth
                            this@VideoEngine.videoHeight = mp.videoHeight
                            Log.d(TAG, "Video dimensions: $videoWidth x $videoHeight")

                            // 计算缩放矩阵
                            if (screenWidth > 0 && screenHeight > 0) {
                                updateVideoScale()
                            }

                            // 如果可见，开始播放
                            if (visible) {
                                mp.start()
                                Log.d(TAG, "Starting video playback after prepare")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in onPrepared: ${e.message}")
                        }
                    }

                    // 开始异步准备
                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing MediaPlayer with file descriptor", e)
                // 如果初始化失败，尝试清除当前的URI设置
                val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().remove(KEY_VIDEO_URI).apply()
                currentVideoUri = null
            }
        }

        /**
         * 使用指定的文件初始化MediaPlayer
         */
        private fun initMediaPlayerWithFile(holder: SurfaceHolder, file: File) {
            try {
                Log.d(TAG, "Initializing MediaPlayer with file: ${file.absolutePath}")

                // 创建MediaPlayer
                mediaPlayer = MediaPlayer().apply {
                    // 使用文件路径设置数据源
                    setDataSource(file.absolutePath)
                    Log.d(TAG, "MediaPlayer data source set to file: ${file.absolutePath}")

                    // 设置错误监听器
                    setOnErrorListener { mp, what, extra ->
                        Log.e(TAG, "MediaPlayer error: $what, $extra")
                        isMediaPlayerPrepared = false
                        releaseMediaPlayer()
                        // 尝试重新初始化
                        handler.postDelayed({
                            initMediaPlayer(holder)
                        }, 1000)
                        true
                    }

                    // 设置Surface
                    try {
                        val surface = holder.surface
                        if (surface.isValid) {
                            setSurface(surface)
                            Log.d(TAG, "Set surface successfully")
                        } else {
                            Log.e(TAG, "Surface is not valid")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error setting surface: ${e.message}")
                    }

                    // 设置视频尺寸获取监听器
                    setOnVideoSizeChangedListener { mp, width, height ->
                        Log.d(TAG, "Video size changed: $width x $height")
                        this@VideoEngine.videoWidth = width
                        this@VideoEngine.videoHeight = height

                        // 如果已经有屏幕尺寸信息，计算缩放矩阵
                        if (screenWidth > 0 && screenHeight > 0) {
                            updateVideoScale()
                        }
                    }

                    // 设置准备完成监听器
                    setOnPreparedListener { mp ->
                        isMediaPlayerPrepared = true
                        Log.d(TAG, "MediaPlayer prepared")

                        try {
                            // 设置循环播放
                            mp.isLooping = true

                            // 获取视频尺寸
                            this@VideoEngine.videoWidth = mp.videoWidth
                            this@VideoEngine.videoHeight = mp.videoHeight
                            Log.d(TAG, "Video dimensions: $videoWidth x $videoHeight")

                            // 计算缩放矩阵
                            if (screenWidth > 0 && screenHeight > 0) {
                                updateVideoScale()
                            }

                            // 如果可见，开始播放
                            if (visible) {
                                mp.start()
                                Log.d(TAG, "Starting video playback after prepare")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in onPrepared: ${e.message}")
                        }
                    }

                    // 开始异步准备
                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing MediaPlayer with file", e)
                // 如果初始化失败，尝试清除当前的URI设置
                val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().remove(KEY_VIDEO_URI).apply()
                currentVideoUri = null
            }
        }

        /**
         * 释放MediaPlayer资源
         */
        @Synchronized
        private fun releaseMediaPlayer() {
            Log.d(TAG, "Releasing MediaPlayer")
            val player = mediaPlayer
            // 首先将mediaPlayer设置为null，避免其他线程访问
            mediaPlayer = null
            isMediaPlayerPrepared = false

            // 然后在单独的try-catch块中处理每个操作
            if (player != null) {
                try {
                    if (player.isPlaying) {
                        player.stop()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping MediaPlayer", e)
                }

                try {
                    player.reset()
                } catch (e: Exception) {
                    Log.e(TAG, "Error resetting MediaPlayer", e)
                }

                try {
                    player.release()
                } catch (e: Exception) {
                    Log.e(TAG, "Error releasing MediaPlayer", e)
                }
            }

            // 清除当前的URI设置，确保下次初始化时重新加载
            try {
                val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().remove(KEY_VIDEO_URI).apply()
                currentVideoUri = null
                Log.d(TAG, "Cleared video URI from preferences")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing video URI", e)
            }

            // 等待一下，确保资源已释放
            try {
                Thread.sleep(50)
            } catch (e: Exception) {
                // 忽略中断异常
            }
        }
    }
}
