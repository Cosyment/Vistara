package com.vistara.aestheticwalls.data.mapper

import com.vistara.aestheticwalls.data.model.Resolution
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.model.pexels.PexelsPhoto
import com.vistara.aestheticwalls.data.model.pixabay.PixabayImage
import com.vistara.aestheticwalls.data.model.unsplash.UnsplashPhoto
import com.vistara.aestheticwalls.data.model.wallhaven.WallhavenWallpaper
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 壁纸数据转换器接口
 * 用于将不同API的响应模型转换为统一的Wallpaper数据模型
 */
interface WallpaperMapper<T> {
    /**
     * 将API特定的模型转换为通用Wallpaper模型
     */
    fun toWallpaper(source: T): Wallpaper
    
    /**
     * 将API特定的模型列表转换为通用Wallpaper模型列表
     */
    fun toWallpapers(sources: List<T>): List<Wallpaper> {
        return sources.map { toWallpaper(it) }
    }
}

/**
 * Unsplash壁纸数据转换器
 */
@Singleton
class UnsplashMapper @Inject constructor() : WallpaperMapper<UnsplashPhoto> {
    
    override fun toWallpaper(source: UnsplashPhoto): Wallpaper {
        return Wallpaper(
            id = "unsplash_${source.id}",
            title = source.description ?: source.altDescription,
            description = source.description,
            url = source.urls.full,
            thumbnailUrl = source.urls.small,
            previewUrl = source.urls.regular,
            width = source.width,
            height = source.height,
            author = source.user.name,
            authorUrl = source.user.links.html,
            source = "Unsplash",
            sourceUrl = source.links.html,
            attributionRequired = true, // Unsplash API要求必须显示署名
            isPremium = false, // Unsplash所有照片都可免费使用
            isLive = false, // Unsplash不提供动态壁纸
            tags = source.tags?.map { it.title } ?: emptyList(),
            createdAt = System.currentTimeMillis(),
            downloadCount = source.likes, // 使用likes作为下载量的近似值
            resolution = Resolution(source.width, source.height)
        )
    }
}

/**
 * Pexels壁纸数据转换器
 */
@Singleton
class PexelsMapper @Inject constructor() : WallpaperMapper<PexelsPhoto> {
    
    override fun toWallpaper(source: PexelsPhoto): Wallpaper {
        return Wallpaper(
            id = "pexels_${source.id}",
            title = source.alt ?: "Pexels Photo",
            url = source.src.original,
            thumbnailUrl = source.src.medium,
            previewUrl = source.src.large,
            width = source.width,
            height = source.height,
            author = source.photographer,
            authorUrl = source.photographerUrl,
            source = "Pexels",
            sourceUrl = source.url,
            attributionRequired = true, // Pexels API要求显示署名
            isPremium = false, // Pexels所有照片都可免费使用
            isLive = false, // Pexels不提供动态壁纸
            resolution = Resolution(source.width, source.height)
        )
    }
}

/**
 * Pixabay壁纸数据转换器
 */
@Singleton
class PixabayMapper @Inject constructor() : WallpaperMapper<PixabayImage> {
    
    override fun toWallpaper(source: PixabayImage): Wallpaper {
        // 从tags字符串中提取标签列表
        val tagsList = source.tags.split(",").map { it.trim() }
        
        return Wallpaper(
            id = "pixabay_${source.id}",
            title = null, // Pixabay不提供图片标题
            url = source.largeImageURL,
            thumbnailUrl = source.webformatURL,
            previewUrl = source.webformatURL,
            width = source.imageWidth,
            height = source.imageHeight,
            author = source.user,
            authorUrl = source.userImageURL,
            source = "Pixabay",
            sourceUrl = source.pageURL,
            attributionRequired = false, // Pixabay不要求署名，但推荐
            isPremium = false, // Pixabay所有图片都可免费使用
            isLive = false, // Pixabay不提供动态壁纸
            tags = tagsList,
            createdAt = System.currentTimeMillis(),
            downloadCount = source.downloads,
            resolution = Resolution(source.imageWidth, source.imageHeight)
        )
    }
}

/**
 * Wallhaven壁纸数据转换器
 */
@Singleton
class WallhavenMapper @Inject constructor() : WallpaperMapper<WallhavenWallpaper> {
    
    override fun toWallpaper(source: WallhavenWallpaper): Wallpaper {
        // 解析分辨率字符串，如 "1920x1080"
        val (width, height) = parseResolution(source.resolution)
        
        return Wallpaper(
            id = "wallhaven_${source.id}",
            title = null, // Wallhaven不提供壁纸标题
            url = source.path,
            thumbnailUrl = source.thumbs.small,
            previewUrl = source.thumbs.original,
            width = width,
            height = height,
            author = source.uploader.username,
            source = "Wallhaven",
            sourceUrl = source.url,
            attributionRequired = false,
            // 根据category判断是否为高级壁纸，例如一些特殊分类可能是付费内容
            isPremium = source.category == "people" && source.purity != "sfw",
            isLive = false, // Wallhaven不提供动态壁纸
            tags = source.tags.map { it.name },
            categoryIds = listOf(source.category),
            createdAt = source.createdAt,
            resolution = Resolution(width, height)
        )
    }
    
    /**
     * 解析分辨率字符串，如 "1920x1080"
     * @return Pair<宽度, 高度>
     */
    private fun parseResolution(resolution: String): Pair<Int, Int> {
        return try {
            val parts = resolution.split("x")
            if (parts.size == 2) {
                Pair(parts[0].toInt(), parts[1].toInt())
            } else {
                Pair(0, 0)
            }
        } catch (e: Exception) {
            Pair(0, 0)
        }
    }
} 