package com.vistara.aestheticwalls.data.model.pexels

import com.google.gson.annotations.SerializedName

/**
 * Pexels 集合响应数据模型
 */
data class PexelsCollectionsResponse(
    val page: Int,
    
    @SerializedName("per_page")
    val perPage: Int,
    
    val collections: List<PexelsCollection>,
    
    @SerializedName("total_results")
    val totalResults: Int? = null,
    
    @SerializedName("next_page")
    val nextPage: String? = null,
    
    @SerializedName("prev_page")
    val prevPage: String? = null
)
