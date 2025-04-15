package com.vistara.aestheticwalls.ui.screens.settings

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 通知设置
            SettingsCategory(title = "通知设置")

            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "下载通知",
                subtitle = "显示壁纸下载完成通知",
                checked = showDownloadNotification,
                onCheckedChange = { viewModel.updateShowDownloadNotification(it) }
            )

            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "壁纸更换通知",
                subtitle = "显示自动壁纸更换通知",
                checked = showWallpaperChangeNotification,
                onCheckedChange = { viewModel.updateShowWallpaperChangeNotification(it) }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 下载设置
            SettingsCategory(title = "下载设置")

            SettingsToggleItem(
                icon = Icons.Default.Settings,
                title = "原始质量",
                subtitle = "下载原始质量的壁纸（较大文件）",
                checked = downloadOriginalQuality,
                onCheckedChange = { viewModel.updateDownloadOriginalQuality(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))
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
