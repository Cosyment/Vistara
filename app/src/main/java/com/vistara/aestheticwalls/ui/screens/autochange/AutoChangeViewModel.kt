package com.vistara.aestheticwalls.ui.screens.autochange

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.data.model.AutoChangeFrequency
import com.vistara.aestheticwalls.data.model.AutoChangeSource
import com.vistara.aestheticwalls.data.repository.UserPrefsRepository
import com.vistara.aestheticwalls.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val userRepository: UserRepository
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

    // 高级用户状态
    private val _isPremiumUser = MutableStateFlow(false)
    val isPremiumUser: StateFlow<Boolean> = _isPremiumUser.asStateFlow()

    init {
        loadSettings()
        checkPremiumStatus()
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
     * 保存设置
     */
    private fun saveSettings() {
        viewModelScope.launch {
            try {
                userPrefsRepository.updateAutoChangeSettings(
                    enabled = _autoChangeEnabled.value,
                    frequency = _autoChangeFrequency.value,
                    wifiOnly = _autoChangeWifiOnly.value,
                    source = _autoChangeSource.value,
                    categoryId = _autoChangeCategory.value
                )
                Log.d(TAG, "Auto change settings saved successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving auto change settings: ${e.message}")
            }
        }
    }
}
