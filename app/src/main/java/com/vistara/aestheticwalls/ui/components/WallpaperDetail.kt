package com.vistara.aestheticwalls.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.Resolution
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.ui.theme.VistaraTheme

@Composable
fun WallpaperDetail(
    wallpaper: Wallpaper,
    isFavorite: Boolean,
    isInfoExpanded: Boolean = false,
    isDownloading: Boolean = false,
    downloadProgress: Float = 0f,
    onBackPressed: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleInfo: () -> Unit = {},
    onSetWallpaper: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    isPremiumUser: Boolean = false,
    editedBitmap: Bitmap? = null
) {
    var showControls by remember { mutableStateOf(true) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 壁纸图片或视频 - 全屏显示，支持缩放
        if (editedBitmap != null) {
            // 显示编辑后的图片，使用可缩放组件
            ZoomableBitmapImage(
                bitmap = editedBitmap,
                contentDescription = wallpaper.title ?: "壁纸",
                modifier = Modifier.fillMaxSize(),
                onTap = { showControls = !showControls })
        } else if (wallpaper.isLive) {
            // 显示动态壁纸（视频）
            // 使用remember确保在wallpaper.id变化时重建组件
            val videoKey = remember { wallpaper.id }
            LiveVideoPlayer(
                wallpaper = wallpaper,
                modifier = Modifier.fillMaxSize(),
                onTap = { showControls = !showControls })
        } else {
            // 显示原始图片
            ZoomableImage(
                imageUrl = wallpaper.url ?: "",
                contentDescription = wallpaper.title ?: "壁纸",
                modifier = Modifier.fillMaxSize(),
                onTap = { showControls = !showControls })
        }

        // 顶部控制栏 (状态栏区域) - 半透明渐变背景
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)) { -it / 3 },
            exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
                animationSpec = tween(
                    300
                )
            ) { -it / 3 },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
                    .statusBarsPadding() // 确保内容不会被状态栏遮挡
                    .height(56.dp)
            ) {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }

                // 如果壁纸有标题，显示标题
                wallpaper.title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 56.dp) // 留出两边按钮的空间
                    )
                }
            }
        }

        // 底部控制栏 - 半透明渐变背景
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)) { it / 3 },
            exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
                animationSpec = tween(
                    300
                )
            ) { it / 3 },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .navigationBarsPadding() // 确保内容不会被导航栏遮挡
                    .padding(16.dp)
            ) {
                // 作者信息和来源
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "作者: ${wallpaper.author}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )

                        Text(
                            text = "来源: ${wallpaper.source}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                // 展开的详细信息
                AnimatedVisibility(visible = isInfoExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        // 分辨率
                        Text(
                            text = "分辨率: ${wallpaper.resolution?.width} x ${wallpaper.resolution?.height}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        // 壁纸类型
                        Text(
                            text = "类型: ${if (wallpaper.isLive) "动态壁纸" else "静态壁纸"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        // 标签显示
                        if (wallpaper.tags.isNotEmpty()) {
                            Text(
                                text = "标签:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .padding(bottom = 4.dp)
                            ) {
                                wallpaper.tags.forEach { tag ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp, vertical = 4.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 操作按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 收藏按钮
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "取消收藏" else "收藏",
                            tint = if (isFavorite) Color.Red else Color.White
                        )
                    }

                    // 编辑按钮 - 仅对静态壁纸显示且不是动态壁纸时
                    if (!wallpaper.isLive) {
                        val canEdit = (!wallpaper.isPremium && !wallpaper.isLive) || isPremiumUser
                        IconButton(
                            onClick = onEdit,
                            enabled = canEdit,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = if (canEdit) 0.2f else 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "编辑",
                                tint = if (canEdit) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // 下载按钮
                    val canDownload = (!wallpaper.isPremium && !wallpaper.isLive) || isPremiumUser
                    val isPremiumWallpaper = wallpaper.isPremium && !isPremiumUser
                    Box(
                        contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)
                    ) {
                        // 下载进度指示器
                        if (isDownloading) {
                            CircularProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.size(40.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.White.copy(alpha = 0.2f),
                                strokeWidth = 2.dp
                            )
                        }

                        IconButton(
                            onClick = onDownload,
                            enabled = !isDownloading,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = if (!isDownloading) 0.2f else 0.1f))
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_download),
                                    contentDescription = "下载",
                                    tint = if (canDownload && !isDownloading) Color.White else Color.White.copy(
                                        alpha = 0.5f
                                    )
                                )
                            }
                        }

                        // 添加皇冠图标
                        if (isPremiumWallpaper) {
                            Text(
                                text = "👑",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 1.dp, y = (-5).dp)
                            )
                        }
                    }

                    // 分享按钮
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "分享",
                            tint = Color.White
                        )
                    }

                    // 信息展开/收起按钮
                    IconButton(
                        onClick = onToggleInfo,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = if (isInfoExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isInfoExpanded) "收起信息" else "展开信息",
                            tint = Color.White
                        )
                    }
                }

                // 设置壁纸按钮 - 主要操作
                val canSetWallpaper = (!wallpaper.isPremium && !wallpaper.isLive) || isPremiumUser
                Button(
                    onClick = onSetWallpaper,
                    // 始终启用按钮，但对于高级壁纸和非高级用户，点击会显示升级提示
                    enabled = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (!canSetWallpaper) {
                            // 对于高级壁纸和非高级用户，显示皇冠图标
                            Text(
                                text = "👑", // 皇冠emoji
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                        Text(
                            text = if (canSetWallpaper) "设置为壁纸" else "升级解锁此壁纸",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // 注意：点击屏幕切换控制栏显示状态的功能已移至ZoomableImage组件中
    }
}

@Composable
fun WallpaperSetOptions(
    onSetHomeScreen: () -> Unit,
    onSetLockScreen: () -> Unit,
    onSetBoth: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "设置为",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = onSetHomeScreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(text = "主屏幕壁纸")
            }

            Button(
                onClick = onSetLockScreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(text = "锁屏壁纸")
            }

            Button(
                onClick = onSetBoth, modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(text = "主屏幕和锁屏")
            }

            TextButton(
                onClick = onDismiss, modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(text = "取消")
            }
        }
    }
}

@Preview
@Composable
fun WallpaperDetailPreview() {
    VistaraTheme {
        WallpaperDetail(
            wallpaper = Wallpaper(
            id = "1",
            title = "Beautiful Landscape",
            url = "https://s3.us-west-2.amazonaws.com/images.unsplash.com/small/photo-1739911013984-8b3bf696a182",
            thumbnailUrl = "https://s3.us-west-2.amazonaws.com/images.unsplash.com/small/photo-1739911013984-8b3bf696a182",
            author = "John Doe",
            source = "Unsplash",
            isPremium = true,
            isLive = false,
            tags = listOf("nature", "landscape"),
            resolution = Resolution(1920, 1080)
        ),
            onBackPressed = {},
            onToggleInfo = {},
            onDownload = {},
            onShare = {},
            onSetWallpaper = {},
            isPremiumUser = false,
            isInfoExpanded = true,
            isDownloading = false,
            isFavorite = false,
            onEdit = {},
            onToggleFavorite = {})
    }
}