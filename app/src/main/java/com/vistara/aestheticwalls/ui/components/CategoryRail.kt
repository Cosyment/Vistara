package com.vistara.aestheticwalls.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vistara.aestheticwalls.data.model.Category

/**
 * 分类导航组件，水平滚动的分类按钮
 * @param categories 分类列表
 * @param onCategoryClick 分类点击回调
 * @param selectedCategoryId 当前选中的分类ID（可选）
 */
@Composable
fun CategoryRail(
    categories: List<Category>,
    onCategoryClick: (Category) -> Unit,
    selectedCategoryId: String? = null,
    modifier: Modifier = Modifier,
    isPremiumUser: Boolean = false
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            CategoryItem(
                category = category,
                isSelected = category.id == selectedCategoryId,
                onClick = { onCategoryClick(category) },
                isPremiumUser = isPremiumUser
            )
        }
    }
}

/**
 * 圆形分类项，带图标
 */
@Composable
fun CategoryIconItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    isPremiumUser: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val containerColor = if (isSelected) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.surfaceVariant
        
    val contentColor = if (isSelected) 
        MaterialTheme.colorScheme.onPrimary 
    else 
        MaterialTheme.colorScheme.onSurfaceVariant
    
    val isAccessible = !category.isPremium || isPremiumUser
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(72.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(containerColor.copy(alpha = if (isAccessible) 1f else 0.5f))
                .clickable(enabled = isAccessible) { onClick() }
        ) {
            if (category.iconUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(category.iconUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = category.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                )
            } else {
                // 没有图标时显示首字母
                Text(
                    text = category.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor.copy(alpha = if (isAccessible) 1f else 0.5f),
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Premium标记
            if (category.isPremium && !isPremiumUser) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "👑",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isAccessible) 
                MaterialTheme.colorScheme.onBackground 
            else 
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 图片背景的分类项
 */
@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    isPremiumUser: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAccessible = !category.isPremium || isPremiumUser
    
    Card(
        modifier = modifier
            .width(100.dp)
            .height(64.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        border = if (isSelected) 
            CardDefaults.outlinedCardBorder().copy(width = 2.dp) 
        else 
            null,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = { if (isAccessible) onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // 封面图片
            if (category.coverUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(category.coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = category.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = if (isAccessible) 1f else 0.5f
                )
            } else {
                // 没有封面时显示渐变背景
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                )
            }
            
            // 叠加一层暗色背景，使文字更清晰
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )
            
            // 分类名称
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            // Premium标记
            if (category.isPremium && !isPremiumUser) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 8.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "👑",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * 大尺寸分类卡片，适用于格子布局
 */
@Composable
fun CategoryCard(
    category: Category,
    onClick: () -> Unit,
    isPremiumUser: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAccessible = !category.isPremium || isPremiumUser
    
    Card(
        modifier = modifier
            .aspectRatio(1.5f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = { if (isAccessible) onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 封面图片
            if (category.coverUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(category.coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = category.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = if (isAccessible) 1f else 0.5f
                )
            } else {
                // 没有封面时显示渐变背景
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                )
            }
            
            // 渐变叠加层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                if (category.wallpaperCount > 0) {
                    Text(
                        text = "${category.wallpaperCount}张壁纸",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            // Premium标记
            if (category.isPremium && !isPremiumUser) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 16.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "高级 👑",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
} 