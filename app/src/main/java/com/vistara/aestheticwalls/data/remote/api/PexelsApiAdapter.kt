package com.vistara.aestheticwalls.data.remote.api

import com.vistara.aestheticwalls.data.mapper.PexelsMapper
import com.vistara.aestheticwalls.data.model.Collection
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.model.pexels.PexelsCollection
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.remote.ApiSource
import com.vistara.aestheticwalls.data.remote.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pexels API适配器实现
 * 将Pexels API转换为通用的壁纸API接口
 */
@Singleton
class PexelsApiAdapter @Inject constructor(
    private val pexelsApiService: PexelsApiService,
    private val pexelsMapper: PexelsMapper
) : WallpaperApiAdapter {

    override fun getApiSource(): ApiSource = ApiSource.PEXELS

    override suspend fun getFeaturedWallpapers(page: Int, pageSize: Int): ApiResult<List<Wallpaper>> {
        return safeApiCall(ApiSource.PEXELS) {
            val response = pexelsApiService.getCuratedPhotos(page = page, perPage = pageSize)
            pexelsMapper.toWallpapers(response.photos)
        }
    }

    override suspend fun searchWallpapers(
        query: String,
        page: Int,
        pageSize: Int,
        filters: Map<String, String>
    ): ApiResult<List<Wallpaper>> {
        return safeApiCall(ApiSource.PEXELS) {
            // 从过滤条件中提取参数
            val orientation = filters["orientation"]
            val size = filters["size"]
            val color = filters["color"]

            val response = pexelsApiService.searchPhotos(
                query = query,
                page = page,
                perPage = pageSize,
                orientation = orientation,
                size = size,
                color = color
            )
            pexelsMapper.toWallpapers(response.photos)
        }
    }

    override suspend fun getWallpaperById(id: String): ApiResult<Wallpaper?> {
        return safeApiCall(ApiSource.PEXELS) {
            val photo = pexelsApiService.getPhoto(id)
            pexelsMapper.toWallpaper(photo)
        }
    }

    override suspend fun getRandomWallpapers(count: Int, category: String?): ApiResult<List<Wallpaper>> {
        // Pexels API没有直接的随机壁纸接口，我们使用精选照片作为替代
        return safeApiCall(ApiSource.PEXELS) {
            // 使用随机页码来模拟随机效果
            val randomPage = (1..50).random()
            val response = pexelsApiService.getCuratedPhotos(page = randomPage, perPage = count)
            pexelsMapper.toWallpapers(response.photos)
        }
    }

    override suspend fun getCollections(page: Int, pageSize: Int): ApiResult<List<Collection>> {
        return safeApiCall(ApiSource.PEXELS) {
            val response = pexelsApiService.getFeaturedCollections(page = page, perPage = pageSize)
            response.collections.map { pexelsCollection ->
                mapPexelsCollection(pexelsCollection)
            }
        }
    }

    override suspend fun getWallpapersByCollection(
        collectionId: String,
        page: Int,
        pageSize: Int
    ): ApiResult<List<Wallpaper>> {
        return safeApiCall(ApiSource.PEXELS) {
            val response = pexelsApiService.getCollectionPhotos(
                id = collectionId,
                page = page,
                perPage = pageSize
            )
            // 检查media是否为null
            if (response.media == null) {
                return@safeApiCall emptyList<Wallpaper>()
            }

            // 过滤出照片类型的媒体，忽略视频等其他类型
            val photos = response.media.filter { it.type == null || it.type == "Photo" }
            if (photos.isEmpty()) {
                return@safeApiCall emptyList<Wallpaper>()
            }

            pexelsMapper.toWallpapers(photos)
        }
    }

    override suspend fun trackDownload(id: String): ApiResult<Unit> {
        // Pexels API不需要跟踪下载，直接返回成功
        return ApiResult.Success(Unit)
    }

    /**
     * 将Pexels集合转换为通用集合模型
     */
    private fun mapPexelsCollection(pexelsCollection: PexelsCollection): Collection {
        // 获取封面图
        val coverUrl = pexelsCollection.media?.firstOrNull()?.let { photo ->
            photo.src.medium
        }

        return Collection(
            id = "pexels_${pexelsCollection.id}",
            title = pexelsCollection.title,
            description = pexelsCollection.description,
            coverUrl = coverUrl,
            wallpaperCount = pexelsCollection.photos_count ?: 0,
            isPremium = false, // Pexels所有内容都是免费的
            wallpapers = emptyList(), // 初始为空，需要进一步获取
            tags = emptyList(), // Pexels集合没有标签
            source = "Pexels",
            sourceUrl = "https://www.pexels.com/collections/${pexelsCollection.id}",
            isFeatured = true // 从精选集合API获取的都是精选
        )
    }
}
