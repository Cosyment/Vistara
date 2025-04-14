package com.vistara.aestheticwalls.ui.screens.lives

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vistara.aestheticwalls.data.model.Resolution
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.ui.components.WallpaperGrid
import com.vistara.aestheticwalls.ui.theme.VistaraTheme

/**
 * 动态壁纸库页面
 * 显示所有动态壁纸，支持分类筛选
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveLibraryScreen(
    onWallpaperClick: (Wallpaper) -> Unit, onSearchClick: () -> Unit = {}
) {
    // 分类数据
    val categories = listOf(
        "全部", "抽象", "科技感", "自然", "赛博朋克", "粒子", "流体", "简约", "卡通"
    )

    // 当前选中的分类
    var selectedCategory by remember { mutableStateOf("全部") }

    // 示例壁纸数据
    val wallpapers = remember {
        List(8) { index ->
            Wallpaper(
                id = "live_$index",
                title = "动态壁纸 $index",
                url = "https://example.com/live_wallpaper$index.mp4",
                thumbnailUrl = "https://example.com/live_thumbnail$index.jpg",
                author = "动画师 $index",
                source = "Vistara",
                isPremium = true, // 动态壁纸默认都是高级内容
                isLive = true,
                tags = listOf("动态", "炫酷"),
                resolution = Resolution(1080, 1920)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "动态壁纸", style = MaterialTheme.typography.titleLarge.copy(
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory

                    Surface(
                        onClick = { selectedCategory = category },
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

            // 壁纸网格
            WallpaperGrid(
                wallpapers = wallpapers,
                onWallpaperClick = onWallpaperClick,
                modifier = Modifier.padding(0.dp)
            )

            // 高级版提示
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "动态壁纸为高级功能，升级会员即可解锁全部内容",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LiveLibraryScreenPreview() {
    VistaraTheme {
        LiveLibraryScreen(
            onWallpaperClick = {})
    }
}
