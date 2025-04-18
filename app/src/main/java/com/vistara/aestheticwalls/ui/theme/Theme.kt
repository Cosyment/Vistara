package com.vistara.aestheticwalls.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Vistara应用的深色配色方案
 */
private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Primary,
    secondary = AppColors.Secondary,
    background = AppColors.DarkBackground,
    surface = AppColors.DarkSurface,
    error = AppColors.DarkError,
    onPrimary = AppColors.DarkOnPrimary,
    onSecondary = AppColors.DarkOnSecondary,
    onBackground = AppColors.DarkOnBackground,
    onSurface = AppColors.DarkOnSurface,
    onError = AppColors.DarkOnError
)

/**
 * Vistara应用的浅色配色方案
 */
private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    secondary = AppColors.Secondary,
    background = AppColors.LightBackground,
    surface = AppColors.LightSurface,
    error = AppColors.Error,
    onPrimary = AppColors.LightOnPrimary,
    onSecondary = AppColors.LightOnSecondary,
    onBackground = AppColors.LightOnBackground,
    onSurface = AppColors.LightOnSurface,
    onError = AppColors.LightOnError
)

/**
 * Vistara应用的主题
 *
 * @param darkTheme 是否使用深色主题
 * @param dynamicColor 是否使用动态颜色（仅Android 12+支持）
 * @param content 主题内容
 */
@Composable
fun VistaraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}