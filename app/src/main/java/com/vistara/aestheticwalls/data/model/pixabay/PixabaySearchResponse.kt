package com.vistara.aestheticwalls.data.model.pixabay

import com.google.gson.annotations.SerializedName

/**
 * Pixabay 搜索响应数据模型
 */
data class PixabaySearchResponse(
    val total: Int,
    
    @SerializedName("totalHits")
    val totalHits: Int,
    
    @SerializedName("hits")
    val images: List<PixabayImage>
) 