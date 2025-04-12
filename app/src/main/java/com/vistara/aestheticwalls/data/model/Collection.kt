package com.vistara.aestheticwalls.data.model

/**
 * 壁纸专题/集合数据模型
 * @param id 专题唯一标识，格式为"平台_原始ID"，如"pexels_123"
 * @param title 专题标题
 * @param description 专题描述
 * @param coverUrl 专题封面图URL
 * @param wallpaperCount 专题中壁纸数量
 * @param isPremium 是否为高级专题（需要付费解锁）
 * @param wallpapers 专题中的壁纸（可选，初始可能为空，需要进一步获取）
 * @param source 来源平台，如"pexels", "unsplash"
 * @param sourceUrl 原始集合页面URL
 * @param isFeatured 是否为精选集合
 */
data class Collection(
    val id: String,
    val title: String,
    val description: String? = null,
    val coverUrl: String? = null,
    val wallpaperCount: Int = 0,
    val isPremium: Boolean = false,
    val wallpapers: List<Wallpaper> = emptyList(),
    val tags: List<String> = emptyList(),
    val source: String? = null,
    val sourceUrl: String? = null,
    val isFeatured: Boolean = false,
    val creator: String? = null
)

/**
 * 首页Banner数据模型
 * @param id 唯一标识
 * @param imageUrl Banner图片URL
 * @param title 标题
 * @param subtitle 副标题
 * @param actionType 点击操作类型
 * @param actionTarget 点击操作目标（可能是专题ID、URL等）
 */
data class Banner(
    val id: String,
    val imageUrl: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val actionType: BannerActionType,
    val actionTarget: String? = null
)

/**
 * Banner点击操作类型
 */
enum class BannerActionType {
    COLLECTION, // 跳转到专题详情
    WALLPAPER,  // 跳转到壁纸详情
    PREMIUM,    // 跳转到付费引导页
    URL         // 跳转到外部链接
}

/**
 * 分类数据模型
 */
data class Category(
    val id: String,
    val name: String,
    val description: String? = null,
    val iconUrl: String? = null,
    val coverUrl: String? = null,
    val wallpaperCount: Int = 0,
    val isPremium: Boolean = false,
    val isHidden: Boolean = false,
    val sortOrder: Int = 0
)