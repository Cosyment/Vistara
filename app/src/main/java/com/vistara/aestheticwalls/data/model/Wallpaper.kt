package com.vistara.aestheticwalls.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 壁纸分辨率数据类
 */
data class Resolution(
    val width: Int, val height: Int
) {
    override fun toString(): String = "${width}x${height}"
}

/**
 * 壁纸实体类
 * 用于表示单个壁纸的所有信息
 */
@Entity(tableName = "wallpapers")
data class Wallpaper(
    @PrimaryKey val id: String, // 格式: "{source}_{originalId}" 例如: "unsplash_123"
    val title: String?,
    val description: String? = null,
    val url: String? = null,
    val thumbnailUrl: String? = null,
    val author: String? = null,
    val authorUrl: String? = null,
    val source: String? = null, // "unsplash", "pexels", "pixabay", "wallhaven"
    val sourceUrl: String? = null, // 原始图片页面URL
    val width: Int = 0,
    val height: Int = 0,
    val color: String? = null, // 主色调
    val blurHash: String? = null, // 用于生成模糊预览
    val isPremium: Boolean = false,
    val isLive: Boolean = false,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadUrl: String? = null, // 高清下载链接
    val downloadCount: Int = 0,
    val resolution: Resolution? = null,
    val tags: List<String> = emptyList(),
    val categoryId: String? = null,
    val previewUrl: String? = null,
    val attributionRequired: Boolean? = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
