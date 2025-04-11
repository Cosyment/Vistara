package com.vistara.aestheticwalls.data.model.pixabay

/**
 * Pixabay API响应模型
 */
data class PixabayResponse(
    val total: Int,
    val totalHits: Int,
    val hits: List<PixabayImage>
) 