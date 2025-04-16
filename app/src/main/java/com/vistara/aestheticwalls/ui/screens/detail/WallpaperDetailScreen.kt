package com.vistara.aestheticwalls.ui.screens.detail

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.WallpaperTarget
import com.vistara.aestheticwalls.ui.components.WallpaperDetail
import com.vistara.aestheticwalls.ui.components.WallpaperSetOptions
import kotlinx.coroutines.launch

/**
 * 壁纸详情页面
 * 显示壁纸详情，提供收藏、下载、分享、设置壁纸等功能
 * 采用沉浸式设计，图片全屏显示
 */
@Composable
fun WallpaperDetailScreen(
    onBackPressed: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToUpgrade: () -> Unit = {},
    viewModel: WallpaperDetailViewModel = hiltViewModel()
) {
    val wallpaperState by viewModel.wallpaperState.collectAsState()
    val isFavorite by viewModel.isFavorite
    val isPremiumUser by viewModel.isPremiumUser
    val showSetWallpaperOptions by viewModel.showSetWallpaperOptions
    val navigateToUpgrade by viewModel.navigateToUpgrade
    val isDownloading by viewModel.isDownloading
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val isInfoExpanded by viewModel.isInfoExpanded
    val needStoragePermission by viewModel.needStoragePermission
    val upgradeResult by viewModel.upgradeResult.collectAsState()

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

    // 监听导航到升级页面的状态
    LaunchedEffect(navigateToUpgrade) {
        if (navigateToUpgrade) {
            onNavigateToUpgrade()
            viewModel.resetNavigateToUpgrade()
        }
    }

    // 设置沉浸式状态栏和导航栏
    val systemUiController = rememberSystemUiController()

    // 使用LaunchedEffect确保系统栏设置在每次重组时都生效
    LaunchedEffect(Unit) {
        // 设置状态栏和导航栏为完全透明
        systemUiController.setStatusBarColor(
            color = Color.Transparent, darkIcons = false // 使用白色图标，因为背景可能是深色
        )
        systemUiController.setNavigationBarColor(
            color = Color.Transparent, darkIcons = false
        )

        // 设置系统栏可见性
        systemUiController.systemBarsDarkContentEnabled = false
    }

    // 使用Scaffold作为根布局，可以更好地控制浮动按钮
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        // 设置Scaffold的内容颜色为透明，确保不影响背景
        containerColor = Color.Transparent, contentColor = Color.White,
        // 移除所有的内容填充，确保全屏效果
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        // 使用Box作为内容区域，不添加填充，保持全屏效果
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                        onSetWallpaper = { viewModel.showSetWallpaperOptions(activity) },
                        onDownload = {
                            if (wallpaper.isPremium && !isPremiumUser) {
                                viewModel.showPremiumPrompt()
                            } else {
                                viewModel.downloadWallpaper()
                                Toast.makeText(context, "开始下载壁纸", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onShare = { viewModel.shareWallpaper() },
                        onEdit = {
                            if (wallpaper.isPremium && !isPremiumUser) {
                                viewModel.showPremiumPrompt()
                            } else {
                                val wallpaperId = wallpaper.id
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
                            WallpaperSetOptions(onSetHomeScreen = {
                                viewModel.setWallpaper(activity, WallpaperTarget.HOME)
                            }, onSetLockScreen = {
                                viewModel.setWallpaper(activity, WallpaperTarget.LOCK)
                            }, onSetBoth = {
                                viewModel.setWallpaper(activity, WallpaperTarget.BOTH)
                            }, onDismiss = { viewModel.hideSetWallpaperOptions() })
                        }
                    }

                    // 高级壁纸提示对话框 - 使用半透明背景
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
            })
    }
}
