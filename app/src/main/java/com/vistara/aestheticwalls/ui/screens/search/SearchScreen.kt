package com.vistara.aestheticwalls.ui.screens.search

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vistara.aestheticwalls.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.model.WallpaperCategory
import com.vistara.aestheticwalls.ui.components.CategoryChip
import com.vistara.aestheticwalls.ui.components.SearchBar
import com.vistara.aestheticwalls.ui.components.WallpaperItem

/**
 * 搜索屏幕
 * 显示搜索栏、搜索历史、热门搜索和搜索结果
 */
@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            Column {
                // 顶部导航栏和搜索栏组合
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 16.dp, top = 8.dp)
                ) {
                    // 返回按钮
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }

                    // 搜索栏
                    SearchBar(
                        query = query,
                        onQueryChange = viewModel::updateQuery,
                        onSearch = viewModel::search,
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                    )
                }

                // 如果有搜索历史、热门搜索或建议，显示它们
                if (query.isEmpty() && (searchHistory.isNotEmpty() || hotSearches.isNotEmpty())) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(hotSearches) { category ->
                            CategoryChip(
                                category = category,
                                onClick = {
                                    viewModel.selectFromHistory(category)
                                    viewModel.search(category)
                                }
                            )
                        }
                    }
                } else if (query.isNotEmpty() && searchSuggestions.isNotEmpty()) {
                    // 显示搜索建议
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        searchSuggestions.forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectSuggestion(suggestion)
                                        viewModel.search(suggestion)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

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
                            text = stringResource(R.string.no_wallpapers_found),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.try_different_keywords),
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
                            text = stringResource(R.string.found_results, searchResults.size),
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
            text = stringResource(R.string.hot_searches),
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
            text = stringResource(R.string.search_tips),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchTip(
                title = stringResource(R.string.use_specific_descriptions),
                description = stringResource(R.string.specific_description_examples)
            )

            SearchTip(
                title = stringResource(R.string.try_different_languages),
                description = stringResource(R.string.english_keywords_tip)
            )

            SearchTip(
                title = stringResource(R.string.combine_keywords),
                description = stringResource(R.string.keyword_combination_examples)
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
