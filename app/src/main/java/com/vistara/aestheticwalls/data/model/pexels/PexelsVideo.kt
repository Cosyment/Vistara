package com.vistara.aestheticwalls.data.model.pexels

import com.google.gson.annotations.SerializedName

/**
 * Pexels 视频数据模型
 */
data class PexelsVideo(
    val id: Int,
    val width: Int,
    val height: Int,
    val url: String, // 视频页面URL
    val image: String, // 视频缩略图URL
    val duration: Int, // 视频时长（秒）
    val user: PexelsVideoUser, // 视频作者
    
    @SerializedName("video_files")
    val videoFiles: List<PexelsVideoFile>, // 不同质量的视频文件
    
    @SerializedName("video_pictures")
    val videoPictures: List<PexelsVideoPicture> // 视频预览图
)

/**
 * Pexels 视频文件数据模型
 * 表示不同质量的视频文件
 */
data class PexelsVideoFile(
    val id: Int,
    val quality: String, // 视频质量，如"hd", "sd"
    val file_type: String, // 文件类型，如"video/mp4"
    val width: Int,
    val height: Int,
    val link: String // 视频文件URL
)

/**
 * Pexels 视频预览图数据模型
 */
data class PexelsVideoPicture(
    val id: Int,
    val picture: String, // 预览图URL
    val nr: Int // 预览图序号
)

/**
 * Pexels 视频作者数据模型
 */
data class PexelsVideoUser(
    val id: Int,
    val name: String, // 作者名称
    val url: String // 作者主页URL
)

/**
 * Pexels 视频搜索响应数据模型
 */
data class PexelsVideoSearchResponse(
    val page: Int,
    
    @SerializedName("per_page")
    val perPage: Int,
    
    @SerializedName("total_results")
    val totalResults: Int,
    
    val videos: List<PexelsVideo>,
    
    @SerializedName("next_page")
    val nextPage: String? = null,
    
    @SerializedName("prev_page")
    val prevPage: String? = null
)
