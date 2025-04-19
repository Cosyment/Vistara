package com.vistara.aestheticwalls.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import java.io.IOException

/**
 * 视频壁纸服务
 * 使用MediaPlayer播放视频作为动态壁纸
 */
class LiveWallpaperService : WallpaperService() {

    // Note: Keep ScaleMode enum as is.
    enum class ScaleMode { FILL, FIT, CENTER_CROP }

    companion object {
        private const val TAG = "LiveWallpaperService"
        const val EXTRA_VIDEO_URI = "video_uri" // Keep if used externally
        private const val PREFS_NAME = "video_wallpaper_prefs"
        private const val KEY_VIDEO_URI = "video_uri"
        private const val KEY_UNIQUE_ID = "unique_id" // For debugging

        // No need for static currentVideoUri, rely solely on SharedPreferences
        // for persistence across service restarts.

        /**
         * 设置视频URI
         */
        fun setVideoUri(context: Context, uri: Uri) {
            Log.d(TAG, "Setting video URI: $uri")
            val uniqueId = System.currentTimeMillis().toString() // For debugging/tracking
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            prefs.edit().putString(KEY_VIDEO_URI, uri.toString()).putString(KEY_UNIQUE_ID, uniqueId) // Store unique ID for debugging
                .apply()
            Log.d(TAG, "Video URI saved to preferences: $uri with unique ID: $uniqueId")
            // Note: No need to update a static variable here.
            // Consider broadcasting an update if the service is already running
            // and needs to change video immediately without full restart.
        }

        /**
         * 获取当前视频URI (从 SharedPreferences 读取)
         */
        fun getCurrentVideoUri(context: Context): Uri? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val uriString = prefs.getString(KEY_VIDEO_URI, null)
            val uniqueId = prefs.getString(KEY_UNIQUE_ID, "none") // For debugging

            return if (uriString != null) {
                try {
                    val uri = Uri.parse(uriString)
                    Log.d(TAG, "Retrieved video URI from preferences: $uri (ID: $uniqueId)")
                    uri
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing saved URI: $uriString", e)
                    null
                }
            } else {
                Log.w(TAG, "No video URI found in preferences.")
                null
            }
        }
    }

    override fun onCreateEngine(): Engine {
        Log.d(TAG, "Creating video wallpaper engine")
        return VideoEngine()
    }

    inner class VideoEngine : Engine() {
        private var mediaPlayer: MediaPlayer? = null
        private var isMediaPlayerPrepared = false
        private var surfaceAvailable = false
        private var surfaceHolder: SurfaceHolder? = null
        private var currentVideoUri: Uri? = null // Store the URI used by this engine instance

        // Use ApplicationContext to avoid leaks
        private val appContext: Context = applicationContext
        private val handler = Handler(Looper.getMainLooper())

        // Dimensions
        private var screenWidth = 0
        private var screenHeight = 0
        private var videoWidth = 0
        private var videoHeight = 0

        // Scaling mode (default or make it configurable via SharedPreferences)
        private var scaleMode = ScaleMode.CENTER_CROP

        private val screenStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        Log.d(TAG, "Screen OFF, pausing video")
                        pauseVideo()
                    }

                    Intent.ACTION_SCREEN_ON -> {
                        // Only resume if the wallpaper is actually visible
                        if (isVisible) {
                            Log.d(TAG, "Screen ON and visible, playing video")
                            playVideo()
                        } else {
                            Log.d(TAG, "Screen ON but not visible, video remains paused")
                        }
                    }
                }
            }
        }

        init {
            Log.d(TAG, "VideoEngine initializing")
            val intentFilter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
            }
            // Use appContext for receiver registration tied to service lifecycle
            appContext.registerReceiver(screenStateReceiver, intentFilter)
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            Log.d(TAG, "Engine onCreate")
            this.surfaceHolder = surfaceHolder
            // Retrieve URI here once, when the engine is created
            currentVideoUri = getCurrentVideoUri(appContext)
            val uniqueId = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_UNIQUE_ID, "N/A")
            Log.d(TAG, "Engine created. Current URI: $currentVideoUri (ID: $uniqueId)")
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            Log.d(TAG, "Surface created")
            surfaceAvailable = true
            this.surfaceHolder = holder // Ensure holder is up-to-date
            initializeAndPlayVideo() // Attempt to start video now that surface exists
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            Log.d(TAG, "Surface changed: $width x $height")
            screenWidth = width
            screenHeight = height
            this.surfaceHolder = holder // Update holder reference
            // Re-apply scaling if video is already prepared
            if (isMediaPlayerPrepared && videoWidth > 0 && videoHeight > 0) {
                Log.d(TAG, "Surface changed, applying video scale")
                updateVideoScale()
                // Ensure video is playing if it should be
                if (isVisible) {
                    playVideo()
                }
            } else if (!isMediaPlayerPrepared && currentVideoUri != null) {
                // If surface changed before media player was ready, try initializing again
                Log.d(TAG, "Surface changed, attempting to initialize video again")
                initializeAndPlayVideo()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            Log.d(TAG, "Surface destroyed")
            surfaceAvailable = false
            // Surface is gone, pause and release the player is typically handled in onDestroy
            // But pausing here ensures it stops drawing immediately.
            pauseVideo()
            // Consider releasing partially initialized player if needed, but full release in onDestroy
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
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
                appContext.unregisterReceiver(screenStateReceiver)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Receiver already unregistered or never registered.", e)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering receiver", e)
            }

            // Release MediaPlayer safely on the main thread
            handler.post {
                releaseMediaPlayer()
            }
            surfaceHolder = null // Clear reference
        }

        /**
         * Initializes MediaPlayer if needed and starts playback.
         */
        private fun initializeAndPlayVideo() {
            if (!surfaceAvailable) {
                Log.w(TAG, "initializeAndPlayVideo called, but surface is not available.")
                return
            }
            if (mediaPlayer != null && isMediaPlayerPrepared) {
                Log.d(TAG, "MediaPlayer already prepared, ensuring playback if visible.")
                if (isVisible) playVideo()
                return
            }
            if (mediaPlayer != null && !isMediaPlayerPrepared) {
                Log.w(TAG, "MediaPlayer exists but is not prepared. Waiting for preparation.")
                // The existing prepareAsync should handle this.
                return
            }

            if (currentVideoUri == null) {
                Log.e(TAG, "Cannot initialize MediaPlayer: Video URI is null.")
                // Optionally display a default image or color here
                return
            }

            Log.d(TAG, "Initializing MediaPlayer with URI: $currentVideoUri")
            releaseMediaPlayer() // Ensure any previous instance is released cleanly

            try {
                mediaPlayer = MediaPlayer().apply {
                    // Set surface *before* setDataSource if possible and valid
                    try {
                        val surface = surfaceHolder?.surface
                        if (surface != null && surface.isValid) {
                            setSurface(surface)
                            Log.d(TAG, "Set surface before data source")
                        } else {
                            Log.w(TAG, "Surface invalid or null when trying to set early")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error setting surface early", e)
                    }

                    val uriString = currentVideoUri.toString()
                    try {
                        if (uriString.startsWith("http")) {
                            Log.d(TAG, "Setting network data source: $uriString")
                            setDataSource(uriString)
                        } else {
                            Log.d(TAG, "Setting content/file data source using context: $currentVideoUri")
                            setDataSource(appContext, currentVideoUri!!) // Use context for content/file URIs
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to setDataSource for URI: $currentVideoUri", e)
                        // Handle error: Maybe notify user, try fallback, or just stop.
                        releaseMediaPlayer() // Clean up failed player
                        return@apply // Exit apply block
                    }

                    isLooping = true
                    setVolume(0f, 0f) // Mute wallpaper videos by default

                    setOnErrorListener { mp, what, extra ->
                        Log.e(TAG, "MediaPlayer error - what: $what, extra: $extra")
                        isMediaPlayerPrepared = false // Mark as not prepared
                        releaseMediaPlayer() // Release the faulty player
                        // Optionally schedule a retry after a delay
                        // handler.postDelayed({ initializeAndPlayVideo() }, 5000)
                        true // Error handled
                    }

                    setOnVideoSizeChangedListener { mp, width, height ->
                        Log.d(TAG, "Video size changed: $width x $height")
                        if (width > 0 && height > 0) {
                            this@VideoEngine.videoWidth = width
                            this@VideoEngine.videoHeight = height
                            if (screenWidth > 0 && screenHeight > 0) {
                                updateVideoScale()
                            }
                        } else {
                            Log.w(TAG, "Video size reported as zero or negative.")
                        }
                    }

                    setOnPreparedListener { mp ->
                        Log.d(TAG, "MediaPlayer prepared.")
                        isMediaPlayerPrepared = true

                        // Get initial video size
                        if (mp.videoWidth > 0 && mp.videoHeight > 0) {
                            this@VideoEngine.videoWidth = mp.videoWidth
                            this@VideoEngine.videoHeight = mp.videoHeight
                            Log.d(TAG, "Initial Video dimensions on prepare: $videoWidth x $videoHeight")
                            if (screenWidth > 0 && screenHeight > 0) {
                                updateVideoScale()
                            }
                        } else {
                            Log.w(TAG, "Video dimensions zero or negative on prepare.")
                            // Request layout or wait for onVideoSizeChanged? Usually size is available here.
                        }

                        // Start playing only if the wallpaper is currently visible
                        if (isVisible && surfaceAvailable) {
                            Log.d(TAG, "MediaPlayer prepared and visible, starting playback.")
                            mp.start()
                        } else {
                            Log.d(TAG, "MediaPlayer prepared but not visible or surface unavailable, will start on visibility change.")
                        }
                    }

                    Log.d(TAG, "Calling prepareAsync()")
                    prepareAsync() // Prepare asynchronously
                }
            } catch (e: IOException) {
                Log.e(TAG, "IOException during MediaPlayer setup", e)
                releaseMediaPlayer()
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "IllegalArgumentException during MediaPlayer setup (likely bad URI or surface)", e)
                releaseMediaPlayer()
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException during MediaPlayer setup (permissions?)", e)
                releaseMediaPlayer()
            } catch (e: IllegalStateException) {
                Log.e(TAG, "IllegalStateException during MediaPlayer setup (player state error?)", e)
                releaseMediaPlayer()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during MediaPlayer setup", e)
                releaseMediaPlayer()
            }
        }

        private fun playVideo() {
            if (mediaPlayer != null && isMediaPlayerPrepared && surfaceAvailable && !mediaPlayer!!.isPlaying) {
                try {
                    Log.d(TAG, "Attempting to start video playback.")
                    mediaPlayer?.start()
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "Error starting MediaPlayer (invalid state?)", e)
                    // May need re-initialization
                    releaseMediaPlayer()
                    handler.postDelayed({ initializeAndPlayVideo() }, 1000) // Retry init
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error starting MediaPlayer", e)
                }
            } else {
                Log.d(
                    TAG,
                    "Conditions not met for playing video: prepared=$isMediaPlayerPrepared, surface=$surfaceAvailable, playing=${mediaPlayer?.isPlaying}"
                )
                // If not prepared but should be playing, ensure initialization is triggered
                if (mediaPlayer == null && currentVideoUri != null && surfaceAvailable) {
                    Log.d(TAG, "playVideo called but player is null, attempting init.")
                    initializeAndPlayVideo()
                }
            }
        }

        private fun pauseVideo() {
            if (mediaPlayer != null && isMediaPlayerPrepared && mediaPlayer!!.isPlaying) {
                try {
                    Log.d(TAG, "Pausing video playback.")
                    mediaPlayer?.pause()
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "Error pausing MediaPlayer (invalid state?)", e)
                    // State might be messed up, potentially release?
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error pausing MediaPlayer", e)
                }
            } else {
                Log.d(TAG, "Conditions not met for pausing video: prepared=$isMediaPlayerPrepared, playing=${mediaPlayer?.isPlaying}")
            }
        }

        private fun updateVideoScale() {
            if (videoWidth <= 0 || videoHeight <= 0 || screenWidth <= 0 || screenHeight <= 0 || mediaPlayer == null || !isMediaPlayerPrepared) {
                Log.w(
                    TAG,
                    "Cannot update video scale - invalid dimensions or player state. Vid($videoWidth x $videoHeight), Screen($screenWidth x $screenHeight), Prepared=$isMediaPlayerPrepared"
                )
                return
            }

            Log.d(TAG, "Updating video scale. Video: ${videoWidth}x$videoHeight, Screen: ${screenWidth}x$screenHeight, Mode: $scaleMode")

            // Using setVideoScalingMode - ensure it's supported and works as expected.
            // Alternatives (more complex) involve TextureView or SurfaceView manipulations not standard in WallpaperService.
            try {
                when (scaleMode) {
                    ScaleMode.FILL -> {
                        // This mode might stretch the video if aspect ratios don't match.
                        // MediaPlayer might do this by default or require a specific mode if available.
                        // Let's explicitly use SCALE_TO_FIT which might be closer, but FILL isn't a direct MediaPlayer mode.
                        // For true fill (stretching), often no specific mode is needed, or it's the default.
                        // Let's try setting it to SCALE_TO_FIT for consistency, behavior might vary.
                        Log.d(TAG, "Setting scaling mode: SCALE_TO_FIT (intended for FILL)")
                        mediaPlayer?.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                    }

                    ScaleMode.FIT -> {
                        // Letterbox/Pillarbox: maintains aspect ratio, fits within screen bounds.
                        Log.d(TAG, "Setting scaling mode: SCALE_TO_FIT")
                        mediaPlayer?.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                    }

                    ScaleMode.CENTER_CROP -> {
                        // Maintain aspect ratio, scale to fill screen, cropping excess.
                        Log.d(TAG, "Setting scaling mode: SCALE_TO_FIT_WITH_CROPPING")
                        mediaPlayer?.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                    }
                }
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Error setting video scaling mode (invalid state?)", e)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting video scaling mode", e)
            }
        }

        @Synchronized
        private fun releaseMediaPlayer() {
            if (mediaPlayer == null) {
                return // Nothing to release
            }
            Log.d(TAG, "Releasing MediaPlayer instance.")
            isMediaPlayerPrepared = false
            val playerToRelease = mediaPlayer
            mediaPlayer = null // Set to null immediately

            // Perform release operations in background or carefully handle states
            try {
                // Check state before calling stop/reset if possible, though catching exceptions is safer
                if (playerToRelease != null) {
                    if (playerToRelease.isPlaying) {
                        playerToRelease.stop()
                    }
                    playerToRelease.reset() // Reset listeners and state
                    playerToRelease.release() // Release native resources
                    Log.d(TAG, "MediaPlayer released successfully.")
                }
            } catch (e: IllegalStateException) {
                Log.e(TAG, "IllegalStateException during MediaPlayer release", e)
                // Might happen if already released or in wrong state, try to release anyway if possible
                try {
                    playerToRelease?.release()
                } catch (ex: Exception) { /* ignore inner */
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during MediaPlayer release", e)
                // Ensure release is called even if stop/reset fail
                try {
                    playerToRelease?.release()
                } catch (ex: Exception) { /* ignore inner */
                }
            }

            // DO NOT clear the URI from SharedPreferences here.
            // It should persist until the user changes it.
        }
    }
}