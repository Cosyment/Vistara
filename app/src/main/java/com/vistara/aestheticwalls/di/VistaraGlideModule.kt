package com.vistara.aestheticwalls.di

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

/**
 * Vistara应用的Glide模块配置
 * 使用@GlideModule注解，Glide会在编译时生成必要的代码
 */
@GlideModule
class VistaraGlideModule : AppGlideModule() {

    /**
     * 配置Glide选项
     */
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)

        // 设置默认的请求选项
        builder.setDefaultRequestOptions(
            RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // 缓存所有版本的图片
                .skipMemoryCache(false) // 使用内存缓存
        )
    }

    /**
     * 禁用解析清单文件
     * 在这个应用中，我们不需要从清单文件中解析GlideModule
     */
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}