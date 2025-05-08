package com.vistara.aestheticwalls.ui.screens.premium

import androidx.activity.compose.LocalActivity
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.ui.icons.AppIcons
import com.vistara.aestheticwalls.ui.theme.VistaraTheme
import com.vistara.aestheticwalls.ui.theme.stringResource

/**
 * 升级页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onBackPressed: () -> Unit,
    onUpgradeSuccess: () -> Unit = {},
    viewModel: PremiumViewModel = hiltViewModel(),
    navController: NavController = rememberNavController()
) {
    val activity = LocalActivity.current
    val isPremiumUser by viewModel.isPremiumUser.collectAsState()
    val isUpgrading by viewModel.isUpgrading.collectAsState()
    val upgradeResult by viewModel.upgradeResult.collectAsState()
    val selectedPlan by viewModel.selectedPlan.collectAsState()
    val billingConnectionState by viewModel.billingConnectionState.collectAsState()
    val productPrices by viewModel.productPrices.collectAsState()

    // 设置导航控制器
    viewModel.setNavController(navController)

    val snackBarHostState = remember { SnackbarHostState() }

    // 显示升级结果
    LaunchedEffect(upgradeResult) {
        upgradeResult?.let {
            when (it) {
                is UpgradeResult.Success -> {
                    snackBarHostState.showSnackbar(it.message)
                    viewModel.clearUpgradeResult()
                    onUpgradeSuccess()
                }

                is UpgradeResult.Error -> {
                    snackBarHostState.showSnackbar(it.message)
                    viewModel.clearUpgradeResult()
                }
            }
        }
    }

    // 定义紫色渐变
    val purpleGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFEC12E2), // 亮紫色
            Color(0xFF8531FF)  // 深紫色
        )
    )

    // 定义深色背景
    val darkBackground = Color(0xFF10082A)

    Scaffold(
        topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.premium), color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = darkBackground
            )
        )
    }, snackbarHost = { SnackbarHost(snackBarHostState) }, containerColor = darkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                // VIP购买部分
                Spacer(modifier = Modifier.height(16.dp))

                // 订阅卡片行
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 周卡
                    SubscriptionCard(
                        title = stringResource(R.string.subscription_title_week),
                        price = productPrices[com.vistara.aestheticwalls.billing.BillingManager.SUBSCRIPTION_WEEKLY]
                            ?: stringResource(R.string.loading),
                        originalPrice = productPrices[com.vistara.aestheticwalls.billing.BillingManager.SUBSCRIPTION_WEEKLY]
                            ?: stringResource(R.string.loading),
                        isSelected = selectedPlan == PremiumPlan.WEEKLY,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.selectPlan(PremiumPlan.WEEKLY) })

                    Spacer(modifier = Modifier.width(12.dp))

                    // 月卡
                    SubscriptionCard(
                        title = stringResource(R.string.subscription_title_month),
                        price = productPrices[com.vistara.aestheticwalls.billing.BillingManager.SUBSCRIPTION_MONTHLY]
                            ?: stringResource(R.string.loading),
                        originalPrice = productPrices[com.vistara.aestheticwalls.billing.BillingManager.SUBSCRIPTION_MONTHLY]
                            ?: stringResource(R.string.loading),
                        isSelected = selectedPlan == PremiumPlan.MONTHLY,
                        modifier = Modifier.weight(1f),
                        showBonus = false,
                        onClick = { viewModel.selectPlan(PremiumPlan.MONTHLY) })

                    Spacer(modifier = Modifier.width(12.dp))

                    // 季卡
                    SubscriptionCard(
                        title = stringResource(R.string.subscription_title_quarter),
                        price = productPrices[com.vistara.aestheticwalls.billing.BillingManager.SUBSCRIPTION_QUARTERLY]
                            ?: stringResource(R.string.loading),
                        originalPrice = productPrices[com.vistara.aestheticwalls.billing.BillingManager.SUBSCRIPTION_QUARTERLY]
                            ?: stringResource(R.string.loading),
                        isSelected = selectedPlan == PremiumPlan.QUARTERLY,
                        modifier = Modifier.weight(1f),
                        showDiscount = false,
                        onClick = { viewModel.selectPlan(PremiumPlan.QUARTERLY) })
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 特权标题卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF3A1B59), // 深紫色
                                        Color(0xFFC125E3), // 中紫色
                                        Color(0xFF3A1B59)  // 深紫色
                                    )
                                )
                            )
                            .padding(vertical = 14.dp), contentAlignment = Alignment.Center
                    ) {
                        // 光效
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .offset(y = (-26).dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF3A1B59).copy(alpha = 0.2f),
                                            Color(0xFFF971F8),
                                            Color(0xFF3A1B59).copy(alpha = 0.2f)
                                        )
                                    )
                                )
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .offset(y = 35.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF3A1B59).copy(alpha = 0.2f),
                                            Color(0xFFF971F8),
                                            Color(0xFF3A1B59).copy(alpha = 0.2f)
                                        )
                                    )
                                )
                        )

                        Text(
                            text = stringResource(R.string.exclusive_privileges),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 特权列表卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF201730) // 深紫色背景
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // 特权列表
                        PremiumFeaturesList()
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 支付说明文字
                Text(
                    text = stringResource(R.string.payment_terms),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF524C5F),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 取消订阅链接
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.cancel_subscription_prefix) + " ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF524C5F)
                    )
                    Text(
                        text = stringResource(R.string.cancel_subscription_link),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF71FE3),
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            viewModel.openSubscriptionManagementPage(
                                activity
                            )
                        })
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 条款链接 - 使用FlowRow以更好地处理文本溢出
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.more_info_prefix),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF524C5F)
                        )
                        Text(
                            text = stringResource(R.string.terms_of_use),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9F2BEE),
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { viewModel.openTermsOfService() })
                        Text(
                            text = " ${stringResource(R.string.and)} ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF524C5F)
                        )
                        Text(
                            text = stringResource(R.string.privacy_policy),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9F2BEE),
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { viewModel.openPrivacyPolicy() })
                        Text(
                            text = ".",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF524C5F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 续订按钮
                Button(
                    onClick = { viewModel.upgrade(activity) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    enabled = !isUpgrading && !isPremiumUser && billingConnectionState == com.vistara.aestheticwalls.billing.BillingConnectionState.CONNECTED,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = if (!isUpgrading && !isPremiumUser && billingConnectionState == com.vistara.aestheticwalls.billing.BillingConnectionState.CONNECTED) {
                                        listOf(
                                            Color(0xFFEC12E2), // 亮紫色
                                            Color(0xFF8531FF)  // 深紫色
                                        )
                                    } else {
                                        listOf(
                                            Color.Gray.copy(alpha = 0.3f),
                                            Color.Gray.copy(alpha = 0.3f)
                                        )
                                    }
                                )
                            ), contentAlignment = Alignment.Center
                    ) {
                        if (isUpgrading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = when {
                                    isPremiumUser -> stringResource(R.string.error_already_premium)
                                    billingConnectionState != com.vistara.aestheticwalls.billing.BillingConnectionState.CONNECTED -> stringResource(
                                        R.string.connecting_payment
                                    )

                                    else -> stringResource(R.string.renew_now)
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 过期日期
                Text(
                    text = stringResource(R.string.expired_on, "2024-5-10"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF524C5F),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 恢复购买按钮 (隐藏，但保留功能)
                Button(
                    onClick = { viewModel.restorePurchases() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.dp), // 高度为0，隐藏按钮但保留功能
                    enabled = !isUpgrading && billingConnectionState == com.vistara.aestheticwalls.billing.BillingConnectionState.CONNECTED,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent, contentColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    // 空内容
                }
            }
        }
    }
}

/**
 * 订阅卡片
 */
