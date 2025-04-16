package com.vistara.aestheticwalls.data.repository

import com.vistara.aestheticwalls.data.model.AutoChangeFrequency
import com.vistara.aestheticwalls.data.model.AutoChangeSource
import com.vistara.aestheticwalls.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

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
     * 获取用户设置流
     */
    fun getUserSettingsFlow(): Flow<UserSettings>

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
     * 获取搜索历史
     */
    suspend fun getSearchHistory(): List<String>

    /**
     * 保存搜索历史
     */
    suspend fun saveSearchHistory(history: List<String>)

    /**
     * 清除搜索历史
     */
    suspend fun clearSearchHistory()

    /**
     * 清除所有用户设置
     */
    suspend fun clearUserSettings()
}
