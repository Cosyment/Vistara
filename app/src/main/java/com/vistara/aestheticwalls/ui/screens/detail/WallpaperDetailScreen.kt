package com.vistara.aestheticwalls.ui.screens.detail

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.vistara.aestheticwalls.BuildConfig
import com.vistara.aestheticwalls.billing.BillingConnectionState
import com.vistara.aestheticwalls.data.model.WallpaperTarget
import com.vistara.aestheticwalls.ui.components.PremiumWallpaperPrompt
import com.vistara.aestheticwalls.ui.components.WallpaperDetail
import com.vistara.aestheticwalls.ui.components.WallpaperSetOptions
import com.vistara.aestheticwalls.data.model.UiState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart

/**
 * 壁纸详情页面
 * 显示壁纸详情，提供收藏、下载、分享、设置壁纸等功能
 * 采用沉浸式设计，图片全屏显示
 */
@Composable
fun WallpaperDetailScreen(
    onBackPressed: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: WallpaperDetailViewModel = hiltViewModel()
) {
    val wallpaperState by viewModel.wallpaperState.collectAsState()
    val isFavorite by viewModel.isFavorite
    val isPremiumUser by viewModel.isPremiumUser
    val showSetWallpaperOptions by viewModel.showSetWallpaperOptions
    val showPremiumPrompt by viewModel.showPremiumPrompt
    val isDownloading by viewModel.isDownloading
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val isInfoExpanded by viewModel.isInfoExpanded
    val needStoragePermission by viewModel.needStoragePermission
    val upgradeResult by viewModel.upgradeResult.collectAsState()
    val billingConnectionState by viewModel.billingConnectionState.collectAsState()

    val context = LocalContext.current
    val activity = LocalActivity.current
    val coroutineScope = rememberCoroutineScope()

    // 创建SnackbarHostState
    val snackbarHostState = remember { SnackbarHostState() }

    // 处理升级结果
    LaunchedEffect(upgradeResult) {
        upgradeResult?.let { result ->
            when (result) {
                is WallpaperDetailViewModel.UpgradeResult.Success -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(result.message)
                    }
                }
                is WallpaperDetailViewModel.UpgradeResult.Error -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(result.message)
                    }
                }
            }
            // 清除升级结果，避免重复显示
            viewModel.clearUpgradeResult()
        }
    }

    // 权限请求器
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 权限已授予，继续下载
            viewModel.continueDownloadAfterPermissionGranted()
            Toast.makeText(context, "开始下载壁纸", Toast.LENGTH_SHORT).show()
        } else {
            // 权限被拒绝
            Toast.makeText(context, "无法下载壁纸：存储权限被拒绝", Toast.LENGTH_SHORT).show()
        }
    }

    // 当页面变为可见时，刷新编辑后的图片
    LaunchedEffect(Unit) {
        viewModel.refreshEditedImage()
    }

    // 设置沉浸式状态栏和导航栏
    val systemUiController = rememberSystemUiController()

    // 使用LaunchedEffect确保系统栏设置在每次重组时都生效
    LaunchedEffect(Unit) {
        // 设置状态栏和导航栏为完全透明
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = false // 使用白色图标，因为背景可能是深色
        )
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = false
        )

        // 设置系统栏可见性
        systemUiController.systemBarsDarkContentEnabled = false
    }

    // 使用Scaffold作为根布局，可以更好地控制浮动按钮
    androidx.compose.material3.Scaffold(
        floatingActionButton = {
            // 开发模式下显示测试支付按钮
            if (BuildConfig.IS_DEV_MODE) {
                FloatingActionButton(
                    onClick = {
                        // 调用测试支付方法
                        viewModel.testPayment(activity)
                        // 显示提示
                        Toast.makeText(context, "正在测试支付...", Toast.LENGTH_SHORT).show()
                    },
                    containerColor = MaterialTheme.colorScheme.error, // 使用错误颜色，更加醒目
                    contentColor = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(72.dp) // 增大按钮尺寸，更容易点击
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "测试支付",
                        modifier = Modifier.size(32.dp) // 增大图标尺寸
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        // 设置Scaffold的内容颜色为透明，确保不影响背景
        containerColor = Color.Transparent,
        contentColor = Color.White,
        // 移除所有的内容填充，确保全屏效果
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        // 使用Box作为内容区域，不添加填充，保持全屏效果
        Box(modifier = Modifier.fillMaxSize()) {
        when (wallpaperState) {
            is UiState.Loading -> {
                // 显示加载中
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is UiState.Error -> {
                // 显示错误信息
                Text(
                    text = (wallpaperState as UiState.Error).message ?: "加载失败",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is UiState.Success -> {
                // 显示壁纸详情
                val wallpaper = (wallpaperState as UiState.Success).data

                // 获取编辑后的图片
                val editedBitmap by viewModel.editedBitmap

                WallpaperDetail(
                    wallpaper = wallpaper,
                    isFavorite = isFavorite,
                    isInfoExpanded = isInfoExpanded,
                    isDownloading = isDownloading,
                    downloadProgress = downloadProgress,
                    onBackPressed = onBackPressed,
                    onToggleFavorite = { viewModel.toggleFavorite() },
                    onToggleInfo = { viewModel.toggleInfoExpanded() },
                    onSetWallpaper = { viewModel.showSetWallpaperOptions() },
                    onDownload = {
                        viewModel.downloadWallpaper()
                        Toast.makeText(context, "开始下载壁纸", Toast.LENGTH_SHORT).show()
                    },
                    onShare = { viewModel.shareWallpaper() },
                    onEdit = {
                        val wallpaperId = wallpaper.id
                        if (wallpaper.isPremium && !isPremiumUser) {
                            viewModel.showPremiumPrompt()
                        } else {
                            onNavigateToEdit(wallpaperId)
                        }
                    },
                    isPremiumUser = isPremiumUser,
                    editedBitmap = editedBitmap
                )

                // 设置壁纸选项对话框 - 使用半透明背景
                if (showSetWallpaperOptions) {
                    Dialog(
                        onDismissRequest = { viewModel.hideSetWallpaperOptions() },
                        properties = DialogProperties(
                            dismissOnBackPress = true,
                            dismissOnClickOutside = true,
                            usePlatformDefaultWidth = false
                        )
                    ) {
                        WallpaperSetOptions(
                            onSetHomeScreen = {
                                // 先显示提示，然后设置壁纸
                                Toast.makeText(context, "正在设置主屏幕壁纸...", Toast.LENGTH_SHORT).show()
                                viewModel.setWallpaper(context, WallpaperTarget.HOME)
                                // 在后台完成设置后再显示成功提示
                                coroutineScope.launch {
                                    delay(500) // 等待一下，避免提示重叠
                                    Toast.makeText(context, "已设置为主屏幕壁纸", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onSetLockScreen = {
                                // 先显示提示，然后设置壁纸
                                Toast.makeText(context, "正在设置锁屏壁纸...", Toast.LENGTH_SHORT).show()
                                viewModel.setWallpaper(context, WallpaperTarget.LOCK)
                                // 在后台完成设置后再显示成功提示
                                coroutineScope.launch {
                                    delay(500) // 等待一下，避免提示重叠
                                    Toast.makeText(context, "已设置为锁屏壁纸", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onSetBoth = {
                                // 先显示提示，然后设置壁纸
                                Toast.makeText(context, "正在设置壁纸...", Toast.LENGTH_SHORT).show()
                                viewModel.setWallpaper(context, WallpaperTarget.BOTH)
                                // 在后台完成设置后再显示成功提示
                                coroutineScope.launch {
                                    delay(500) // 等待一下，避免提示重叠
                                    Toast.makeText(context, "已设置为主屏幕和锁屏壁纸", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDismiss = { viewModel.hideSetWallpaperOptions() }
                        )
                    }
                }

                // 高级壁纸提示对话框 - 使用半透明背景
                if (showPremiumPrompt) {
                    Dialog(
                        onDismissRequest = { viewModel.hidePremiumPrompt() },
                        properties = DialogProperties(
                            dismissOnBackPress = true,
                            dismissOnClickOutside = true,
                            usePlatformDefaultWidth = false
                        )
                    ) {
                        PremiumWallpaperPrompt(
                            onUpgrade = {
                                viewModel.upgradeToPremium(activity)
                                viewModel.hidePremiumPrompt()
                            },
                            onDismiss = { viewModel.hidePremiumPrompt() },
                            isConnected = billingConnectionState == BillingConnectionState.CONNECTED
                        )
                    }
                }
            }
        }
    }
    }

    // 存储权限请求对话框
    if (needStoragePermission) {
        AlertDialog(
            onDismissRequest = { /* 不允许通过点击外部关闭 */ },
            title = { Text("需要存储权限") },
            text = { Text("为了将壁纸保存到相册，需要获取存储权限。请允许此权限以便下载壁纸。") },
            confirmButton = {
                Button(onClick = { permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE) }) {
                    Text("授予权限")
                }
            },
            dismissButton = {
                Button(onClick = {
                    Toast.makeText(context, "无法下载壁纸：需要存储权限", Toast.LENGTH_SHORT).show()
                    viewModel.resetPermissionRequest()
                }) {
                    Text("取消")
                }
            }
        )
    }
}
