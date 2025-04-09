package com.vistara.aestheticwalls.data.repository

import com.vistara.aestheticwalls.data.model.UserSettings

/**
 * 用户偏好设置仓库接口
 * 处理用户设置的存储和检索
 */
interface UserPreferencesRepository {
    /**
     * 获取用户设置
     */
    suspend fun getUserSettings(): UserSettings
    
    /**
     * 保存用户设置
     */
    suspend fun saveUserSettings(settings: UserSettings)
    
    /**
     * 更新自动壁纸更换设置
     */
    suspend fun updateAutoChangeSettings(
        enabled: Boolean? = null,
        frequency: com.vistara.aestheticwalls.data.model.AutoChangeFrequency? = null,
        wifiOnly: Boolean? = null,
        source: com.vistara.aestheticwalls.data.model.AutoChangeSource? = null,
        categoryId: String? = null
    )
    
    /**
     * 更新通知设置
     */
    suspend fun updateNotificationSettings(
        showDownloadNotification: Boolean? = null,
        showWallpaperChangeNotification: Boolean? = null
    )
    
    /**
     * 清除所有用户设置
     */
    suspend fun clearUserSettings()
} 