package com.vistara.aestheticwalls.data.model.pixabay

import com.google.gson.annotations.SerializedName

/**
 * Pixabay 图片数据模型
 */
data class PixabayImage(
    val id: Int,
    
    @SerializedName("pageURL")
    val pageUrl: String,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("tags")
    val tags: String,
    
    @SerializedName("previewURL")
    val previewUrl: String,
    
    @SerializedName("previewWidth")
    val previewWidth: Int,
    
    @SerializedName("previewHeight")
    val previewHeight: Int,
    
    @SerializedName("webformatURL")
    val webFormatUrl: String,
    
    @SerializedName("webformatWidth")
    val webFormatWidth: Int,
    
    @SerializedName("webformatHeight")
    val webFormatHeight: Int,
    
    @SerializedName("largeImageURL")
    val largeImageUrl: String,
    
    @SerializedName("imageWidth")
    val imageWidth: Int,
    
    @SerializedName("imageHeight")
    val imageHeight: Int,
    
    @SerializedName("imageSize")
    val imageSize: Int,
    
    @SerializedName("views")
    val views: Int,
    
    @SerializedName("downloads")
    val downloads: Int,
    
    @SerializedName("collections")
    val collections: Int,
    
    @SerializedName("likes")
    val likes: Int,
    
    @SerializedName("comments")
    val comments: Int,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("user")
    val userName: String,
    
    @SerializedName("userImageURL")
    val userImageUrl: String
) 