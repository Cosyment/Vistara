package com.vistara.aestheticwalls.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vistara.aestheticwalls.data.model.WallpaperCategory

/**
 * 分类选择器组件
 * 显示一个水平滚动的分类列表，支持选中状态
 */
@Composable
fun CategorySelector(
    categories: List<WallpaperCategory>,
    selectedCategory: WallpaperCategory,
    onCategorySelected: (WallpaperCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    // 使用remember缓存分类列表，避免不必要的重组
    val rememberedCategories = remember(categories) { categories }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(
            items = rememberedCategories,
            key = { it.apiValue } // 使用分类的API值作为key，确保在重组时保持正确的状态
        ) { category ->
            val isSelected = category == selectedCategory

            Surface(
                onClick = { onCategorySelected(category) },
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(category.titleRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * 兼容旧版本的分类选择器组件
 * 使用字符串资源ID列表作为分类
 */
@Composable
fun CategorySelector(
    categories: List<Int>,
    selectedCategory: Int,
    onCategorySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 使用remember缓存分类列表，避免不必要的重组
    val rememberedCategories = remember(categories) { categories }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(
            items = rememberedCategories,
            key = { it } // 使用分类资源ID作为key，确保在重组时保持正确的状态
        ) { categoryResId ->
            val isSelected = categoryResId == selectedCategory

            Surface(
                onClick = { onCategorySelected(categoryResId) },
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(categoryResId),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