@Composable
private fun SubscriptionCard(
    title: String,
    price: String,
    originalPrice: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    showBonus: Boolean = false,
    showDiscount: Boolean = false,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFEC12E2), // 亮紫色
                Color(0xFF8531FF)  // 深紫色
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF3D3A47), // 深灰色
                Color(0xFF3D3A47)  // 深灰色
            )
        )
    }

    val textColor = if (isSelected) {
        Color(0xFF9F2BEE) // 紫色
    } else {
        Color.White.copy(alpha = 0.6f) // 半透明白色
    }

    Box(modifier = modifier) {
        // 如果有奖励标签，显示在卡片顶部
        if (showBonus) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-8).dp)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFEC12E2), // 亮紫色
                                    Color(0xFF8531FF)  // 深紫色
                                )
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.bonus_diamonds, 50),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = AppIcons.Diamond,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        // 主卡片
        Card(
            onClick = onClick, modifier = Modifier
                .fillMaxWidth()
                .height(140.dp) // 固定高度，确保所有卡片高度一致
                .border(
                    width = 1.dp, brush = borderColor, shape = RoundedCornerShape(12.dp)
                ), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ), elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center // 垂直居中内容
            ) {
                // 标题
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 原价（带删除线）
                if (showDiscount) {
                    Text(
                        text = originalPrice,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.2f),
                        textDecoration = TextDecoration.LineThrough
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }

                // 价格
                Text(
                    text = price,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
        }

        // 如果有折扣标签，显示在卡片顶部
        if (showDiscount) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-8).dp)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF3D3A47) // 深灰色
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.discount_percent_off, 50),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * 高级功能列表
 */
