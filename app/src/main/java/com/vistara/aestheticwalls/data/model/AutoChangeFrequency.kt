package com.vistara.aestheticwalls.data.model

/**
 * 自动更换壁纸的频率
 */
enum class AutoChangeFrequency(val isPremium: Boolean = false) {
    DAILY(false),           // 每天
    TWELVE_HOURS(false),    // 每12小时
    SIX_HOURS(true),        // 每6小时
    HOURLY(true),           // 每小时
    EACH_UNLOCK(true)       // 每次解锁
}
