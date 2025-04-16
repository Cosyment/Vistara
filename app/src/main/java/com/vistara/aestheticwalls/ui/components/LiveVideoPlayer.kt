package com.vistara.aestheticwalls.ui.components

import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.vistara.aestheticwalls.data.model.Wallpaper

/**
 * 动态壁纸播放器组件
 * 用于详情页全屏播放动态壁纸
 *
 * @param wallpaper 壁纸数据
 * @param onTap 点击回调
 * @param modifier 修饰符
 */
@OptIn(UnstableApi::class)
@Composable
fun LiveVideoPlayer(
    wallpaper: Wallpaper, onTap: () -> Unit, modifier: Modifier = Modifier
) {
    // 跟踪重组频率 - 仅在调试时启用
    // val lastRecompositionTime = remember { mutableStateOf(0L) }
    // val currentTime = System.currentTimeMillis()
    // if (lastRecompositionTime.value > 0 && currentTime - lastRecompositionTime.value < 500) {
    //     Log.d(
    //         "LiveVideoPlayer",
    //         "Frequent recomposition detected: ${currentTime - lastRecompositionTime.value}ms for ${wallpaper.id}"
    //     )
    // }
    // lastRecompositionTime.value = currentTime
    // 获取上下文和生命周期
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 创建ExoPlayer实例 - 使用remember确保稳定性
    val exoPlayer = remember(wallpaper.id) {
        Log.d("LiveVideoPlayer", "Creating new ExoPlayer instance for ${wallpaper.id}")
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            playWhenReady = true

            // 设置视频URL
            wallpaper.url?.let { url ->
                if (url.isNotEmpty()) {
                    Log.d("LiveVideoPlayer", "Setting media item: $url")
                    setMediaItem(MediaItem.fromUri(url))
                    prepare()
                }
            }
        }
    }

    // 简化状态管理 - 使用remember确保状态稳定性
    var isBuffering by remember(wallpaper.id) { mutableStateOf(true) }

    // 使用key包装整个内容，确保在wallpaper.id变化时完全重建组件
    // 这有助于防止部分重组导致的问题

    // 监听播放器状态 - 仅在exoPlayer变化时重新设置监听器
    DisposableEffect(key1 = exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        isBuffering = false
                    }

                    Player.STATE_BUFFERING -> {
                        isBuffering = true
                    }

                    Player.STATE_ENDED -> {
                        // 循环播放
                        exoPlayer.seekTo(0)
                        exoPlayer.play()
                    }

                    else -> {}
                }
            }
        }

        exoPlayer.addListener(listener)

        // 监听生命周期
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                }

                Lifecycle.Event.ON_RESUME -> {
                    exoPlayer.play()
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            exoPlayer.removeListener(listener)
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            exoPlayer.release()
        }
    }

    // 使用remember确保在wallpaper.id变化时完全重建组件
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 静态缩略图作为背景
        AsyncImage(
            model = wallpaper.thumbnailUrl,
            contentDescription = wallpaper.title,
            modifier = Modifier.fillMaxSize()
        )

        // 视频播放器 - 使用key确保稳定性
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setKeepContentOnPlayerReset(true)
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    layoutParams =
                        android.widget.FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    setOnClickListener { onTap() }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                // 仅在需要时更新播放器
                if (view.player != exoPlayer) {
                    view.player = exoPlayer
                }
            },
            // 防止重组时更新
            // shouldUpdate = { _ -> false }
        )

        // 缓冲指示器 - 仅在缓冲时显示
        if (isBuffering) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp), color = Color.White, strokeWidth = 3.dp
                )
            }
        }
    }
}
