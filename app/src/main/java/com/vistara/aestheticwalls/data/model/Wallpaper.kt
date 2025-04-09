package com.vistara.aestheticwalls.data.model

/**
 * 壁纸分辨率数据类
 */
data class Resolution(
    val width: Int, val height: Int
) {
    override fun toString(): String = "${width}x${height}"
}

/**
 * 壁纸数据模型
 */
data class Wallpaper(
    val id: String,
    val title: String? = null,
    val description: String? = null,
    val url: String,
    val thumbnailUrl: String,
    val previewUrl: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val aspectRatio: Float = width.toFloat() / height.toFloat(),
    val author: String? = null,
    val authorUrl: String? = null,
    val source: String,
    val sourceUrl: String? = null,
    val attributionRequired: Boolean = false,
    val isPremium: Boolean = false,
    val isLive: Boolean = false,
    val tags: List<String> = emptyList(),
    val categoryIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val downloadCount: Int = 0,
    val resolution: Resolution = Resolution(width, height)
)
