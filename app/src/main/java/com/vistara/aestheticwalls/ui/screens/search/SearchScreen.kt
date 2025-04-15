package com.vistara.aestheticwalls.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.ui.components.AdvancedSearchBar
import com.vistara.aestheticwalls.ui.components.WallpaperItem

/**
 * 搜索屏幕
 * 显示搜索栏、搜索历史、热门搜索和搜索结果
 */
@Composable
fun SearchScreen(
    onWallpaperClick: (Wallpaper) -> Unit,
    onBackClick: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    // 从ViewModel获取状态
    val query by viewModel.query.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val searchSuggestions by viewModel.searchSuggestions.collectAsState()
    val hotSearches by viewModel.hotSearches.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索栏
            AdvancedSearchBar(
                query = query,
                onQueryChange = viewModel::updateQuery,
                onSearch = viewModel::search,
                searchHistory = searchHistory,
                onHistoryItemSelected = viewModel::selectFromHistory,
                onClearHistory = viewModel::clearSearchHistory,
                categories = hotSearches,
                onCategorySelected = viewModel::selectFromHistory,
                suggestions = searchSuggestions,
                onSuggestionSelected = viewModel::selectSuggestion,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // 搜索结果或初始状态
            if (query.isEmpty() && searchResults.isEmpty()) {
                // 初始状态：显示热门搜索
                InitialSearchState(
                    hotSearches = hotSearches,
                    onCategorySelected = viewModel::selectFromHistory,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            } else if (isLoading) {
                // 加载状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (searchResults.isEmpty() && query.isNotEmpty()) {
                // 无结果状态
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "未找到相关壁纸",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "尝试使用其他关键词搜索",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // 搜索结果
                LazyColumn(
                    state = rememberLazyListState(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 显示搜索结果数量
                    item {
                        Text(
                            text = "找到 ${searchResults.size} 个结果",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 搜索结果网格
                    items(searchResults.chunked(2)) { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowItems.forEachIndexed { index, wallpaper ->
                                WallpaperItem(
                                    wallpaper = wallpaper,
                                    onClick = { onWallpaperClick(wallpaper) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(240.dp)
                                )

                                // 如果是奇数个，最后一行添加空白占位
                                if (index == 0 && rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 初始搜索状态
 * 显示热门搜索和搜索建议
 */
@Composable
private fun InitialSearchState(
    hotSearches: List<String>,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "热门搜索",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(hotSearches) { category ->
                com.vistara.aestheticwalls.ui.components.CategoryChip(
                    category = category,
                    onClick = { onCategorySelected(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "搜索提示",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchTip(
                title = "使用具体描述",
                description = "例如：'山水风景'、'黑白抽象'、'城市夜景'"
            )

            SearchTip(
                title = "尝试不同语言",
                description = "有时英文关键词可能会有更多结果"
            )

            SearchTip(
                title = "组合关键词",
                description = "例如：'极简 黑白'、'自然 绿色'"
            )
        }
    }
}

/**
 * 搜索提示项
 */
@Composable
private fun SearchTip(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
