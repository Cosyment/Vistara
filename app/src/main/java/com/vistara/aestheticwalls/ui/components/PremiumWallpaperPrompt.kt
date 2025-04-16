package com.vistara.aestheticwalls.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.vistara.aestheticwalls.billing.BillingManager
import com.vistara.aestheticwalls.ui.theme.VistaraTheme

/**
 * 高级壁纸提示对话框
 * 当用户尝试使用高级功能时显示
 * 支持选择不同的订阅计划
 */
@Composable
fun PremiumWallpaperPrompt(
    onUpgrade: (String) -> Unit,
    onDismiss: () -> Unit,
    isConnected: Boolean = true,
    isPremiumUser: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 选中的订阅计划
    var selectedPlan by remember { mutableStateOf(BillingManager.SUBSCRIPTION_MONTHLY) }

    // 动画效果
    val infiniteTransition = rememberInfiniteTransition(label = "crown_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.1f, animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse
        ), label = "scale_animation"
    )

    // 使用半透明背景和模糊效果的卡片
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            // 使用半透明背景 - 完全透明底色，内容区域使用半透明效果
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        // 使用渐变背景的内容区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // 标题 - 带动画效果
                Text(
                    text = if (isPremiumUser) "您已是高级会员" else "解锁高级功能",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 皇冠图标 - 带动画效果和光晕效果
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD700),  // 金色
                                    Color(0xFFFFA500),  // 橙色
                                    Color(0xFFFF8C00)   // 深橙色
                                )
                            )
                        )
                        // 添加光晕效果
                        .graphicsLayer {
                            shadowElevation = 8f
                            shape = CircleShape
                            clip = true
                        }, contentAlignment = Alignment.Center
                ) {
                    // 添加光晕效果的内层
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFD700).copy(alpha = 0.8f),
                                        Color(0xFFFFD700).copy(alpha = 0.6f),
                                        Color(0xFFFFD700).copy(alpha = 0.4f)
                                    )
                                )
                            ), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👑",
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.scale(1.2f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 描述
                Text(
                    text = if (isPremiumUser) "感谢您的支持，您可以使用所有高级功能"
                    else "升级到高级版，解锁所有壁纸和功能",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 功能列表
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    PremiumFeatureItem(text = "无限下载高清壁纸")
                    PremiumFeatureItem(text = "去除所有广告")
                    PremiumFeatureItem(text = "专属高级壁纸")
                    PremiumFeatureItem(text = "高级编辑功能")
                    PremiumFeatureItem(text = "自动更换壁纸")
                }

                // 只有非高级用户才显示订阅选项
                if (!isPremiumUser) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // 订阅计划选择
                    Text(
                        text = "选择订阅计划",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 订阅计划选项 - 使用Row布局确保大小统一
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // 月会员
                        SubscriptionPlanItem(
                            title = "月会员",
                            price = "¥18.99",
                            isSelected = selectedPlan == BillingManager.SUBSCRIPTION_MONTHLY,
                            onClick = { selectedPlan = BillingManager.SUBSCRIPTION_MONTHLY },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // 季度会员
                        SubscriptionPlanItem(
                            title = "季度会员",
                            price = "¥49.99",
                            isSelected = selectedPlan == BillingManager.SUBSCRIPTION_QUARTERLY,
                            onClick = { selectedPlan = BillingManager.SUBSCRIPTION_QUARTERLY },
                            discount = "省17%",
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // 年会员
                        SubscriptionPlanItem(
                            title = "年会员",
                            price = "¥168.99",
                            isSelected = selectedPlan == BillingManager.SUBSCRIPTION_YEARLY,
                            onClick = { selectedPlan = BillingManager.SUBSCRIPTION_YEARLY },
                            discount = "省27%",
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 升级按钮 - 带动画效果和渐变背景
                    Button(
                        onClick = { onUpgrade(selectedPlan) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        enabled = isConnected,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        // 使用主题色渐变背景
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                ), contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isConnected) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = if (isConnected) "立即升级" else "正在连接支付服务...",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    // 高级用户显示已解锁状态
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF4CAF50), Color(0xFF8BC34A)
                                    )
                                )
                            )
                            .padding(12.dp), contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "已解锁全部高级功能",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isPremiumUser) "关闭" else "稍后再说",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 高级功能项
 */
@Composable
private fun PremiumFeatureItem(
    text: String, modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 订阅计划项
 */
@Composable
private fun SubscriptionPlanItem(
    title: String,
    price: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    discount: String? = null,
    modifier: Modifier = Modifier
) {
    // 选中状态的动画 - 使用更平滑的动画
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "selection_scale"
    )

    // 选中状态的背景颜色
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    // 卡片内容颜色
    val contentColor = if (isSelected) {
        Color.Blue // 金色
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(modifier = modifier) {
        // 折扣标签 - 放在顶部
        if (discount != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-10).dp)
                    .zIndex(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFF5722), // 深橙色
                                Color(0xFFFF9800)  // 橙色
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = discount,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 使用半透明和渐变背景的卡片
        Card(
            modifier = Modifier
                .scale(scale)
                .height(90.dp) // 固定高度确保统一
                .fillMaxWidth()
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(onClick = { onClick() }), shape = RoundedCornerShape(16.dp),
//            colors = CardDefaults.cardColors(
//                containerColor = backgroundColor
//            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 6.dp else 2.dp
            )
        ) {
            // 使用渐变背景的内容区域
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (isSelected) {
                            // 选中时使用金色渐变
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFD700).copy(alpha = 0.2f),
                                    Color(0xFFFFD700).copy(alpha = 0.05f)
                                )
                            )
                        } else {
                            // 非选中时使用半透明渐变
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                        }
                    ), contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 标题
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 价格
                    Text(
                        text = price,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun PremiumWallpaperPromptPreview() {
    VistaraTheme {
        PremiumWallpaperPrompt(
            onUpgrade = {},
            onDismiss = {},
            isConnected = true,
            isPremiumUser = false
        )
    }
}
