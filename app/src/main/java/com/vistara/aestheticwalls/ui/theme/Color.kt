package com.vistara.aestheticwalls.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Vistara应用的颜色系统
 * 根据项目规范定义
 */

object AppColors {
    // 主要颜色
    val Primary = Color(0xFF3F51B5)
    val PrimaryVariant = Color(0xFF3700B3)
    val Secondary = Color(0xFF03A9F4)

    // 浅色主题背景和表面
    val LightBackground = Color(0xFFF5F5F5)
    val LightSurface = Color(0xFFFFFFFF)

    // 深色主题背景和表面
    val DarkBackground = Color(0xFF121212)
    val DarkSurface = Color(0xFF1E1E1E)

    // 当前使用的背景和表面（将在Theme.kt中根据深浅色主题设置）
    val Background = LightBackground
    val Surface = LightSurface

    // 错误颜色
    val Error = Color(0xFFB00020)
    val DarkError = Color(0xFFCF6679)

    // 浅色主题文字颜色
    val LightOnPrimary = Color(0xFFFFFFFF)
    val LightOnSecondary = Color(0xFF000000)
    val LightOnBackground = Color(0xFF000000)
    val LightOnSurface = Color(0xFF000000)
    val LightOnError = Color(0xFFFFFFFF)

    // 深色主题文字颜色
    val DarkOnPrimary = Color(0xFFFFFFFF)
    val DarkOnSecondary = Color(0xFFFFFFFF)
    val DarkOnBackground = Color(0xFFFFFFFF)
    val DarkOnSurface = Color(0xFFFFFFFF)
    val DarkOnError = Color(0xFF000000)

    // 当前使用的文字颜色（将在Theme.kt中根据深浅色主题设置）
    val OnPrimary = LightOnPrimary
    val OnSecondary = LightOnSecondary
    val OnBackground = LightOnBackground
    val OnSurface = LightOnSurface
    val OnError = LightOnError

    // 辅助颜色
    val Premium = Color(0xFFFFD700) // 金色，表示高级内容

    // 壁纸详情页背景颜色
    val WallpaperDetailBackground = Color.Black
}