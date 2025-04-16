package com.vistara.aestheticwalls.ui.screens.settings

import android.Manifest
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vistara.aestheticwalls.ui.screens.settings.SettingsViewModel.NotificationType
import com.vistara.aestheticwalls.ui.theme.VistaraTheme

/**
 * 设置页面
 * 用户可以在这里调整应用的各种设置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    // 从ViewModel获取状态
    val darkTheme by viewModel.darkTheme.collectAsState()
    val dynamicColors by viewModel.dynamicColors.collectAsState()
    val showDownloadNotification by viewModel.showDownloadNotification.collectAsState()
    val showWallpaperChangeNotification by viewModel.showWallpaperChangeNotification.collectAsState()
    val downloadOriginalQuality by viewModel.downloadOriginalQuality.collectAsState()
    val cacheSize by viewModel.cacheSize.collectAsState()
    val isClearingCache by viewModel.isClearingCache.collectAsState()
    val appVersion by viewModel.appVersion.collectAsState()
    val needNotificationPermission by viewModel.needNotificationPermission.collectAsState()

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
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // 对话框状态
    var showClearCacheDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            // 主题设置
            SettingsCategory(title = "主题设置")

            SettingsToggleItem(
                icon = Icons.Default.Info,
                title = "深色主题",
                subtitle = "启用应用深色主题",
                checked = darkTheme,
                onCheckedChange = { viewModel.updateDarkTheme(it) }
            )

            SettingsToggleItem(
                icon = Icons.Default.Settings,
                title = "动态颜色",
                subtitle = "使用系统动态颜色（仅Android 12+）",
                checked = dynamicColors,
                onCheckedChange = { viewModel.updateDynamicColors(it) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // 通知设置
            SettingsCategory(title = "通知设置")

            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "下载通知",
                subtitle = "显示壁纸下载完成通知",
                checked = showDownloadNotification,
                onCheckedChange = {
                    currentNotificationType = NotificationType.DOWNLOAD
                    viewModel.updateShowDownloadNotification(it)
                }
            )

            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "壁纸更换通知",
                subtitle = "显示自动壁纸更换通知",
                checked = showWallpaperChangeNotification,
                onCheckedChange = {
                    currentNotificationType = NotificationType.WALLPAPER_CHANGE
                    viewModel.updateShowWallpaperChangeNotification(it)
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // 下载设置
            SettingsCategory(title = "下载设置")

            SettingsToggleItem(
                icon = Icons.Default.Settings,
                title = "原始质量",
                subtitle = "下载原始质量的壁纸（较大文件）",
                checked = downloadOriginalQuality,
                onCheckedChange = { viewModel.updateDownloadOriginalQuality(it) }
            )

            SettingsActionItem(
                icon = Icons.Default.Delete,
                title = "清除缓存",
                subtitle = "当前缓存大小: $cacheSize",
                onClick = { showClearCacheDialog = true },
                trailingContent = if (isClearingCache) {
                    { CircularProgressIndicator(modifier = Modifier.width(24.dp).height(24.dp)) }
                } else null
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // 关于应用
            SettingsCategory(title = "关于应用")

            SettingsActionItem(
                icon = Icons.Default.Info,
                title = "应用版本",
                subtitle = "$appVersion",
                onClick = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 清除缓存对话框
            if (showClearCacheDialog) {
                AlertDialog(
                    onDismissRequest = { showClearCacheDialog = false },
                    title = { Text("清除缓存") },
                    text = { Text("确定要清除应用缓存吗？这将删除所有缓存的图片和数据。") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.clearCache()
                                showClearCacheDialog = false
                            },
                            enabled = !isClearingCache
                        ) {
                            Text("清除")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showClearCacheDialog = false }
                        ) {
                            Text("取消")
                        }
                    }
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
    title: String,
    modifier: Modifier = Modifier
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
        modifier = modifier.fillMaxWidth(),
        onClick = { onCheckedChange(!checked) }
    ) {
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
                checked = checked,
                onCheckedChange = onCheckedChange
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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
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
