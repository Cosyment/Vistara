package com.vistara.aestheticwalls.data.model.wallhaven

import com.google.gson.annotations.SerializedName

/**
 * Wallhaven 搜索响应数据模型
 */
data class WallhavenSearchResponse(
    val data: List<WallhavenWallpaper>,
    val meta: WallhavenMetaData
)

/**
 * Wallhaven 搜索元数据
 */
data class WallhavenMetaData(
    @SerializedName("current_page")
    val currentPage: Int,
    
    @SerializedName("last_page")
    val lastPage: Int,
    
    @SerializedName("per_page")
    val perPage: Int,
    
    val total: Int,
    
    val query: String? = null,
    val seed: String? = null
) 