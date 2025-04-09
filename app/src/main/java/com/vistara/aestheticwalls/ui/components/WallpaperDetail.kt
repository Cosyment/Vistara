package com.vistara.aestheticwalls.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.Resolution
import com.vistara.aestheticwalls.data.model.Wallpaper

@Composable
fun WallpaperDetail(
    wallpaper: Wallpaper,
    isFavorite: Boolean,
    onBackPressed: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSetWallpaper: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    isPremiumUser: Boolean = false
) {
    var showControls by remember { mutableStateOf(true) }
    val context = LocalContext.current
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 壁纸图片
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(wallpaper.url)
                .crossfade(true)
                .build(),
            contentDescription = wallpaper.title ?: "壁纸",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
        )
        
        // 顶部控制栏 (状态栏区域)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
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
                            .padding(horizontal = 64.dp)
                    )
                }
            }
        }
        
        // 底部控制栏
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                // 作者信息和来源
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
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
                        
                        Text(
                            text = "${wallpaper.resolution.width} x ${wallpaper.resolution.height}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    
                    if (wallpaper.isPremium && !isPremiumUser) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "高级 👑",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                // 标签显示
                if (wallpaper.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 16.dp)
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
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
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
                            .size(48.dp)
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
                        val canEdit = !wallpaper.isPremium || isPremiumUser
                        IconButton(
                            onClick = onEdit,
                            enabled = canEdit,
                            modifier = Modifier
                                .size(48.dp)
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
                    val canDownload = !wallpaper.isPremium || isPremiumUser
                    IconButton(
                        onClick = onDownload,
                        enabled = canDownload,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = if (canDownload) 0.2f else 0.1f))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_download),
                            contentDescription = "下载",
                            tint = if (canDownload) Color.White else Color.White.copy(alpha = 0.5f)
                        )
                    }
                    
                    // 分享按钮
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "分享",
                            tint = Color.White
                        )
                    }
                }
                
                // 设置壁纸按钮 - 主要操作
                val canSetWallpaper = !wallpaper.isPremium || isPremiumUser
                Button(
                    onClick = onSetWallpaper,
                    enabled = canSetWallpaper,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = if (canSetWallpaper) "设置为壁纸" else "升级解锁此壁纸",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
        
        // 点击屏幕切换控制栏显示状态
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { showControls = !showControls }
        )
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
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
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
                onClick = onSetBoth,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(text = "主屏幕和锁屏")
            }
            
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(text = "取消")
            }
        }
    }
}

@Composable
fun PremiumWallpaperPrompt(
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "高级壁纸",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "此壁纸为高级会员专享内容",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Text(
                text = "升级到Vistara高级版解锁全部内容",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Button(
                onClick = onUpgrade,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "立即升级",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(text = "返回")
            }
        }
    }
} 