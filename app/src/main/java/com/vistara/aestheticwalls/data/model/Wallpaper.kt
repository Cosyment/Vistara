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
 * @param id 壁纸唯一标识
 * @param title 壁纸标题（可选）
 * @param url 壁纸完整URL
 * @param thumbnailUrl 缩略图URL
 * @param author 作者
 * @param source 来源平台（如Unsplash, Pexels等）
 * @param isPremium 是否为高级壁纸（需要付费解锁）
 * @param isLive 是否为动态壁纸
 * @param tags 标签列表
 * @param resolution 分辨率
 * @param aspectRatio 宽高比
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
