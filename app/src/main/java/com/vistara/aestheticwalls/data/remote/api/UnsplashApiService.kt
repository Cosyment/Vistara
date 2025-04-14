package com.vistara.aestheticwalls.data.remote.api

import com.vistara.aestheticwalls.data.model.unsplash.UnsplashCollection
import com.vistara.aestheticwalls.data.model.unsplash.UnsplashPhoto
import com.vistara.aestheticwalls.data.model.unsplash.UnsplashSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Unsplash API 服务接口
 * 文档参考: https://unsplash.com/documentation
 */
interface UnsplashApiService {
    companion object {
        const val BASE_URL = "https://api.unsplash.com/"
    }

    /**
     * 获取精选照片
     * @param page 页码，从1开始
     * @param perPage 每页数量，默认10
     * @param orderBy 排序方式，最新(latest)、最热(popular)、精选(editorial)
     */
    @GET("photos")
    suspend fun getPhotos(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("order_by") orderBy: String = "editorial"
    ): List<UnsplashPhoto>

    /**
     * 搜索照片
     * @param query 搜索关键词
     * @param page 页码，从1开始
     * @param perPage 每页数量，默认10
     * @param orderBy 排序方式，相关性(relevant)、最新(latest)
     * @param color 过滤颜色，如black_and_white、black、white、yellow、orange、red、purple、blue、green...
     */
    @GET("search/photos")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("order_by") orderBy: String = "relevant",
        @Query("color") color: String? = null
    ): UnsplashSearchResponse

    /**
     * 获取照片详情
     * @param id 照片ID
     */
    @GET("photos/{id}")
    suspend fun getPhoto(@Path("id") id: String): UnsplashPhoto

    /**
     * 获取随机照片
     * @param count 照片数量，默认1
     * @param query 搜索关键词(可选)
     * @param orientation 照片方向，landscape、portrait、squarish
     */
    @GET("photos/random")
    suspend fun getRandomPhotos(
        @Query("count") count: Int = 1,
        @Query("query") query: String? = null,
        @Query("orientation") orientation: String? = null
    ): List<UnsplashPhoto>

    /**
     * 获取相关照片（类似推荐）
     * @param id 照片ID
     */
    @GET("photos/{id}/related")
    suspend fun getRelatedPhotos(@Path("id") id: String): List<UnsplashPhoto>

    /**
     * 跟踪照片下载（必须调用！）
     * 当用户下载照片时，必须调用此API更新下载次数，这是Unsplash API使用条款要求
     * @param id 照片ID
     */
    @GET("photos/{id}/download")
    suspend fun trackDownload(@Path("id") id: String): Any

    /**
     * 获取精选集合
     * @param page 页码，从1开始
     * @param perPage 每页数量，默认10
     */
    @GET("collections/featured")
    suspend fun getFeaturedCollections(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10
    ): List<UnsplashCollection>

    /**
     * 获取集合中的照片
     * @param id 集合ID
     * @param page 页码，从1开始
     * @param perPage 每页数量，默认10
     */
    @GET("collections/{id}/photos")
    suspend fun getCollectionPhotos(
        @Path("id") id: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10
    ): List<UnsplashPhoto>
}