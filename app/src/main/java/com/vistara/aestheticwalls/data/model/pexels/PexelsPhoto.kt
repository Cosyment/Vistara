package com.vistara.aestheticwalls.data.model.pexels

import com.google.gson.annotations.SerializedName

/**
 * Pexels 照片数据模型
 */
data class PexelsPhoto(
    val id: Int,
    val width: Int,
    val height: Int,
    val url: String,
    val photographer: String,
    
    @SerializedName("photographer_url")
    val photographerUrl: String,
    
    @SerializedName("photographer_id")
    val photographerId: Int,
    
    @SerializedName("avg_color")
    val avgColor: String?,
    
    val src: PexelsPhotoSources,
    val liked: Boolean,
    val alt: String?
)

/**
 * Pexels 照片不同尺寸URL
 */
data class PexelsPhotoSources(
    val original: String,
    val large2x: String,
    val large: String,
    val medium: String,
    val small: String,
    val portrait: String,
    val landscape: String,
    val tiny: String
) 