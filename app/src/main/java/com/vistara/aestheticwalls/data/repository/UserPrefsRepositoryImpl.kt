package com.vistara.aestheticwalls.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.vistara.aestheticwalls.data.model.AppLanguage
import com.vistara.aestheticwalls.data.model.AutoChangeFrequency
import com.vistara.aestheticwalls.data.model.AutoChangeSource
import com.vistara.aestheticwalls.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户偏好设置仓库实现类
 * 使用DataStore存储用户设置
 */
@Singleton
class UserPrefsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPrefsRepository {

    companion object {
        // 通用设置
        private val DARK_THEME = booleanPreferencesKey("dark_theme")
        private val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        private val APP_LANGUAGE = stringPreferencesKey("app_language")

        // 自动更换壁纸设置
        private val AUTO_CHANGE_ENABLED = booleanPreferencesKey("auto_change_enabled")
        private val AUTO_CHANGE_FREQUENCY = stringPreferencesKey("auto_change_frequency")
        private val AUTO_CHANGE_WIFI_ONLY = booleanPreferencesKey("auto_change_wifi_only")
        private val AUTO_CHANGE_SOURCE = stringPreferencesKey("auto_change_source")
        private val AUTO_CHANGE_CATEGORY = stringPreferencesKey("auto_change_category")

        // 通知设置
        private val SHOW_DOWNLOAD_NOTIFICATION = booleanPreferencesKey("show_download_notification")
        private val SHOW_WALLPAPER_CHANGE_NOTIFICATION =
            booleanPreferencesKey("show_wallpaper_change_notification")

        // 下载设置
        private val DOWNLOAD_ORIGINAL_QUALITY = booleanPreferencesKey("download_original_quality")
        private val DOWNLOAD_LOCATION = stringPreferencesKey("download_location")

        // 高级用户状态
        private val IS_PREMIUM_USER = booleanPreferencesKey("is_premium_user")
        private val PREMIUM_EXPIRY_DATE = longPreferencesKey("premium_expiry_date")

        // 搜索历史
        private val SEARCH_HISTORY = stringSetPreferencesKey("search_history")
    }

    /**
     * 获取用户设置
     */
    override suspend fun getUserSettings(): UserSettings {
        return getUserSettingsFlow().first()
    }

    /**
     * 获取用户设置流
     */
    override fun getUserSettingsFlow(): Flow<UserSettings> {
        return dataStore.data.map { preferences ->
            UserSettings(
                // 通用设置
                darkTheme = preferences[DARK_THEME] ?: false,
                dynamicColors = preferences[DYNAMIC_COLORS] ?: true,
                appLanguage = preferences[APP_LANGUAGE]?.let {
                    try {
                        AppLanguage.valueOf(it)
                    } catch (e: Exception) {
                        AppLanguage.SYSTEM
                    }
                } ?: AppLanguage.SYSTEM,

                // 自动更换壁纸设置
                autoChangeEnabled = preferences[AUTO_CHANGE_ENABLED] ?: false,
                autoChangeFrequency = preferences[AUTO_CHANGE_FREQUENCY]?.let {
                    try {
                        AutoChangeFrequency.valueOf(it)
                    } catch (e: Exception) {
                        AutoChangeFrequency.DAILY
                    }
                } ?: AutoChangeFrequency.DAILY,
                autoChangeWifiOnly = preferences[AUTO_CHANGE_WIFI_ONLY] ?: true,
                autoChangeSource = preferences[AUTO_CHANGE_SOURCE]?.let {
                    try {
                        AutoChangeSource.valueOf(it)
                    } catch (e: Exception) {
                        AutoChangeSource.FAVORITES
                    }
                } ?: AutoChangeSource.FAVORITES,
                autoChangeCategory = preferences[AUTO_CHANGE_CATEGORY],

                // 通知设置
                showDownloadNotification = preferences[SHOW_DOWNLOAD_NOTIFICATION] ?: true,
                showWallpaperChangeNotification = preferences[SHOW_WALLPAPER_CHANGE_NOTIFICATION]
                    ?: true,

                // 下载设置
                downloadOriginalQuality = preferences[DOWNLOAD_ORIGINAL_QUALITY] ?: true,
                downloadLocation = preferences[DOWNLOAD_LOCATION],

                // 高级用户状态
                isPremiumUser = preferences[IS_PREMIUM_USER] ?: false,
                premiumExpiryDate = preferences[PREMIUM_EXPIRY_DATE] ?: 0L)
        }
    }

