package com.vistara.aestheticwalls.data.remote.api

import com.vistara.aestheticwalls.data.model.pixabay.PixabaySearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Pixabay API 服务接口
 * 文档参考: https://pixabay.com/api/docs/
 */
interface PixabayApiService {
    companion object {
        const val BASE_URL = "https://pixabay.com/api/"
    }

    /**
     * 搜索图片
     * @param query 搜索关键词
     * @param page 页码，从1开始
     * @param perPage 每页数量，默认20，最大200
     * @param imageType 图片类型，all、photo、illustration、vector
     * @param orientation 照片方向，all、horizontal、vertical
     * @param category 分类，如fashion、nature、backgrounds、travel等
     * @param minWidth 最小宽度
     * @param minHeight 最小高度
     * @param colors 过滤颜色，如red、orange、yellow、green、blue...
     * @param safeSearch 是否开启安全搜索，默认开启
     */
    @GET(".")
    suspend fun searchImages(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("image_type") imageType: String? = null,
        @Query("orientation") orientation: String? = null,
        @Query("category") category: String? = null,
        @Query("min_width") minWidth: Int? = null,
        @Query("min_height") minHeight: Int? = null,
        @Query("colors") colors: String? = null,
        @Query("safesearch") safeSearch: Boolean = true
    ): PixabaySearchResponse
    
    /**
     * 根据ID获取图片详情
     * @param id 图片ID
     */
    @GET(".")
    suspend fun getImageById(@Query("id") id: Int): PixabaySearchResponse
} 