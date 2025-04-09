package com.vistara.aestheticwalls.data.model.unsplash

import com.google.gson.annotations.SerializedName

/**
 * Unsplash 照片数据模型
 */
data class UnsplashPhoto(
    val id: String,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    val width: Int,
    val height: Int,
    val color: String,
    
    @SerializedName("blur_hash")
    val blurHash: String? = null,
    
    val description: String? = null,
    
    @SerializedName("alt_description")
    val altDescription: String? = null,
    
    val urls: UnsplashUrls,
    val links: UnsplashLinks,
    val likes: Int = 0,
    
    @SerializedName("liked_by_user")
    val likedByUser: Boolean = false,
    
    val user: UnsplashUser,
    val tags: List<UnsplashTag>? = null
)

/**
 * Unsplash 图片URL
 */
data class UnsplashUrls(
    val raw: String,
    val full: String,
    val regular: String,
    val small: String,
    val thumb: String,
    
    @SerializedName("small_s3")
    val smallS3: String
)

/**
 * Unsplash 链接
 */
data class UnsplashLinks(
    val self: String,
    val html: String,
    val download: String,
    
    @SerializedName("download_location")
    val downloadLocation: String
)

/**
 * Unsplash 用户信息
 */
data class UnsplashUser(
    val id: String,
    val username: String,
    val name: String,
    
    @SerializedName("first_name")
    val firstName: String,
    
    @SerializedName("last_name")
    val lastName: String? = null,
    
    @SerializedName("portfolio_url")
    val portfolioUrl: String? = null,
    
    val bio: String? = null,
    val location: String? = null,
    
    @SerializedName("total_likes")
    val totalLikes: Int = 0,
    
    @SerializedName("total_photos")
    val totalPhotos: Int = 0,
    
    @SerializedName("total_collections")
    val totalCollections: Int = 0,
    
    @SerializedName("instagram_username")
    val instagramUsername: String? = null,
    
    @SerializedName("twitter_username")
    val twitterUsername: String? = null,
    
    @SerializedName("profile_image")
    val profileImage: UnsplashUserProfileImage,
    
    val links: UnsplashUserLinks
)

/**
 * Unsplash 用户头像
 */
data class UnsplashUserProfileImage(
    val small: String,
    val medium: String,
    val large: String
)

/**
 * Unsplash 用户链接
 */
data class UnsplashUserLinks(
    val self: String,
    val html: String,
    val photos: String,
    val likes: String,
    val portfolio: String,
    val following: String,
    val followers: String
)

/**
 * Unsplash 标签
 */
data class UnsplashTag(
    val type: String,
    val title: String
) 