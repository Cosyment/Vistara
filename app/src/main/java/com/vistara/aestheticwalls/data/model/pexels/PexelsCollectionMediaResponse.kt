package com.vistara.aestheticwalls.data.model.pexels

import com.google.gson.annotations.SerializedName

/**
 * Pexels 集合媒体响应数据模型
 */
data class PexelsCollectionMediaResponse(
    val page: Int,

    @SerializedName("per_page")
    val perPage: Int,

    val media: List<PexelsPhoto>?,

    @SerializedName("next_page")
    val nextPage: String? = null,

    @SerializedName("prev_page")
    val prevPage: String? = null,

    @SerializedName("total_results")
    val totalResults: Int? = null
)
