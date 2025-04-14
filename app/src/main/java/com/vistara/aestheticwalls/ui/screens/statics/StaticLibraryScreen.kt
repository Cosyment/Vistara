package com.vistara.aestheticwalls.ui.screens.statics

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.ui.components.ErrorState
import com.vistara.aestheticwalls.ui.components.LoadingState
import com.vistara.aestheticwalls.ui.components.WallpaperGrid
import com.vistara.aestheticwalls.ui.theme.VistaraTheme

/**
 * 静态壁纸库页面
 * 显示所有静态壁纸，支持分类筛选
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun StaticLibraryScreen(
    onWallpaperClick: (Wallpaper) -> Unit,
    onSearchClick: () -> Unit = {},
    viewModel: StaticLibraryViewModel = hiltViewModel()
) {
    // 获取ViewModel中的状态
    val wallpapersState by viewModel.wallpapersState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categories = viewModel.categories
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val canLoadMore by viewModel.canLoadMore.collectAsState()

    // 创建LazyListState来监听滚动状态
    val listState = rememberLazyListState()

    // 下拉刷新状态
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh() }
    )

    // 检测是否滚动到底部
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            // 记录滚动状态信息
            Log.d("StaticLibraryScreen", "Scroll state: lastVisibleItemIndex=$lastVisibleItemIndex, totalItemsNumber=$totalItemsNumber")

            // 如果最后可见项的索引接近总项数，则认为滚动到了底部
            // 增加判断条件，确保滚动到底部时能正确触发
            val reachedEnd = lastVisibleItemIndex > 0 && lastVisibleItemIndex >= totalItemsNumber - 1
            val reachedEndWithBuffer = layoutInfo.visibleItemsInfo.isNotEmpty() &&
                    layoutInfo.visibleItemsInfo.last().index >= totalItemsNumber - 3

            val shouldLoad = reachedEnd || reachedEndWithBuffer
            Log.d("StaticLibraryScreen", "shouldLoadMore: $shouldLoad (reachedEnd=$reachedEnd, reachedEndWithBuffer=$reachedEndWithBuffer)")
            shouldLoad
        }
    }

    // 当滚动到底部时自动加载更多
    LaunchedEffect(shouldLoadMore.value, isLoadingMore, canLoadMore, wallpapersState) {
        Log.d("StaticLibraryScreen", "LaunchedEffect triggered: shouldLoadMore=${shouldLoadMore.value}, isLoadingMore=$isLoadingMore, canLoadMore=$canLoadMore")
        if (shouldLoadMore.value && !isLoadingMore && canLoadMore && wallpapersState is UiState.Success) {
            Log.d("StaticLibraryScreen", "Calling loadMore()")
            viewModel.loadMore()
        } else {
            Log.d("StaticLibraryScreen", "Not calling loadMore() because: shouldLoadMore=${shouldLoadMore.value}, isLoadingMore=$isLoadingMore, canLoadMore=$canLoadMore, wallpapersState is Success=${wallpapersState is UiState.Success}")
        }
    }

    // 监听滚动状态变化
    LaunchedEffect(listState) {
        // 每次滚动状态变化时都会触发
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collect { lastIndex ->
                val totalItems = listState.layoutInfo.totalItemsCount
                Log.d("StaticLibraryScreen", "Scroll state changed: lastIndex=$lastIndex, totalItems=$totalItems")

                // 如果滚动到了底部附近，则加载更多
                if (lastIndex > 0 && lastIndex >= totalItems - 3 && !isLoadingMore && canLoadMore && wallpapersState is UiState.Success) {
                    Log.d("StaticLibraryScreen", "Reached bottom, loading more...")
                    viewModel.loadMore()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "静态壁纸", style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }, actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search, contentDescription = "搜索"
                        )
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                // 滚动到底部时自动加载更多
                content = {
                // 分类选择器
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            val isSelected = category == selectedCategory

                            Surface(
                                onClick = { viewModel.filterByCategory(category) },
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // 根据状态显示不同的内容
                when (wallpapersState) {
                    is UiState.Loading -> {
                        item {
                            LoadingState(message = "正在加载壁纸...")
                        }
                    }

                    is UiState.Success -> {
                        val wallpapers = (wallpapersState as UiState.Success<List<Wallpaper>>).data
                        if (wallpapers.isEmpty()) {
                            // 显示空状态
                            item {
                                Box(
                                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "没有找到壁纸",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            // 显示壁纸网格
                            item {
                                WallpaperGrid(
                                    wallpapers = wallpapers,
                                    onWallpaperClick = onWallpaperClick,
                                    modifier = Modifier.padding(0.dp)
                                )
                            }

                            // 如果正在加载更多，显示加载指示器
                            if (isLoadingMore) {
                                item {
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

                            // 如果没有更多数据，显示到底部提示
                            if (!canLoadMore) {
                                item {
                                    Text(
                                        text = "已经到底了",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp)
                                    )
                                }
                            }

                            // 底部空白，防止内容被遮挡，但不要太大
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                    is UiState.Error -> {
                        item {
                            ErrorState(
                                message = (wallpapersState as UiState.Error).message,
                                onRetry = { viewModel.refresh() }
                            )
                        }
                    }
                }
            })

            // 显示下拉刷新指示器
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StaticLibraryScreenPreview() {
    VistaraTheme {
        // 注意：预览中不会显示真实数据，因为没有提供真实的ViewModel
        // 这里只是UI预览
        StaticLibraryScreen(onWallpaperClick = {}, onSearchClick = {})
    }
}