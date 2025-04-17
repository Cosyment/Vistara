package com.vistara.aestheticwalls.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.Banner
import com.vistara.aestheticwalls.data.model.BannerActionType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 自动轮播 Banner组件
 *
 * @param banners 轮播项目列表
 * @param onBannerClick Banner点击回调
 * @param autoScrollDuration 自动滚动间隔，单位毫秒
 * @param showIndicator 是否显示指示器
 * @param modifier 组件修饰符
 */
@OptIn(ExperimentalFoundationApi::class)
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
    val pageCount = banners.size

    // 使用 HorizontalPager 的 PagerState
    val pagerState = rememberPagerState(pageCount = { pageCount })

    // 自动滚动
    LaunchedEffect(pageCount) {
        if (pageCount > 1) {
            while (true) {
                delay(autoScrollDuration)
                val nextPage = (pagerState.currentPage + 1) % pageCount
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // 使用 HorizontalPager 实现可滑动的轮播图
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val banner = banners[page]
            BannerItem(
                banner = banner,
                onClick = { onBannerClick(banner) },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 指示器
        if (showIndicator && pageCount > 1) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                repeat(pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
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
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    )
                }
            }
        }
    }
}

/**
 * Banner项目视图
 */
@Composable
private fun BannerItem(
    banner: Banner,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
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
                        text = stringResource(id = R.string.premium),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Banner文字
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = banner.title ?: "",
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
        }
    }
}
