package com.vistara.aestheticwalls.ui.screens.autochange

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vistara.aestheticwalls.data.model.AutoChangeFrequency
import com.vistara.aestheticwalls.data.model.AutoChangeSource
import com.vistara.aestheticwalls.data.model.WallpaperTarget
import com.vistara.aestheticwalls.ui.theme.VistaraTheme

/**
 * 自动更换壁纸设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoChangeScreen(
    onBackPressed: () -> Unit,
    viewModel: AutoChangeViewModel = hiltViewModel()
) {
    // 从ViewModel获取状态
    val autoChangeEnabled by viewModel.autoChangeEnabled.collectAsState()
    val autoChangeFrequency by viewModel.autoChangeFrequency.collectAsState()
    val autoChangeWifiOnly by viewModel.autoChangeWifiOnly.collectAsState()
    val autoChangeSource by viewModel.autoChangeSource.collectAsState()
    val autoChangeTarget by viewModel.autoChangeTarget.collectAsState()
    val isPremiumUser by viewModel.isPremiumUser.collectAsState()
    val isChangingWallpaper by viewModel.isChangingWallpaper.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("自动更换壁纸") },
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
                .padding(16.dp)
        ) {
            // 启用开关
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "自动更换壁纸",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "定期自动更换手机壁纸",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoChangeEnabled,
                        onCheckedChange = { viewModel.updateAutoChangeEnabled(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 只有在启用自动更换时才显示设置选项
            if (autoChangeEnabled) {
                Text(
                    text = "更换设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 更换频率
                FrequencySelector(
                    currentFrequency = autoChangeFrequency,
                    onFrequencySelected = { viewModel.updateAutoChangeFrequency(it) },
                    isPremiumUser = isPremiumUser
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 仅在WiFi下更换
                SettingsToggleItem(
                    icon = Icons.Default.Settings,
                    title = "仅在WiFi下更换",
                    subtitle = "避免使用移动数据流量",
                    checked = autoChangeWifiOnly,
                    onCheckedChange = { viewModel.updateAutoChangeWifiOnly(it) }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )

                // 壁纸来源
                Text(
                    text = "壁纸来源",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                SourceSelector(
                    currentSource = autoChangeSource,
                    onSourceSelected = { viewModel.updateAutoChangeSource(it) },
                    isPremiumUser = isPremiumUser
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 壁纸目标屏幕
                Text(
                    text = "壁纸目标",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                TargetSelector(
                    currentTarget = autoChangeTarget,
                    onTargetSelected = { viewModel.updateAutoChangeTarget(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 应用设置按钮
                Button(
                    onClick = { viewModel.applyAutoChangeSettings() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isChangingWallpaper
                ) {
                    Text("应用设置")
                }
            }
        }
    }
}

/**
 * 频率选择器
 */
@Composable
private fun FrequencySelector(
    currentFrequency: AutoChangeFrequency,
    onFrequencySelected: (AutoChangeFrequency) -> Unit,
    isPremiumUser: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "更换频率",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = getFrequencyText(currentFrequency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                AutoChangeFrequency.values().forEach { frequency ->
                    val isEnabled = !frequency.isPremium || isPremiumUser
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = getFrequencyText(frequency),
                                    color = if (isEnabled) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                if (frequency.isPremium) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "高级",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        onClick = {
                            if (isEnabled) {
                                onFrequencySelected(frequency)
                                expanded = false
                            }
                        },
                        trailingIcon = {
                            if (frequency == currentFrequency) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        enabled = isEnabled
                    )
                }
            }
        }
    }
}

/**
 * 来源选择器
 */
@Composable
private fun SourceSelector(
    currentSource: AutoChangeSource,
    onSourceSelected: (AutoChangeSource) -> Unit,
    isPremiumUser: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "壁纸来源",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = getSourceText(currentSource),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                AutoChangeSource.values().forEach { source ->
                    val isEnabled = !source.isPremium || isPremiumUser
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = getSourceText(source),
                                    color = if (isEnabled) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                if (source.isPremium) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "高级",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        onClick = {
                            if (isEnabled) {
                                onSourceSelected(source)
                                expanded = false
                            }
                        },
                        trailingIcon = {
                            if (source == currentSource) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        enabled = isEnabled
                    )
                }
            }
        }
    }
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
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 16.dp)
            )

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
 * 获取频率文本
 */
private fun getFrequencyText(frequency: AutoChangeFrequency): String {
    return when (frequency) {
        AutoChangeFrequency.DAILY -> "每天"
        AutoChangeFrequency.TWELVE_HOURS -> "每12小时"
        AutoChangeFrequency.SIX_HOURS -> "每6小时"
        AutoChangeFrequency.HOURLY -> "每小时"
        AutoChangeFrequency.EACH_UNLOCK -> "每次解锁"
    }
}

/**
 * 获取来源文本
 */
private fun getSourceText(source: AutoChangeSource): String {
    return when (source) {
        AutoChangeSource.FAVORITES -> "我的收藏"
        AutoChangeSource.DOWNLOADED -> "已下载壁纸"
        AutoChangeSource.CATEGORY -> "指定分类"
        AutoChangeSource.TRENDING -> "热门壁纸"
    }
}

/**
 * 目标屏幕选择器
 */
@Composable
private fun TargetSelector(
    currentTarget: WallpaperTarget,
    onTargetSelected: (WallpaperTarget) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "壁纸目标",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = getTargetText(currentTarget),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                WallpaperTarget.values().forEach { target ->
                    DropdownMenuItem(
                        text = { Text(getTargetText(target)) },
                        onClick = {
                            onTargetSelected(target)
                            expanded = false
                        },
                        trailingIcon = {
                            if (target == currentTarget) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * 获取目标文本
 */
private fun getTargetText(target: WallpaperTarget): String {
    return when (target) {
        WallpaperTarget.HOME -> "仅主屏幕"
        WallpaperTarget.LOCK -> "仅锁屏"
        WallpaperTarget.BOTH -> "主屏幕和锁屏"
    }
}

@Preview(showBackground = true)
@Composable
fun AutoChangeScreenPreview() {
    VistaraTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // 注意：预览中不会显示真实数据，因为没有提供真实的ViewModel
            // 这里只是UI预览
            AutoChangeScreen(onBackPressed = {})
        }
    }
}
