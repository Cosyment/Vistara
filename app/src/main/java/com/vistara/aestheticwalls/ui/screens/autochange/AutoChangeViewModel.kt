package com.vistara.aestheticwalls.ui.screens.autochange

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.vistara.aestheticwalls.data.model.AutoChangeFrequency
import com.vistara.aestheticwalls.data.model.AutoChangeHistory
import com.vistara.aestheticwalls.data.model.AutoChangeSource
import com.vistara.aestheticwalls.data.model.Category
import com.vistara.aestheticwalls.data.model.WallpaperTarget
import com.vistara.aestheticwalls.data.repository.UserPrefsRepository
import com.vistara.aestheticwalls.data.repository.UserRepository
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import com.vistara.aestheticwalls.worker.AutoWallpaperWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 自动更换壁纸设置页面的ViewModel
 */
@HiltViewModel
class AutoChangeViewModel @Inject constructor(
    private val userPrefsRepository: UserPrefsRepository,
    private val userRepository: UserRepository,
    private val wallpaperRepository: WallpaperRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "AutoChangeViewModel"
    }

    // 是否启用自动更换
    private val _autoChangeEnabled = MutableStateFlow(false)
    val autoChangeEnabled: StateFlow<Boolean> = _autoChangeEnabled.asStateFlow()

    // 更换频率
    private val _autoChangeFrequency = MutableStateFlow(AutoChangeFrequency.DAILY)
    val autoChangeFrequency: StateFlow<AutoChangeFrequency> = _autoChangeFrequency.asStateFlow()

    // 仅在WiFi下更换
    private val _autoChangeWifiOnly = MutableStateFlow(true)
    val autoChangeWifiOnly: StateFlow<Boolean> = _autoChangeWifiOnly.asStateFlow()

    // 壁纸来源
    private val _autoChangeSource = MutableStateFlow(AutoChangeSource.FAVORITES)
    val autoChangeSource: StateFlow<AutoChangeSource> = _autoChangeSource.asStateFlow()

    // 分类ID（当来源为CATEGORY时使用）
    private val _autoChangeCategory = MutableStateFlow<String?>(null)
    val autoChangeCategory: StateFlow<String?> = _autoChangeCategory.asStateFlow()

    // 壁纸目标屏幕
    private val _autoChangeTarget = MutableStateFlow(WallpaperTarget.BOTH)
    val autoChangeTarget: StateFlow<WallpaperTarget> = _autoChangeTarget.asStateFlow()

    // 可用分类列表
    private val _availableCategories = MutableStateFlow<List<Category>>(emptyList())
    val availableCategories: StateFlow<List<Category>> = _availableCategories.asStateFlow()

    // 自动更换历史
    private val _autoChangeHistory = MutableStateFlow<List<AutoChangeHistory>>(emptyList())
    val autoChangeHistory: StateFlow<List<AutoChangeHistory>> = _autoChangeHistory.asStateFlow()

    // 高级用户状态
    private val _isPremiumUser = MutableStateFlow(false)
    val isPremiumUser: StateFlow<Boolean> = _isPremiumUser.asStateFlow()

    // 测试更换状态
    private val _isChangingWallpaper = MutableStateFlow(false)
    val isChangingWallpaper: StateFlow<Boolean> = _isChangingWallpaper.asStateFlow()

    init {
        loadSettings()
        checkPremiumStatus()
        loadCategories()
        loadAutoChangeHistory()
    }

    /**
     * 加载设置
     */
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val userSettings = userPrefsRepository.getUserSettings()

                // 更新状态
                _autoChangeEnabled.value = userSettings.autoChangeEnabled
                _autoChangeFrequency.value = userSettings.autoChangeFrequency
                _autoChangeWifiOnly.value = userSettings.autoChangeWifiOnly
                _autoChangeSource.value = userSettings.autoChangeSource
                _autoChangeCategory.value = userSettings.autoChangeCategory
                _autoChangeTarget.value = userSettings.autoChangeTarget ?: WallpaperTarget.BOTH

                Log.d(TAG, "Auto change settings loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading auto change settings: ${e.message}")
            }
        }
    }

    /**
     * 检查高级用户状态
     */
    private fun checkPremiumStatus() {
        viewModelScope.launch {
            try {
                val isPremium = userRepository.isPremiumUser.first()
                _isPremiumUser.value = isPremium
                Log.d(TAG, "Premium status: $isPremium")

                // 如果不是高级用户，但选择了高级功能，则重置为免费功能
                if (!isPremium) {
                    if (_autoChangeFrequency.value.isPremium) {
                        _autoChangeFrequency.value = AutoChangeFrequency.DAILY
                    }
                    if (_autoChangeSource.value.isPremium) {
                        _autoChangeSource.value = AutoChangeSource.FAVORITES
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking premium status: ${e.message}")
                _isPremiumUser.value = false
            }
        }
    }

    /**
     * 更新自动更换启用状态
     */
    fun updateAutoChangeEnabled(enabled: Boolean) {
        _autoChangeEnabled.value = enabled
        saveSettings()
    }

    /**
     * 更新自动更换频率
     */
    fun updateAutoChangeFrequency(frequency: AutoChangeFrequency) {
        // 检查是否是高级功能且用户不是高级用户
        if (frequency.isPremium && !_isPremiumUser.value) {
            Log.d(TAG, "Cannot set premium frequency for non-premium user")
            return
        }

        _autoChangeFrequency.value = frequency
        saveSettings()
    }

    /**
     * 更新仅在WiFi下更换设置
     */
    fun updateAutoChangeWifiOnly(wifiOnly: Boolean) {
        _autoChangeWifiOnly.value = wifiOnly
        saveSettings()
    }

    /**
     * 更新壁纸来源
     */
    fun updateAutoChangeSource(source: AutoChangeSource) {
        // 检查是否是高级功能且用户不是高级用户
        if (source.isPremium && !_isPremiumUser.value) {
            Log.d(TAG, "Cannot set premium source for non-premium user")
            return
        }

        _autoChangeSource.value = source
        saveSettings()
    }

    /**
     * 更新分类ID
     */
    fun updateAutoChangeCategory(categoryId: String?) {
        _autoChangeCategory.value = categoryId
        saveSettings()
    }

    /**
     * 更新壁纸目标屏幕
     */
    fun updateAutoChangeTarget(target: WallpaperTarget) {
        _autoChangeTarget.value = target
        saveSettings()
    }

    /**
     * 加载可用分类
     */
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = wallpaperRepository.getCategories()
                _availableCategories.value = categories
                Log.d(TAG, "Loaded ${categories.size} categories")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading categories: ${e.message}")
                _availableCategories.value = emptyList()
            }
        }
    }

    /**
     * 加载自动更换历史
     */
    private fun loadAutoChangeHistory() {
        viewModelScope.launch {
            wallpaperRepository.getAutoChangeHistory().collect { history ->
                _autoChangeHistory.value = history
                Log.d(TAG, "Loaded ${history.size} history items")
            }
        }
    }

    /**
     * 测试自动更换壁纸
     */
    fun testAutoChange(activity: Activity?) {
        if (activity == null) return

        viewModelScope.launch {
            try {
                _isChangingWallpaper.value = true

                // 根据来源获取壁纸
                val wallpaper = when (_autoChangeSource.value) {
                    AutoChangeSource.FAVORITES -> {
                        wallpaperRepository.getRandomFavoriteWallpaper()
                    }
                    AutoChangeSource.CATEGORY -> {
                        _autoChangeCategory.value?.let { categoryId ->
                            wallpaperRepository.getRandomWallpaperByCategory(categoryId)
                        } ?: wallpaperRepository.getRandomWallpaper()
                    }
                    AutoChangeSource.DOWNLOADED -> {
                        wallpaperRepository.getRandomDownloadedWallpaper()
                    }
                    AutoChangeSource.TRENDING -> {
                        wallpaperRepository.getRandomTrendingWallpaper()
                    }
                }

                if (wallpaper != null) {
                    // 记录历史
                    val history = AutoChangeHistory(
                        wallpaperId = wallpaper.id,
                        wallpaperUrl = wallpaper.url ?: "",
                        timestamp = System.currentTimeMillis(),
                        targetScreen = when (_autoChangeTarget.value) {
                            WallpaperTarget.HOME -> "home"
                            WallpaperTarget.LOCK -> "lock"
                            WallpaperTarget.BOTH -> "both"
                        }
                    )
                    wallpaperRepository.recordAutoChangeHistory(history)

                    // TODO: 实际设置壁纸的逻辑
                    // 这里需要实现下载并设置壁纸的逻辑

                    Log.d(TAG, "Test auto change completed successfully")
                } else {
                    Log.e(TAG, "No wallpaper found for test auto change")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during test auto change: ${e.message}")
            } finally {
                _isChangingWallpaper.value = false
            }
        }
    }

    /**
     * 应用自动更换设置
     */
    fun applyAutoChangeSettings() {
        val workManager = WorkManager.getInstance(context)

        if (_autoChangeEnabled.value) {
            // 启用自动更换
            AutoWallpaperWorker.schedule(
                workManager,
                _autoChangeFrequency.value,
                _autoChangeWifiOnly.value
            )
            Log.d(TAG, "Auto change scheduled with frequency: ${_autoChangeFrequency.value}")
        } else {
            // 禁用自动更换
            AutoWallpaperWorker.cancelAutoWallpaperChange(context)
            Log.d(TAG, "Auto change cancelled")
        }
    }

    /**
     * 保存设置
     */
    private fun saveSettings() {
        viewModelScope.launch {
            try {
                // 获取当前设置
                val currentSettings = userPrefsRepository.getUserSettings()

                // 创建更新后的设置对象
                val updatedSettings = currentSettings.copy(
                    autoChangeEnabled = _autoChangeEnabled.value,
                    autoChangeFrequency = _autoChangeFrequency.value,
                    autoChangeWifiOnly = _autoChangeWifiOnly.value,
                    autoChangeSource = _autoChangeSource.value,
                    autoChangeCategory = _autoChangeCategory.value,
                    autoChangeTarget = _autoChangeTarget.value
                )

                // 保存设置
                userPrefsRepository.saveUserSettings(updatedSettings)
                Log.d(TAG, "Auto change settings saved successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving auto change settings: ${e.message}")
            }
        }
    }
}
