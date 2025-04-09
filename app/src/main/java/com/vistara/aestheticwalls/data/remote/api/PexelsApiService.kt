package com.vistara.aestheticwalls.data.remote.api

import com.vistara.aestheticwalls.data.model.pexels.PexelsCollection
import com.vistara.aestheticwalls.data.model.pexels.PexelsPhoto
import com.vistara.aestheticwalls.data.model.pexels.PexelsSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Pexels API 服务接口
 * 文档参考: https://www.pexels.com/api/documentation/
 */
interface PexelsApiService {
    companion object {
        const val BASE_URL = "https://api.pexels.com/v1/"
    }

    /**
     * 获取精选照片
     * @param page 页码，从1开始
     * @param perPage 每页数量，默认10，最大80
     */
    @GET("curated")
    suspend fun getCuratedPhotos(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10
    ): PexelsSearchResponse

    /**
     * 搜索照片
     * @param query 搜索关键词
     * @param page 页码，从1开始
     * @param perPage 每页数量，默认10，最大80
     * @param orientation 照片方向，landscape、portrait、square
     * @param size 照片尺寸，large(>24MP)、medium(>12MP)、small(>4MP)
     * @param color 过滤颜色，如red、orange、yellow、green、blue...
     */
    @GET("search")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("orientation") orientation: String? = null,
        @Query("size") size: String? = null,
        @Query("color") color: String? = null
    ): PexelsSearchResponse

    /**
     *
     * 获取照片详情
     * @param id 照片ID
     */
    @GET("photos/{id}")
    suspend fun getPhoto(@Path("id") id: String): PexelsPhoto

    /**
     * 获取精选集合
     * @param page 页码，从1开始
     * @param perPage 每页数量，默认10，最大80
     */
    @GET("collections/featured")
    suspend fun getFeaturedCollections(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10
    ): List<PexelsCollection>
    
    /**
     * 获取集合中的照片
     * @param id 集合ID
     * @param page 页码，从1开始
     * @param perPage 每页数量，默认10，最大80
     */
    @GET("collections/{id}")
    suspend fun getCollectionPhotos(
        @Path("id") id: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10
    ): PexelsSearchResponse
} 