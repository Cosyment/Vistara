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
) {
    // 临时Banner数据
    val demoBanners = listOf(
        Banner(
            id = "1",
            imageUrl = "https://example.com/banner1.jpg",
            title = "精选专题：自然风光",
            subtitle = "探索大自然的壮丽",
            actionType = BannerActionType.COLLECTION,
            actionTarget = "nature_collection"
        ), Banner(
            id = "2",
            imageUrl = "https://example.com/banner2.jpg",
            title = "高级动态壁纸",
            subtitle = "解锁更多精彩",
            actionType = BannerActionType.PREMIUM,
            actionTarget = "premium_live"
        )
    )

    // 临时壁纸数据
    val demoWallpapers = listOf(
        Wallpaper(
            id = "1",
            title = "山川美景",
            url = "https://example.com/wallpaper1.jpg",
            thumbnailUrl = "https://example.com/wallpaper1_thumb.jpg",
            author = "张三",
            source = "Unsplash",
            isPremium = false,
            isLive = false,
            tags = listOf("自然", "山川"),
            resolution = Resolution(1920, 1080)
        ), Wallpaper(
            id = "2",
            title = "城市夜景",
            url = "https://example.com/wallpaper2.jpg",
            thumbnailUrl = "https://example.com/wallpaper2_thumb.jpg",
            author = "李四",
            source = "Pexels",
            isPremium = true,
            isLive = false,
            tags = listOf("城市", "夜景"),
            resolution = Resolution(3840, 2160)
        ), Wallpaper(
            id = "3",
            title = "动态海洋",
            url = "https://example.com/wallpaper3.mp4",
            thumbnailUrl = "https://example.com/wallpaper3_thumb.jpg",
            author = "王五",
            source = "Pixabay",
            isPremium = true,
            isLive = true,
            tags = listOf("海洋", "动态"),
            resolution = Resolution(1920, 1080)
        ), Wallpaper(
            id = "4",
            title = "星空银河",
            url = "https://example.com/wallpaper4.jpg",
            thumbnailUrl = "https://example.com/wallpaper4_thumb.jpg",
            author = "赵六",
            source = "Wallhaven",
            isPremium = false,
            isLive = false,
            tags = listOf("星空", "夜晚"),
            resolution = Resolution(2560, 1440)
        )
    )

    // 过滤后的不同类型壁纸列表
    val staticWallpapers = demoWallpapers.filter { !it.isLive }
    val liveWallpapers = demoWallpapers.filter { it.isLive }

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
        LazyColumn(
            state = rememberLazyListState(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
            item {
                Carousel(
                    banners = demoBanners,
                    onBannerClick = onBannerClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(15.dp))
                )
            }

            // 每日精选
            item {
                DailyPickSection(
                    onWallpaperClick = { onWallpaperClick(it) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 分类导航
            item {
                CategorySection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

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

            // 最新上传标题
            item {
                WallpaperSectionTitle(
                    title = "最新上传",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // 最新上传壁纸行
            for (i in 0 until demoWallpapers.size step 2) {
                item {
                    WallpaperItem2Columns(
                        wallpaper1 = demoWallpapers[i],
                        wallpaper2 = if (i + 1 < demoWallpapers.size) demoWallpapers[i + 1] else null,
                        onWallpaperClick = onWallpaperClick,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
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
private fun DailyPickSection(
    onWallpaperClick: (Wallpaper) -> Unit, modifier: Modifier = Modifier
) {
    // 临时壁纸数据
    val featuredWallpaper = Wallpaper(
        id = "featured_1",
        title = "宁静山谷",
        url = "https://example.com/featured.jpg",
        thumbnailUrl = "https://example.com/featured_thumb.jpg",
        author = "艺术家A",
        source = "Unsplash",
        isPremium = false,
        isLive = false,
        tags = listOf("自然", "山川"),
        resolution = Resolution(1920, 1080)
    )

    Column(modifier = modifier) {
        Text(
            text = "每日精选", style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ), modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(380f / 456f)
                .clickable { onWallpaperClick(featuredWallpaper) },
            shape = RoundedCornerShape(15.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = featuredWallpaper.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent, Color.Black.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(15.dp)
                ) {
                    Column {
                        Text(
                            text = "宁静山谷", style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold, shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 3f
                                )
                            ), color = Color.White
                        )
                        Text(
                            text = "来自：艺术家A", style = MaterialTheme.typography.bodySmall.copy(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 3f
                                )
                            ), color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
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
        ), modifier = modifier.padding(bottom = 12.dp)
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
