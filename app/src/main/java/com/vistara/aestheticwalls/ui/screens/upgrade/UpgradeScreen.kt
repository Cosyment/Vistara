package com.vistara.aestheticwalls.ui.screens.upgrade

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vistara.aestheticwalls.ui.theme.VistaraTheme

/**
 * 升级页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeScreen(
    onBackPressed: () -> Unit,
    onUpgradeSuccess: () -> Unit = {},
    viewModel: UpgradeViewModel = hiltViewModel()
) {
    val isPremiumUser by viewModel.isPremiumUser.collectAsState()
    val isUpgrading by viewModel.isUpgrading.collectAsState()
    val upgradeResult by viewModel.upgradeResult.collectAsState()
    val selectedPlan by viewModel.selectedPlan.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 显示升级结果
    LaunchedEffect(upgradeResult) {
        upgradeResult?.let {
            when (it) {
                is UpgradeResult.Success -> {
                    snackbarHostState.showSnackbar(it.message)
                    viewModel.clearUpgradeResult()
                    onUpgradeSuccess()
                }
                is UpgradeResult.Error -> {
                    snackbarHostState.showSnackbar(it.message)
                    viewModel.clearUpgradeResult()
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("升级到高级版") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 标题
            Text(
                text = "解锁全部高级功能",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 副标题
            Text(
                text = "享受无限壁纸、自动更换和更多功能",
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
                text = "选择套餐",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 月度套餐
            PlanCard(
                plan = UpgradePlan.MONTHLY,
                isSelected = selectedPlan == UpgradePlan.MONTHLY,
                onClick = { viewModel.selectPlan(UpgradePlan.MONTHLY) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 年度套餐
            PlanCard(
                plan = UpgradePlan.YEARLY,
                isSelected = selectedPlan == UpgradePlan.YEARLY,
                onClick = { viewModel.selectPlan(UpgradePlan.YEARLY) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 终身套餐
            PlanCard(
                plan = UpgradePlan.LIFETIME,
                isSelected = selectedPlan == UpgradePlan.LIFETIME,
                onClick = { viewModel.selectPlan(UpgradePlan.LIFETIME) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 升级按钮
            Button(
                onClick = { viewModel.upgrade() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUpgrading && !isPremiumUser
            ) {
                if (isUpgrading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isPremiumUser) "您已是高级用户" else "立即升级",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 说明文字
            Text(
                text = "支付成功后，您将立即获得高级功能。订阅可随时取消。",
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
        PremiumFeatureItem(text = "无限下载高质量壁纸")
        PremiumFeatureItem(text = "移除所有广告")
        PremiumFeatureItem(text = "自动更换壁纸（每小时/每次解锁）")
        PremiumFeatureItem(text = "独家高级壁纸")
        PremiumFeatureItem(text = "优先获取新功能")
    }
}

/**
 * 高级功能项
 */
@Composable
private fun PremiumFeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * 套餐卡片
 */
@Composable
private fun PlanCard(
    plan: UpgradePlan,
    isSelected: Boolean,
    onClick: () -> Unit
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
                selected = isSelected,
                onClick = onClick
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
                text = plan.price,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpgradeScreenPreview() {
    VistaraTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // 注意：预览中不会显示真实数据，因为没有提供真实的ViewModel
            // 这里只是UI预览
            UpgradeScreen(onBackPressed = {})
        }
    }
}
