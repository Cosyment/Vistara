package com.vistara.aestheticwalls.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vistara.aestheticwalls.data.model.Resolution
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.ui.components.WallpaperGrid
import com.vistara.aestheticwalls.ui.theme.VistaraTheme

/**
 * 首页屏幕
 * 显示壁纸推荐、分类和信息流
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onWallpaperClick: (Wallpaper) -> Unit = {},
    modifier: Modifier = Modifier
) {
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
        ),
        Wallpaper(
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
        ),
        Wallpaper(
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
        ),
        Wallpaper(
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
                title = { Text("Vistara壁纸") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            state = rememberLazyListState(),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 今日推荐模块
            item {
                Text(
                    text = "今日推荐",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            // 壁纸网格为独立的item
            item {
                WallpaperGrid(
                    wallpapers = demoWallpapers,
                    onWallpaperClick = onWallpaperClick,
                    columns = 2,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 热门静态壁纸标题
            item {
                Text(
                    text = "热门静态",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            // 静态壁纸网格
            item {
                WallpaperGrid(
                    wallpapers = staticWallpapers,
                    onWallpaperClick = onWallpaperClick,
                    columns = 2,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 只有在有动态壁纸时才显示动态壁纸部分
            if (liveWallpapers.isNotEmpty()) {
                // 动态壁纸标题
                item {
                    Text(
                        text = "炫酷动态",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                // 动态壁纸网格
                item {
                    WallpaperGrid(
                        wallpapers = liveWallpapers,
                        onWallpaperClick = onWallpaperClick,
                        columns = 2,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
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
            HomeScreen()
        }
    }
}
