package com.vistara.aestheticwalls.ui.screens.premium

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vistara.aestheticwalls.R
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
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val activity = LocalActivity.current
    val isPremiumUser by viewModel.isPremiumUser.collectAsState()
    val isUpgrading by viewModel.isUpgrading.collectAsState()
    val upgradeResult by viewModel.upgradeResult.collectAsState()
    val selectedPlan by viewModel.selectedPlan.collectAsState()
    val billingConnectionState by viewModel.billingConnectionState.collectAsState()
    val productPrices by viewModel.productPrices.collectAsState()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.upgrade_to_premium)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                })
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // 顶部卡片
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 皇冠图标
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                    )
                }

                // 标题
                Text(
                    text = stringResource(R.string.premium_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 副标题
                Text(
                    text = stringResource(R.string.premium_description),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 功能列表卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onBackground
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // 功能列表
                        PremiumFeaturesList()
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 套餐选择
                Text(
                    text = stringResource(R.string.select_subscription),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                PlanCard(
                    plan = PremiumPlan.WEEKLY,
                    isSelected = selectedPlan == PremiumPlan.WEEKLY,
                    price = productPrices[com.vistara.aestheticwalls.billing.BillingManager.SUBSCRIPTION_WEEKLY]
                        ?: stringResource(R.string.loading),
                    onClick = { viewModel.selectPlan(PremiumPlan.WEEKLY) })

                Spacer(modifier = Modifier.height(12.dp))

                // 月度套餐
                PlanCard(
                    plan = PremiumPlan.MONTHLY,
                    isSelected = selectedPlan == PremiumPlan.MONTHLY,
                    price = productPrices[com.vistara.aestheticwalls.billing.BillingManager.SUBSCRIPTION_MONTHLY]
                        ?: stringResource(R.string.loading),
                    onClick = { viewModel.selectPlan(PremiumPlan.MONTHLY) })

                Spacer(modifier = Modifier.height(12.dp))

                // 季度套餐
                PlanCard(
                    plan = PremiumPlan.QUARTERLY,
                    isSelected = selectedPlan == PremiumPlan.QUARTERLY,
                    price = productPrices[com.vistara.aestheticwalls.billing.BillingManager.SUBSCRIPTION_QUARTERLY]
                        ?: stringResource(R.string.loading),
                    onClick = { viewModel.selectPlan(PremiumPlan.QUARTERLY) })

                Spacer(modifier = Modifier.height(32.dp))

                // 升级按钮
                Button(
                    onClick = { viewModel.upgrade(activity) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isUpgrading && !isPremiumUser && billingConnectionState == com.vistara.aestheticwalls.billing.BillingConnectionState.CONNECTED,
                    shape = RoundedCornerShape(25.dp),
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
                                            Color(0xFF9F2BEE), // 紫色渐变起始色
                                            Color(0xFF8545FF)  // 紫色渐变结束色
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

                                    else -> stringResource(R.string.upgrade_now)
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 恢复购买按钮
                Button(
                    onClick = { viewModel.restorePurchases() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUpgrading && billingConnectionState == com.vistara.aestheticwalls.billing.BillingConnectionState.CONNECTED,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF9F2BEE),
                        disabledContainerColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.restore_purchases),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.restore_purchases),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 说明文字
                Text(
                    text = stringResource(R.string.payment_success_notice),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 订阅条款说明
                Text(
                    text = stringResource(R.string.subscription_terms),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 取消订阅入口
                Button(
                    onClick = { viewModel.openSubscriptionManagementPage(activity) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUpgrading && billingConnectionState == com.vistara.aestheticwalls.billing.BillingConnectionState.CONNECTED,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.manage_subscription),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.manage_subscription),
                        style = MaterialTheme.typography.bodyLarge
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
            text = stringResource(R.string.premium_feature_1), icon = Icons.Default.Refresh
        )
        PremiumFeatureItem(
            text = stringResource(R.string.premium_feature_2), icon = Icons.Default.Refresh
        )
        PremiumFeatureItem(
            text = stringResource(R.string.premium_feature_5), icon = Icons.Default.Refresh
        )
        PremiumFeatureItem(
            text = stringResource(R.string.premium_feature_3), icon = Icons.Default.Star
        )
        PremiumFeatureItem(
            text = stringResource(R.string.premium_feature_4), icon = Icons.Default.Edit
        )
    }
}

/**
 * 高级功能项
 */
@Composable
private fun PremiumFeatureItem(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // 图标容器
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF9F2BEE).copy(alpha = 0.1f), // 浅紫色
                            Color(0xFF8545FF).copy(alpha = 0.1f)  // 浅紫色
                        )
                    ), shape = CircleShape
                ), contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF9F2BEE),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 套餐卡片
 */
@Composable
private fun PlanCard(
    plan: PremiumPlan, isSelected: Boolean, price: String, onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        Color(0xFF9F2BEE) // 紫色
    } else {
        Color.Transparent
    }

    val backgroundColor = if (isSelected) {
        Color(0xFFF5EEFF) // 浅紫色背景
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        onClick = onClick, modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp, color = borderColor, shape = RoundedCornerShape(16.dp)
            ), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ), elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧圆形选择指示器
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = if (isSelected) Color(0xFF9F2BEE) else Color.LightGray.copy(
                            alpha = 0.3f
                        ), shape = CircleShape
                    ), contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = Color.White, shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 中间套餐信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(plan.titleResId),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(0xFF9F2BEE) else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(plan.descriptionResId),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 折扣标签
                plan.discountResId?.let {
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFF5252), shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(it),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            // 右侧价格
            Text(
                text = price,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF9F2BEE) else MaterialTheme.colorScheme.primary
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
