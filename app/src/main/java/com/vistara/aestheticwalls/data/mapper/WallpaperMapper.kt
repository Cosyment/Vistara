package com.vistara.aestheticwalls.data.mapper

import com.vistara.aestheticwalls.data.model.Resolution
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.model.pexels.PexelsPhoto
import com.vistara.aestheticwalls.data.model.pexels.PexelsVideo
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
 * 支持照片和视频转换为壁纸
 */
@Singleton
class PexelsMapper @Inject constructor() {

    /**
     * 将Pexels照片转换为壁纸
     */
    fun toWallpaper(source: PexelsPhoto): Wallpaper {
        return Wallpaper(
            id = "pexels_photo_${source.id}",
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
            isLive = false, // 照片不是动态壁纸
            resolution = Resolution(source.width, source.height))
    }

    /**
     * 将Pexels视频转换为壁纸
     */
    fun toWallpaper(source: PexelsVideo): Wallpaper {
        // 获取适合移动设备的中等质量视频文件
        // 选择分辨率适中的MP4文件，避免选择过高分辨率导致性能问题
        val videoFile = source.videoFiles
            .filter { it.file_type == "video/mp4" } // 只使用MP4格式
            .filter { it.width <= 1280 && it.height <= 720 } // 限制分辨率不超过720p
            .maxByOrNull { it.width * it.height } // 选择满足条件的最高分辨率
            ?: source.videoFiles
                .filter { it.file_type == "video/mp4" }
                .minByOrNull { it.width * it.height } // 如果没有满足条件的，选择最低分辨率

        // 获取最高质量的预览图
        val bestPreview = source.videoPictures.firstOrNull()?.picture ?: source.image

        // 根据视频属性生成标签
        val tags = mutableListOf<String>("\u52a8\u6001")

        // 根据视频尺寸添加分类标签
        if (source.width > source.height) {
            tags.add("\u98ce\u666f")
            tags.add("\u81ea\u7136")
        } else if (source.width < source.height) {
            tags.add("\u4eba\u50cf")
        } else {
            tags.add("\u65b9\u5f62")
        }

        // 根据视频ID添加随机标签
        when (source.id % 5) {
            0 -> tags.add("\u62bd\u8c61")
            1 -> tags.add("\u79d1\u6280\u611f")
            2 -> tags.add("\u8d5b\u535a\u670b\u514b")
            3 -> tags.add("\u7c92\u5b50")
            4 -> tags.add("\u6d41\u4f53")
        }

        // 生成更友好的标题
        // 使用视频尺寸和属性来生成描述性标题
        val title = when {
            // 根据视频尺寸判断类型
            source.width > source.height * 1.5 -> "\u5168\u666f\u52a8\u6001\u58c1\u7eb8" // 宽屏全景
            source.width > source.height -> "\u98ce\u666f\u52a8\u6001\u58c1\u7eb8" // 普通横向
            source.height > source.width * 1.5 -> "\u7ad6\u5c4f\u52a8\u6001\u58c1\u7eb8" // 竖屏
            source.height > source.width -> "\u4eba\u50cf\u52a8\u6001\u58c1\u7eb8" // 竖向
            else -> "\u65b9\u5f62\u52a8\u6001\u58c1\u7eb8" // 正方形
        }

        // 选择最适合的视频URL
        val videoUrl = if (videoFile != null) {
            // 使用直接的视频文件URL
            videoFile.link
        } else {
            // 如果没有找到适合的视频文件，使用默认URL
            // 注意：这可能不是直接可播放的URL
            "https://www.pexels.com/video/${source.id}/download"
        }

        // 打印日志以便调试
        android.util.Log.d("PexelsMapper", "Video URL for ID ${source.id}: $videoUrl")
        if (videoFile != null) {
            android.util.Log.d("PexelsMapper", "Selected video file: ${videoFile.quality}, ${videoFile.width}x${videoFile.height}")
        }

        return Wallpaper(
            id = "pexels_video_${source.id}",
            title = "",
            url = videoUrl, // 使用选择的视频URL
            thumbnailUrl = bestPreview, // 使用最佳预览图
            previewUrl = bestPreview,
            width = source.width,
            height = source.height,
            author = source.user.name,
            authorUrl = source.user.url,
            source = "Pexels",
            sourceUrl = source.url,
            attributionRequired = true, // Pexels API要求显示署名
            isPremium = source.id % 3 == 0, // 每三个视频中有一个是高级内容
            isLive = true, // 这是动态壁纸
            tags = tags, // 添加标签便于分类
            resolution = Resolution(source.width, source.height))
    }

    /**
     * 将Pexels照片列表转换为壁纸列表
     */
    fun toWallpapersFromPhotos(sources: List<PexelsPhoto>): List<Wallpaper> = sources.map { toWallpaper(it) }

    /**
     * 将Pexels视频列表转换为壁纸列表
     */
    fun toWallpapersFromVideos(sources: List<PexelsVideo>): List<Wallpaper> = sources.map { toWallpaper(it) }
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