package com.vistara.aestheticwalls.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.Banner
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.ui.components.Carousel
import com.vistara.aestheticwalls.ui.components.FeaturedWallpaperSection
import com.vistara.aestheticwalls.ui.components.LoadingState
import com.vistara.aestheticwalls.ui.components.WallpaperItem
import com.vistara.aestheticwalls.ui.theme.VistaraTheme
import kotlinx.coroutines.delay

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
                        stringResource(R.string.home),
                        style = MaterialTheme.typography.titleLarge.copy(
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
            LoadingState(message = stringResource(R.string.loading_wallpapers))
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
                        text = stringResource(R.string.loading_failed),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error ?: stringResource(R.string.unknown_error),
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
                            text = stringResource(R.string.retry),
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
                                text = stringResource(R.string.partial_data_loading_failed),
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
                                    text = stringResource(R.string.refresh),
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
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .clickable { onSearch("") },
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search_icon),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = stringResource(R.string.search_high_quality_wallpapers),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
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
                            .padding(horizontal = 10.dp)
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
                        .padding(horizontal = 16.dp),
                    onWallpaperClick = onWallpaperClick
                )
            }

            // 热门静态壁纸
            if (staticWallpapers.isNotEmpty()) {
                // 热门静态标题
                item {
                    WallpaperSectionTitle(
                        title = stringResource(R.string.hot_static),
                        modifier = Modifier.padding(horizontal = 16.dp)
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
                        title = stringResource(R.string.cool_dynamic),
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
                        title = stringResource(R.string.latest_uploads),
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
    modifier: Modifier = Modifier,
    onWallpaperClick: (Wallpaper) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // 临时分类数据
    val demoCategories = listOf(
        stringResource(R.string.nature_scenery), stringResource(R.string.architecture),
        stringResource(R.string.animals), stringResource(R.string.category_abstract),
        stringResource(R.string.space), stringResource(R.string.minimal)
    )

    // 获取当前上下文
    val context = LocalContext.current

    // 获取当前选中的分类和分类壁纸
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categoryWallpapers by viewModel.categoryWallpapers.collectAsState()
    val isCategoryLoading by viewModel.isCategoryLoading.collectAsState()

    // 默认选择第一个分类
    LaunchedEffect(Unit) {
        if (selectedCategory == null && demoCategories.isNotEmpty()) {
            viewModel.loadWallpapersByCategory(demoCategories.first())
        }
    }

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.explore_categories),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(demoCategories) { category ->
                val isSelected = selectedCategory == category

                // 使用动画效果切换分类标签的颜色
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .animateContentSize(animationSpec = tween(300)), // 添加大小变化动画
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                    onClick = {
                        // 处理分类点击，加载对应分类的壁纸
                        // 直接调用 ViewModel 的方法加载对应分类的壁纸，不显示 Toast
                        viewModel.loadWallpapersByCategory(category)
                    }) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // 显示分类壁纸
        if (selectedCategory != null) {
            Spacer(modifier = Modifier.height(16.dp))

            // 使用动画来切换加载状态和壁纸内容
            // 记录上一次的壁纸列表，用于平滑过渡
            var previousWallpapers by remember { mutableStateOf(emptyList<Wallpaper>()) }
            var showPrevious by remember { mutableStateOf(false) }

            // 当分类或加载状态变化时更新
            LaunchedEffect(selectedCategory, isCategoryLoading) {
                if (!isCategoryLoading && categoryWallpapers.isNotEmpty()) {
                    // 如果有新的壁纸数据，先显示上一次的数据，然后渐变切换
                    if (previousWallpapers.isNotEmpty() && previousWallpapers != categoryWallpapers) {
                        showPrevious = true
                        delay(100) // 短暂停后开始渐变
                        showPrevious = false
                    }
                    previousWallpapers = categoryWallpapers
                }
            }

            // 加载状态显示
            AnimatedVisibility(
                visible = isCategoryLoading,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // 壁纸内容显示
            AnimatedVisibility(
                visible = !isCategoryLoading && (categoryWallpapers.isNotEmpty() || previousWallpapers.isNotEmpty()),
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                // 显示分类壁纸
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // 根据状态显示当前或上一次的壁纸
                    val wallpapersToShow =
                        if (showPrevious) previousWallpapers else categoryWallpapers

                    items(wallpapersToShow.take(6)) { wallpaper ->
                        CategoryWallpaperItem(
                            wallpaper = wallpaper,
                            onClick = { onWallpaperClick(wallpaper) },
                            modifier = Modifier
                                .width(120.dp)
                                .height(180.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryWallpaperItem(
    wallpaper: Wallpaper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 壁纸图片
            AsyncImage(
                model = wallpaper.thumbnailUrl,
                contentDescription = wallpaper.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 渐变底部阴影
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(60.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // 壁纸标题
            Text(
                text = wallpaper.title ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
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
