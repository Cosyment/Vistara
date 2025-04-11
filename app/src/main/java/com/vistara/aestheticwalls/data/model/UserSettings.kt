package com.vistara.aestheticwalls.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户设置数据类
 * 包含用户的所有偏好设置
 */
data class UserSettings(
    // 通用设置
    val darkTheme: Boolean = false,
    val dynamicColors: Boolean = true,
    
    // 自动更换壁纸设置
    val autoChangeEnabled: Boolean = false,
    val autoChangeFrequency: AutoChangeFrequency = AutoChangeFrequency.DAILY,
    val autoChangeWifiOnly: Boolean = true,
    val autoChangeSource: AutoChangeSource = AutoChangeSource.FAVORITES,
    val autoChangeCategory: String? = null,
    
    // 通知设置
    val showDownloadNotification: Boolean = true,
    val showWallpaperChangeNotification: Boolean = true,
    
    // 下载设置
    val downloadOriginalQuality: Boolean = true,
    val downloadLocation: String? = null,
    
    // 高级用户状态
    val isPremiumUser: Boolean = false,
    val premiumExpiryDate: Long = 0L
)

/**
 * 自动更换壁纸频率
 */
enum class AutoChangeFrequency(val hours: Int, val isPremium: Boolean = false) {
    DAILY(24), // 每天，免费
    TWELVE_HOURS(12, true), // 每12小时，高级
    SIX_HOURS(6, true), // 每6小时，高级
    HOURLY(1, true), // 每小时，高级
    EACH_UNLOCK(0, true) // 每次解锁，高级
}

/**
 * 自动更换壁纸来源
 */
enum class AutoChangeSource(val isPremium: Boolean = false) {
    FAVORITES(false), // 我的收藏，免费
    CATEGORY(true), // 指定分类，高级
    DOWNLOADED(false), // 已下载，免费
    TRENDING(true) // 热门壁纸，高级
}

/**
 * 应用主题设置
 */
enum class AppTheme {
    LIGHT, // 浅色主题
    DARK, // 深色主题
    SYSTEM // 跟随系统
}

/**
 * 用户搜索历史条目
 */
data class SearchHistoryItem(
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 用户收藏壁纸
 */
data class FavoriteWallpaper(
    val wallpaperId: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 下载的壁纸
 */
data class DownloadedWallpaper(
    val wallpaperId: String,
    val localPath: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 自动更换壁纸历史
 */
@Entity(tableName = "auto_change_history")
data class AutoChangeHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wallpaperId: String,
    val wallpaperUrl: String,
    val timestamp: Long = System.currentTimeMillis(),
    val success: Boolean = true,
    val errorMessage: String? = null,
    val targetScreen: String?=null // "home", "lock", "both"
)

/**
 * 壁纸设置目标屏幕
 */
enum class WallpaperTarget {
    HOME, // 主屏幕
    LOCK, // 锁屏
    BOTH  // 同时设置
} 