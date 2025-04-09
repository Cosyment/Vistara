package com.vistara.aestheticwalls.data.remote

import javax.inject.Inject
import javax.inject.Singleton

/**
 * API密钥管理类
 * 负责提供和管理各平台的API密钥
 * 集中管理密钥，便于后续切换到更安全的密钥存储方式
 */
@Singleton
class ApiKeyManager @Inject constructor() {
    
    /**
     * 获取Unsplash API密钥
     */
    fun getUnsplashApiKey(): String {
        // 实际项目中应该使用正确的API密钥
        // 此处仅用于演示，实际开发中应从安全的地方获取密钥
        return "your_unsplash_api_key_here"
    }
    
    /**
     * 获取Pexels API密钥
     */
    fun getPexelsApiKey(): String {
        return "your_pexels_api_key_here"
    }
    
    /**
     * 获取Pixabay API密钥
     */
    fun getPixabayApiKey(): String {
        return "your_pixabay_api_key_here"
    }
    
    /**
     * 获取Wallhaven API密钥
     */
    fun getWallhavenApiKey(): String {
        return "your_wallhaven_api_key_here"
    }
    
    /**
     * 判断是否为调试模式
     */
    fun isDebugMode(): Boolean {
        // 实际开发中应该根据构建类型判断
        return true
    }
}