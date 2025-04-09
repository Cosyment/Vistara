package com.vistara.aestheticwalls.data.model.unsplash

import com.google.gson.annotations.SerializedName

/**
 * Unsplash 集合数据模型
 */
data class UnsplashCollection(
    val id: String,
    val title: String,
    val description: String? = null,
    
    @SerializedName("published_at")
    val publishedAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    val curated: Boolean,
    val featured: Boolean,
    
    @SerializedName("total_photos")
    val totalPhotos: Int,
    
    val private: Boolean,
    
    @SerializedName("share_key")
    val shareKey: String,
    
    val tags: List<UnsplashTag>? = null,
    val links: UnsplashCollectionLinks,
    val user: UnsplashUser,
    
    @SerializedName("cover_photo")
    val coverPhoto: UnsplashPhoto,
    
    @SerializedName("preview_photos")
    val previewPhotos: List<UnsplashPhoto>? = null
)

/**
 * Unsplash 集合链接
 */
data class UnsplashCollectionLinks(
    val self: String,
    val html: String,
    val photos: String,
    val related: String
) 