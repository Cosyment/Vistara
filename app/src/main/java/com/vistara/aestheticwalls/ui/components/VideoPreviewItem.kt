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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.vistara.aestheticwalls.data.model.Wallpaper

// REMOVE: import com.vistara.aestheticwalls.utils.SharedExoPlayer

// Removed lifecycle imports as they are not used for local control anymore

private const val TAG = "VideoPreviewItem" // Added TAG for logging consistency

/**
 * Displays a video preview, integrating with centralized playback control.
 * Retains the original UI layout structure.
 *
 * @param wallpaper 壁纸数据
 * @param exoPlayer The shared ExoPlayer instance from the parent screen.
 * @param isCurrentlyPlaying Whether this specific item should be playing (controlled by parent).
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@OptIn(UnstableApi::class) // Keep OptIn if needed for PlayerView/ExoPlayer APIs used
@Composable
fun VideoPreviewItem(
    wallpaper: Wallpaper, exoPlayer: ExoPlayer,      // CHANGED: Receive shared player
    isCurrentlyPlaying: Boolean, // CHANGED: Receive playing state
    onClick: () -> Unit, modifier: Modifier = Modifier
) {

    var isBuffering by remember(wallpaper.id) { mutableStateOf(true) }

    LaunchedEffect(exoPlayer, wallpaper.id) {
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
                        // 视频结束时重新开始
                        exoPlayer.seekTo(0)
                    }

                    Player.STATE_IDLE -> {
                        isBuffering = true
                    }
                }
            }

            // 监听播放器错误
            override fun onPlayerError(error: PlaybackException) {
                // 记录错误信息
                Log.e("VideoPreviewItem", "Player error: ${error.message}")
                error.cause?.let { Log.e("VideoPreviewItem", "Cause: ${it.message}") }
                // 如果无法切换到低分辨率，回退到静态图片
                isBuffering = false
            }
        }

        exoPlayer.addListener(listener)
    }

    // --- Keep your existing UI structure ---
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black) // Keep original colors
    ) {
        Box(
            modifier = Modifier.fillMaxSize() // Keep original main Box
        ) {

            // --- Static Background/Thumbnail (Always present) ---
            // Keep your AsyncImage here, it acts as the background/default view
            AsyncImage(
                model = wallpaper.previewUrl ?: wallpaper.url, // Use appropriate URL
                contentDescription = wallpaper.title, // Content description for accessibility
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop // Crop to fill the card bounds
                // Keep placeholder/error logic for AsyncImage if needed,
                // but remember they need Painter?, not Composables.
            )

            // --- Conditional Player View (Overlay) ---
            // Use standard if-statement for clarity, or AnimatedVisibility if preferred
            if (isCurrentlyPlaying&&!isBuffering) {
                // Use a key that includes wallpaper.id to potentially help
                // Compose differentiate PlayerView instances if items change rapidly,
                // though the update block is the primary driver.
                key(wallpaper.id) {
                    AndroidView(
                        factory = { ctx ->
                            Log.d(TAG, "Factory creating PlayerView for ${wallpaper.id}")
                            PlayerView(ctx).apply {
                                // Keep your original PlayerView settings
                                useController = false
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                setKeepContentOnPlayerReset(true)
                                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                                setUseArtwork(false)
                                layoutParams = android.widget.FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                                clipToOutline = true // Keep clipping settings
                                outlineProvider = android.view.ViewOutlineProvider.BACKGROUND // Keep outline provider
                            }
                        },
                        // This update block is crucial for connecting/disconnecting the player
                        update = { playerView ->
                            Log.d(TAG, "Update PlayerView for ${wallpaper.id}. isCurrentlyPlaying=$isCurrentlyPlaying")
                            // Attach the single shared ExoPlayer only when this item is the one playing
                            playerView.player = if (isCurrentlyPlaying) exoPlayer else null
                        },
                        // Optional: Reset player reference when view is detached
                        onReset = { playerView ->
                            Log.d(TAG, "Resetting player in PlayerView for ${wallpaper.id}")
                            playerView.player = null
                        }, modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)) // Clip PlayerView itself if needed
                            .background(Color.Black) // Keep background if needed for player
                    )
                }
            }

            // Use AnimatedVisibility to show/hide the buffering indicator smoothly
            androidx.compose.animation.AnimatedVisibility(
                visible = isCurrentlyPlaying && isBuffering, // NEW logic
                enter = fadeIn(animationSpec = tween(500, delayMillis = 300)), // Keep animations
                exit = fadeOut(animationSpec = tween(500)), modifier = Modifier.fillMaxSize() // Fill size to overlay correctly
            ) {
                // Keep your buffering indicator layout
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)), // Semi-transparent background
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp), color = Color.White, strokeWidth = 3.dp
                    )
                }
            }

            // --- Overlays (Keep original placement and visibility logic) ---
            // These likely overlay everything else, so keep them outside the if/else
            if (wallpaper.isPremium) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .padding(horizontal = 6.dp, vertical = 2.dp) // Keep original padding
                ) {
                    Text(
                        text = "👑", // Keep premium indicator
                        style = MaterialTheme.typography.labelSmall, color = Color.White
                    )
                }
            }

            // Author info Box - Keep as is
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth() // More likely fillMaxWidth for bottom overlay
                    .background(
                        Color.Black.copy(alpha = 0.4f) // Keep background scrim
                    )
                    .padding(8.dp), // Keep padding
                contentAlignment = Alignment.BottomStart // Keep alignment
            ) {
                Text(
                    text = wallpaper.author ?: "", // Keep author text
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis // Keep text overflow handling
                )
            }
        }
    }
}