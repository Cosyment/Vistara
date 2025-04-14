package com.vistara.aestheticwalls.data.remote.api

import com.vistara.aestheticwalls.data.model.pexels.PexelsCollection
import com.vistara.aestheticwalls.data.model.pexels.PexelsCollectionMediaResponse
import com.vistara.aestheticwalls.data.model.pexels.PexelsCollectionsResponse
import com.vistara.aestheticwalls.data.model.pexels.PexelsPhoto
import com.vistara.aestheticwalls.data.model.pexels.PexelsSearchResponse
import com.vistara.aestheticwalls.data.model.pexels.PexelsVideo
import com.vistara.aestheticwalls.data.model.pexels.PexelsVideoSearchResponse
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
        const val VIDEO_BASE_URL = "https://api.pexels.com/videos/"
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
    ): PexelsCollectionsResponse

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
    ): PexelsCollectionMediaResponse

    /**
     * 获取精选视频
     * @param page 页码，从1开始
     * @param perPage 每页数量，默认10，最大80
     */
    @GET("popular")
    suspend fun getPopularVideos(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10
    ): PexelsVideoSearchResponse

    /**
     * 搜索视频
     * @param query 搜索关键词
     * @param page 页码，从1开始
     * @param perPage 每页数量，默认10，最大80
     * @param orientation 视频方向，landscape、portrait、square
     * @param size 视频尺寸，large(>1280x720)、medium(>960x540)、small(>640x360)
     */
    @GET("search")
    suspend fun searchVideos(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("orientation") orientation: String? = null,
        @Query("size") size: String? = null
    ): PexelsVideoSearchResponse

    /**
     * 获取视频详情
     * @param id 视频ID
     */
    @GET("videos/{id}")
    suspend fun getVideo(@Path("id") id: String): PexelsVideo
}