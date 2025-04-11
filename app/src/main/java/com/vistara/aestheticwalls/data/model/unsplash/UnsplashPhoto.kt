package com.vistara.aestheticwalls.data.model.unsplash

import com.google.gson.annotations.SerializedName

/**
 * Unsplash 照片数据模型
 */
data class UnsplashPhoto(
    val id: String,
    val description: String?,
    val alt_description: String?,
    val width: Int,
    val height: Int,
    val urls: UnsplashUrls,
    val links: UnsplashLinks,
    val user: UnsplashUser,
    val likes: Int,
    val tags: List<UnsplashTag>?
)

/**
 * Unsplash 图片URL
 */
data class UnsplashUrls(
    val raw: String,
    val full: String,
    val regular: String,
    val small: String,
    val thumb: String
)

/**
 * Unsplash 链接
 */
data class UnsplashLinks(
    val self: String,
    val html: String,
    val download: String,
    val download_location: String
)

/**
 * Unsplash 用户信息
 */
data class UnsplashUser(
    val id: String,
    val username: String,
    val name: String,
    val portfolio_url: String?,
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
    val likes: String
)

/**
 * Unsplash 标签
 */
data class UnsplashTag(
    val title: String
) 