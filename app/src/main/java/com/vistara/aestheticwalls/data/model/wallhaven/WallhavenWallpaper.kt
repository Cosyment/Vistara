package com.vistara.aestheticwalls.data.model.wallhaven

import com.google.gson.annotations.SerializedName

/**
 * Wallhaven 壁纸详情响应
 */
data class WallhavenWallpaperResponse(
    val data: WallhavenWallpaper
)

/**
 * Wallhaven 壁纸数据模型
 */
data class WallhavenWallpaper(
    val id: String,
    val url: String,
    
    @SerializedName("short_url")
    val shortUrl: String,
    
    val uploader: WallhavenUploader? = null,
    val views: Int,
    val favorites: Int,
    val source: String,
    val purity: String,
    val category: String,
    
    @SerializedName("dimension_x")
    val dimensionX: Int,
    
    @SerializedName("dimension_y")
    val dimensionY: Int,
    
    val resolution: String,
    val ratio: String,
    
    @SerializedName("file_size")
    val fileSize: Int,
    
    @SerializedName("file_type")
    val fileType: String,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    val colors: List<String>,
    val path: String,
    val thumbs: WallhavenThumbs,
    val tags: List<WallhavenTag>
)

/**
 * Wallhaven 上传者信息
 */
data class WallhavenUploader(
    val username: String,
    val group: String,
    val avatar: WallhavenAvatar
)

/**
 * Wallhaven 头像
 */
data class WallhavenAvatar(
    @SerializedName("200px")
    val large: String,
    
    @SerializedName("128px")
    val medium: String,
    
    @SerializedName("32px")
    val small: String,
    
    @SerializedName("20px")
    val tiny: String
)

/**
 * Wallhaven 缩略图
 */
data class WallhavenThumbs(
    val large: String,
    val original: String,
    val small: String
)

/**
 * Wallhaven 标签
 */
data class WallhavenTag(
    val id: Int,
    val name: String,
    val alias: String,
    
    @SerializedName("category_id")
    val categoryId: Int,
    
    val category: String,
    
    @SerializedName("purity")
    val purity: String,
    
    @SerializedName("created_at")
    val createdAt: String
) 