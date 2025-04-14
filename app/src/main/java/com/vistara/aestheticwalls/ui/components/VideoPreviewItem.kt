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

    // 视频状态
    var isVideoReady by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    var isPlayerVisible by remember { mutableStateOf(false) }
    var isVideoInitialized by remember { mutableStateOf(false) }

    // 创建ExoPlayer实例
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                // 设置循环播放
                repeatMode = Player.REPEAT_MODE_ONE
                // 设置静音
                volume = 0f

                wallpaper.url?.let { url ->
                    Log.d("VideoPreviewItem", "Setting media item: $url")
                    setMediaItem(MediaItem.fromUri(url))
                    prepare()
                }
            }
    }

    // 监听生命周期事件
    DisposableEffect(lifecycleOwner) {
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
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.release()
        }
    }

    // 监听播放器状态
    LaunchedEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        isVideoReady = true
                        isBuffering = false

                        // 设置循环范围为前3秒
                        if (exoPlayer.duration > 3000 && exoPlayer.duration != C.TIME_UNSET) {
                            exoPlayer.seekTo(0)
                        }

                        // 显示播放器
                        isPlayerVisible = true
                    }
                    Player.STATE_BUFFERING -> {
                        isBuffering = true
                    }
                    Player.STATE_ENDED -> {
                        // 视频结束时重新开始
                        exoPlayer.seekTo(0)
                    }
                    Player.STATE_IDLE -> {
                        isVideoReady = false
                        isBuffering = true
                    }
                }
            }

            // 监听播放器错误
            override fun onPlayerError(error: PlaybackException) {
                // 记录错误信息
                Log.e("VideoPreviewItem", "Player error: ${error.message}")
                error.cause?.let { Log.e("VideoPreviewItem", "Cause: ${it.message}") }

                // 如果是模拟数据的URL，直接回退到静态图片
                wallpaper.url?.let { originalUrl ->
                    if (originalUrl.contains("example.com")) {
                        Log.d("VideoPreviewItem", "Mock video URL detected, falling back to static image")
                        isPlayerVisible = false
                        isVideoReady = false
                        isBuffering = false
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
    LaunchedEffect(isVisible) {
        if (isVisible && !isVideoInitialized && wallpaper.url != null) {
            // 延迟200毫秒再加载，避免快速滚动时频繁加载
            delay(200)

            // 如果仍然可见，则加载视频
            if (isVisible) {
                Log.e("OptimizedVideoPreviewItem", "Video URL: ${wallpaper.url}")
                isVideoInitialized = true
                exoPlayer.setMediaItem(MediaItem.fromUri(wallpaper.url))

                // 使用低分辨率视频以提高性能
                exoPlayer.prepare()
            }
        }
    }

    // 根据可见性控制播放状态
    LaunchedEffect(isVisible, isVideoReady, isBuffering) {
        if (isVisible && isVideoReady && !isBuffering) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    // 处理循环播放逻辑
    LaunchedEffect(isVisible, isVideoReady, isBuffering) {
        // 只有当视频可见、准备就绪且不在缓冲时才循环播放
        if (isVisible && isVideoReady && !isBuffering) {
            val loopDuration = 3000L // 3秒
            val playDuration = 6000L // 每个视频播放6秒
            var totalPlayTime = 0L
            var lastSeekTime = 0L

            // 循环播放前3秒
            while (isVisible && isVideoReady && !isBuffering) {
                val currentTime = System.currentTimeMillis()

                // 如果当前播放位置超过3秒，重置到开头
                if (exoPlayer.currentPosition > loopDuration) {
                    exoPlayer.seekTo(0)
                    lastSeekTime = currentTime
                }

                // 计算总播放时间
                if (lastSeekTime > 0) {
                    totalPlayTime += (currentTime - lastSeekTime)
                    lastSeekTime = currentTime
                } else {
                    lastSeekTime = currentTime
                }

                // 如果播放时间超过6秒，通知播放完成
                if (totalPlayTime >= playDuration) {
                    // 通知播放完成
                    onVideoComplete(wallpaper.id)
                    totalPlayTime = 0L
                }

                // 等待一小段时间再检查
                delay(500) // 每500毫秒检查一次
            }
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
            androidx.compose.animation.AnimatedVisibility(
                visible = isPlayerVisible,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(300)),
                modifier = Modifier.fillMaxSize()
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

                            // 优化性能设置
                            setKeepContentOnPlayerReset(true)

                            // 设置布局参数
                            layoutParams = android.widget.FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 缓冲指示器
            if (isBuffering && isVisible) {
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
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(4.dp)
                        )
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
