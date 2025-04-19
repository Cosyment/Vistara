package com.vistara.aestheticwalls.utils

import androidx.media3.exoplayer.ExoPlayer

/**
 * 共享ExoPlayer单例
 * 用于在整个应用中共享一个ExoPlayer实例，避免为每个视频预览项创建单独的实例
 * 这样可以显著提高性能，减少内存使用，并避免滑动卡顿
 */
object SharedExoPlayer {
    /**
     * 共享的ExoPlayer实例
     * 可空，但在应用启动后会立即初始化
     */
    var player: ExoPlayer? = null

    /**
     * 当前正在播放的视频ID
     * 用于跟踪当前正在播放的视频，避免多个视频同时播放
     */
    var currentPlayingId: String? = null

    /**
     * 释放ExoPlayer资源
     * 应在应用退出时调用
     */
    fun release() {
        player?.release()
        player = null
        currentPlayingId = null
    }
}