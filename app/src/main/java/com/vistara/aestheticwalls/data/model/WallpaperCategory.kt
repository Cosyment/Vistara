package com.vistara.aestheticwalls.data.model

import androidx.annotation.StringRes
import com.vistara.aestheticwalls.R

/**
 * 壁纸分类枚举
 * @param apiValue 用于 API 请求的英文值（必须与后端 API 参数保持一致）
 * @param titleRes 显示在 UI 上的多语言资源 ID
 */
enum class WallpaperCategory(val apiValue: String, @StringRes val titleRes: Int) {
    ALL("all", R.string.category_all),
    ABSTRACT("abstract", R.string.category_abstract),
    ANIMALS("animals", R.string.category_animals),
    ARCHITECTURE("architecture", R.string.category_architecture),
    ART("art", R.string.category_art),
    CARS("cars", R.string.category_cars),
    CITY("city", R.string.category_city),
    DARK("dark", R.string.category_dark),
    FASHION("fashion", R.string.category_fashion),
    FLOWERS("flowers", R.string.category_flowers),
    FOOD("food", R.string.category_food),
    LANDSCAPE("landscape", R.string.category_landscape),
    LOVE("love", R.string.category_love),
    MINIMAL("minimal", R.string.category_minimal),
    NATURE("nature", R.string.category_nature),
    PEOPLE("people", R.string.category_people),
    SPACE("space", R.string.category_space),
    SPORTS("sports", R.string.category_sports),
    TECHNOLOGY("technology", R.string.category_technology),
    TRAVEL("travel", R.string.category_travel),
    CYBERPUNK("cyberpunk", R.string.category_cyberpunk),
    FLUID("fluid", R.string.category_fluid),
    PARTICLE("particle", R.string.category_particle),
    PORTRAIT("portrait", R.string.category_portrait),
    ILLUSTRATION("illustration", R.string.category_illustration);

    companion object {
        /**
         * 获取所有分类
         */
        fun getAllCategories(): List<WallpaperCategory> = values().toList()

        /**
         * 获取常用分类（用于首页展示）
         */
        fun getCommonCategories(): List<WallpaperCategory> = listOf(
            ALL, NATURE, ABSTRACT, MINIMAL, DARK, ARCHITECTURE, ANIMALS, TECHNOLOGY
        )
    }
}
