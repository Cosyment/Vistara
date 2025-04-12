package com.vistara.aestheticwalls.ui.screens.detail

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.vistara.aestheticwalls.data.model.WallpaperTarget
import com.vistara.aestheticwalls.ui.components.PremiumWallpaperPrompt
import com.vistara.aestheticwalls.ui.components.WallpaperDetail
import com.vistara.aestheticwalls.ui.components.WallpaperSetOptions
import com.vistara.aestheticwalls.data.model.UiState

/**
 * 壁纸详情页面
 * 显示壁纸详情，提供收藏、下载、分享、设置壁纸等功能
 */
@Composable
fun WallpaperDetailScreen(
    onBackPressed: () -> Unit,
    viewModel: WallpaperDetailViewModel = hiltViewModel()
) {
    val wallpaperState by viewModel.wallpaperState.collectAsState()
    val isFavorite by viewModel.isFavorite
    val isPremiumUser by viewModel.isPremiumUser
    val showSetWallpaperOptions by viewModel.showSetWallpaperOptions
    val showPremiumPrompt by viewModel.showPremiumPrompt
    val context = LocalContext.current

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

                WallpaperDetail(
                    wallpaper = wallpaper,
                    isFavorite = isFavorite,
                    onBackPressed = onBackPressed,
                    onToggleFavorite = { viewModel.toggleFavorite() },
                    onSetWallpaper = { viewModel.showSetWallpaperOptions() },
                    onDownload = {
                        viewModel.downloadWallpaper()
                        Toast.makeText(context, "开始下载壁纸", Toast.LENGTH_SHORT).show()
                    },
                    onShare = { viewModel.shareWallpaper() },
                    onEdit = { viewModel.editWallpaper() },
                    isPremiumUser = isPremiumUser
                )

                // 设置壁纸选项对话框
                if (showSetWallpaperOptions) {
                    androidx.compose.ui.window.Dialog(
                        onDismissRequest = { viewModel.hideSetWallpaperOptions() }
                    ) {
                        WallpaperSetOptions(
                            onSetHomeScreen = {
                                viewModel.setWallpaper(context, WallpaperTarget.HOME)
                                Toast.makeText(context, "已设置为主屏幕壁纸", Toast.LENGTH_SHORT).show()
                            },
                            onSetLockScreen = {
                                viewModel.setWallpaper(context, WallpaperTarget.LOCK)
                                Toast.makeText(context, "已设置为锁屏壁纸", Toast.LENGTH_SHORT).show()
                            },
                            onSetBoth = {
                                viewModel.setWallpaper(context, WallpaperTarget.BOTH)
                                Toast.makeText(context, "已设置为主屏幕和锁屏壁纸", Toast.LENGTH_SHORT).show()
                            },
                            onDismiss = { viewModel.hideSetWallpaperOptions() }
                        )
                    }
                }

                // 高级壁纸提示对话框
                if (showPremiumPrompt) {
                    androidx.compose.ui.window.Dialog(
                        onDismissRequest = { viewModel.hidePremiumPrompt() }
                    ) {
                        PremiumWallpaperPrompt(
                            onUpgrade = {
                                // TODO: 实现升级逻辑
                                viewModel.hidePremiumPrompt()
                            },
                            onDismiss = { viewModel.hidePremiumPrompt() }
                        )
                    }
                }
            }
        }
    }
}
