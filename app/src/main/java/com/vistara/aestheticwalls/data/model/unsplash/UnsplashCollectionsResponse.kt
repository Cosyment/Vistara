package com.vistara.aestheticwalls.data.model.unsplash

import com.google.gson.annotations.SerializedName

/**
 * Unsplash 集合响应数据模型
 */
data class UnsplashCollectionsResponse(
    val total: Int,
    
    @SerializedName("total_pages")
    val totalPages: Int,
    
    val results: List<UnsplashCollection>
)
