package com.vistara.aestheticwalls.ui.components

import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.OptIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.utils.SharedExoPlayer
import kotlinx.coroutines.delay

/**
 * 优化的视频预览组件
 * 包含更好的缓冲逻辑和作者信息显示
 *
 * @param wallpaper 壁纸数据
 * @param isVisible 是否在可视区域内
 * @param onClick 点击回调
 * @param onVideoComplete 视频播放完成回调
 * @param modifier 修饰符
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPreviewItem(
    wallpaper: Wallpaper,
    isVisible: Boolean,
    onClick: () -> Unit,
    onVideoComplete: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 获取上下文和生命周期
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 视频状态 - 使用remember(wallpaper.id)确保状态与特定壁纸关联
    var isVideoReady by remember(wallpaper.id) { mutableStateOf(false) }
    var isBuffering by remember(wallpaper.id) { mutableStateOf(true) }
    var isPlayerVisible by remember(wallpaper.id) { mutableStateOf(false) }
    var isVideoInitialized by remember(wallpaper.id) { mutableStateOf(false) }

    // 获取或创建共享ExoPlayer实例
    // 使用应用级别的单例模式，而不是为每个项创建实例
    val exoPlayer = remember {
        // 检查是否已经存在共享ExoPlayer实例
        if (SharedExoPlayer.player == null) {
            // 创建新实例并存储在共享对象中
            val newPlayer = ExoPlayer.Builder(context)
                .build()
                .apply {
                    // 设置循环播放
                    repeatMode = Player.REPEAT_MODE_ONE
                    // 设置静音
                    volume = 0f
                    // 设置播放时立即播放
                    playWhenReady = true
                }
            SharedExoPlayer.player = newPlayer
            newPlayer
        } else {
            // 使用现有实例
            SharedExoPlayer.player!!
        }
    }

    // 跟踪当前播放的视频ID
    LaunchedEffect(isVisible, wallpaper.id) {
        if (isVisible) {
            // 更新当前播放的视频ID
            SharedExoPlayer.currentPlayingId = wallpaper.id
        }
    }

    // 监听生命周期事件
    DisposableEffect(lifecycleOwner, wallpaper.id) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                }
                Lifecycle.Event.ON_STOP -> {
                    // 在停止时释放资源
                    exoPlayer.stop()
                    exoPlayer.clearMediaItems()
                    isVideoInitialized = false
                    isVideoReady = false
                    isBuffering = true
                    isPlayerVisible = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    // 只在可见且视频已准备好时播放
                    if (isVisible && isVideoReady && !isBuffering) {
                        exoPlayer.play()
                    }
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // 不释放共享ExoPlayer实例，只清除媒体项
            if (SharedExoPlayer.currentPlayingId == wallpaper.id) {
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
                SharedExoPlayer.currentPlayingId = null
            }
        }
    }

    // 监听播放器状态
    // 添加wallpaper.id作为key，确保当壁纸变化时重新执行
    LaunchedEffect(exoPlayer, wallpaper.id) {
        val listener = object : Player.Listener {
            // 缓冲计数器，用于跟踪缓冲状态切换的频率
            var bufferingCount = 0
            var lastBufferingTime = 0L

            override fun onPlaybackStateChanged(state: Int) {
                val currentTime = System.currentTimeMillis()
                when (state) {
                    Player.STATE_READY -> {
                        Log.d("VideoPreviewItem", "Player state ready for ${wallpaper.id}")

                        // 如果在短时间内频繁切换到缓冲状态，不立即更新UI
                        if (currentTime - lastBufferingTime > 500) {
                            // 重置缓冲计数器
                            bufferingCount = 0

                            isVideoReady = true
                            isBuffering = false

                            // 设置循环范围为前3秒
                            if (exoPlayer.duration > 3000 && exoPlayer.duration != C.TIME_UNSET) {
                                exoPlayer.seekTo(0)
                            }

                            // 显示播放器
                            isPlayerVisible = true
                        } else {
                            Log.d("VideoPreviewItem", "Ignoring rapid state change to READY for ${wallpaper.id}")
                        }
                    }
                    Player.STATE_BUFFERING -> {
                        Log.d("VideoPreviewItem", "Player buffering for ${wallpaper.id}")

                        // 记录缓冲时间
                        lastBufferingTime = currentTime
                        bufferingCount++

                        // 只有当缓冲计数器超过阈值时才显示缓冲指示器
                        if (bufferingCount > 3) {
                            isBuffering = true
                        }
                        // 不要隐藏播放器，避免闪烁
                    }
                    Player.STATE_ENDED -> {
                        Log.d("VideoPreviewItem", "Player ended for ${wallpaper.id}")
                        // 视频结束时重新开始
                        exoPlayer.seekTo(0)
                    }
                    Player.STATE_IDLE -> {
                        Log.d("VideoPreviewItem", "Player idle for ${wallpaper.id}")
                        isVideoReady = false
                        isBuffering = true
                        // 不要隐藏播放器，避免闪烁
                    }
                }
            }

            // 监听播放器错误
            override fun onPlayerError(error: PlaybackException) {
                // 记录错误信息
                Log.e("VideoPreviewItem", "Player error: ${error.message}")
                error.cause?.let { Log.e("VideoPreviewItem", "Cause: ${it.message}") }

                // 如果是模拟数据的URL，使用替代视频URL
                wallpaper.url?.let { originalUrl ->
                    if (originalUrl.contains("example.com")) {
                        Log.d("VideoPreviewItem", "Mock video URL detected, using fallback video")
                        // 使用一个真实的视频URL作为替代
                        // 这里使用Pexels的一个示例视频
                        val fallbackUrl = "https://player.vimeo.com/external/371845664.sd.mp4?s=3b6a9f5ea3e4e1d9c3279487ea6c74bfccc4edd9&profile_id=139&oauth2_token_id=57447761"
                        exoPlayer.setMediaItem(MediaItem.fromUri(fallbackUrl))
                        exoPlayer.prepare()
                        return
                    }

                    // 尝试使用低分辨率版本
                    if (originalUrl.contains("uhd") || originalUrl.contains("4k") || originalUrl.contains("2160") ||
                        originalUrl.contains("3840") || originalUrl.contains("4096")) {
                        // 如果是高清视频，尝试使用低分辨率版本
                        val lowerResUrl = originalUrl
                            .replace("uhd", "hd")
                            .replace("2160", "720")
                            .replace("4096", "1280")
                            .replace("3840", "1280")

                        Log.d("VideoPreviewItem", "Trying lower resolution URL: $lowerResUrl")
                        exoPlayer.setMediaItem(MediaItem.fromUri(lowerResUrl))
                        exoPlayer.prepare()
                        return
                    }

                    // 如果是Pexels视频，尝试使用更低分辨率版本
                    if (originalUrl.contains("pexels.com") && originalUrl.contains("video-files")) {
                        // 尝试使用最低分辨率版本
                        val parts = originalUrl.split("/")
                        if (parts.size >= 2) {
                            val videoId = parts[parts.size - 2]
                            val lowestResUrl = "https://player.vimeo.com/external/" + videoId + ".sd.mp4"
                            Log.d("VideoPreviewItem", "Trying Vimeo URL: $lowestResUrl")
                            exoPlayer.setMediaItem(MediaItem.fromUri(lowestResUrl))
                            exoPlayer.prepare()
                            return
                        }
                    }
                }

                // 如果无法切换到低分辨率，回退到静态图片
                isPlayerVisible = false
                isVideoReady = false
                isBuffering = false
            }
        }

        exoPlayer.addListener(listener)
    }

    // 延迟加载视频，只在可见时加载
    // 使用wallpaper.id作为key，确保当壁纸变化时重新执行
    LaunchedEffect(isVisible, wallpaper.id) {
        if (isVisible && !isVideoInitialized && wallpaper.url != null) {
            // 延迟200毫秒再加载，避免快速滚动时频繁加载
            delay(200)

            // 如果仍然可见，则加载视频
            if (isVisible) {
                Log.d("VideoPreviewItem", "Loading video URL: ${wallpaper.url}")
                isVideoInitialized = true

                try {
                    // 使用壁纸对象中的实际URL
                    val videoUrl = wallpaper.url
                    if (videoUrl.isNullOrEmpty()) {
                        // 如果URL为空，使用备用视频
                        Log.d("VideoPreviewItem", "Empty video URL for ${wallpaper.id}, using fallback")
                        val fallbackUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                        exoPlayer.setMediaItem(MediaItem.fromUri(fallbackUrl))
                    } else {
                        Log.d("VideoPreviewItem", "Using actual video URL for ${wallpaper.id}: $videoUrl")
                        // 检查当前播放的视频ID
                        if (SharedExoPlayer.currentPlayingId != wallpaper.id) {
                            // 重置播放器状态
                            exoPlayer.stop()
                            exoPlayer.clearMediaItems()

                            // 设置新的媒体项
                            exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))

                            // 更新当前播放的视频ID
                            SharedExoPlayer.currentPlayingId = wallpaper.id
                        } else if (exoPlayer.currentMediaItem == null) {
                            // 如果当前没有媒体项，设置新的媒体项
                            exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
                        }
                    }

                    // 设置播放时立即播放
                    exoPlayer.playWhenReady = true

                    // 准备并播放
                    exoPlayer.prepare()

                    // 记录日志
                    Log.d("VideoPreviewItem", "Video prepared for ${wallpaper.id} with URL $videoUrl")

                    // 延迟一秒检查播放状态
                    delay(1000)
                    if (exoPlayer.playbackState != Player.STATE_READY && exoPlayer.playbackState != Player.STATE_BUFFERING) {
                        Log.d("VideoPreviewItem", "Playback state after 1s: ${exoPlayer.playbackState}")
                        // 如果仍然不是准备就绪或缓冲状态，尝试重新准备
                        exoPlayer.seekTo(0)
                        exoPlayer.play()
                    }
                } catch (e: Exception) {
                    // 处理异常
                    Log.e("VideoPreviewItem", "Error loading video: ${e.message}")
                    isVideoReady = false
                    isBuffering = false
                }
            }
        }
    }

    // 根据可见性控制播放状态
    // 添加wallpaper.id作为key，确保当壁纸变化时重新执行
    LaunchedEffect(isVisible, isVideoReady, isBuffering, wallpaper.id) {
        try {
            // 只有当这个视频是当前激活的视频时才播放
            if (isVisible && isVideoReady && !isBuffering && SharedExoPlayer.currentPlayingId == wallpaper.id) {
                exoPlayer.play()
            } else {
                // 如果这个视频是当前激活的视频，则暂停
                if (SharedExoPlayer.currentPlayingId == wallpaper.id) {
                    exoPlayer.pause()
                }

                // 如果不可见，释放更多资源
                if (!isVisible) {
                    // 重置播放器状态
                    isPlayerVisible = false

                    // 如果视频已经初始化且是当前激活的视频，重置到开始位置
                    if (isVideoInitialized && SharedExoPlayer.currentPlayingId == wallpaper.id) {
                        exoPlayer.seekTo(0)
                        // 清除当前激活的视频ID
                        SharedExoPlayer.currentPlayingId = null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("VideoPreviewItem", "Error controlling playback: ${e.message}")
        }
    }

    // 处理循环播放逻辑
    // 添加wallpaper.id作为key，确保当壁纸变化时重新执行
    LaunchedEffect(isVisible, isVideoReady, isBuffering, wallpaper.id) {
        // 只有当视频可见、准备就绪且不在缓冲时才循环播放
        // 并且必须是当前激活的视频
        if (isVisible && isVideoReady && !isBuffering && SharedExoPlayer.currentPlayingId == wallpaper.id) {
            Log.d("VideoPreviewItem", "Starting playback timer for ${wallpaper.id}")
            val playDuration = 6000L // 每个视频播放6秒
            var totalPlayTime = 0L
            val startTime = System.currentTimeMillis()

            try {
                // 使用更安全的方式检查状态，避免无限循环
                var isPlaying = true
                while (isPlaying && totalPlayTime < playDuration) {
                    // 检查组件是否仍然在组合中且是当前激活的视频
                    if (!isVisible || !isVideoReady || isBuffering || SharedExoPlayer.currentPlayingId != wallpaper.id) {
                        isPlaying = false
                        break
                    }

                    val currentTime = System.currentTimeMillis()
                    totalPlayTime = currentTime - startTime

                    // 等待一小段时间再检查
                    delay(500) // 每500毫秒检查一次
                }

                // 只有在正常完成播放时才通知
                if (isPlaying && totalPlayTime >= playDuration && SharedExoPlayer.currentPlayingId == wallpaper.id) {
                    // 通知播放完成
                    Log.d("VideoPreviewItem", "Video complete: ${wallpaper.id}")
                    // 播放完成后隐藏播放器，显示缩略图
                    isPlayerVisible = false
                    onVideoComplete(wallpaper.id)
                }
            } catch (e: Exception) {
                Log.e("VideoPreviewItem", "Error in playback timer: ${e.message}")
            }

            Log.d("VideoPreviewItem", "Playback timer ended for ${wallpaper.id}")
        } else if (isVisible && !isVideoReady) {
            // 如果视频可见但还没有准备好，每秒检查一次状态
            Log.d("VideoPreviewItem", "Waiting for video to be ready: ${wallpaper.id}")
            delay(1000)
        }
    }

    // UI部分
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 静态缩略图作为背景
            AsyncImage(
                model = wallpaper.thumbnailUrl,
                contentDescription = wallpaper.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 视频播放器，仅在准备就绪后显示
            // 添加明确的裁剪边界和背景色，确保视频内容不会溢出
            androidx.compose.animation.AnimatedVisibility(
                visible = isPlayerVisible,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(300)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)) // 确保与卡片形状一致
                        .background(Color.Black) // 添加背景色防止透明溢出
                ) {
                    // 使用key参数确保播放器视图的稳定性
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

                                // 优化性能设置
                                setKeepContentOnPlayerReset(true)

                                // 减少重绘频率
                                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                                setUseArtwork(false)

                                // 设置布局参数
                                layoutParams = android.widget.FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)

                                // 确保视频内容不会溢出
                                clipToOutline = true
                                outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { view ->
                            // 仅在需要时更新播放器
                            if (view.player != exoPlayer) {
                                view.player = exoPlayer
                            }
                        },
                        onReset = { playerView ->
                            // 释放资源
                            playerView.player = null
                        }
                        // 注意：AndroidView不支持key参数，我们已经在remember中使用wallpaper.id作为key
                    )
                }
            }

            // 缓冲指示器，使用动画过渡，增加动画时间和延迟以减少闪烁
            androidx.compose.animation.AnimatedVisibility(
                visible = isBuffering && isVisible,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 300)),
                exit = fadeOut(animationSpec = tween(500)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                }
            }

            // 高级标记
            if (wallpaper.isPremium) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "👑", // 皮冠表情
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            // 作者信息
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.4f)
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = wallpaper.author ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
