package com.vistara.aestheticwalls.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vistara.aestheticwalls.data.model.Banner
import com.vistara.aestheticwalls.data.model.BannerActionType
import com.vistara.aestheticwalls.data.model.Resolution
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.ui.components.Carousel
import com.vistara.aestheticwalls.ui.components.FeaturedWallpaperSection
import com.vistara.aestheticwalls.ui.components.LoadingState
import com.vistara.aestheticwalls.ui.components.SearchBar
import com.vistara.aestheticwalls.ui.components.WallpaperItem
import com.vistara.aestheticwalls.ui.theme.VistaraTheme

/**
 * 首页屏幕
 * 显示壁纸推荐、分类和信息流
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onWallpaperClick: (Wallpaper) -> Unit,
    onSearch: (String) -> Unit = {},
    onBannerClick: (Banner) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    // 从ViewModel获取数据
    val banners by viewModel.banners.collectAsState()
    val featuredWallpapers by viewModel.featuredWallpapers.collectAsState()
    val staticWallpapers by viewModel.staticWallpapers.collectAsState()
    val liveWallpapers by viewModel.liveWallpapers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "首页", style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ), modifier = Modifier.background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                        )
                    )
                )
            )
        }) { paddingValues ->

        // 显示加载状态
        if (isLoading) {
            LoadingState(message = "正在加载壁纸...")
            return@Scaffold
        }

        // 显示错误状态
        if (error != null && featuredWallpapers.isEmpty() && staticWallpapers.isEmpty() && liveWallpapers.isEmpty()) {
            // 只有当所有数据都为空时才显示错误状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "加载失败",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error ?: "未知错误",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        onClick = { viewModel.refresh() },
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "重试",
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            return@Scaffold
        }

        LazyColumn(
            state = rememberLazyListState(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 错误提示条
            if (error != null) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "部分数据加载失败，请刷新重试",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                onClick = { viewModel.refresh() },
                                color = MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "刷新",
                                    color = MaterialTheme.colorScheme.onError,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 搜索栏
            item {
                SearchBar(
                    query = "",
                    onQueryChange = {},
                    onSearch = onSearch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = "搜索高清壁纸、动态效果...",
                )
            }

            // 轮播Banner
            if (banners.isNotEmpty()) {
                item {
                    Carousel(
                        banners = banners,
                        onBannerClick = onBannerClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(15.dp))
                    )
                }
            }

            // 每日精选
            if (featuredWallpapers.isNotEmpty()) {
                item {
                    FeaturedWallpaperSection(
                        wallpaper = featuredWallpapers.first(),
                        onWallpaperClick = onWallpaperClick,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // 分类导航
            item {
                CategorySection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            // 热门静态壁纸
            if (staticWallpapers.isNotEmpty()) {
                // 热门静态标题
                item {
                    WallpaperSectionTitle(
                        title = "热门静态", modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // 热门静态壁纸行
                for (i in 0 until staticWallpapers.size step 2) {
                    item {
                        WallpaperItem2Columns(
                            wallpaper1 = staticWallpapers[i],
                            wallpaper2 = if (i + 1 < staticWallpapers.size) staticWallpapers[i + 1] else null,
                            onWallpaperClick = onWallpaperClick,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // 炫酷动态壁纸
            if (liveWallpapers.isNotEmpty()) {
                // 炫酷动态标题
                item {
                    WallpaperSectionTitle(
                        title = "炫酷动态",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // 炫酷动态壁纸行
                for (i in 0 until liveWallpapers.size step 2) {
                    item {
                        WallpaperItem2Columns(
                            wallpaper1 = liveWallpapers[i],
                            wallpaper2 = if (i + 1 < liveWallpapers.size) liveWallpapers[i + 1] else null,
                            onWallpaperClick = onWallpaperClick,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // 最新上传壁纸
            if (featuredWallpapers.isNotEmpty()) {
                // 最新上传标题
                item {
                    WallpaperSectionTitle(
                        title = "最新上传",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp)
                    )
                }

                // 最新上传壁纸行
                for (i in 0 until featuredWallpapers.size step 2) {
                    item {
                        WallpaperItem2Columns(
                            wallpaper1 = featuredWallpapers[i],
                            wallpaper2 = if (i + 1 < featuredWallpapers.size) featuredWallpapers[i + 1] else null,
                            onWallpaperClick = onWallpaperClick,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    VistaraTheme {
        Surface {
            HomeScreen(
                onWallpaperClick = {})
        }
    }
}

@Composable
private fun CategorySection(
    modifier: Modifier = Modifier
) {
    // 临时分类数据
    val demoCategories = listOf(
        "自然风景", "建筑", "动物", "抽象", "太空", "简约"
    )

    Column(modifier = modifier) {
        Text(
            text = "探索分类", style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ), modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(demoCategories) { category ->
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = { /* 处理点击 */ }) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WallpaperSectionTitle(
    title: String, modifier: Modifier = Modifier
) {
    Text(
        text = title, style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.SemiBold
        ), modifier = modifier.padding(bottom = 5.dp)
    )
}

@Composable
private fun WallpaperItem2Columns(
    wallpaper1: Wallpaper,
    wallpaper2: Wallpaper?,
    onWallpaperClick: (Wallpaper) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 第一个壁纸
        WallpaperItem(
            wallpaper = wallpaper1,
            onClick = { onWallpaperClick(wallpaper1) },
            modifier = Modifier
                .weight(1f)
                .aspectRatio(0.75f) // 设置宽高比为3:4
        )

        // 第二个壁纸（如果有）
        if (wallpaper2 != null) {
            WallpaperItem(
                wallpaper = wallpaper2,
                onClick = { onWallpaperClick(wallpaper2) },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(0.75f) // 设置宽高比为3:4
            )
        } else {
            // 如果没有第二个壁纸，添加一个空的占位空间
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
