package com.vistara.aestheticwalls.ui.screens.autochange

import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vistara.aestheticwalls.BuildConfig
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.AutoChangeFrequency
import com.vistara.aestheticwalls.data.model.AutoChangeSource
import com.vistara.aestheticwalls.data.model.WallpaperTarget
import com.vistara.aestheticwalls.ui.components.LoginPromptDialog
import com.vistara.aestheticwalls.ui.icons.AppIcons
import com.vistara.aestheticwalls.ui.theme.VistaraTheme

/**
 * 自动更换壁纸设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoChangeScreen(
    onBackPressed: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    viewModel: AutoChangeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    // 从ViewModel获取状态
    val autoChangeEnabled by viewModel.autoChangeEnabled.collectAsState()
    val autoChangeFrequency by viewModel.autoChangeFrequency.collectAsState()
    val autoChangeWifiOnly by viewModel.autoChangeWifiOnly.collectAsState()
    val autoChangeSource by viewModel.autoChangeSource.collectAsState()
    val autoChangeTarget by viewModel.autoChangeTarget.collectAsState()
    val isPremiumUser by viewModel.isPremiumUser.collectAsState()
    val isChangingWallpaper by viewModel.isChangingWallpaper.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val needLogin by viewModel.needLogin.collectAsState()
    val settingsApplied by viewModel.settingsApplied.collectAsState()

    // 登录提示对话框
    if (needLogin) {
        LoginPromptDialog(
            onDismiss = {
            viewModel.clearNeedLogin()
            onBackPressed()
        }, onConfirm = {
            viewModel.clearNeedLogin()
            onNavigateToLogin()
        }, message = stringResource(R.string.auto_wallpaper_login_required)
        )
    }

    // 当设置应用成功时返回上级页面
    LaunchedEffect(settingsApplied) {
        if (settingsApplied) {
            // 显示成功提示
            Toast.makeText(context, R.string.settings_applied_successfully, Toast.LENGTH_SHORT)
                .show()
            // 返回上级页面
            onBackPressed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.auto_change_wallpaper)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                })
        }) { paddingValues ->
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
                            text = stringResource(R.string.auto_change_wallpaper),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.auto_change_wallpaper_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoChangeEnabled,
                        onCheckedChange = { viewModel.updateAutoChangeEnabled(it) })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 只有在启用自动更换时才显示设置选项
            if (autoChangeEnabled) {
                Text(
                    text = stringResource(R.string.change_settings),
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
                    icon = AppIcons.Wifi,
                    title = stringResource(R.string.wifi_only_change),
                    subtitle = stringResource(R.string.avoid_mobile_data),
                    checked = autoChangeWifiOnly,
                    onCheckedChange = { viewModel.updateAutoChangeWifiOnly(it) })

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )

                // 壁纸来源
                Text(
                    text = stringResource(R.string.wallpaper_source),
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
                    text = stringResource(R.string.wallpaper_target),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                TargetSelector(
                    currentTarget = autoChangeTarget,
                    onTargetSelected = { viewModel.updateAutoChangeTarget(it) })

                Spacer(modifier = Modifier.height(24.dp))

                // 应用设置按钮
                Button(
                    onClick = { viewModel.applyAutoChangeSettings() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isChangingWallpaper
                ) {
                    Text(stringResource(R.string.apply_settings))
                }

                // 测试解锁广播按钮（仅当频率设置为每次解锁时显示）
                if (BuildConfig.IS_DEV_MODE && autoChangeFrequency == AutoChangeFrequency.EACH_UNLOCK && isPremiumUser) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.testUnlockBroadcast(context) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(stringResource(R.string.test_unlock_broadcast))
                    }
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
        onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = AppIcons.Frequency,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.change_frequency),
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
                expanded = expanded, onDismissRequest = { expanded = false }) {
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
                                    text = stringResource(R.string.premium),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }, onClick = {
                        if (isEnabled) {
                            onFrequencySelected(frequency)
                            expanded = false
                        }
                    }, trailingIcon = {
                        if (frequency == currentFrequency) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }, enabled = isEnabled
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
        onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = AppIcons.AutoChange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.wallpaper_source),
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
                expanded = expanded, onDismissRequest = { expanded = false }) {
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
                                    text = stringResource(R.string.premium),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }, onClick = {
                        if (isEnabled) {
                            onSourceSelected(source)
                            expanded = false
                        }
                    }, trailingIcon = {
                        if (source == currentSource) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }, enabled = isEnabled
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
        modifier = modifier.fillMaxWidth(), onClick = { onCheckedChange(!checked) }) {
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
                checked = checked, onCheckedChange = onCheckedChange
            )
        }
    }
}

/**
 * 获取频率文本
 */
@Composable
private fun getFrequencyText(frequency: AutoChangeFrequency): String {
    return when (frequency) {
        AutoChangeFrequency.DAILY -> stringResource(R.string.daily)
        AutoChangeFrequency.TWELVE_HOURS -> stringResource(R.string.every_twelve_hours)
        AutoChangeFrequency.SIX_HOURS -> stringResource(R.string.every_six_hours)
        AutoChangeFrequency.HOURLY -> stringResource(R.string.hourly)
        AutoChangeFrequency.EACH_UNLOCK -> stringResource(R.string.each_unlock)
    }
}

/**
 * 获取来源文本
 */
@Composable
private fun getSourceText(source: AutoChangeSource): String {
    return when (source) {
        AutoChangeSource.FAVORITES -> stringResource(R.string.my_favorites)
        AutoChangeSource.DOWNLOADED -> stringResource(R.string.downloaded_wallpapers)
        AutoChangeSource.CATEGORY -> stringResource(R.string.specific_category)
        AutoChangeSource.TRENDING -> stringResource(R.string.trending_wallpapers)
    }
}

/**
 * 目标屏幕选择器
 */
@Composable
private fun TargetSelector(
    currentTarget: WallpaperTarget, onTargetSelected: (WallpaperTarget) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = AppIcons.WallpaperTarget,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.wallpaper_target),
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
                expanded = expanded, onDismissRequest = { expanded = false }) {
                WallpaperTarget.values().forEach { target ->
                    DropdownMenuItem(text = { Text(getTargetText(target)) }, onClick = {
                        onTargetSelected(target)
                        expanded = false
                    }, trailingIcon = {
                        if (target == currentTarget) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    })
                }
            }
        }
    }
}

/**
 * 获取目标文本
 */
@Composable
private fun getTargetText(target: WallpaperTarget): String {
    return when (target) {
        WallpaperTarget.HOME -> stringResource(R.string.home_screen_only)
        WallpaperTarget.LOCK -> stringResource(R.string.lock_screen_only)
        WallpaperTarget.BOTH -> stringResource(R.string.home_and_lock_screen)
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
