package com.vistara.aestheticwalls.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import com.vistara.aestheticwalls.R
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.ui.theme.stringResource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 动态壁纸网格组件
 * 使用统一大小的网格布局，专门用于显示动态壁纸
 */
@Composable
fun LiveVideoGrid(
    wallpapers: List<Wallpaper>,
    onWallpaperClick: (Wallpaper) -> Unit,
    onLoadMore: () -> Unit = {},
    isLoadingMore: Boolean = false,
    canLoadMore: Boolean = true,
    showEndMessage: Boolean = false,
    columns: Int = 2,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    videoPlaybackManager: VideoPlaybackManager? = null,
    gridState: LazyGridState = rememberLazyGridState(),
    modifier: Modifier = Modifier
) {

    // 跟踪可见的壁纸项
    val visibleWallpaperIds = remember { mutableStateListOf<String>() }

    // 滚动状态
    var isScrolling by remember { mutableStateOf(false) }

    // 协程作用域
    val coroutineScope = rememberCoroutineScope()

    // 滚动停止计时器
    var scrollStopTimer: Job? by remember { mutableStateOf(null) }

    // 检测是否滚动到底部
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            // 如果最后可见项的索引接近总项数，则认为滚动到了底部
            lastVisibleItemIndex > 0 && lastVisibleItemIndex >= totalItemsNumber - 5
        }
    }

    // 当滚动到底部时自动加载更多
    LaunchedEffect(shouldLoadMore.value, isLoadingMore, canLoadMore) {
        if (shouldLoadMore.value && !isLoadingMore && canLoadMore) {
            Log.d("LiveVideoGrid", "Reached bottom, loading more...")
            onLoadMore()
        }
    }

    // 最后一次滚动状态更新时间
    var lastScrollStateUpdateTime by remember { mutableStateOf(0L) }

    // 监听滚动状态
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.isScrollInProgress }
            .collectLatest { scrolling ->
                val currentTime = System.currentTimeMillis()

                // 限制更新频率，避免频繁重组
                if (currentTime - lastScrollStateUpdateTime < 100) {
                    return@collectLatest
                }

                lastScrollStateUpdateTime = currentTime

                // 更新滚动状态
                isScrolling = scrolling

                // 通知视频播放管理器
                videoPlaybackManager?.setScrolling(scrolling)

                // 如果停止滚动，设置延迟恢复播放
                if (!scrolling) {
                    // 取消之前的计时器
                    scrollStopTimer?.cancel()

                    // 创建新的计时器，延迟500毫秒后恢复播放
                    scrollStopTimer = coroutineScope.launch {
                        delay(500) // 等待500毫秒再恢复播放，避免频繁切换
                        videoPlaybackManager?.setScrolling(false)
                    }
                }
            }
    }

    // 跟踪可见项目，用于视频播放控制
    // 使用限流和延迟处理减少更新频率
    LaunchedEffect(gridState) {
        var lastUpdateTime = 0L

        snapshotFlow { gridState.layoutInfo.visibleItemsInfo }
            .collectLatest { visibleItems ->
                val currentTime = System.currentTimeMillis()

                // 限制更新频率，避免频繁重组
                // 在滚动过程中使用更长的间隔，停止滚动后使用更短的间隔
                val minInterval = if (isScrolling) 300L else 150L
                if (currentTime - lastUpdateTime < minInterval) {
                    return@collectLatest
                }

                lastUpdateTime = currentTime

                // 清除之前的可见项
                val oldVisibleIds = visibleWallpaperIds.toList()

                // 更新可见项列表
                val newVisibleIds = visibleItems.mapNotNull { info ->
                    wallpapers.getOrNull(info.index)?.id
                }

                // 检查是否有变化，如果没有变化则不更新
                if (oldVisibleIds.size == newVisibleIds.size && oldVisibleIds.containsAll(newVisibleIds)) {
                    return@collectLatest
                }

                // 更新可见项列表
                visibleWallpaperIds.clear()
                visibleWallpaperIds.addAll(newVisibleIds)

                // 更新视频播放管理器
                videoPlaybackManager?.let { manager ->
                    // 移除不再可见的视频
                    oldVisibleIds.forEach { id ->
                        if (id !in newVisibleIds) {
                            manager.removeVisibleVideo(id)
                        }
                    }

                    // 添加新可见的视频
                    newVisibleIds.forEach { id ->
                        if (id !in oldVisibleIds) {
                            val wallpaper = wallpapers.find { it.id == id }
                            if (wallpaper?.isLive == true) {
                                manager.addVisibleVideo(id)
                            }
                        }
                    }
                }
            }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = gridState,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        // 壁纸项
        items(
            items = wallpapers,
            key = { wallpaper -> wallpaper.id } // 使用壁纸ID作为稳定的键
        ) { wallpaper ->
            // 使用固定高度，确保统一的视觉效果
            val itemHeight = 240.dp

            // 检查该壁纸是否应该播放视频
            val isVisible = wallpaper.id in visibleWallpaperIds
            val shouldPlayVideo = videoPlaybackManager?.shouldPlayVideo(wallpaper.id) ?: false

            // 使用优化的视频预览组件
            VideoPreviewItem(
                wallpaper = wallpaper,
                isVisible = isVisible && shouldPlayVideo,
                onClick = { onWallpaperClick(wallpaper) },
                onVideoComplete = { videoId ->
                    // 通知视频播放管理器视频播放完成
                    videoPlaybackManager?.notifyVideoComplete(videoId)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .padding(2.dp)
            )
        }

        // 如果正在加载更多，显示加载指示器
        if (isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }
            }
        }

        // 如果显示底部提示，添加一个底部项
        if (showEndMessage) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.end_of_list),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
