package com.vistara.aestheticwalls.ui.screens.premium

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.ui.theme.VistaraTheme

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

    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.upgrade_to_premium)) }, navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back)
                )
            }
        })
    }, snackbarHost = { SnackbarHost(snackBarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 功能列表
            PremiumFeaturesList()

            Spacer(modifier = Modifier.height(24.dp))

            // 套餐选择
            Text(
                text = stringResource(R.string.select_subscription),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 月度套餐
            PlanCard(
                plan = UpgradePlan.MONTHLY,
                isSelected = selectedPlan == UpgradePlan.MONTHLY,
                price = productPrices[com.vistara.aestheticwalls.billing.BillingManager.SUBSCRIPTION_MONTHLY]
                    ?: stringResource(R.string.loading),
                onClick = { viewModel.selectPlan(UpgradePlan.MONTHLY) })

            Spacer(modifier = Modifier.height(8.dp))

            // 年度套餐
            PlanCard(
                plan = UpgradePlan.YEARLY,
                isSelected = selectedPlan == UpgradePlan.YEARLY,
                price = productPrices[com.vistara.aestheticwalls.billing.BillingManager.SUBSCRIPTION_YEARLY]
                    ?: stringResource(R.string.loading),
                onClick = { viewModel.selectPlan(UpgradePlan.YEARLY) })

            Spacer(modifier = Modifier.height(8.dp))

            // 终身套餐
            PlanCard(
                plan = UpgradePlan.LIFETIME,
                isSelected = selectedPlan == UpgradePlan.LIFETIME,
                price = productPrices[com.vistara.aestheticwalls.billing.BillingManager.PREMIUM_LIFETIME]
                    ?: stringResource(R.string.loading),
                onClick = { viewModel.selectPlan(UpgradePlan.LIFETIME) })

            Spacer(modifier = Modifier.height(24.dp))

            // 升级按钮
            Button(
                onClick = { viewModel.upgrade(activity) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUpgrading && !isPremiumUser && billingConnectionState == com.vistara.aestheticwalls.billing.BillingConnectionState.CONNECTED
            ) {
                if (isUpgrading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = when {
                            isPremiumUser -> stringResource(R.string.error_already_premium)
                            billingConnectionState != com.vistara.aestheticwalls.billing.BillingConnectionState.CONNECTED -> stringResource(R.string.connecting_payment)
                            else -> stringResource(R.string.upgrade_now)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 恢复购买按钮
            Button(
                onClick = { viewModel.restorePurchases() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUpgrading && billingConnectionState == com.vistara.aestheticwalls.billing.BillingConnectionState.CONNECTED
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.restore_purchases),
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(R.string.restore_purchases), style = MaterialTheme.typography.bodyLarge
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
        }
    }
}

/**
 * 高级功能列表
 */
@Composable
private fun PremiumFeaturesList() {
    Column(modifier = Modifier.fillMaxWidth()) {
        PremiumFeatureItem(text = stringResource(R.string.premium_feature_1))
        PremiumFeatureItem(text = stringResource(R.string.premium_feature_2))
        PremiumFeatureItem(text = stringResource(R.string.premium_feature_5))
        PremiumFeatureItem(text = stringResource(R.string.premium_feature_3))
        PremiumFeatureItem(text = stringResource(R.string.premium_feature_4))
    }
}

/**
 * 高级功能项
 */
@Composable
private fun PremiumFeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text, style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * 套餐卡片
 */
@Composable
private fun PlanCard(
    plan: UpgradePlan, isSelected: Boolean, price: String, onClick: () -> Unit
) {
    val borderModifier = if (isSelected) {
        Modifier.border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(12.dp)
        )
    } else {
        Modifier
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected, onClick = onClick
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    plan.discount?.let {
                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = plan.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = price,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
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