    /**
     * 保存用户设置
     */
    override suspend fun saveUserSettings(settings: UserSettings) {
        dataStore.edit { preferences ->
            // 通用设置
            preferences[DARK_THEME] = settings.darkTheme
            preferences[DYNAMIC_COLORS] = settings.dynamicColors
            preferences[APP_LANGUAGE] = settings.appLanguage.name

            // 自动更换壁纸设置
            preferences[AUTO_CHANGE_ENABLED] = settings.autoChangeEnabled
            preferences[AUTO_CHANGE_FREQUENCY] = settings.autoChangeFrequency.name
            preferences[AUTO_CHANGE_WIFI_ONLY] = settings.autoChangeWifiOnly
            preferences[AUTO_CHANGE_SOURCE] = settings.autoChangeSource.name
            settings.autoChangeCategory?.let { preferences[AUTO_CHANGE_CATEGORY] = it }

            // 通知设置
            preferences[SHOW_DOWNLOAD_NOTIFICATION] = settings.showDownloadNotification
            preferences[SHOW_WALLPAPER_CHANGE_NOTIFICATION] =
                settings.showWallpaperChangeNotification

            // 下载设置
            preferences[DOWNLOAD_ORIGINAL_QUALITY] = settings.downloadOriginalQuality
            settings.downloadLocation?.let { preferences[DOWNLOAD_LOCATION] = it }

            // 高级用户状态
            preferences[IS_PREMIUM_USER] = settings.isPremiumUser
            preferences[PREMIUM_EXPIRY_DATE] = settings.premiumExpiryDate
        }
    }

    /**
     * 更新自动壁纸更换设置
     */
    override suspend fun updateAutoChangeSettings(
        enabled: Boolean?,
        frequency: AutoChangeFrequency?,
        wifiOnly: Boolean?,
        source: AutoChangeSource?,
        categoryId: String?
    ) {
        dataStore.edit { preferences ->
            enabled?.let { preferences[AUTO_CHANGE_ENABLED] = it }
            frequency?.let { preferences[AUTO_CHANGE_FREQUENCY] = it.name }
            wifiOnly?.let { preferences[AUTO_CHANGE_WIFI_ONLY] = it }
            source?.let { preferences[AUTO_CHANGE_SOURCE] = it.name }
            categoryId?.let { preferences[AUTO_CHANGE_CATEGORY] = it }
        }
    }

    /**
     * 更新通知设置
     */
    override suspend fun updateNotificationSettings(
        showDownloadNotification: Boolean?, showWallpaperChangeNotification: Boolean?
    ) {
        dataStore.edit { preferences ->
            showDownloadNotification?.let { preferences[SHOW_DOWNLOAD_NOTIFICATION] = it }
            showWallpaperChangeNotification?.let {
                preferences[SHOW_WALLPAPER_CHANGE_NOTIFICATION] = it
            }
        }
    }

    /**
     * 获取搜索历史
     */
    override suspend fun getSearchHistory(): List<String> {
        return dataStore.data.map { preferences ->
            preferences[SEARCH_HISTORY]?.toList() ?: emptyList()
        }.first()
    }

    /**
     * 保存搜索历史
     */
    override suspend fun saveSearchHistory(history: List<String>) {
        dataStore.edit { preferences ->
            preferences[SEARCH_HISTORY] = history.toSet()
        }
    }

    /**
     * 清除搜索历史
     */
    override suspend fun clearSearchHistory() {
        dataStore.edit { preferences ->
            preferences.remove(SEARCH_HISTORY)
        }
    }

    /**
     * 清除所有用户设置
     */
    override suspend fun clearUserSettings() {
        dataStore.edit { it.clear() }
    }
}