@Composable
private fun PremiumFeaturesList() {
    Column(modifier = Modifier.fillMaxWidth()) {
        PremiumFeatureItem(
            title = stringResource(R.string.premium_feature_1),
            description = stringResource(R.string.premium_feature_1_desc),
            iconIndex = 1
        )

        Spacer(modifier = Modifier.height(32.dp))

        PremiumFeatureItem(
            title = stringResource(R.string.premium_feature_2),
            description = stringResource(R.string.premium_feature_2_desc),
            iconIndex = 2
        )

        Spacer(modifier = Modifier.height(32.dp))

        PremiumFeatureItem(
            title = stringResource(R.string.premium_feature_3),
            description = stringResource(R.string.premium_feature_3_desc),
            iconIndex = 3
        )

        Spacer(modifier = Modifier.height(32.dp))

        PremiumFeatureItem(
            title = stringResource(R.string.premium_feature_4),
            description = stringResource(R.string.premium_feature_4_desc),
            iconIndex = 4
        )

        Spacer(modifier = Modifier.height(32.dp))

        PremiumFeatureItem(
            title = stringResource(R.string.premium_feature_5),
            description = stringResource(R.string.premium_feature_5_desc),
            iconIndex = 5
        )

//        Spacer(modifier = Modifier.height(32.dp))
//
//        PremiumFeatureItem(
//            title = stringResource(R.string.premium_feature_5),
//            description = stringResource(R.string.feature_photo_voice_desc),
//            iconIndex = 6
//        )

//        Spacer(modifier = Modifier.height(32.dp))
//
//        PremiumFeatureItem(
//            title = stringResource(R.string.feature_secret_content),
//            description = stringResource(R.string.feature_secret_content_desc),
//            iconIndex = 7
//        )
    }
}

/**
 * 高级功能项
 */
@Composable
private fun PremiumFeatureItem(
    title: String, description: String, iconIndex: Int
) {
    Row(
        verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()
    ) {
        // 图标容器
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    color = Color(0xFF4D1D58), // 深紫色
                    shape = CircleShape
                ), contentAlignment = Alignment.Center
        ) {
            // 这里使用不同的图标，根据iconIndex选择
            Icon(
                imageVector = when (iconIndex) {
                    1 -> AppIcons.UnlimitedDownload
                    2 -> AppIcons.SpecialEffectWallpaper
                    3 -> AppIcons.ExclusiveWallpaper
                    4 -> AppIcons.AdvancedEditing
                    5 -> AppIcons.AutoWallpaperChange
//                    6 -> Icons.Default.PhotoCamera
//                    7 -> Icons.Default.Lock
                    else -> Icons.Default.Star
                }, contentDescription = null, tint = Color(0xFFF71FE3), // 亮紫色
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = title, style = MaterialTheme.typography.titleMedium, color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PremiumScreenPreview() {
    VistaraTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // 注意：预览中不会显示真实数据，因为没有提供真实的ViewModel
            // 这里只是UI预览
            PremiumScreen(onBackPressed = {})
        }
    }
}
