package com.vistara.aestheticwalls.data.remote.api

import com.vistara.aestheticwalls.data.model.Collection
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.remote.ApiSource

/**
 * 壁纸API适配器接口
 * 定义所有壁纸平台共有的操作，实现平台无关的抽象
 */
interface WallpaperApiAdapter {
    /**
     * 获取API来源
     */
    fun getApiSource(): ApiSource

    /**
     * 获取精选壁纸
     * @param page 页码，从1开始
     * @param pageSize 每页数量
     * @return 壁纸列表
     */
    suspend fun getFeaturedWallpapers(page: Int, pageSize: Int): ApiResult<List<Wallpaper>>

    /**
     * 搜索壁纸
     * @param query 搜索关键词
     * @param page 页码，从1开始
     * @param pageSize 每页数量
     * @param filters 过滤条件，如颜色、方向等
     * @return 壁纸列表
     */
    suspend fun searchWallpapers(
        query: String, 
        page: Int, 
        pageSize: Int,
        filters: Map<String, String> = emptyMap()
    ): ApiResult<List<Wallpaper>>

    /**
     * 获取壁纸详情
     * @param id 壁纸ID（原始ID，不包含前缀）
     * @return 壁纸详情
     */
    suspend fun getWallpaperById(id: String): ApiResult<Wallpaper?>

    /**
     * 获取随机壁纸
     * @param count 数量
     * @param category 分类
     * @return 壁纸列表
     */
    suspend fun getRandomWallpapers(
        count: Int, 
        category: String? = null
    ): ApiResult<List<Wallpaper>>

    /**
     * 获取壁纸集合列表
     * @param page 页码，从1开始
     * @param pageSize 每页数量
     * @return 集合列表
     */
    suspend fun getCollections(
        page: Int, 
        pageSize: Int
    ): ApiResult<List<Collection>>

    /**
     * 获取集合中的壁纸
     * @param collectionId 集合ID
     * @param page 页码，从1开始
     * @param pageSize 每页数量
     * @return 壁纸列表
     */
    suspend fun getWallpapersByCollection(
        collectionId: String, 
        page: Int, 
        pageSize: Int
    ): ApiResult<List<Wallpaper>>

    /**
     * 跟踪壁纸下载
     * @param id 壁纸ID（原始ID，不包含前缀）
     */
    suspend fun trackDownload(id: String): ApiResult<Unit>
}
