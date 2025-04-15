package com.vistara.aestheticwalls.data.model

/**
 * 自动更换壁纸的来源
 */
enum class AutoChangeSource(val isPremium: Boolean = false) {
    FAVORITES(false),    // 收藏
    DOWNLOADED(false),   // 已下载
    CATEGORY(true),      // 指定分类
    TRENDING(true)       // 热门
}
