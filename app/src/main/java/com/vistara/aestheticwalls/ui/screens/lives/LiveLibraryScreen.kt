package com.vistara.aestheticwalls.ui.screens.lives

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.ui.components.CategorySelector
import com.vistara.aestheticwalls.ui.components.LiveVideoGrid
import com.vistara.aestheticwalls.ui.components.LoadingState
import com.vistara.aestheticwalls.ui.components.rememberVideoPlaybackManager
import com.vistara.aestheticwalls.ui.theme.VistaraTheme
import com.vistara.aestheticwalls.ui.theme.stringResource

/**
 * 动态壁纸库页面
 * 显示所有动态壁纸，支持分类筛选
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Suppress("OPT_IN_USAGE")
@Composable
fun LiveLibraryScreen(
    onWallpaperClick: (Wallpaper) -> Unit, onSearchClick: () -> Unit = {}, viewModel: LiveLibraryViewModel = hiltViewModel()
) {
    // 从 ViewModel 中获取状态
    val wallpapersState by viewModel.wallpapersState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categories = viewModel.categories
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val canLoadMore by viewModel.canLoadMore.collectAsState()

    // 获取当前Activity
    val activity = LocalActivity.current

    // 创建SnackbarHostState
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, topBar = {
        TopAppBar(
            title = {
            Text(
                stringResource(R.string.category_live), style = MaterialTheme.typography.titleLarge.copy(
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
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f), titleContentColor = MaterialTheme.colorScheme.onBackground
        )
        )
    }) { paddingValues ->
        val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {})
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            // 分类选择器 - 使用remember缓存分类选择器
            CategorySelector(
                categories = categories, selectedCategory = selectedCategory, onCategorySelected = { category ->
                    viewModel.filterByCategory(category)
                })
            // 将整个when表达式包裹在一个组合函数中
            val content = @Composable {
                when (wallpapersState) {
                    is UiState.Loading -> {
                        LoadingState()
                    }

                    is UiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (wallpapersState as UiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    is UiState.Success -> {
                        val wallpapers = (wallpapersState as UiState.Success<List<Wallpaper>>).data
                        if (wallpapers.isEmpty()) {
                            // 显示空状
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
                                // 显示动态壁纸网格 (统一大小布局)
                                Box(modifier = Modifier.weight(1f)) {
                                    // 创建LazyGridState并保持它的状态
                                    val gridState = rememberLazyGridState()

                                    // 创建视频播放管理器
                                    val videoPlaybackManager = rememberVideoPlaybackManager()
                                    // 启用顺序播放模式，确保同一时间只有一个视频在播放
                                    videoPlaybackManager.setSequentialPlayback(true)

                                    // 使用remember缓存LiveVideoGrid组件
                                    val rememberedWallpapers = remember(wallpapers) { wallpapers }
                                    val rememberedIsLoadingMore = remember(isLoadingMore) { isLoadingMore }
                                    val rememberedCanLoadMore = remember(canLoadMore) { canLoadMore }

                                    LiveVideoGrid(
                                        wallpapers = rememberedWallpapers,
                                        onWallpaperClick = onWallpaperClick,
                                        onLoadMore = { viewModel.loadMore() },
                                        isLoadingMore = rememberedIsLoadingMore,
                                        canLoadMore = rememberedCanLoadMore,
                                        showEndMessage = !rememberedCanLoadMore,
                                        videoPlaybackManager = videoPlaybackManager,
                                        // 使用固定列数，确保统一大小
                                        columns = 2,
                                        gridState = gridState,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 调用组合函数
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun LiveLibraryScreenPreview() {
    VistaraTheme {
        // 注意：Preview中不能使用hiltViewModel，所以这里只是一个简单的预览
        // 实际使用时需要提供真实的ViewModel
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.category_live))
        }
    }
}
