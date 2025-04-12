package com.vistara.aestheticwalls.data.repository

import com.vistara.aestheticwalls.data.model.AutoChangeFrequency
import com.vistara.aestheticwalls.data.model.AutoChangeSource
import com.vistara.aestheticwalls.data.model.UserSettings

/**
 * 用户偏好设置仓库接口
 * 负责存储和检索用户设置
 */
interface UserPrefsRepository {
    
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
        frequency: AutoChangeFrequency? = null,
        wifiOnly: Boolean? = null,
        source: AutoChangeSource? = null,
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
