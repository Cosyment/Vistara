package com.vistara.aestheticwalls.data.remote.api

import com.vistara.aestheticwalls.data.model.wallhaven.WallhavenSearchResponse
import com.vistara.aestheticwalls.data.model.wallhaven.WallhavenWallpaper
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Wallhaven API 服务接口
 * 文档参考: https://wallhaven.cc/help/api
 */
interface WallhavenApiService {
    companion object {
        const val BASE_URL = "https://wallhaven.cc/api/v1/"
        
        // 内容过滤类型
        const val PURITY_SFW = "1" // 安全内容
        const val PURITY_SKETCHY = "2" // 有争议内容
        const val PURITY_NSFW = "3" // 成人内容（本应用不使用）
        
        // 排序方式
        const val SORTING_DATE_ADDED = "date_added" // 上传日期
        const val SORTING_RELEVANCE = "relevance" // 相关性
        const val SORTING_RANDOM = "random" // 随机
        const val SORTING_VIEWS = "views" // 浏览量
        const val SORTING_FAVORITES = "favorites" // 收藏数
        const val SORTING_TOPLIST = "toplist" // 排行榜
    }

    /**
     * 搜索壁纸
     * @param query 搜索关键词
     * @param categories 分类，如111表示包含General、Anime和People三个分类
     * @param purity 内容过滤，如100表示只包含SFW内容
     * @param sorting 排序方式
     * @param order 排序顺序，desc（降序）或asc（升序）
     * @param page 页码，从1开始
     */
    @GET("search")
    suspend fun search(
        @Query("q") query: String = "",
        @Query("categories") categories: String = "111",
        @Query("purity") purity: String = PURITY_SFW, // 默认只显示安全内容
        @Query("sorting") sorting: String = SORTING_DATE_ADDED,
        @Query("order") order: String = "desc",
        @Query("page") page: Int = 1,
        @Query("atleast") minResolution: String? = null,
        @Query("resolutions") resolutions: String? = null,
        @Query("ratios") aspectRatios: String? = null,
        @Query("colors") colors: String? = null
    ): WallhavenSearchResponse

    /**
     * 获取壁纸详情
     * @param id 壁纸ID
     */
    @GET("w/{id}")
    suspend fun getWallpaper(@Path("id") id: String): WallhavenWallpaper
    
    /**
     * 获取标签相关壁纸
     * @param tagId 标签ID
     * @param page 页码，从1开始
     */
    @GET("tag/{id}")
    suspend fun getWallpapersByTag(
        @Path("id") tagId: Int,
        @Query("page") page: Int = 1,
        @Query("purity") purity: String = PURITY_SFW
    ): WallhavenSearchResponse
} 