package com.vistara.aestheticwalls.data.remote.api

import com.vistara.aestheticwalls.data.mapper.UnsplashMapper
import com.vistara.aestheticwalls.data.model.Collection
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.model.unsplash.UnsplashCollection
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.remote.ApiSource
import com.vistara.aestheticwalls.data.remote.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unsplash API适配器实现
 * 将Unsplash API转换为通用的壁纸API接口
 */
@Singleton
class UnsplashApiAdapter @Inject constructor(
    private val unsplashApiService: UnsplashApiService,
    private val unsplashMapper: UnsplashMapper
) : WallpaperApiAdapter {

    override fun getApiSource(): ApiSource = ApiSource.UNSPLASH

    override suspend fun getFeaturedWallpapers(page: Int, pageSize: Int): ApiResult<List<Wallpaper>> {
        return safeApiCall(ApiSource.UNSPLASH) {
            val response = unsplashApiService.getPhotos(page = page, perPage = pageSize)
            unsplashMapper.toWallpapers(response)
        }
    }

    override suspend fun searchWallpapers(
        query: String,
        page: Int,
        pageSize: Int,
        filters: Map<String, String>
    ): ApiResult<List<Wallpaper>> {
        return safeApiCall(ApiSource.UNSPLASH) {
            // 从过滤条件中提取参数
            val color = filters["color"]
            val orderBy = filters["order_by"] ?: "relevant"

            val response = unsplashApiService.searchPhotos(
                query = query,
                page = page,
                perPage = pageSize,
                orderBy = orderBy,
                color = color
            )
            unsplashMapper.toWallpapers(response.results)
        }
    }

    override suspend fun getWallpaperById(id: String): ApiResult<Wallpaper?> {
        return safeApiCall(ApiSource.UNSPLASH) {
            val photo = unsplashApiService.getPhoto(id)
            unsplashMapper.toWallpaper(photo)
        }
    }

    override suspend fun getRandomWallpapers(count: Int, category: String?): ApiResult<List<Wallpaper>> {
        return safeApiCall(ApiSource.UNSPLASH) {
            val response = unsplashApiService.getRandomPhotos(
                count = count,
                query = category
            )
            unsplashMapper.toWallpapers(response)
        }
    }

    override suspend fun getCollections(page: Int, pageSize: Int): ApiResult<List<Collection>> {
        return safeApiCall(ApiSource.UNSPLASH) {
            val response = unsplashApiService.getFeaturedCollections(page = page, perPage = pageSize)
            val collections = mutableListOf<Collection>()
            for (unsplashCollection in response) {
                collections.add(mapUnsplashCollection(unsplashCollection))
            }
            collections
        }
    }

    override suspend fun getWallpapersByCollection(
        collectionId: String,
        page: Int,
        pageSize: Int
    ): ApiResult<List<Wallpaper>> {
        return safeApiCall(ApiSource.UNSPLASH) {
            val response = unsplashApiService.getCollectionPhotos(
                id = collectionId,
                page = page,
                perPage = pageSize
            )
            unsplashMapper.toWallpapers(response)
        }
    }

    override suspend fun trackDownload(id: String): ApiResult<Unit> {
        return safeApiCall(ApiSource.UNSPLASH) {
            unsplashApiService.trackDownload(id)
            Unit
        }
    }

    /**
     * 将Unsplash集合转换为通用集合模型
     */
    private fun mapUnsplashCollection(unsplashCollection: UnsplashCollection): Collection {
        return Collection(
            id = "unsplash_${unsplashCollection.id}",
            title = unsplashCollection.title,
            description = unsplashCollection.description,
            coverUrl = unsplashCollection.coverPhoto.urls.regular,
            wallpaperCount = unsplashCollection.totalPhotos,
            isPremium = false, // Unsplash所有内容都是免费的
            wallpapers = emptyList(), // 初始为空，需要进一步获取
            tags = unsplashCollection.tags?.map { it.title } ?: emptyList(),
            source = "Unsplash",
            sourceUrl = unsplashCollection.links.html,
            isFeatured = unsplashCollection.featured,
            creator = unsplashCollection.user.name
        )
    }
}
