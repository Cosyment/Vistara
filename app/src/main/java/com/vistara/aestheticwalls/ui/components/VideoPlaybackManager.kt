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

/**
 * 视频播放管理器
 * 用于控制视频播放，优化性能
 */
class VideoPlaybackManager {
    // 当前可见的视频ID列表
    private val visibleVideoIds = mutableStateListOf<String>()

    // 当前正在播放的视频ID
    private val playingVideoIds = mutableStateListOf<String>()

    // 是否正在滚动
    private var isScrolling = false

    // 滚动停止计时器
    private var scrollStopTimer: kotlinx.coroutines.Job? = null

    // 当前正在播放的视频ID
    private var currentPlayingId: String? = null

    // 是否使用顺序播放模式
    private var useSequentialPlayback = true

    // 视频播放完成回调
    private var onVideoCompleteListener: ((String) -> Unit)? = null

    /**
     * 添加可见视频
     */
    fun addVisibleVideo(id: String) {
        if (!visibleVideoIds.contains(id)) {
            visibleVideoIds.add(id)
            updatePlayingVideos()
        }
    }

    /**
     * 移除可见视频
     */
    fun removeVisibleVideo(id: String) {
        visibleVideoIds.remove(id)
        playingVideoIds.remove(id)
        updatePlayingVideos()
    }

    /**
     * 清除所有可见视频
     */
    fun clearVisibleVideos() {
        visibleVideoIds.clear()
        playingVideoIds.clear()
    }

    /**
     * 更新正在播放的视频列表
     */
    private fun updatePlayingVideos() {
        // 如果正在滚动，不播放任何视频
        if (isScrolling) {
            playingVideoIds.clear()
            currentPlayingId = null
            return
        }

        // 如果没有可见的视频，清空播放列表
        if (visibleVideoIds.isEmpty()) {
            playingVideoIds.clear()
            currentPlayingId = null
            return
        }

        // 如果当前没有正在播放的视频，选择一个开始播放
        if (currentPlayingId == null) {
            // 如果使用顺序播放模式，选择第一个可见的视频
            if (useSequentialPlayback) {
                currentPlayingId = visibleVideoIds.firstOrNull()
                playingVideoIds.clear()
                if (currentPlayingId != null) {
                    playingVideoIds.add(currentPlayingId!!)
                }
            } else {
                // 非顺序模式，同时播放所有可见视频
                playingVideoIds.clear()
                playingVideoIds.addAll(visibleVideoIds)
            }
        } else {
            // 如果当前正在播放的视频不再可见，选择一个新的视频
            if (currentPlayingId !in visibleVideoIds) {
                currentPlayingId = visibleVideoIds.firstOrNull()
                playingVideoIds.clear()
                if (currentPlayingId != null) {
                    playingVideoIds.add(currentPlayingId!!)
                }
            }
        }
    }

    /**
     * 检查视频是否应该播放
     */
    fun shouldPlayVideo(id: String): Boolean {
        return id in playingVideoIds
    }

    /**
     * 设置是否使用顺序播放模式
     */
    fun setSequentialPlayback(useSequential: Boolean) {
        useSequentialPlayback = useSequential
        updatePlayingVideos()
    }

    /**
     * 设置视频播放完成回调
     */
    fun setOnVideoCompleteListener(listener: (String) -> Unit) {
        onVideoCompleteListener = listener
    }

    /**
     * 通知视频播放完成
     */
    fun notifyVideoComplete(videoId: String) {
        if (videoId == currentPlayingId) {
            // 当前播放的视频已完成，播放下一个
            currentPlayingId = null
            playingVideoIds.clear()

            // 回调通知
            onVideoCompleteListener?.invoke(videoId)

            // 更新播放列表，播放下一个视频
            updatePlayingVideos()
        }
    }

    /**
     * 设置滚动状态
     * 当滚动时暂停所有视频播放
     */
    fun setScrolling(scrolling: Boolean) {
        if (isScrolling != scrolling) {
            isScrolling = scrolling

            if (scrolling) {
                // 开始滚动时清除所有正在播放的视频
                playingVideoIds.clear()
                // 取消之前的计时器
                scrollStopTimer?.cancel()
                scrollStopTimer = null
            } else {
                // 滚动停止后更新播放列表
                updatePlayingVideos()
            }
        }
    }

    /**
     * 检查是否正在滚动
     */
    fun isScrolling(): Boolean {
        return isScrolling
    }
}

/**
 * 创建并记住视频播放管理器
 */
@Composable
fun rememberVideoPlaybackManager(): VideoPlaybackManager {
    val lifecycleOwner = LocalLifecycleOwner.current
    val manager = remember { VideoPlaybackManager() }

    // 监听生命周期事件
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // 应用进入后台时清除所有播放
                    manager.clearVisibleVideos()
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            manager.clearVisibleVideos()
        }
    }

    return manager
}
