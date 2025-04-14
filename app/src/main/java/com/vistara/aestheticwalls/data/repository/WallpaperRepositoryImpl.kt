package com.vistara.aestheticwalls.data.repository

import android.util.Log
import com.vistara.aestheticwalls.data.local.WallpaperDao
import com.vistara.aestheticwalls.data.mapper.PexelsMapper
import com.vistara.aestheticwalls.data.mapper.PixabayMapper
import com.vistara.aestheticwalls.data.mapper.UnsplashMapper
import com.vistara.aestheticwalls.data.mapper.WallhavenMapper
import com.vistara.aestheticwalls.data.model.AutoChangeHistory
import com.vistara.aestheticwalls.data.model.Category
import com.vistara.aestheticwalls.data.model.Resolution
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.remote.ApiSource
import com.vistara.aestheticwalls.data.remote.ApiLoadBalancer
import com.vistara.aestheticwalls.data.remote.ApiUsageTracker
import com.vistara.aestheticwalls.data.remote.api.PexelsApiAdapter
import com.vistara.aestheticwalls.data.remote.api.PexelsApiService
import com.vistara.aestheticwalls.data.remote.api.PixabayApiService
import com.vistara.aestheticwalls.data.remote.api.UnsplashApiService
import com.vistara.aestheticwalls.data.remote.api.WallhavenApiService
import com.vistara.aestheticwalls.data.remote.api.WallpaperApiAdapter
import com.vistara.aestheticwalls.data.remote.safeApiCall
import com.vistara.aestheticwalls.utils.NetworkMonitor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 壁纸仓库实现类
 * 整合各API数据源，提供统一的数据访问接口
 */
