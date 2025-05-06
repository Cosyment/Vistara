package com.vistara.aestheticwalls.ui.screens.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import com.vistara.aestheticwalls.ui.icons.AppIcons
import com.vistara.aestheticwalls.ui.theme.stringResource
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
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vistara.aestheticwalls.ui.theme.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.ui.components.LoginPromptDialog
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
    onDiamondClick: () -> Unit = {},
    onTestToolsClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    viewModel: MineViewModel = hiltViewModel()
) {
    // 从ViewModel获取状态
    val username by viewModel.username.collectAsState()
    val userPhotoUrl by viewModel.userPhotoUrl.collectAsState()
    val isPremiumUser by viewModel.isPremiumUser.collectAsState()
    val diamondBalance by viewModel.diamondBalance.collectAsState()
    val isDebugMode by viewModel.isDebugMode.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val needLoginAction by viewModel.needLoginAction.collectAsState()

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

    // 登录提示对话框
    needLoginAction?.let { action ->
        val message = when (action) {
            MineViewModel.LoginAction.FAVORITES -> stringResource(R.string.favorites_login_required)
            MineViewModel.LoginAction.DOWNLOADS -> stringResource(R.string.downloads_login_required)
            MineViewModel.LoginAction.AUTO_WALLPAPER -> stringResource(R.string.auto_wallpaper_login_required)
        }

        LoginPromptDialog(
            onDismiss = { viewModel.clearNeedLoginAction() }, onConfirm = {
            viewModel.clearNeedLoginAction()
            onLoginClick()
        }, message = message
        )
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
                username = username,
                userPhotoUrl = userPhotoUrl,
                isPremiumUser = isPremiumUser,
                diamondBalance = diamondBalance,
                isLoggedIn = isLoggedIn,
                onLoginClick = onLoginClick,
                onDiamondClick = onDiamondClick
            )

            // 升级横幅
            if (!isPremiumUser && isLoggedIn) {
                PremiumBanner(
                    onClick = {
                        // 调用ViewModel的升级方法
//                        viewModel.upgradeToPremium()
                        onUpgradeClick()
                    }, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 功能列表
            FeatureItem(
                icon = AppIcons.Favorite,
                title = stringResource(R.string.my_favorites),
                subtitle = stringResource(R.string.my_favorites_desc),
                onClick = {
                    viewModel.checkLoginAndExecute(MineViewModel.LoginAction.FAVORITES) {
                        onFavoritesClick()
                    }
                })

            FeatureItem(
                icon = AppIcons.Download,
                title = stringResource(R.string.my_downloads),
                subtitle = stringResource(R.string.my_downloads_desc),
                onClick = {
                    viewModel.checkLoginAndExecute(MineViewModel.LoginAction.DOWNLOADS) {
                        onDownloadsClick()
                    }
                })

            FeatureItem(
                icon = AppIcons.Refresh,
                title = stringResource(R.string.auto_change_wallpaper),
                subtitle = stringResource(R.string.auto_change_wallpaper_desc),
                onClick = {
                    viewModel.checkLoginAndExecute(MineViewModel.LoginAction.AUTO_WALLPAPER) {
                        onAutoChangeClick()
                    }
                })

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )

            FeatureItem(
                icon = AppIcons.Settings,
                title = stringResource(R.string.settings),
                subtitle = stringResource(R.string.settings_desc),
                onClick = onSettingsClick
            )

            FeatureItem(
                icon = AppIcons.Star,
                title = stringResource(R.string.rate_feedback),
                subtitle = stringResource(R.string.rate_feedback_desc),
                onClick = onFeedbackClick
            )

            FeatureItem(
                icon = AppIcons.Info,
                title = stringResource(R.string.about_credits),
                subtitle = stringResource(R.string.about_credits_desc),
                onClick = onAboutClick
            )

            // 开发者模式下显示测试工具入口
            if (isDebugMode) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )

                FeatureItem(
                    icon = AppIcons.Build,
                    title = stringResource(R.string.test_tools),
                    subtitle = stringResource(R.string.test_tools_desc),
                    onClick = onTestToolsClick
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
    username: String,
    userPhotoUrl: String?,
    isPremiumUser: Boolean,
    diamondBalance: Int,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit = {},
    onDiamondClick: () -> Unit = {},
    modifier: Modifier = Modifier
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
                .size(if (isPremiumUser) 84.dp else 80.dp)
        ) {
            // 高级用户外层光晕效果
            if (isPremiumUser) {
                // 流光动画效果
                val infiniteTransition = rememberInfiniteTransition(label = "premium_avatar_animation")

                // 旋转动画 - 更快的旋转速度
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(8000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotation_animation"
                )

                // 闪光动画 - 更明显的透明度变化
                val shimmerAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "shimmer_animation"
                )

                // 流光位置动画 - 限制在头像区域内
                val shimmerOffset by infiniteTransition.animateFloat(
                    initialValue = -80f,
                    targetValue = 80f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "shimmer_offset_animation"
                )
//                FxkOtt
                // 外层光晕
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .graphicsLayer {
                            this.rotationZ = rotation
                        }
                        .background(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color(0xFFFFD700).copy(alpha = shimmerAlpha), // 金色
                                    Color(0xFFFFA500).copy(alpha = 0.3f), // 橙色
                                    Color(0xFFFFD700).copy(alpha = shimmerAlpha), // 金色
                                    Color(0xFFFFC107).copy(alpha = 0.3f), // 浅金色
                                    Color(0xFFFFD700).copy(alpha = shimmerAlpha)  // 金色
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // 流光效果 - 限制在头像区域内
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)  // 先裁剪成圆形
                        .graphicsLayer {
                            translationX = shimmerOffset
                        }
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.0f),
                                    Color.White.copy(alpha = 0.4f),
                                    Color.White.copy(alpha = 0.0f)
                                ),
                                startX = -40f,
                                endX = 40f
                            )
                        )
                )
            }

            // 头像主体
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = if (isPremiumUser) 1.5.dp else 2.dp,
                        brush = if (isPremiumUser) {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFFD700), // 金色
                                    Color(0xFFFFA500), // 橙色
                                    Color(0xFFFF8C00)  // 深橙色
                                )
                            )
                        } else {
                            SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        },
                        shape = CircleShape
                    )
            ) {
                if (isLoggedIn && !userPhotoUrl.isNullOrEmpty()) {
                    // 使用 AsyncImage 加载用户头像
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(userPhotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // 默认头像图标
                    Icon(
                        imageVector = AppIcons.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // 高级用户小皇冠标识
            if (isPremiumUser) {
                // 微妙的缩放动画
                val infiniteTransition = rememberInfiniteTransition(label = "crown_animation")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "crown_scale_animation"
                )

                // 微妙的旋转动画
                val crownRotation by infiniteTransition.animateFloat(
                    initialValue = -5f,
                    targetValue = 5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "crown_rotation_animation"
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD700), // 金色
                                    Color(0xFFFFA500)  // 橙色
                                )
                            ),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = AppIcons.Crown,
                        contentDescription = stringResource(R.string.premium_user),
                        tint = Color.White,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.Center)
                            .graphicsLayer {
                                rotationZ = crownRotation
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoggedIn) {
            // 用户名
            Text(
                text = username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
            )

            // 钻石余额
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable(onClick = onDiamondClick),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.Diamond,
                        contentDescription = "Diamond Balance",
                        tint = Color(0xFF00BCD4),
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = diamondBalance.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // 会员状态
            if (isPremiumUser) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(top = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFFD700), // 金色
                                        Color(0xFFFFA500), // 橙色
                                        Color(0xFFFF8C00)  // 深橙色
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.7f),
                                        Color.White.copy(alpha = 0.2f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = AppIcons.Crown,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text(
                                text = stringResource(R.string.premium_user),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            // 未登录状态
            Text(
                text = stringResource(R.string.not_logged_in), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 登录按钮
            Card(
                onClick = onLoginClick, colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ), shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.login),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * 升级横幅
 */
@Composable
private fun PremiumBanner(
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
                text = stringResource(R.string.premium_banner),
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
