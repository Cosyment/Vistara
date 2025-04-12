package com.vistara.aestheticwalls.data.remote

import android.content.Context
import com.vistara.aestheticwalls.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API密钥管理类
 * 负责提供和管理各平台的API密钥
 * 集中管理密钥，便于后续切换到更安全的密钥存储方式
 */
@Singleton
class ApiKeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * 获取Unsplash API密钥
     */
    fun getUnsplashApiKey(): String {
        return BuildConfig.UNSPLASH_API_KEY
    }

    /**
     * 获取Pexels API密钥
     */
    fun getPexelsApiKey(): String {
        return BuildConfig.PEXELS_API_KEY
    }

    /**
     * 获取Pixabay API密钥
     */
    fun getPixabayApiKey(): String {
        return BuildConfig.PIXABAY_API_KEY
    }

    /**
     * 获取Wallhaven API密钥
     */
    fun getWallhavenApiKey(): String {
        return BuildConfig.WALLHAVEN_API_KEY
    }

    /**
     * 判断是否为调试模式
     */
    fun isDebugMode(): Boolean {
        return BuildConfig.DEBUG
    }
}