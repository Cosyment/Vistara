package com.vistara.aestheticwalls.ui.screens.statics

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.vistara.aestheticwalls.ui.theme.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.model.WallpaperCategory
import com.vistara.aestheticwalls.ui.components.CategorySelector
import com.vistara.aestheticwalls.ui.components.ErrorState
import com.vistara.aestheticwalls.ui.components.LoadingState
import com.vistara.aestheticwalls.ui.components.WallpaperStaggeredGrid
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

    // 下拉刷新状态
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing, onRefresh = { viewModel.refresh() })

    // 不再使用自动加载更多的逻辑，而是依赖WallpaperStaggeredGrid组件中的滚动到底部检测

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                Text(
                    stringResource(R.string.category_static), style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }, actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search, contentDescription = stringResource(R.string.search_hint)
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
            )
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            // 分类选择器 - 使用remember缓存分类选择器
            CategorySelector(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    viewModel.filterByCategory(category)
                })

            // 根据状态显示不同的内容
            when (wallpapersState) {
                is UiState.Loading -> {
                    LoadingState()
                }

                is UiState.Success -> {
                    val wallpapers = (wallpapersState as UiState.Success<List<Wallpaper>>).data
                    if (wallpapers.isEmpty()) {
                        // 显示空状态
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_wallpapers_found),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        // 使用Column包裹LazyRow和WallpaperStaggeredGrid
                        Column(modifier = Modifier.fillMaxSize()) {
                            // 显示壁纸网格 (瀑布流布局)
                            Box(modifier = Modifier.weight(1f)) {
                                // 创建LazyStaggeredGridState并保持它的状态
                                val gridState = rememberLazyStaggeredGridState()

                                // 使用remember缓存WallpaperStaggeredGrid组件
                                val rememberedWallpapers = remember(wallpapers) { wallpapers }
                                val rememberedIsLoadingMore =
                                    remember(isLoadingMore) { isLoadingMore }
                                val rememberedCanLoadMore = remember(canLoadMore) { canLoadMore }

                                WallpaperStaggeredGrid(
                                    wallpapers = rememberedWallpapers,
                                    onWallpaperClick = onWallpaperClick,
                                    onLoadMore = { viewModel.loadMore() },
                                    isLoadingMore = rememberedIsLoadingMore,
                                    canLoadMore = rememberedCanLoadMore,
                                    showEndMessage = !rememberedCanLoadMore,
                                    gridState = gridState,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                is UiState.Error -> {
                    ErrorState(
                        message = (wallpapersState as UiState.Error).message,
                        onRetry = { viewModel.refresh() })
                }
            }

            // 显示下拉刷新指示器
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
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