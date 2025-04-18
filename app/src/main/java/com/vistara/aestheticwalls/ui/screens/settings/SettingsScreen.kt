package com.vistara.aestheticwalls.ui.screens.settings

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.ui.components.ConfirmDialog
import com.vistara.aestheticwalls.ui.components.LanguageSelector
import com.vistara.aestheticwalls.ui.icons.AppIcons
import com.vistara.aestheticwalls.ui.screens.settings.SettingsViewModel.NotificationType
import com.vistara.aestheticwalls.ui.theme.VistaraTheme
import com.vistara.aestheticwalls.ui.theme.stringResource

/**
 * 设置页面
 * 用户可以在这里调整应用的各种设置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    // 从ViewModel获取状态
    val darkTheme by viewModel.darkTheme.collectAsState()
    val dynamicColors by viewModel.dynamicColors.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val showDownloadNotification by viewModel.showDownloadNotification.collectAsState()
    val showWallpaperChangeNotification by viewModel.showWallpaperChangeNotification.collectAsState()
    val downloadOriginalQuality by viewModel.downloadOriginalQuality.collectAsState()
    val cacheSize by viewModel.cacheSize.collectAsState()
    val isClearingCache by viewModel.isClearingCache.collectAsState()
    val appVersion by viewModel.appVersion.collectAsState()
    val needNotificationPermission by viewModel.needNotificationPermission.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isLoggingOut by viewModel.isLoggingOut.collectAsState()
    val operationResult by viewModel.operationResult.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 当前请求的通知类型
    var currentNotificationType by remember { mutableStateOf<NotificationType?>(null) }

    // 权限请求器
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 权限已授予，继续开启通知
            currentNotificationType?.let { viewModel.onNotificationPermissionGranted(it) }
        } else {
            // 权限被拒绝
            viewModel.onNotificationPermissionDenied()
        }
        currentNotificationType = null
    }

    // 监听权限请求状态
    LaunchedEffect(needNotificationPermission) {
        if (needNotificationPermission) {
            // 请求通知权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // 对话框状态
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    // 显示操作结果
    LaunchedEffect(operationResult) {
        operationResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearOperationResult()
        }
    }

    // 监听语言更新状态
    val languageUpdated by viewModel.languageUpdated.collectAsState()
    // 监听语言更新状态，触发 UI 刷新
    LaunchedEffect(languageUpdated) {
        if (languageUpdated) {
            Log.d("SettingsScreen", "语言更新触发，当前语言: ${viewModel.appLanguage.value}")
            try {
                val config = context.resources.configuration
                val settings = viewModel.appLanguage.value
                if (settings != com.vistara.aestheticwalls.data.model.AppLanguage.SYSTEM) {
                    // 设置指定语言
                    val locale = java.util.Locale(settings.code)
                    val localeList = android.os.LocaleList(locale)
                    config.setLocales(localeList)
                } else {
                    // 直接使用 LocaleManager 的强制系统语言方法
                    val localeManager = viewModel.getLocaleManager()

                    // 获取真正的系统语言用于更新资源配置
                    var systemLocale = localeManager.getSystemLocale()
                    // 处理英语区域设置，将 en_US 转换为 en
                    if (systemLocale.language == "en") {
                        // 创建一个新的只有语言代码的 Locale
                        systemLocale = java.util.Locale("en")
                        Log.d("SettingsScreen", "处理英语区域设置，转换为: $systemLocale")
                    }

                    // 更新资源配置
                    val systemLocaleList = android.os.LocaleList(systemLocale)
                    config.setLocales(systemLocaleList)
                }

                // 强制刷新页面，特别是对系统语言切换
                Log.d("SettingsScreen", "系统语言切换，强制刷新页面")
            } catch (e: Exception) {
                Log.e("SettingsScreen", "Error updating resources: ${e.message}")
            }
        }
    }

    // 使用 refreshTrigger 强制重组整个页面
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        TopAppBar(title = {
            Text(stringResource(R.string.settings))
        }, navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        })
    }) { paddingValues ->
        // 使用 refreshTrigger 强制重组整个页面内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            // 主题设置
            SettingsCategory(title = stringResource(R.string.theme_settings))

            SettingsToggleItem(
                icon = AppIcons.DarkMode,
                title = stringResource(R.string.dark_theme),
                subtitle = stringResource(R.string.dark_theme_desc),
                checked = darkTheme,
                onCheckedChange = { viewModel.updateDarkTheme(it) })

            SettingsToggleItem(
                icon = AppIcons.Palette,
                title = stringResource(R.string.dynamic_colors),
                subtitle = stringResource(R.string.dynamic_colors_desc),
                checked = dynamicColors,
                onCheckedChange = { viewModel.updateDynamicColors(it) })

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // 语言设置
            SettingsCategory(title = stringResource(R.string.language_settings))

            LanguageSelector(
                currentLanguage = appLanguage,
                onLanguageSelected = { viewModel.updateAppLanguage(it) })

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // 通知设置
            SettingsCategory(title = stringResource(R.string.notification_settings))

            SettingsToggleItem(
                icon = AppIcons.Notifications,
                title = stringResource(R.string.download_notification),
                subtitle = stringResource(R.string.download_notification_desc),
                checked = showDownloadNotification,
                onCheckedChange = {
                    currentNotificationType = NotificationType.DOWNLOAD
                    viewModel.updateShowDownloadNotification(it)
                })

            SettingsToggleItem(
                icon = AppIcons.Notifications,
                title = stringResource(R.string.wallpaper_change_notification),
                subtitle = stringResource(R.string.wallpaper_change_notification_desc),
                checked = showWallpaperChangeNotification,
                onCheckedChange = {
                    currentNotificationType = NotificationType.WALLPAPER_CHANGE
                    viewModel.updateShowWallpaperChangeNotification(it)
                })

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // 下载设置
            SettingsCategory(title = stringResource(R.string.download_settings))

            SettingsToggleItem(
                icon = AppIcons.HighQuality,
                title = stringResource(R.string.original_quality),
                subtitle = stringResource(R.string.original_quality_desc),
                checked = downloadOriginalQuality,
                onCheckedChange = {
                    viewModel.updateDownloadOriginalQuality(it)
                })

            SettingsActionItem(
                icon = AppIcons.Delete,
                title = stringResource(R.string.clear_cache),
                subtitle = stringResource(R.string.current_cache_size, cacheSize),
                onClick = {
                    showClearCacheDialog = true
                },
                trailingContent = if (isClearingCache) {
                    {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .width(24.dp)
                                .height(24.dp)
                        )
                    }
                } else null)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // 关于应用
            SettingsCategory(title = stringResource(R.string.about_app))

            SettingsActionItem(
                icon = AppIcons.Version,
                title = stringResource(R.string.app_version),
                subtitle = appVersion,
                onClick = {})

            // 账户设置
            if (isLoggedIn) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )

                SettingsCategory(title = stringResource(R.string.account_settings))

                SettingsActionItem(
                    icon = AppIcons.ExitToApp,
                    title = stringResource(R.string.sign_out),
                    subtitle = stringResource(R.string.sign_out_desc),
                    onClick = { showLogoutConfirmDialog = true },
                    iconTint = MaterialTheme.colorScheme.error,
                    trailingContent = if (isLoggingOut) {
                        {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(24.dp)
                            )
                        }
                    } else null)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 清除缓存对话框
            if (showClearCacheDialog) {
                ConfirmDialog(
                    onDismiss = { showClearCacheDialog = false },
                    onConfirm = {
                        viewModel.clearCache()
                        showClearCacheDialog = false
                    },
                    title = stringResource(R.string.clear_cache_title),
                    message = stringResource(R.string.clear_cache_message),
                    confirmText = stringResource(R.string.clear),
                    dismissText = stringResource(R.string.cancel),
                    isLoading = isClearingCache
                )
            }

            // 退出登录确认对话框
            if (showLogoutConfirmDialog) {
                ConfirmDialog(
                    onDismiss = { showLogoutConfirmDialog = false },
                    onConfirm = {
                        viewModel.signOut()
                        showLogoutConfirmDialog = false
                    },
                    title = stringResource(R.string.sign_out_confirm_title),
                    message = stringResource(R.string.sign_out_confirm_message),
                    confirmText = stringResource(R.string.sign_out),
                    dismissText = stringResource(R.string.cancel),
                    isLoading = isLoggingOut
                )
            }
        }
    }
}

/**
 * 设置类别标题
 */
@Composable
private fun SettingsCategory(
    title: String, modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * 设置开关项
 */
@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(), onClick = { onCheckedChange(!checked) }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = checked, onCheckedChange = onCheckedChange
            )
        }
    }
}

/**
 * 设置操作项
 */
@Composable
private fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(), onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon, contentDescription = null, tint = iconTint
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    VistaraTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // 注意：预览中不会显示真实数据，因为没有提供真实的ViewModel
            // 这里只是UI预览
            SettingsScreen(onBackPressed = {})
        }
    }
}
