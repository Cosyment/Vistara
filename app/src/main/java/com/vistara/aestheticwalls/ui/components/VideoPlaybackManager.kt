package com.vistara.aestheticwalls.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.vistara.aestheticwalls.data.model.Wallpaper
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

/**
 * 视频播放管理器
 * 用于控制视频播放，优化性能
 */
class VideoPlaybackManager {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val visibleVideoIds = mutableSetOf<String>()
    private val playingVideoIds = mutableSetOf<String>()
    private var currentPlayingId: String? = null
    private var isScrolling = false
    private var useSequentialPlayback = true
    private var onVideoCompleteListener: ((String) -> Unit)? = null
    private var scrollStopTimer: Job? = null
    private var lastUpdateTime = 0L
    private val UPDATE_INTERVAL = 150L // 更新间隔

    fun addVisibleVideo(id: String) {
        if (System.currentTimeMillis() - lastUpdateTime < UPDATE_INTERVAL) return
        lastUpdateTime = System.currentTimeMillis()
        
        if (visibleVideoIds.add(id)) {
            updatePlayingVideos()
        }
    }

    fun removeVisibleVideo(id: String) {
        if (System.currentTimeMillis() - lastUpdateTime < UPDATE_INTERVAL) return
        lastUpdateTime = System.currentTimeMillis()
        
        visibleVideoIds.remove(id)
        playingVideoIds.remove(id)
        if (id == currentPlayingId) {
            currentPlayingId = null
            updatePlayingVideos()
        }
    }

    fun clearVisibleVideos() {
        visibleVideoIds.clear()
        playingVideoIds.clear()
        currentPlayingId = null
    }

    private fun updatePlayingVideos() {
        if (isScrolling) {
            playingVideoIds.clear()
            currentPlayingId = null
            return
        }

        if (visibleVideoIds.isEmpty()) {
            playingVideoIds.clear()
            currentPlayingId = null
            return
        }

        if (useSequentialPlayback) {
            if (currentPlayingId == null || currentPlayingId !in visibleVideoIds) {
                currentPlayingId = visibleVideoIds.firstOrNull()
                playingVideoIds.clear()
                currentPlayingId?.let { playingVideoIds.add(it) }
            }
        } else {
            playingVideoIds.clear()
            playingVideoIds.addAll(visibleVideoIds)
        }
    }

    fun shouldPlayVideo(id: String): Boolean = id in playingVideoIds

    fun setSequentialPlayback(useSequential: Boolean) {
        if (useSequentialPlayback != useSequential) {
            useSequentialPlayback = useSequential
            updatePlayingVideos()
        }
    }

    fun setVideoCompleteListener(listener: (String) -> Unit) {
        onVideoCompleteListener = listener
    }

    fun notifyVideoComplete(videoId: String) {
        if (videoId != currentPlayingId) return
        
        scope.launch {
            currentPlayingId = null
            playingVideoIds.clear()
            onVideoCompleteListener?.invoke(videoId)

            if (useSequentialPlayback && visibleVideoIds.isNotEmpty()) {
                val currentIndex = visibleVideoIds.indexOf(videoId)
                currentPlayingId = when {
                    currentIndex != -1 && currentIndex < visibleVideoIds.size - 1 -> 
                        visibleVideoIds.elementAt(currentIndex + 1)
                    else -> visibleVideoIds.firstOrNull()
                }
                currentPlayingId?.let { playingVideoIds.add(it) }
            } else {
                updatePlayingVideos()
            }
        }
    }

    fun setScrolling(scrolling: Boolean) {
        if (isScrolling == scrolling) return
        
        scope.launch {
            isScrolling = scrolling
            scrollStopTimer?.cancel()

            if (scrolling) {
                playingVideoIds.clear()
            } else {
                scrollStopTimer = scope.launch {
                    delay(300) // 等待滚动完全停止
                    updatePlayingVideos()
                }
            }
        }
    }

    fun isScrolling(): Boolean = isScrolling

    fun dispose() {
        scope.cancel()
        clearVisibleVideos()
    }
}

/**
 * 创建并记住视频播放管理器
 */
@Composable
fun rememberVideoPlaybackManager(): VideoPlaybackManager {
    val lifecycleOwner = LocalLifecycleOwner.current
    val manager = remember { VideoPlaybackManager() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> manager.clearVisibleVideos()
                Lifecycle.Event.ON_DESTROY -> manager.dispose()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            manager.dispose()
        }
    }

    return manager
}
