package com.vistara.aestheticwalls.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vistara.aestheticwalls.data.model.Category
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

/**
 * 分类标签组件，用于标签云或筛选场景
 * 
 * @param category 分类数据
 * @param selected 是否选中
 * @param onClick 点击回调
 * @param icon 可选的图标组件
 * @param isPremiumUser 用户是否为高级用户，用于确定是否可访问高级分类
 */
@Composable
fun CategoryChip(
    category: Category,
    selected: Boolean = false,
    onClick: () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    isPremiumUser: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isAccessible = !category.isPremium || isPremiumUser
    
    AssistChip(
        onClick = { if (isAccessible) onClick() },
        label = {
            Text(
                text = category.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimary
                else if (isAccessible)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        },
        modifier = modifier,
        enabled = isAccessible,
        leadingIcon = icon?.let {
            {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    icon()
                }
            }
        },
        trailingIcon = if (category.isPremium && !isPremiumUser) {
            {
                Text(
                    text = "👑",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        } else null,
        border = BorderStroke(
            width = if (selected) 0.dp else 1.dp,
            color = if (selected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) 
                MaterialTheme.colorScheme.primary.copy(alpha = if (isAccessible) 1f else 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isAccessible) 1f else 0.3f),
        )
    )
}

/**
 * 带数量指示的分类标签组件
 */
@Composable
fun CategoryChipWithCount(
    category: Category, 
    selected: Boolean = false,
    onClick: () -> Unit,
    isPremiumUser: Boolean = false,
    modifier: Modifier = Modifier
) {
    CategoryChip(
        category = category,
        selected = selected,
        onClick = onClick,
        isPremiumUser = isPremiumUser,
        modifier = modifier,
        icon = null
    )
}

/**
 * 分类筛选标签组
 */
@Composable
fun CategoryFilterChips(
    categories: List<Category>,
    selectedCategoryId: String? = null,
    onCategorySelected: (Category) -> Unit,
    isPremiumUser: Boolean = false,
    modifier: Modifier = Modifier
) {
    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            CategoryChip(
                category = category,
                selected = category.id == selectedCategoryId,
                onClick = { onCategorySelected(category) },
                isPremiumUser = isPremiumUser
            )
        }
    }
} 