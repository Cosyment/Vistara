package com.vistara.aestheticwalls.ui.screens.mine

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.BuildConfig
import com.vistara.aestheticwalls.data.repository.AuthRepository
import com.vistara.aestheticwalls.data.repository.DiamondRepository
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
    private val userPrefsRepository: UserPrefsRepository,
    private val authRepository: AuthRepository,
    private val diamondRepository: DiamondRepository
) : ViewModel() {

    companion object {
        private const val TAG = "MineViewModel"
    }

    // 用户名
    private val _username = MutableStateFlow("Vistara User")
    val username: StateFlow<String> = _username.asStateFlow()

    // 用户头像
    private val _userPhotoUrl = MutableStateFlow<String?>(null)
    val userPhotoUrl: StateFlow<String?> = _userPhotoUrl.asStateFlow()

    // 高级用户状态
    private val _isPremiumUser = MutableStateFlow(false)
    val isPremiumUser: StateFlow<Boolean> = _isPremiumUser.asStateFlow()

    // 钻石余额
    private val _diamondBalance = MutableStateFlow(0)
    val diamondBalance: StateFlow<Int> = _diamondBalance.asStateFlow()

    // 调试模式状态
    private val _isDebugMode = MutableStateFlow(false)
    val isDebugMode: StateFlow<Boolean> = _isDebugMode.asStateFlow()

    // 登录状态
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // 需要登录的操作类型
    private val _needLoginAction = MutableStateFlow<LoginAction?>(null)
    val needLoginAction: StateFlow<LoginAction?> = _needLoginAction.asStateFlow()

    init {
        loadUserData()
        checkDebugMode()
    }

    /**
     * 刷新用户数据
     * 在页面每次显示时调用
     */
    fun refreshUserData() {
        loadUserData()
    }

    /**
     * 加载用户数据
     */
    private fun loadUserData() {
        viewModelScope.launch {
            try {
                // 检查登录状态
                _isLoggedIn.value = userRepository.checkUserLoggedIn()
                Log.d(TAG, "Login status: ${_isLoggedIn.value}")

                // 检查高级用户状态
                val isPremium = userRepository.isPremiumUser.first()
                _isPremiumUser.value = isPremium
                Log.d(TAG, "Premium status: $isPremium")

                // 获取钻石余额
                val balance = diamondRepository.getDiamondBalance().first()
                _diamondBalance.value = balance
                Log.d(TAG, "Diamond balance: $balance")

                // 获取用户设置
                val userSettings = userPrefsRepository.getUserSettings()

                // 获取用户名和头像
                if (_isLoggedIn.value) {
                    val name = authRepository.userName.first()
                    if (!name.isNullOrEmpty()) {
                        _username.value = name
                    }

                    val photoUrl = authRepository.userPhotoUrl.first()
                    _userPhotoUrl.value = photoUrl

                    Log.d(TAG, "User info loaded: name=$name, photoUrl=$photoUrl")
                }

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
        _isDebugMode.value = BuildConfig.DEBUG // 开发阶段默认启用
    }

    /**
     * 需要登录的操作类型
     */
    enum class LoginAction {
        FAVORITES,
        DOWNLOADS,
        AUTO_WALLPAPER
    }

    /**
     * 清除需要登录的操作
     */
    fun clearNeedLoginAction() {
        _needLoginAction.value = null
    }

    /**
     * 设置需要登录的操作
     */
    fun setNeedLoginAction(action: LoginAction) {
        _needLoginAction.value = action
    }

    /**
     * 检查登录状态并执行操作
     * @param action 需要登录的操作类型
     * @param onLoggedIn 已登录时执行的操作
     * @return 是否已登录
     */
    fun checkLoginAndExecute(action: LoginAction, onLoggedIn: () -> Unit): Boolean {
        return if (_isLoggedIn.value) {
            onLoggedIn()
            true
        } else {
            _needLoginAction.value = action
            false
        }
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
