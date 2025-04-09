package com.vistara.aestheticwalls.data.model.unsplash

import com.google.gson.annotations.SerializedName

/**
 * Unsplash 搜索响应数据模型
 */
data class UnsplashSearchResponse(
    val total: Int,
    
    @SerializedName("total_pages")
    val totalPages: Int,
    
    val results: List<UnsplashPhoto>
) 