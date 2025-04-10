package com.vistara.aestheticwalls.data.repository

import com.vistara.aestheticwalls.data.model.AutoChangeHistory
import com.vistara.aestheticwalls.data.model.Category
import com.vistara.aestheticwalls.data.model.Wallpaper
import java.io.File
import kotlinx.coroutines.flow.Flow

/**
 * 壁纸仓库接口
 * 定义所有与壁纸相关的数据操作
 */
interface WallpaperRepository {

    /**
     * 获取推荐壁纸
     */
    suspend fun getFeaturedWallpapers(page: Int, pageSize: Int): List<Wallpaper>
    
    /**
     * 获取热门壁纸
     */
    suspend fun getTrendingWallpapers(page: Int, pageSize: Int): List<Wallpaper>
    
    /**
     * 按分类获取壁纸
     */
    suspend fun getWallpapersByCategory(categoryId: String, page: Int, pageSize: Int): List<Wallpaper>
    
    /**
     * 搜索壁纸
     */
    suspend fun searchWallpapers(query: String, page: Int, pageSize: Int): List<Wallpaper>
    
    /**
     * 获取壁纸详情
     */
    suspend fun getWallpaperById(id: String): Wallpaper?
    
    /**
     * 获取随机壁纸
     */
    suspend fun getRandomWallpaper(): Wallpaper?
    
    /**
     * 按分类获取随机壁纸
     */
    suspend fun getRandomWallpaperByCategory(categoryId: String): Wallpaper?
    
    /**
     * 获取随机收藏壁纸
     */
    suspend fun getRandomFavoriteWallpaper(): Wallpaper?
    
    /**
     * 获取随机下载壁纸
     */
    suspend fun getRandomDownloadedWallpaper(): Wallpaper?
    
    /**
     * 获取随机热门壁纸
     */
    suspend fun getRandomTrendingWallpaper(): Wallpaper?
    
    /**
     * 获取所有分类
     */
    suspend fun getCategories(): List<Category>
    
    /**
     * 获取分类详情
     */
    suspend fun getCategoryById(id: String): Category?
    
    /**
     * 收藏壁纸
     */
    suspend fun favoriteWallpaper(wallpaper: Wallpaper): Boolean
    
    /**
     * 取消收藏壁纸
     */
    suspend fun unfavoriteWallpaper(wallpaperId: String): Boolean
    
    /**
     * 检查壁纸是否已收藏
     */
    suspend fun isWallpaperFavorited(wallpaperId: String): Boolean
    
    /**
     * 获取收藏的壁纸
     */
    fun getFavoriteWallpapers(): Flow<List<Wallpaper>>
    
    /**
     * 获取下载的壁纸
     */
    fun getDownloadedWallpapers(): Flow<List<Wallpaper>>
    
    /**
     * 记录壁纸下载
     */
    suspend fun trackWallpaperDownload(wallpaperId: String)
    
    /**
     * 获取本地文件
     */
    suspend fun getLocalFile(wallpaperId: String): File?
    
    /**
     * 记录自动更换历史
     */
    suspend fun recordAutoChangeHistory(history: AutoChangeHistory)
    
    /**
     * 获取自动更换历史
     */
    fun getAutoChangeHistory(): Flow<List<AutoChangeHistory>>
} 