@Singleton
class WallpaperRepositoryImpl @Inject constructor(
    private val unsplashApiService: UnsplashApiService,
    private val pexelsApiService: PexelsApiService,
    private val pixabayApiService: PixabayApiService,
    private val wallhavenApiService: WallhavenApiService,
    private val unsplashMapper: UnsplashMapper,
    private val pexelsMapper: PexelsMapper,
    private val pixabayMapper: PixabayMapper,
    private val wallhavenMapper: WallhavenMapper,
    private val wallpaperDao: WallpaperDao,
    private val apiLoadBalancer: ApiLoadBalancer,
    private val apiUsageTracker: ApiUsageTracker,
    private val networkMonitor: NetworkMonitor,
    private val wallpaperApiAdapter: WallpaperApiAdapter, // 新增的壁纸API适配器
    private val pexelsApiAdapter: WallpaperApiAdapter // Pexels API适配器，用于获取视频
) : WallpaperRepository {

    companion object {
        private const val TAG = "WallpaperRepositoryImpl"
    }

    /**
     * 获取推荐壁纸
     * 使用壁纸API适配器获取数据
     */
    override suspend fun getFeaturedWallpapers(page: Int, pageSize: Int): ApiResult<List<Wallpaper>> = withContext(Dispatchers.IO) {
        try {
            // 检查网络连接
            if (!networkMonitor.isNetworkAvailable()) {
                // 如果无网络连接，尝试从本地获取数据
                val localWallpapers = wallpaperDao.getFavoritesList()
                if (localWallpapers.isNotEmpty()) {
                    return@withContext ApiResult.Success(localWallpapers)
                }
                // 如果本地没有数据，返回错误
                return@withContext ApiResult.Error(
                    message = "无网络连接，无法获取壁纸",
                    source = wallpaperApiAdapter.getApiSource()
                )
            }

            // 直接使用壁纸API适配器获取精选壁纸
            return@withContext wallpaperApiAdapter.getFeaturedWallpapers(page, pageSize)
        } catch (e: Exception) {
            ApiResult.Error(
                message = e.message ?: "获取推荐壁纸失败",
                source = wallpaperApiAdapter.getApiSource()
            )
        }
    }

    override suspend fun getWallpapers(
        type: String,
        page: Int,
        pageSize: Int
    ): ApiResult<List<Wallpaper>> = withContext(Dispatchers.IO) {
        try {
            val wallpapers = when (type.lowercase()) {
                "static" -> {
                    // 获取静态壁纸，使用 Unsplash 和 Pexels 的组合
                    val unsplashResponse = safeApiCall(ApiSource.UNSPLASH) {
                        unsplashApiService.getPhotos(page = page, perPage = pageSize / 2)
                    }

                    val pexelsResponse = safeApiCall(ApiSource.PEXELS) {
                        pexelsApiService.getCuratedPhotos(page = page, perPage = pageSize / 2)
                    }

                    val unsplashWallpapers = when (unsplashResponse) {
                        is ApiResult.Success -> unsplashMapper.toWallpapers(unsplashResponse.data)
                        else -> emptyList()
                    }

                    val pexelsWallpapers = when (pexelsResponse) {
                        is ApiResult.Success -> pexelsMapper.toWallpapersFromPhotos(pexelsResponse.data.photos)
                        else -> emptyList()
                    }

                    unsplashWallpapers + pexelsWallpapers
                }
                "live" -> {
                    // 使用 Pexels 的视频 API 获取动态壁纸
                    val pexelsAdapter = pexelsApiAdapter as PexelsApiAdapter
                    val pexelsResponse = pexelsAdapter.getPopularVideos(page, pageSize)

                    val pexelsVideos = when (pexelsResponse) {
                        is ApiResult.Success -> pexelsResponse.data
                        else -> emptyList()
                    }

                    // 如果没有获取到视频数据，返回模拟数据
                    if (pexelsVideos.isEmpty()) {
                        Log.w(TAG, "未能从 Pexels API 获取视频数据，使用模拟数据")

                        // 生成更多样的模拟数据，并且添加与分类匹配的标签
                        List(pageSize) { index ->
                            // 生成随机宽高比
                            val isLandscape = index % 3 != 1 // 大部分是横向
                            val width = if (isLandscape) 1920 else 1080
                            val height = if (isLandscape) 1080 else 1920

                            // 生成随机标签
                            val tags = mutableListOf("动态")

                            // 根据宽高比添加分类标签
                            if (isLandscape) {
                                tags.add("风景")
                                tags.add("自然")
                            } else {
                                tags.add("人像")
                            }

                            // 根据索引添加随机标签
                            when (index % 5) {
                                0 -> tags.add("抽象")
                                1 -> tags.add("科技感")
                                2 -> tags.add("赛博朋克")
                                3 -> tags.add("粒子")
                                4 -> tags.add("流体")
                            }

                            // 生成随机标题
                            val title = when (index % 4) {
                                0 -> "动态壁纸 ${page}_$index"
                                1 -> "炫酷动态 ${page}_$index"
                                2 -> "流动背景 ${page}_$index"
                                else -> "动态壁纸 ${page}_$index"
                            }

                            // 使用更真实的缩略图 URL
                            val imageId = (index + page * pageSize) % 1000 + 100
                            val thumbnailUrl = "https://picsum.photos/id/$imageId/${width/4}/${height/4}"

                            Wallpaper(
                                id = "live_${page}_$index",
                                title = title,
                                url = "https://example.com/live_wallpaper${page}_$index.mp4",
                                thumbnailUrl = thumbnailUrl,
                                previewUrl = "https://picsum.photos/id/$imageId/${width/2}/${height/2}",
                                author = "动画师 ${index + 1}",
                                source = "Vistara",
                                isPremium = index % 3 == 0, // 每三个视频中有一个是高级内容
                                isLive = true,
                                tags = tags,
                                width = width,
                                height = height,
                                resolution = Resolution(width, height)
                            )
                        }
                    } else {
                        pexelsVideos
                    }
                }
                else -> emptyList()
            }

            ApiResult.Success(wallpapers)
        } catch (e: Exception) {
            ApiResult.Error(message = e.message ?: "获取壁纸失败", source = ApiSource.UNSPLASH)
        }
    }

    /**
     * 获取热门壁纸，综合多个来源
     */
    override suspend fun getTrendingWallpapers(page: Int, pageSize: Int): List<Wallpaper> {
        // 使用Unsplash的热门图片
        val response = safeApiCall(ApiSource.UNSPLASH) {
            unsplashApiService.getPhotos(
                page = page,
                perPage = pageSize,
                orderBy = "popular"
            )
        }

        return when (response) {
            is ApiResult.Success -> unsplashMapper.toWallpapers(response.data)
            else -> emptyList()
        }
    }

    /**
     * 按分类获取壁纸
     */
    override suspend fun getWallpapersByCategory(categoryId: String, page: Int, pageSize: Int): List<Wallpaper> {
        // 根据分类ID确定搜索关键词和API源
        val (apiSource, query) = getCategoryInfo(categoryId)

        return when (apiSource) {
            ApiSource.UNSPLASH -> {
                val response = safeApiCall(ApiSource.UNSPLASH) {
                    unsplashApiService.searchPhotos(query = query, page = page, perPage = pageSize)
                }

                when (response) {
                    is ApiResult.Success -> unsplashMapper.toWallpapers(response.data.results)
                    else -> emptyList()
                }
            }
            ApiSource.PEXELS -> {
                val response = safeApiCall(ApiSource.PEXELS) {
                    pexelsApiService.searchPhotos(query = query, page = page, perPage = pageSize)
                }

                when (response) {
                    is ApiResult.Success -> pexelsMapper.toWallpapersFromPhotos(response.data.photos)
                    else -> emptyList()
                }
            }
            ApiSource.PIXABAY -> {
                val response = safeApiCall(ApiSource.PIXABAY) {
                    pixabayApiService.searchImages(query = query, page = page, perPage = pageSize)
                }

                when (response) {
                    is ApiResult.Success -> pixabayMapper.toWallpapers(response.data.hits)
                    else -> emptyList()
                }
            }
            ApiSource.WALLHAVEN -> {
                val response = safeApiCall(ApiSource.WALLHAVEN) {
                    wallhavenApiService.search(query = query, page = page)
                }

                when (response) {
                    is ApiResult.Success -> wallhavenMapper.toWallpapers(response.data.data)
                    else -> emptyList()
                }
            }
        }
    }

    /**
     * 根据分类ID获取API源和搜索关键词
     */
    private fun getCategoryInfo(categoryId: String): Pair<ApiSource, String> {
        val parts = categoryId.split("_")
        return if (parts.size >= 2) {
            val source = when (parts[0]) {
                "unsplash" -> ApiSource.UNSPLASH
                "pexels" -> ApiSource.PEXELS
                "pixabay" -> ApiSource.PIXABAY
                "wallhaven" -> ApiSource.WALLHAVEN
                else -> ApiSource.UNSPLASH
            }
            val query = parts[1].replace("-", " ")
            Pair(source, query)
        } else {
            Pair(ApiSource.UNSPLASH, "nature")
        }
    }

    /**
     * 搜索壁纸
     * 使用壁纸API适配器搜索壁纸
     */
    override suspend fun searchWallpapers(query: String, page: Int, pageSize: Int): List<Wallpaper> {
        // 检查网络连接
        if (!networkMonitor.isNetworkAvailable()) {
            // 如果无网络连接，尝试从本地获取数据
            val localWallpapers = wallpaperDao.searchFavorites("%$query%")
            if (localWallpapers.isNotEmpty()) {
                return localWallpapers
            }
            return emptyList()
        }

        // 使用壁纸API适配器搜索壁纸
        val result = wallpaperApiAdapter.searchWallpapers(query, page, pageSize)
        return when (result) {
            is ApiResult.Success -> result.data
            else -> emptyList()
        }
    }

    /**
     * 获取壁纸详情
     * 使用壁纸API适配器获取壁纸详情
     */
    override suspend fun getWallpaperById(id: String): Wallpaper? {
        // 从本地数据库先查询
        val localWallpaper = wallpaperDao.getWallpaperById(id)
        if (localWallpaper != null) {
            return localWallpaper
        }

        // 检查网络连接
        if (!networkMonitor.isNetworkAvailable()) {
            return null
        }

        // 从ID解析来源和原始ID
        val parts = id.split("_")
        if (parts.size < 2) return null

        val source = parts[0]
        val originalId = parts[1]

        // 如果是Pexels来源，使用壁纸API适配器
        if (source == "pexels") {
            val result = wallpaperApiAdapter.getWallpaperById(originalId)
            return when (result) {
                is ApiResult.Success -> result.data
                else -> null
            }
        }

        // 其他来源仍然使用原来的方式
        return when (source) {
            "unsplash" -> {
                val response = safeApiCall(ApiSource.UNSPLASH) {
                    unsplashApiService.getPhoto(originalId)
                }

                when (response) {
                    is ApiResult.Success -> unsplashMapper.toWallpaper(response.data)
                    else -> null
                }
            }
            "pixabay" -> {
                val response = safeApiCall(ApiSource.PIXABAY) {
                    pixabayApiService.getImageById(originalId.toInt())
                }

                when (response) {
                    is ApiResult.Success -> {
                        if (response.data.hits.isNotEmpty()) {
                            pixabayMapper.toWallpaper(response.data.hits[0])
                        } else null
                    }
                    else -> null
                }
            }
            "wallhaven" -> {
                val response = safeApiCall(ApiSource.WALLHAVEN) {
                    wallhavenApiService.getWallpaper(originalId)
                }

                when (response) {
                    is ApiResult.Success -> wallhavenMapper.toWallpaper(response.data)
                    else -> null
                }
            }
            else -> null
        }
    }

    /**
     * 获取随机壁纸
     * 使用壁纸API适配器获取随机壁纸
     */
    override suspend fun getRandomWallpaper(): Wallpaper? {
        // 检查网络连接
        if (!networkMonitor.isNetworkAvailable()) {
            // 如果无网络连接，尝试从本地获取随机收藏壁纸
            val favorites = wallpaperDao.getFavoritesList()
            return if (favorites.isNotEmpty()) {
                favorites.random()
            } else null
        }

        // 使用壁纸API适配器获取随机壁纸
        val result = wallpaperApiAdapter.getRandomWallpapers(count = 1)
        return when (result) {
            is ApiResult.Success -> result.data.firstOrNull()
            else -> null
        }
    }

    /**
     * 按分类获取随机壁纸
     */
    override suspend fun getRandomWallpaperByCategory(categoryId: String): Wallpaper? {
        val (apiSource, query) = getCategoryInfo(categoryId)

        // 使用Unsplash的随机API + 查询参数
        val response = safeApiCall(ApiSource.UNSPLASH) {
            unsplashApiService.getRandomPhotos(count = 1, query = query)
        }

        return when (response) {
            is ApiResult.Success -> {
                if (response.data.isNotEmpty()) {
                    unsplashMapper.toWallpaper(response.data[0])
                } else null
            }
            else -> null
        }
    }

    /**
     * 获取所有分类
     */
    override suspend fun getCategories(): List<Category> {
        // 返回预定义的分类列表
        return listOf(
            Category("unsplash_nature", "自然风景", "大自然的壮丽景色", "https://images.unsplash.com/photo-1433086966358-54859d0ed716"),
            Category("unsplash_architecture", "建筑设计", "令人惊叹的建筑作品", "https://images.unsplash.com/photo-1487958449943-2429e8be8625"),
            Category("pexels_animals", "动物世界", "可爱与野性的动物", "https://images.pexels.com/photos/567540/pexels-photo-567540.jpeg"),
            Category("pixabay_space", "太空星空", "浩瀚宇宙的奥秘", "https://pixabay.com/get/g3c5223df6fab2f7a71dc0dc74302a6bcaef06ea54b1eeef55fb66c81c3fac9aa1e673c4e5de69e7ee61c8b83acb47458_1280.jpg"),
            Category("wallhaven_digital-art", "数字艺术", "创意设计与数字艺术", "https://w.wallhaven.cc/full/dp/wallhaven-dpevlo.jpg"),
            Category("unsplash_minimal", "简约风格", "极简主义设计美学", "https://images.unsplash.com/photo-1449247709967-d4461a6a6103"),
            Category("pexels_abstract", "抽象艺术", "抽象与超现实主义", "https://images.pexels.com/photos/2110951/pexels-photo-2110951.jpeg"),
            Category("pixabay_flowers", "花卉植物", "绚丽多彩的花卉世界", "https://pixabay.com/get/gbaed09c11ef0d9fb5d6b08e92a2784dfb5b32c3a7fd5bdf11dafbe60e11c3eaa3e5c58ffcaef0e621687e97c7a4b08fc_1280.jpg")
        )
    }

    /**
     * 获取分类详情
     */
    override suspend fun getCategoryById(id: String): Category? {
        return getCategories().find { it.id == id }
    }

    /**
     * 下面是收藏、下载等本地数据相关的功能实现
     */

    override suspend fun favoriteWallpaper(wallpaper: Wallpaper): Boolean = withContext(Dispatchers.IO) {
        try {
            wallpaperDao.insertFavorite(wallpaper)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun unfavoriteWallpaper(wallpaperId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            wallpaperDao.deleteFavorite(wallpaperId)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun isWallpaperFavorited(wallpaperId: String): Boolean = withContext(Dispatchers.IO) {
        wallpaperDao.isFavorite(wallpaperId)
    }

    override fun getFavoriteWallpapers(): Flow<List<Wallpaper>> {
        return wallpaperDao.getAllFavorites()
    }

    override fun getDownloadedWallpapers(): Flow<List<Wallpaper>> {
        return wallpaperDao.getAllDownloaded()
    }

    override suspend fun trackWallpaperDownload(wallpaperId: String) {
        // 如果是Unsplash的壁纸，需要调用下载追踪API
        if (wallpaperId.startsWith("unsplash_")) {
            val originalId = wallpaperId.substringAfter("unsplash_")
            try {
                unsplashApiService.trackDownload(originalId)
            } catch (e: Exception) {
                // 忽略错误，但应该记录日志
            }
        }

        // 记录到本地下载历史
        try {
            wallpaperDao.insertDownload(wallpaperId)
        } catch (e: Exception) {
            // 忽略错误，但应该记录日志
        }
    }

    override suspend fun getLocalFile(wallpaperId: String): File? {
        // 从本地存储中获取已下载的文件
        // 具体实现依赖于文件存储机制
        return null // 暂时返回null，待实现
    }

    override suspend fun recordAutoChangeHistory(history: AutoChangeHistory) {
        // 记录自动更换历史
        try {
            wallpaperDao.insertAutoChangeHistory(history)
        } catch (e: Exception) {
            // 忽略错误，但应该记录日志
        }
    }

    override fun getAutoChangeHistory(): Flow<List<AutoChangeHistory>> {
        return wallpaperDao.getAutoChangeHistory()
    }

    /**
     * 获取随机收藏壁纸
     */
    override suspend fun getRandomFavoriteWallpaper(): Wallpaper? = withContext(Dispatchers.IO) {
        // 从收藏列表中随机选择一张
        val favorites = wallpaperDao.getFavoritesList()
        favorites.randomOrNull()
    }

    /**
     * 获取随机下载壁纸
     */
    override suspend fun getRandomDownloadedWallpaper(): Wallpaper? = withContext(Dispatchers.IO) {
        // 从下载列表中随机选择一张
        val downloaded = wallpaperDao.getDownloadedList()
        downloaded.randomOrNull()
    }

    /**
     * 获取随机热门壁纸
     */
    override suspend fun getRandomTrendingWallpaper(): Wallpaper? {
        // 从热门壁纸中随机选择一张
        val trending = getTrendingWallpapers(1, 10)
        return trending.randomOrNull()
    }
}