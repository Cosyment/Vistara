package com.vistara.aestheticwalls.data.model.pixabay

import com.google.gson.annotations.SerializedName

/**
 * Pixabay图片模型
 */
data class PixabayImage(
    val id: Int,
    val pageURL: String,
    val type: String,
    val tags: String,
    val previewURL: String,
    val previewWidth: Int,
    val previewHeight: Int,
    val webformatURL: String,
    val webformatWidth: Int,
    val webformatHeight: Int,
    val largeImageURL: String,
    @SerializedName("imageWidth")
    val width: Int,
    @SerializedName("imageHeight")
    val height: Int,
    val imageSize: Int,
    val views: Int,
    val downloads: Int,
    val collections: Int,
    val likes: Int,
    val comments: Int,
    @SerializedName("user_id")
    val userId: Int,
    val user: String,
    val userImageURL: String
) 