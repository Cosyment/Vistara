package com.vistara.aestheticwalls.data.model

/**
 * 用户设置数据模型
 */
data class UserSettings(
    val isPremiumUser: Boolean = false,
    val premiumExpiryDate: Long? = null,
    val autoChangeEnabled: Boolean = false,
    val autoChangeFrequency: AutoChangeFrequency = AutoChangeFrequency.DAILY,
    val autoChangeSource: AutoChangeSource = AutoChangeSource.FAVORITES,
    val autoChangeCategory: String? = null,
    val autoChangeWifiOnly: Boolean = true,
    val showDownloadNotification: Boolean = true,
    val language: String = "zh", // 默认中文
    val theme: AppTheme = AppTheme.SYSTEM,
    val lastUpdatedTime: Long = System.currentTimeMillis()
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
data class AutoChangeHistory(
    val wallpaperId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val targetScreen: WallpaperTarget = WallpaperTarget.BOTH
)

/**
 * 壁纸设置目标屏幕
 */
enum class WallpaperTarget {
    HOME, // 主屏幕
    LOCK, // 锁屏
    BOTH  // 同时设置
} 