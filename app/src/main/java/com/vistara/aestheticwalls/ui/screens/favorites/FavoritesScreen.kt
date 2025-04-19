package com.vistara.aestheticwalls.ui.screens.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vistara.aestheticwalls.ui.theme.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vistara.aestheticwalls.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.ui.components.ErrorState
import com.vistara.aestheticwalls.ui.components.LoginPromptDialog
import com.vistara.aestheticwalls.ui.components.WallpaperGrid

/**
 * 收藏页面
 * 显示用户收藏的壁纸列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBackPressed: () -> Unit,
    onWallpaperClick: (Wallpaper) -> Unit,
    onNavigateToLogin: () -> Unit = {},
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favoritesState by viewModel.favoritesState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_favorites)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (favoritesState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is UiState.Error -> {
                    val errorMessage = (favoritesState as UiState.Error).message
                    if (errorMessage == stringResource(R.string.login_required_to_view_favorites)) {
                        // 显示登录提示对话框
                        LoginPromptDialog(
                            onDismiss = { onBackPressed() },
                            onConfirm = { onNavigateToLogin() },
                            message = stringResource(R.string.favorites_login_required)
                        )
                    } else {
                        ErrorState(
                            message = errorMessage,
                            onRetry = { viewModel.refresh() }
                        )
                    }
                }

                is UiState.Success -> {
                    val wallpapers = (favoritesState as UiState.Success<List<Wallpaper>>).data

                    if (wallpapers.isEmpty()) {
                        EmptyFavoritesContent()
                    } else {
                        WallpaperGrid(
                            wallpapers = wallpapers,
                            onWallpaperClick = onWallpaperClick,
                            contentPadding = PaddingValues(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 空收藏内容
 * 当用户没有收藏任何壁纸时显示
 */
@Composable
private fun EmptyFavoritesContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.3f))

        // 可以添加一个图标或图片
        // Image(...)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.no_favorite_wallpapers),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.browse_and_favorite_tip),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Spacer(modifier = Modifier.weight(0.7f))
    }
}
