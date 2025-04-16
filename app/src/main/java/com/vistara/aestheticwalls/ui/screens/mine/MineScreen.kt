package com.vistara.aestheticwalls.ui.screens.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.vistara.aestheticwalls.ui.theme.VistaraTheme

/**
 * 个人中心页面
 * 显示用户信息和功能入口
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineScreen(
    onFavoritesClick: () -> Unit = {},
    onDownloadsClick: () -> Unit = {},
    onAutoChangeClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onFeedbackClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onUpgradeClick: () -> Unit = {},
    onTestToolsClick: () -> Unit = {},
    viewModel: MineViewModel = hiltViewModel()
) {
    // 从ViewModel获取状态
    val username by viewModel.username.collectAsState()
    val isPremiumUser by viewModel.isPremiumUser.collectAsState()
    val isDebugMode by viewModel.isDebugMode.collectAsState()

    // 使用生命周期事件监听器来检测页面可见性变化
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentViewModel = rememberUpdatedState(viewModel)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // 当页面恢复可见时刷新用户数据
                currentViewModel.value.refreshUserData()
            }
        }

        // 添加观察者
        lifecycleOwner.lifecycle.addObserver(observer)

        // 当组件离开组合时移除观察者
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 首次加载时也刷新用户数据
    LaunchedEffect(Unit) {
        viewModel.refreshUserData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(bottom = 16.dp)
        ) {
            // 用户信息区域
            MineHeader(
                username = username, isPremiumUser = isPremiumUser
            )

            // 升级横幅
            if (!isPremiumUser) {
                UpgradeBanner(
                    onClick = {
                        // 调用ViewModel的升级方法
                        viewModel.upgradeToPremium()
                        onUpgradeClick()
                    }, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 功能列表
            FeatureItem(
                icon = Icons.Default.Favorite, title = "我的收藏", subtitle = "查看所有收藏的壁纸", onClick = onFavoritesClick
            )

            FeatureItem(
                icon = Icons.Default.Star, title = "我的下载", subtitle = "查看所有下载的壁纸", onClick = onDownloadsClick
            )

            FeatureItem(
                icon = Icons.Default.Refresh, title = "自动更换壁纸", subtitle = "设置自动更换壁纸的频率和来源", onClick = onAutoChangeClick
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )

            FeatureItem(
                icon = Icons.Default.Settings, title = "设置", subtitle = "调整应用偏好和通知", onClick = onSettingsClick
            )

            FeatureItem(
                icon = Icons.Default.Star, title = "评分与反馈", subtitle = "帮助我们改进应用", onClick = onFeedbackClick
            )

            FeatureItem(
                icon = Icons.Default.Info, title = "关于与致谢", subtitle = "查看应用信息和版权", onClick = onAboutClick
            )

            // 开发者模式下显示测试工具入口
            if (isDebugMode) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )

                FeatureItem(
                    icon = Icons.Default.Build, title = "测试工具", subtitle = "测试API接口和其他功能", onClick = onTestToolsClick
                )
            }
        }
    }
}

/**
 * 用户信息头部
 */
@Composable
private fun MineHeader(
    username: String, isPremiumUser: Boolean, modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier
            .fillMaxWidth()
            .padding(top = 32.dp, bottom = 16.dp)
    ) {
        // 用户头像
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 用户名
        Text(
            text = username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
        )

        // 会员状态
        if (isPremiumUser) {
            Text(
                text = "高级会员",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * 升级横幅
 */
@Composable
private fun UpgradeBanner(
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ), onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF8E2DE2), Color(0xFF4A00E0)
                        )
                    )
                )
                .padding(16.dp), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✨ 解锁全部特权，畅享高清视界 ✨",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

/**
 * 功能项
 */
@Composable
private fun FeatureItem(
    icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick, color = Color.Transparent, modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 16.dp)
                )

                Column {
                    Text(
                        text = title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface
                    )

                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MineScreenPreview() {
    VistaraTheme {
        // 注意：预览中不会显示真实数据，因为没有提供真实的ViewModel
        // 这里只是UI预览
        MineScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun MineScreenPremiumPreview() {
    VistaraTheme {
        // 注意：预览中不会显示真实数据，因为没有提供真实的ViewModel
        // 这里只是UI预览，手动传入isPremiumUser参数
        MineScreen()
    }
}
