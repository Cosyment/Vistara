package com.vistara.aestheticwalls.data.remote.api

import com.vistara.aestheticwalls.data.model.pixabay.PixabayResponse
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
    @GET("api/")
    suspend fun searchImages(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 30,
        @Query("safesearch") safeSearch: Boolean = true
    ): PixabayResponse
    
    /**
     * 根据ID获取图片详情
     * @param id 图片ID
     */
    @GET("api/")
    suspend fun getImageById(
        @Query("id") id: Int,
        @Query("safesearch") safeSearch: Boolean = true
    ): PixabayResponse
} 