package com.vistara.aestheticwalls.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.data.repository.UserPrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 设置页面的ViewModel
 * 管理用户设置数据和状态
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPrefsRepository: UserPrefsRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    // 深色主题
    private val _darkTheme = MutableStateFlow(false)
    val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()

    // 动态颜色
    private val _dynamicColors = MutableStateFlow(true)
    val dynamicColors: StateFlow<Boolean> = _dynamicColors.asStateFlow()

    // 下载通知
    private val _showDownloadNotification = MutableStateFlow(true)
    val showDownloadNotification: StateFlow<Boolean> = _showDownloadNotification.asStateFlow()

    // 壁纸更换通知
    private val _showWallpaperChangeNotification = MutableStateFlow(true)
    val showWallpaperChangeNotification: StateFlow<Boolean> = _showWallpaperChangeNotification.asStateFlow()

    // 下载原始质量
    private val _downloadOriginalQuality = MutableStateFlow(true)
    val downloadOriginalQuality: StateFlow<Boolean> = _downloadOriginalQuality.asStateFlow()

    init {
        loadUserSettings()
    }

    /**
     * 加载用户设置
     */
    private fun loadUserSettings() {
        viewModelScope.launch {
            try {
                val userSettings = userPrefsRepository.getUserSettings()
                
                // 更新状态
                _darkTheme.value = userSettings.darkTheme
                _dynamicColors.value = userSettings.dynamicColors
                _showDownloadNotification.value = userSettings.showDownloadNotification
                _showWallpaperChangeNotification.value = userSettings.showWallpaperChangeNotification
                _downloadOriginalQuality.value = userSettings.downloadOriginalQuality
                
                Log.d(TAG, "User settings loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user settings: ${e.message}")
            }
        }
    }

    /**
     * 更新深色主题设置
     */
    fun updateDarkTheme(enabled: Boolean) {
        _darkTheme.value = enabled
        saveSettings()
    }

    /**
     * 更新动态颜色设置
     */
    fun updateDynamicColors(enabled: Boolean) {
        _dynamicColors.value = enabled
        saveSettings()
    }

    /**
     * 更新下载通知设置
     */
    fun updateShowDownloadNotification(enabled: Boolean) {
        _showDownloadNotification.value = enabled
        saveSettings()
    }

    /**
     * 更新壁纸更换通知设置
     */
    fun updateShowWallpaperChangeNotification(enabled: Boolean) {
        _showWallpaperChangeNotification.value = enabled
        saveSettings()
    }

    /**
     * 更新下载原始质量设置
     */
    fun updateDownloadOriginalQuality(enabled: Boolean) {
        _downloadOriginalQuality.value = enabled
        saveSettings()
    }

    /**
     * 保存所有设置
     */
    private fun saveSettings() {
        viewModelScope.launch {
            try {
                // 获取当前设置
                val currentSettings = userPrefsRepository.getUserSettings()
                
                // 创建更新后的设置对象
                val updatedSettings = currentSettings.copy(
                    darkTheme = _darkTheme.value,
                    dynamicColors = _dynamicColors.value,
                    showDownloadNotification = _showDownloadNotification.value,
                    showWallpaperChangeNotification = _showWallpaperChangeNotification.value,
                    downloadOriginalQuality = _downloadOriginalQuality.value
                )
                
                // 保存设置
                userPrefsRepository.saveUserSettings(updatedSettings)
                Log.d(TAG, "Settings saved successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving settings: ${e.message}")
            }
        }
    }
}
