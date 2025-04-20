package com.vistara.aestheticwalls.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.ui.theme.stringResource
import kotlinx.coroutines.flow.collectLatest

/**
 * 瀑布流壁纸网格组件
 * 使用LazyVerticalStaggeredGrid实现瀑布流布局
 *
 * @param wallpapers 壁纸列表
 * @param onWallpaperClick 点击壁纸回调
 * @param onLoadMore 加载更多回调
 * @param isLoadingMore 是否正在加载更多
 * @param canLoadMore 是否可以加载更多
 * @param showEndMessage 是否显示到底部提示
 * @param columns 网格列数，默认为2
 * @param contentPadding 内容内边距
 * @param videoPlaybackManager 视频播放管理器，用于控制视频预览
 * @param modifier 可选的修饰符
 */
@Composable
fun WallpaperStaggeredGrid(
    wallpapers: List<Wallpaper>,
    onWallpaperClick: (Wallpaper) -> Unit,
    onLoadMore: () -> Unit = {},
    isLoadingMore: Boolean = false,
    canLoadMore: Boolean = true,
    showEndMessage: Boolean = false,
    columns: Int = 2,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    videoPlaybackManager: VideoPlaybackManager? = null,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    modifier: Modifier = Modifier
) {

    // 跟踪可见的壁纸项
    val visibleWallpaperIds = remember { mutableStateListOf<String>() }

    // 检测是否滚动到底部
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val firstVisibleItemIndex = gridState.firstVisibleItemIndex

            // 如果最后可见项的索引接近总项数，则认为滚动到了底部
            lastVisibleItemIndex > 0 && lastVisibleItemIndex >= totalItemsNumber - 5
        }
    }

    // 当滚动到底部时自动加载更多
    LaunchedEffect(shouldLoadMore.value, isLoadingMore, canLoadMore) {
        if (shouldLoadMore.value && !isLoadingMore && canLoadMore) {
            Log.d("WallpaperStaggeredGrid", "Reached bottom, loading more...")
            // 记录当前滚动位置
            val firstVisibleItemIndex = gridState.firstVisibleItemIndex
            val firstVisibleItemScrollOffset = gridState.firstVisibleItemScrollOffset
            Log.d("WallpaperStaggeredGrid", "Current scroll position: index=$firstVisibleItemIndex, offset=$firstVisibleItemScrollOffset")

            onLoadMore()
        }
    }

    // 跟踪可见项目，用于视频播放控制
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo }.collectLatest { visibleItems ->
                // 清除之前的可见项
                val oldVisibleIds = visibleWallpaperIds.toList()
                visibleWallpaperIds.clear()

                // 更新可见项列表
                val newVisibleIds = visibleItems.mapNotNull { info ->
                    wallpapers.getOrNull(info.index)?.id
                }
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
                        val wallpaper = wallpapers.find { it.id == id }
                        if (wallpaper?.isLive == true) {
                            manager.addVisibleVideo(id)
                        }
                    }
                }
            }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columns),
        state = gridState,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        // 壁纸项
        items(
            items = wallpapers,
            key = { wallpaper -> wallpaper.id } // 使用壁纸ID作为稳定的键
        ) { wallpaper ->
            // 根据壁纸的宽高比计算高度
            val aspectRatio = calculateAspectRatio(wallpaper)
            val itemHeight = remember(aspectRatio) {
                // 根据宽高比和壁纸ID生成不同的高度，使瀑布流更自然
                (180 + (wallpaper.id.hashCode() % 120)).dp
            }

            // 检查该壁纸是否应该播放视频
            val isVisible = wallpaper.id in visibleWallpaperIds
            val shouldPlayVideo = videoPlaybackManager?.shouldPlayVideo(wallpaper.id) ?: false

            if (wallpaper.isLive) {
                // 动态壁纸使用视频预览组件
//                VideoPreviewItem(
//                    wallpaper = wallpaper,
//                    isVisible = isVisible && shouldPlayVideo,
//                    onClick = { onWallpaperClick(wallpaper) },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(itemHeight)
//                        .padding(4.dp)
//                )
            } else {
                // 静态壁纸使用普通壁纸项
                WallpaperItem(
                    wallpaper = wallpaper,
                    onClick = { onWallpaperClick(wallpaper) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .padding(4.dp)
                )
            }
        }

        // 如果正在加载更多，显示加载指示器
        if (isLoadingMore) {
            // 使用虚拟壁纸项来显示加载指示器
            val loadingWallpapers = List(columns) { index ->
                Wallpaper(
                    id = "loading_$index", title = null, thumbnailUrl = null, author = null
                )
            }

            items(
                items = loadingWallpapers,
                key = { it.id } // 使用生成的ID作为稳定的键
            ) { _ ->
                // 空白项，仅用于占位
                Box(modifier = Modifier.height(0.dp)) {}
            }

            // 使用单独的项来显示加载指示器，确保它占满整个宽度
            item(span = StaggeredGridItemSpan.FullLine) {
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
            // 使用虚拟壁纸项来占位，确保“已经到底了”提示在最后一行
            val endSpacerWallpapers = List(columns) { index ->
                Wallpaper(
                    id = "end_spacer_$index", title = null, thumbnailUrl = null, author = null
                )
            }

            items(
                items = endSpacerWallpapers,
                key = { it.id } // 使用生成的ID作为稳定的键
            ) { _ ->
                // 空白项，仅用于占位
                Box(modifier = Modifier.height(0.dp)) {}
            }

            // 使用单独的项来显示“已经到底了”提示，确保它占满整个宽度
            item(span = StaggeredGridItemSpan.FullLine) {
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

/**
 * 计算壁纸的宽高比
 * 如果壁纸有分辨率信息，则使用实际宽高比
 * 否则使用默认宽高比0.75f (3:4)
 */
private fun calculateAspectRatio(wallpaper: Wallpaper): Float {
    return if (wallpaper.width > 0 && wallpaper.height > 0) {
        wallpaper.width.toFloat() / wallpaper.height.toFloat()
    } else if (wallpaper.resolution != null && wallpaper.resolution.width > 0 && wallpaper.resolution.height > 0) {
        wallpaper.resolution.width.toFloat() / wallpaper.resolution.height.toFloat()
    } else {
        // 默认宽高比
        0.75f
    }
}
