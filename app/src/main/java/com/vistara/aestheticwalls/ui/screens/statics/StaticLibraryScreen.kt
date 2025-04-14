package com.vistara.aestheticwalls.ui.screens.statics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
@OptIn(ExperimentalMaterial3Api::class)
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
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh, contentDescription = "刷新"
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
            // 分类选择器
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

            // 根据状态显示不同的内容
            when (wallpapersState) {
                is UiState.Loading -> {
                    LoadingState(message = "正在加载壁纸...")
                }

                is UiState.Success -> {
                    val wallpapers = (wallpapersState as UiState.Success<List<Wallpaper>>).data
                    if (wallpapers.isEmpty()) {
                        // 显示空状态
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "没有找到壁纸",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        // 显示壁纸网格
                        WallpaperGrid(
                            wallpapers = wallpapers,
                            onWallpaperClick = onWallpaperClick,
                            modifier = Modifier.padding(0.dp)
                        )
                    }
                }

                is UiState.Error -> {
                    val errorMessage = (wallpapersState as UiState.Error).message
                    ErrorState(
                        message = errorMessage, onRetry = { viewModel.refresh() })
                }
            }
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