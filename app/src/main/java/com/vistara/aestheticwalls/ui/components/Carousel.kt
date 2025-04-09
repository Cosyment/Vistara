package com.vistara.aestheticwalls.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vistara.aestheticwalls.data.model.Banner
import com.vistara.aestheticwalls.data.model.BannerActionType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 自动轮播Banner组件
 *
 * @param banners 轮播项目列表
 * @param onBannerClick Banner点击回调
 * @param autoScrollDuration 自动滚动间隔，单位毫秒
 * @param showIndicator 是否显示指示器
 * @param modifier 组件修饰符
 */
@Composable
fun Carousel(
    banners: List<Banner>,
    onBannerClick: (Banner) -> Unit,
    autoScrollDuration: Long = 3000,
    showIndicator: Boolean = true,
    modifier: Modifier = Modifier,
) {
    if (banners.isEmpty()) return
    
    val coroutineScope = rememberCoroutineScope()
    var currentPage by remember { mutableStateOf(0) }
    val pageCount = banners.size
    
    // 自动滚动
    LaunchedEffect(pageCount) {
        if (pageCount > 1) {
            while (true) {
                delay(autoScrollDuration)
                currentPage = (currentPage + 1) % pageCount
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // 主要Banner内容
        BannerPager(
            banners = banners,
            currentPage = currentPage,
            onPageChange = { page -> currentPage = page },
            onBannerClick = onBannerClick,
            modifier = Modifier.fillMaxSize()
        )
        
        // 指示器
        if (showIndicator && pageCount > 1) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                repeat(pageCount) { index ->
                    val isSelected = currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                coroutineScope.launch {
                                    currentPage = index
                                }
                            }
                    )
                }
            }
        }
    }
}

/**
 * Banner分页组件
 */
@Composable
private fun BannerPager(
    banners: List<Banner>,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    onBannerClick: (Banner) -> Unit,
    modifier: Modifier = Modifier
) {
    val pageCount = banners.size
    if (pageCount <= 0) return
    
    Box(
        modifier = modifier
    ) {
        for (i in 0 until pageCount) {
            val banner = banners[i]
            val pageOffset = calculatePageOffset(i, currentPage, pageCount)
            
            BannerItem(
                banner = banner,
                pageOffset = pageOffset,
                onClick = { onBannerClick(banner) },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val scale = 0.9f + 0.1f * (1 - pageOffset.coerceIn(0f, 1f))
                        scaleX = scale
                        scaleY = scale
                        alpha = 1 - pageOffset.coerceIn(0f, 1f) * 0.5f
                    }
            )
        }
    }
}

/**
 * 计算页面偏移量
 */
private fun calculatePageOffset(page: Int, currentPage: Int, pageCount: Int): Float {
    val currentPosition = currentPage % pageCount
    val offset = minOf(
        abs(page - currentPosition), 
        abs(page - (currentPosition + pageCount)), 
        abs(page - (currentPosition - pageCount))
    )
    return offset.toFloat()
}

/**
 * Banner项目视图
 */
@Composable
private fun BannerItem(
    banner: Banner,
    pageOffset: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Card(
        modifier = modifier
            .padding(horizontal = 8.dp * (1 - pageOffset.coerceIn(0f, 1f)))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp * (1 - pageOffset.coerceIn(0f, 1f))
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Banner图片
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(banner.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = banner.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // 渐变底部阴影
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(80.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
            
            // Banner文字
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = banner.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                banner.subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Premium标记（如果适用）
            if (banner.actionType == BannerActionType.PREMIUM) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "高级 👑",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
} 