package com.vistara.aestheticwalls.data.repository

import android.util.Log
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.local.WallpaperDao
import com.vistara.aestheticwalls.data.mapper.PexelsMapper
import com.vistara.aestheticwalls.data.mapper.PixabayMapper
import com.vistara.aestheticwalls.data.mapper.UnsplashMapper
import com.vistara.aestheticwalls.data.mapper.WallhavenMapper
import com.vistara.aestheticwalls.data.model.AutoChangeHistory
import com.vistara.aestheticwalls.data.model.Category
import com.vistara.aestheticwalls.data.model.Resolution
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.remote.ApiLoadBalancer
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.remote.ApiSource
import com.vistara.aestheticwalls.data.remote.ApiUsageTracker
import com.vistara.aestheticwalls.data.remote.api.PexelsApiAdapter
import com.vistara.aestheticwalls.data.remote.api.PexelsApiService
import com.vistara.aestheticwalls.data.remote.api.PixabayApiService
import com.vistara.aestheticwalls.data.remote.api.UnsplashApiService
import com.vistara.aestheticwalls.data.remote.api.WallhavenApiService
import com.vistara.aestheticwalls.data.remote.api.WallpaperApiAdapter
import com.vistara.aestheticwalls.data.remote.safeApiCall
import com.vistara.aestheticwalls.utils.NetworkMonitor
import com.vistara.aestheticwalls.utils.StringProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
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
    private val pexelsApiAdapter: WallpaperApiAdapter, // Pexels API适配器，用于获取视频
    private val stringProvider: StringProvider // 字符串提供者，用于获取字符串资源
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
                    message = stringProvider.getString(R.string.no_network_wallpapers),
                    source = wallpaperApiAdapter.getApiSource()
                )
            }

            // 直接使用壁纸API适配器获取精选壁纸
            return@withContext wallpaperApiAdapter.getFeaturedWallpapers(page, pageSize)
        } catch (e: Exception) {
            ApiResult.Error(
                message = e.message ?: stringProvider.getString(R.string.failed_to_get_recommended_wallpapers),
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

                            // 生成更友好的标题
                            val title = when (index % 8) {
                                0 -> "流动的光影"
                                1 -> "炫酷动态壁纸"
                                2 -> "流动的背景"
                                3 -> "灵动的粒子"
                                4 -> "流体艺术"
                                5 -> "科技感动态"
                                6 -> "赛博朋克风格"
                                else -> "灵动壁纸"
                            }

                            // 使用更真实的缩略图 URL
                            val imageId = (index + page * pageSize) % 1000 + 100
                            val thumbnailUrl = "https://picsum.photos/id/$imageId/${width / 4}/${height / 4}"

                            // 使用不同的视频URL来确保每个壁纸都有不同的视频
                            val videoUrl = when (index % 5) {
                                0 -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                                1 -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
                                2 -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                                3 -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
                                else -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
                            }

                            Wallpaper(
                                id = "live_${page}_$index",
                                title = title,
                                url = videoUrl,
                                thumbnailUrl = thumbnailUrl,
                                previewUrl = "https://picsum.photos/id/$imageId/${width / 2}/${height / 2}",
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
            ApiResult.Error(message = e.message ?: stringProvider.getString(R.string.failed_to_get_wallpapers), source = ApiSource.UNSPLASH)
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
    override suspend fun getWallpapersByCategory(categoryId: String, page: Int, pageSize: Int): ApiResult<List<Wallpaper>> =
        withContext(Dispatchers.IO) {
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
                        message = stringProvider.getString(R.string.no_network_category_wallpapers),
                        source = ApiSource.UNSPLASH
                    )
                }

                // 根据分类ID确定搜索关键词和API源
                val (apiSource, query) = getCategoryInfo(categoryId)

                // 根据不同的API源获取壁纸
                val wallpapers = when (apiSource) {
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

                // 如果获取到的壁纸为空，生成模拟数据
                if (wallpapers.isEmpty()) {
                    // 生成模拟数据
                    val mockWallpapers = generateMockWallpapers(categoryId, page, pageSize)
                    return@withContext ApiResult.Success(mockWallpapers)
                }

                ApiResult.Success(wallpapers)
            } catch (e: Exception) {
                Log.e(TAG, "按分类获取壁纸失败", e)
                ApiResult.Error(
                    message = e.message ?: stringProvider.getString(R.string.failed_to_get_category_wallpapers),
                    source = ApiSource.UNSPLASH
                )
            }
        }

    /**
     * 生成模拟壁纸数据
     */
    private fun generateMockWallpapers(categoryId: String, page: Int, pageSize: Int): List<Wallpaper> {
        val (_, query) = getCategoryInfo(categoryId)

        return List(pageSize) { index ->
            // 生成随机宽高比
            val isLandscape = index % 3 != 1 // 大部分是横向
            val width = if (isLandscape) 1920 else 1080
            val height = if (isLandscape) 1080 else 1920

            // 生成随机标签
            val tags = mutableListOf(query)

            // 根据宽高比添加分类标签
            if (isLandscape) {
                tags.add("风景")
            } else {
                tags.add("人像")
            }

            // 生成更友好的标题
            val title = when (query) {
                "自然风景" -> "自然风光 ${index + 1}"
                "建筑" -> "建筑之美 ${index + 1}"
                "动物" -> "动物世界 ${index + 1}"
                "抽象" -> "抽象艺术 ${index + 1}"
                "太空" -> "浮游太空 ${index + 1}"
                "简约" -> "简约设计 ${index + 1}"
                else -> "$query ${index + 1}"
            }

            // 使用更真实的缩略图 URL
            val imageId = (index + page * pageSize) % 1000 + 100
            val thumbnailUrl = "https://picsum.photos/id/$imageId/${width / 4}/${height / 4}"

            Wallpaper(
                id = "${categoryId}_${page}_$index",
                title = title,
                url = "https://picsum.photos/id/$imageId/$width/$height",
                thumbnailUrl = thumbnailUrl,
                previewUrl = "https://picsum.photos/id/$imageId/${width / 2}/${height / 2}",
                author = "Vistara",
                source = "Vistara",
                isPremium = index % 5 == 0, // 每五个壁纸中有一个是高级内容
                isLive = false,
                tags = tags,
                width = width,
                height = height,
                resolution = Resolution(width, height)
            )
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

        // 特殊处理动态壁纸ID
        if (id.startsWith("live_")) {
            Log.d(TAG, "处理模拟动态壁纸ID: $id")
            return recreateLiveWallpaper(id)
        }

        // 从ID解析来源和原始ID
        val parts = id.split("_")
        if (parts.size < 2) return null

        val source = parts[0]

        // 处理Pexels视频壁纸
        if (id.startsWith("pexels_video_")) {
            Log.d(TAG, "处理Pexels视频壁纸ID: $id")
            // 提取原始ID，去掉"pexels_video_"前缀
            val videoId = id.substringAfter("pexels_video_")
            try {
                // 使用PexelsApiService获取视频详情
                val video = pexelsApiService.getVideo(videoId)
                return pexelsMapper.toWallpaper(video)
            } catch (e: Exception) {
                Log.e(TAG, "获取Pexels视频详情失败: ${e.message}")

                // 如果获取失败，尝试从本地缓存中获取
                // 直接使用getWallpaperById方法再次尝试获取
                val cachedWallpaper = wallpaperDao.getWallpaperById(id)
                if (cachedWallpaper != null) {
                    Log.d(TAG, "从缓存中找到壁纸: $id")
                    return cachedWallpaper
                }

                // 如果缓存中也没有，创建一个模拟的壁纸对象
                Log.d(TAG, "创建模拟的Pexels视频壁纸: $id")
                return createMockVideoWallpaper(id, videoId.toIntOrNull() ?: 1000)
            }
        }

        // 处理Pexels照片壁纸
        if (id.startsWith("pexels_photo_")) {
            Log.d(TAG, "处理Pexels照片壁纸ID: $id")
            // 提取原始ID，去掉"pexels_photo_"前缀
            val photoId = id.substringAfter("pexels_photo_")
            try {
                // 使用PexelsApiService获取照片详情
                val photo = pexelsApiService.getPhoto(photoId)
                return pexelsMapper.toWallpaper(photo)
            } catch (e: Exception) {
                Log.e(TAG, "获取Pexels照片详情失败: ${e.message}")

                // 如果获取失败，尝试从本地缓存中获取
                val cachedWallpaper = wallpaperDao.getWallpaperById(id)
                if (cachedWallpaper != null) {
                    Log.d(TAG, "从缓存中找到壁纸: $id")
                    return cachedWallpaper
                }

                // 如果缓存中也没有，创建一个模拟的壁纸对象
                Log.d(TAG, "创建模拟的Pexels照片壁纸: $id")
                return createMockPhotoWallpaper(id, photoId, "Pexels")
            }
        }

        // 如果是其他Pexels照片来源，使用壁纸API适配器
        val originalId = parts[1]
        if (source == "pexels") {
            try {
                val result = wallpaperApiAdapter.getWallpaperById(originalId)
                return when (result) {
                    is ApiResult.Success -> result.data
                    else -> {
                        // 如果API调用失败，尝试从缓存中获取
                        val cachedWallpaper = wallpaperDao.getWallpaperById(id)
                        if (cachedWallpaper != null) {
                            Log.d(TAG, "从缓存中找到Pexels壁纸: $id")
                            return cachedWallpaper
                        }
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取Pexels壁纸详情失败: ${e.message}")
                // 尝试从缓存中获取
                val cachedWallpaper = wallpaperDao.getWallpaperById(id)
                if (cachedWallpaper != null) {
                    return cachedWallpaper
                }
                return null
            }
        }

        // 其他来源使用原来的方式，但增强错误处理
        return when (source) {
            "unsplash" -> {
                try {
                    val response = safeApiCall(ApiSource.UNSPLASH) {
                        unsplashApiService.getPhoto(originalId)
                    }

                    when (response) {
                        is ApiResult.Success -> unsplashMapper.toWallpaper(response.data)
                        else -> {
                            // 如果API调用失败，尝试从缓存中获取
                            val cachedWallpaper = wallpaperDao.getWallpaperById(id)
                            if (cachedWallpaper != null) {
                                Log.d(TAG, "从缓存中找到Unsplash壁纸: $id")
                                return cachedWallpaper
                            }
                            // 如果缓存中也没有，创建一个模拟的壁纸对象
                            Log.d(TAG, "创建模拟的Unsplash壁纸: $id")
                            return createMockPhotoWallpaper(id, originalId, "Unsplash")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "获取Unsplash壁纸详情失败: ${e.message}")
                    // 尝试从缓存中获取
                    val cachedWallpaper = wallpaperDao.getWallpaperById(id)
                    if (cachedWallpaper != null) {
                        return cachedWallpaper
                    }
                    // 创建模拟壁纸
                    return createMockPhotoWallpaper(id, originalId, "Unsplash")
                }
            }

            "pixabay" -> {
                try {
                    val response = safeApiCall(ApiSource.PIXABAY) {
                        pixabayApiService.getImageById(originalId.toInt())
                    }

                    when (response) {
                        is ApiResult.Success -> {
                            if (response.data.hits.isNotEmpty()) {
                                pixabayMapper.toWallpaper(response.data.hits[0])
                            } else {
                                // 如果没有结果，尝试从缓存中获取
                                val cachedWallpaper = wallpaperDao.getWallpaperById(id)
                                if (cachedWallpaper != null) {
                                    Log.d(TAG, "从缓存中找到Pixabay壁纸: $id")
                                    return cachedWallpaper
                                }
                                // 创建模拟壁纸
                                return createMockPhotoWallpaper(id, originalId, "Pixabay")
                            }
                        }

                        else -> {
                            // 如果API调用失败，尝试从缓存中获取
                            val cachedWallpaper = wallpaperDao.getWallpaperById(id)
                            if (cachedWallpaper != null) {
                                Log.d(TAG, "从缓存中找到Pixabay壁纸: $id")
                                return cachedWallpaper
                            }
                            // 创建模拟壁纸
                            return createMockPhotoWallpaper(id, originalId, "Pixabay")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "获取Pixabay壁纸详情失败: ${e.message}")
                    // 尝试从缓存中获取
                    val cachedWallpaper = wallpaperDao.getWallpaperById(id)
                    if (cachedWallpaper != null) {
                        return cachedWallpaper
                    }
                    // 创建模拟壁纸
                    return createMockPhotoWallpaper(id, originalId, "Pixabay")
                }
            }

            "wallhaven" -> {
                try {
                    val response = safeApiCall(ApiSource.WALLHAVEN) {
                        wallhavenApiService.getWallpaper(originalId)
                    }

                    when (response) {
                        is ApiResult.Success -> wallhavenMapper.toWallpaper(response.data)
                        else -> {
                            // 如果API调用失败，尝试从缓存中获取
                            val cachedWallpaper = wallpaperDao.getWallpaperById(id)
                            if (cachedWallpaper != null) {
                                Log.d(TAG, "从缓存中找到Wallhaven壁纸: $id")
                                return cachedWallpaper
                            }
                            // 创建模拟壁纸
                            return createMockPhotoWallpaper(id, originalId, "Wallhaven")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "获取Wallhaven壁纸详情失败: ${e.message}")
                    // 尝试从缓存中获取
                    val cachedWallpaper = wallpaperDao.getWallpaperById(id)
                    if (cachedWallpaper != null) {
                        return cachedWallpaper
                    }
                    // 创建模拟壁纸
                    return createMockPhotoWallpaper(id, originalId, "Wallhaven")
                }
            }

            else -> {
                // 对于未知来源，尝试从缓存中获取
                val cachedWallpaper = wallpaperDao.getWallpaperById(id)
                if (cachedWallpaper != null) {
                    Log.d(TAG, "从缓存中找到未知来源壁纸: $id")
                    return cachedWallpaper
                }
                // 如果缓存中也没有，创建一个通用的模拟壁纸对象
                Log.d(TAG, "创建通用模拟壁纸: $id")
                return createMockPhotoWallpaper(id, originalId, "Unknown")
            }
        }
    }

    /**
     * 重新创建动态壁纸对象
     * 用于处理动态壁纸详情页
     */
    private fun recreateLiveWallpaper(id: String): Wallpaper? {
        try {
            // 解析ID格式：live_page_index
            val parts = id.split("_")
            if (parts.size < 3) return null

            val page = parts[1].toIntOrNull() ?: 1
            val index = parts[2].toIntOrNull() ?: 0

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

            // 生成更友好的标题
            val title = when (index % 8) {
                0 -> "流动的光影"
                1 -> "炫酷动态壁纸"
                2 -> "流动的背景"
                3 -> "灵动的粒子"
                4 -> "流体艺术"
                5 -> "科技感动态"
                6 -> "赛博朋克风格"
                else -> "灵动壁纸"
            }

            // 使用更真实的缩略图 URL
            // 使用与原始生成逻辑相同的pageSize值(20)
            val pageSize = 20
            val imageId = (index + page * pageSize) % 1000 + 100
            val thumbnailUrl = "https://picsum.photos/id/$imageId/${width / 4}/${height / 4}"

            // 使用不同的视频URL来确保每个壁纸都有不同的视频
            val videoUrl = when (index % 5) {
                0 -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                1 -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
                2 -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                3 -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
                else -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
            }

            return Wallpaper(
                id = id,
                title = title,
                url = videoUrl,
                thumbnailUrl = thumbnailUrl,
                previewUrl = "https://picsum.photos/id/$imageId/${width / 2}/${height / 2}",
                author = "动画师 ${index + 1}",
                source = "Vistara",
                isPremium = index % 3 == 0, // 每三个视频中有一个是高级内容
                isLive = true,
                tags = tags,
                width = width,
                height = height,
                resolution = Resolution(width, height)
            )
        } catch (e: Exception) {
            Log.e(TAG, "重新创建动态壁纸失败: ${e.message}")
            return null
        }
    }

    /**
     * 创建模拟的Pexels视频壁纸
     * 用于处理网络错误时的回退方案
     */
    private fun createMockVideoWallpaper(id: String, videoId: Int): Wallpaper? {
        try {
            // 生成随机宽高比
            val isLandscape = videoId % 3 != 1 // 大部分是横向
            val width = if (isLandscape) 1280 else 720
            val height = if (isLandscape) 720 else 1280

            // 生成随机标签
            val tags = mutableListOf("动态", "Pexels")

            // 根据宽高比添加分类标签
            if (isLandscape) {
                tags.add("风景")
                tags.add("自然")
            } else {
                tags.add("人像")
            }

            // 根据视频ID添加随机标签
            when (videoId % 5) {
                0 -> tags.add("抽象")
                1 -> tags.add("科技感")
                2 -> tags.add("赛博朋克")
                3 -> tags.add("粒子")
                4 -> tags.add("流体")
            }

            // 生成更友好的标题
            val title = when (videoId % 8) {
                0 -> "流动的光影"
                1 -> "炫酷动态壁纸"
                2 -> "流动的背景"
                3 -> "灵动的粒子"
                4 -> "流体艺术"
                5 -> "科技感动态"
                6 -> "赛博朋克风格"
                else -> "灵动壁纸"
            }

            // 使用更真实的缩略图 URL
            val imageId = videoId % 1000 + 100
            val thumbnailUrl = "https://picsum.photos/id/$imageId/${width / 2}/${height / 2}"

            // 使用不同的视频URL来确保每个壁纸都有不同的视频
            val videoUrl = when (videoId % 5) {
                0 -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                1 -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
                2 -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                3 -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
                else -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
            }

            return Wallpaper(
                id = id,
                title = title,
                url = videoUrl,
                thumbnailUrl = thumbnailUrl,
                previewUrl = "https://picsum.photos/id/$imageId/$width/$height",
                author = "Pexels 作者 $videoId",
                source = "Pexels",
                isPremium = videoId % 3 == 0, // 每三个视频中有一个是高级内容
                isLive = true,
                tags = tags,
                width = width,
                height = height,
                resolution = Resolution(width, height)
            )
        } catch (e: Exception) {
            Log.e(TAG, "创建模拟的Pexels视频壁纸失败: ${e.message}")
            return null
        }
    }

    /**
     * 创建模拟的静态壁纸
     * 用于处理网络错误时的回退方案
     */
    private fun createMockPhotoWallpaper(id: String, photoId: String, source: String): Wallpaper? {
        try {
            // 生成随机宽高比
            val numericId = photoId.hashCode() // 将字符串ID转换为数字
            val isLandscape = numericId % 3 != 1 // 大部分是横向
            val width = if (isLandscape) 1920 else 1080
            val height = if (isLandscape) 1080 else 1920

            // 生成随机标签
            val tags = mutableListOf<String>()

            // 根据来源添加标签
            tags.add(source)

            // 根据宽高比添加分类标签
            if (isLandscape) {
                tags.add("风景")
                tags.add("自然")
            } else {
                tags.add("人像")
            }

            // 根据照片ID添加随机标签
            when (numericId % 5) {
                0 -> tags.add("抽象")
                1 -> tags.add("自然")
                2 -> tags.add("城市")
                3 -> tags.add("动物")
                4 -> tags.add("建筑")
            }

            // 生成随机标题
            val title = when (numericId % 4) {
                0 -> "$source 壁纸 $photoId"
                1 -> "$source 精选 $photoId"
                2 -> "$source 高清壁纸 $photoId"
                else -> "$source 壁纸 $photoId"
            }

            // 使用更真实的缩略图 URL
            val imageId = Math.abs(numericId) % 1000 + 100
            val thumbnailUrl = "https://picsum.photos/id/$imageId/${width / 4}/${height / 4}"
            val previewUrl = "https://picsum.photos/id/$imageId/${width / 2}/${height / 2}"
            val fullUrl = "https://picsum.photos/id/$imageId/$width/$height"

            return Wallpaper(
                id = id,
                title = title,
                url = fullUrl,
                thumbnailUrl = thumbnailUrl,
                previewUrl = previewUrl,
                author = "$source 作者 $photoId",
                source = source,
                isPremium = numericId % 3 == 0, // 每三个壁纸中有一个是高级内容
                isLive = false, // 这是静态壁纸
                tags = tags,
                width = width,
                height = height,
                resolution = Resolution(width, height)
            )
        } catch (e: Exception) {
            Log.e(TAG, "创建模拟的静态壁纸失败: ${e.message}")
            return null
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
            Category(
                "pixabay_space",
                "太空星空",
                "浩瀚宇宙的奥秘",
                "https://pixabay.com/get/g3c5223df6fab2f7a71dc0dc74302a6bcaef06ea54b1eeef55fb66c81c3fac9aa1e673c4e5de69e7ee61c8b83acb47458_1280.jpg"
            ),
            Category("wallhaven_digital-art", "数字艺术", "创意设计与数字艺术", "https://w.wallhaven.cc/full/dp/wallhaven-dpevlo.jpg"),
            Category("unsplash_minimal", "简约风格", "极简主义设计美学", "https://images.unsplash.com/photo-1449247709967-d4461a6a6103"),
            Category("pexels_abstract", "抽象艺术", "抽象与超现实主义", "https://images.pexels.com/photos/2110951/pexels-photo-2110951.jpeg"),
            Category(
                "pixabay_flowers",
                "花卉植物",
                "绚丽多彩的花卉世界",
                "https://pixabay.com/get/gbaed09c11ef0d9fb5d6b08e92a2784dfb5b32c3a7fd5bdf11dafbe60e11c3eaa3e5c58ffcaef0e621687e97c7a4b08fc_1280.jpg"
            )
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
            // 创建一个新的Wallpaper对象，将isFavorite设置为true
            val favoriteWallpaper = wallpaper.copy(isFavorite = true)
            wallpaperDao.insertFavorite(favoriteWallpaper)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error favoriting wallpaper: ${e.message}")
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

    override suspend fun trackWallpaperDownload(wallpaperId: String) { withContext(Dispatchers.IO) {
        // 如果是Unsplash的壁纸，需要调用下载追踪API
        if (wallpaperId.startsWith("unsplash_")) {
            val originalId = wallpaperId.substringAfter("unsplash_")
            try {
                unsplashApiService.trackDownload(originalId)
            } catch (e: Exception) {
                Log.e(TAG, "调用Unsplash下载追踪API失败", e)
            }
        }

        // 检查壁纸是否已存在于数据库中
        val existingWallpaper = wallpaperDao.getWallpaperById(wallpaperId)

        if (existingWallpaper != null) {
            // 如果壁纸已存在，直接标记为已下载
            try {
                wallpaperDao.insertDownload(wallpaperId)
                Log.d(TAG, "标记已存在壁纸为已下载: $wallpaperId")
            } catch (e: Exception) {
                Log.e(TAG, "标记壁纸为已下载失败", e)
            }
        } else {
            // 如果壁纸不存在，需要先获取壁纸详情并插入到数据库
            try {
                // 获取壁纸详情
                val wallpaper = getWallpaperById(wallpaperId)

                if (wallpaper != null) {
                    // 创建一个新的壁纸对象，将isDownloaded设置为true
                    val downloadedWallpaper = wallpaper.copy(isDownloaded = true)
                    // 插入到数据库
                    wallpaperDao.insertFavorite(downloadedWallpaper)
                    Log.d(TAG, "插入新壁纸并标记为已下载: $wallpaperId")
                } else {
                    Log.e(TAG, "无法获取壁纸详情，无法记录下载: $wallpaperId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "插入新壁纸并标记为已下载失败", e)
            }
        }
    }}

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
     * 获取相关壁纸推荐
     */
    override suspend fun getRelatedWallpapers(wallpaperId: String, limit: Int): List<Wallpaper> {
        // 获取当前壁纸
        val currentWallpaper = getWallpaperById(wallpaperId) ?: return emptyList()

        // 使用壁纸的标签作为搜索关键词
        val tags = currentWallpaper.tags
        if (tags.isEmpty()) {
            // 如果没有标签，返回随机壁纸
            return getTrendingWallpapers(1, limit)
        }

        // 随机选择一个标签作为搜索关键词
        val randomTag = tags.random()

        // 搜索相关壁纸
        val relatedWallpapers = searchWallpapers(randomTag, 1, limit + 1)

        // 过滤掉当前壁纸
        return relatedWallpapers.filter { it.id != wallpaperId }.take(limit)
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