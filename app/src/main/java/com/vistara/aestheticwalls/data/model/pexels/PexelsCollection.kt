package com.vistara.aestheticwalls.data.model.pexels

import com.google.gson.annotations.SerializedName

/**
 * Pexels 集合数据模型
 */
data class PexelsCollection(
    val id: String,
    val title: String,
    val description: String? = null,
    
    @SerializedName("private")
    val isPrivate: Boolean,
    
    @SerializedName("media_count")
    val mediaCount: Int,
    
    @SerializedName("photos_count")
    val photosCount: Int,
    
    @SerializedName("videos_count")
    val videosCount: Int
) 