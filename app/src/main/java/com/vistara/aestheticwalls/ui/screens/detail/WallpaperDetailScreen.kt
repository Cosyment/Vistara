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
import androidx.compose.ui.res.stringResource
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
import com.vistara.aestheticwalls.ui.theme.AppColors
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.vistara.aestheticwalls.R
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.WallpaperTarget
import com.vistara.aestheticwalls.ui.components.LoginPromptDialog
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
    onNavigateToLogin: () -> Unit = {},
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
    val isProcessingWallpaper by viewModel.isProcessingWallpaper
    val wallpaperSetSuccess by viewModel.wallpaperSetSuccess.collectAsState()

    // 登录相关状态
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val needLoginAction by viewModel.needLoginAction.collectAsState()

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

    // 处理壁纸设置成功消息
    LaunchedEffect(wallpaperSetSuccess) {
        wallpaperSetSuccess?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            // 清除成功消息，避免重复显示
            viewModel.clearWallpaperSetSuccess()
        }
    }

    // 权限请求器
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 权限已授予，继续下载
            viewModel.continueDownloadAfterPermissionGranted()
            Toast.makeText(context, R.string.start_download_wallpaper, Toast.LENGTH_SHORT).show()
        } else {
            // 权限被拒绝
            Toast.makeText(context, R.string.download_failed_permission_denied, Toast.LENGTH_SHORT).show()
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
        // 设置Scaffold的内容颜色为黑色，确保壁纸详情页始终使用黑色背景
        containerColor = AppColors.WallpaperDetailBackground, contentColor = Color.White,
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
                        onSetWallpaper = {
                            // 检查登录状态
                            if (!isLoggedIn) {
                                viewModel.setNeedLoginAction(WallpaperDetailViewModel.LoginAction.SET_WALLPAPER)
                                return@WallpaperDetail
                            }
                            viewModel.showSetWallpaperOptions(activity)
                        },
                        onDownload = {
                            if (wallpaper.isPremium && !isPremiumUser) {
                                viewModel.showPremiumPrompt()
                            } else {
                                viewModel.downloadWallpaper()
                                Toast.makeText(context, R.string.start_download_wallpaper, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onShare = { viewModel.shareWallpaper() },
                        onEdit = {
                            // 检查登录状态
                            if (!isLoggedIn) {
                                viewModel.setNeedLoginAction(WallpaperDetailViewModel.LoginAction.EDIT)
                                return@WallpaperDetail
                            }

                            if (wallpaper.isPremium && !isPremiumUser) {
                                viewModel.showPremiumPrompt()
                            } else {
                                val wallpaperId = wallpaper.id
                                onNavigateToEdit(wallpaperId)
                            }
                        },
                        isPremiumUser = isPremiumUser,
                        editedBitmap = editedBitmap,
                        isProcessingWallpaper = isProcessingWallpaper
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
            title = { Text(stringResource(R.string.storage_permission_required)) },
            text = { Text(stringResource(R.string.storage_permission_rationale)) },
            confirmButton = {
                Button(onClick = { permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE) }) {
                    Text(stringResource(R.string.grant_permission))
                }
            },
            dismissButton = {
                Button(onClick = {
                    Toast.makeText(context, R.string.download_failed_permission_required, Toast.LENGTH_SHORT).show()
                    viewModel.resetPermissionRequest()
                }) {
                    Text(stringResource(R.string.cancel))
                }
            })
    }

    // 登录提示对话框
    needLoginAction?.let { action ->
        val message = when (action) {
            WallpaperDetailViewModel.LoginAction.FAVORITE -> stringResource(R.string.favorite_login_required)
            WallpaperDetailViewModel.LoginAction.DOWNLOAD -> stringResource(R.string.download_login_required)
            WallpaperDetailViewModel.LoginAction.SET_WALLPAPER -> stringResource(R.string.set_wallpaper_login_required)
            WallpaperDetailViewModel.LoginAction.EDIT -> stringResource(R.string.edit_login_required)
        }

        LoginPromptDialog(
            onDismiss = { viewModel.clearNeedLoginAction() },
            onConfirm = {
                viewModel.clearNeedLoginAction()
                onNavigateToLogin()
            },
            message = message
        )
    }
}
