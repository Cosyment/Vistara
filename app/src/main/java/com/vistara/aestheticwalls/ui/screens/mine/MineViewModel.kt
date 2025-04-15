package com.vistara.aestheticwalls.ui.screens.mine

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * 个人中心页面的ViewModel
 * 管理用户数据和状态
 */
@HiltViewModel
class MineViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userPrefsRepository: UserPrefsRepository
) : ViewModel() {

    companion object {
        private const val TAG = "MineViewModel"
    }

    // 用户名
    private val _username = MutableStateFlow("Vistara 用户")
    val username: StateFlow<String> = _username.asStateFlow()

    // 高级用户状态
    private val _isPremiumUser = MutableStateFlow(false)
    val isPremiumUser: StateFlow<Boolean> = _isPremiumUser.asStateFlow()

    // 调试模式状态
    private val _isDebugMode = MutableStateFlow(false)
    val isDebugMode: StateFlow<Boolean> = _isDebugMode.asStateFlow()

    init {
        loadUserData()
        checkDebugMode()
    }

    /**
     * 加载用户数据
     */
    private fun loadUserData() {
        viewModelScope.launch {
            try {
                // 检查高级用户状态
                val isPremium = userRepository.isPremiumUser.first()
                _isPremiumUser.value = isPremium
                Log.d(TAG, "Premium status: $isPremium")

                // 获取用户设置
                val userSettings = userPrefsRepository.getUserSettings()
                // 这里可以根据实际需求加载更多用户数据
                
                Log.d(TAG, "User data loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user data: ${e.message}")
            }
        }
    }

    /**
     * 检查调试模式状态
     * 在实际应用中，这可能来自构建配置或开发者选项
     */
    private fun checkDebugMode() {
        // 这里可以根据实际需求实现调试模式的检测逻辑
        // 例如，可以检查BuildConfig.DEBUG或特定的开发者选项
        _isDebugMode.value = true // 开发阶段默认启用
    }

    /**
     * 升级到高级版
     */
    fun upgradeToPremium() {
        viewModelScope.launch {
            try {
                userRepository.updatePremiumStatus(true)
                _isPremiumUser.value = true
                Log.d(TAG, "Upgraded to premium")
            } catch (e: Exception) {
                Log.e(TAG, "Error upgrading to premium: ${e.message}")
            }
        }
    }

    /**
     * 取消高级版
     * 主要用于测试
     */
    fun cancelPremium() {
        viewModelScope.launch {
            try {
                userRepository.updatePremiumStatus(false)
                _isPremiumUser.value = false
                Log.d(TAG, "Cancelled premium")
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling premium: ${e.message}")
            }
        }
    }

    /**
     * 切换调试模式
     */
    fun toggleDebugMode() {
        _isDebugMode.value = !_isDebugMode.value
        Log.d(TAG, "Debug mode toggled: ${_isDebugMode.value}")
    }

    /**
     * 清除用户数据
     */
    fun clearUserData() {
        viewModelScope.launch {
            try {
                userRepository.clearUserData()
                userPrefsRepository.clearUserSettings()
                _isPremiumUser.value = false
                Log.d(TAG, "User data cleared")
                
                // 重新加载用户数据
                loadUserData()
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing user data: ${e.message}")
            }
        }
    }
}
