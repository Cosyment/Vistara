package com.vistara.aestheticwalls.data.model.pexels

import com.google.gson.annotations.SerializedName

/**
 * Pexels 搜索响应数据模型
 */
data class PexelsSearchResponse(
    val page: Int,
    
    @SerializedName("per_page")
    val perPage: Int,
    
    val photos: List<PexelsPhoto>,
    
    @SerializedName("total_results")
    val totalResults: Int,
    
    @SerializedName("next_page")
    val nextPage: String? = null,
    
    @SerializedName("prev_page")
    val prevPage: String? = null
) 