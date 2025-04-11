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
    fun toWallpapers(sources: List<T>): List<Wallpaper> = sources.map { toWallpaper(it) }
}

/**
 * Unsplash壁纸数据转换器
 */
@Singleton
class UnsplashMapper @Inject constructor() : WallpaperMapper<UnsplashPhoto> {

    override fun toWallpaper(source: UnsplashPhoto): Wallpaper {
        return Wallpaper(
            id = "unsplash_${source.id}",
            title = source.description ?: source.alt_description,
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
            downloadCount = source.likes, // 使用likes作为下载量的近似值
            resolution = Resolution(source.width, source.height))
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
            title = source.alt,
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
            resolution = Resolution(source.width, source.height))
    }
}

/**
 * Pixabay壁纸数据转换器
 */
@Singleton
class PixabayMapper @Inject constructor() : WallpaperMapper<PixabayImage> {

    override fun toWallpaper(source: PixabayImage): Wallpaper {
        val tagsList = source.tags.split(",").map { it.trim() }
        
        return Wallpaper(
            id = "pixabay_${source.id}",
            title = null,
            url = source.largeImageURL,
            thumbnailUrl = source.webformatURL,
            previewUrl = source.webformatURL,
            width = source.width,
            height = source.height,
            author = source.user,
            authorUrl = source.userImageURL,
            source = "Pixabay",
            sourceUrl = source.pageURL,
            attributionRequired = false,
            isPremium = false,
            isLive = false,
            tags = tagsList,
            downloadCount = source.downloads,
            resolution = Resolution(source.width, source.height))
    }
}

/**
 * Wallhaven壁纸数据转换器
 */
@Singleton
class WallhavenMapper @Inject constructor() : WallpaperMapper<WallhavenWallpaper> {

    override fun toWallpaper(source: WallhavenWallpaper): Wallpaper {
        return Wallpaper(
            id = "wallhaven_${source.id}",
            title = null,
            url = source.path,
            thumbnailUrl = source.thumbs.small,
            previewUrl = source.thumbs.original,
            width = source.dimensionX,
            height = source.dimensionY,
            author = source.uploader?.username,
            source = "Wallhaven",
            sourceUrl = source.url,
            attributionRequired = false,
            isPremium = source.category == "people" && source.purity != "sfw",
            isLive = false,
            tags = source.tags.map { it.name },
            resolution = Resolution(source.dimensionX, source.dimensionY))
    }
} 