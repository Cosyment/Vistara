package com.vistara.aestheticwalls.ui.screens.test

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 测试用户状态ViewModel
 * 用于测试环境中修改用户的高级状态
 */
@HiltViewModel
class TestViewModel @Inject constructor(
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "TestViewModel"
    }

    // 用户高级状态
    private val _isPremiumUser = MutableStateFlow(false)
    val isPremiumUser: StateFlow<Boolean> = _isPremiumUser.asStateFlow()

    // 用户登录状态
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // 操作结果
    private val _operationResult = MutableStateFlow<String?>(null)
    val operationResult: StateFlow<String?> = _operationResult.asStateFlow()

    init {
        checkPremiumStatus()
        checkLoginStatus()
    }

    /**
     * 检查登录状态
     */
    private fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                val isLoggedIn = userRepository.checkUserLoggedIn()
                _isLoggedIn.value = isLoggedIn
                Log.d(TAG, "Login status: $isLoggedIn")
            } catch (e: Exception) {
                Log.e(TAG, "Error checking login status: ${e.message}")
                _operationResult.value = context.getString(R.string.check_login_status_failed, e.message)
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
            } catch (e: Exception) {
                Log.e(TAG, "Error checking premium status: ${e.message}")
                _operationResult.value = context.getString(R.string.check_premium_status_failed, e.message)
            }
        }
    }

    /**
     * 设置用户为高级用户
     */
    fun enablePremiumUser() {
        viewModelScope.launch {
            try {
                userRepository.updatePremiumStatus(true)
                _isPremiumUser.value = true
                _operationResult.value = context.getString(R.string.set_premium_success)
                Log.d(TAG, "User set to premium")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting premium status: ${e.message}")
                _operationResult.value = context.getString(R.string.set_premium_failed, e.message)
            }
        }
    }

    /**
     * 取消用户的高级状态
     */
    fun disablePremiumUser() {
        viewModelScope.launch {
            try {
                userRepository.updatePremiumStatus(false)
                _isPremiumUser.value = false
                _operationResult.value = context.getString(R.string.disable_premium_success)
                Log.d(TAG, "Premium status disabled")
            } catch (e: Exception) {
                Log.e(TAG, "Error disabling premium status: ${e.message}")
                _operationResult.value = context.getString(R.string.disable_premium_failed, e.message)
            }
        }
    }

    /**
     * 模拟登录成功
     */
    fun simulateLogin() {
        viewModelScope.launch {
            try {
                // 更新登录状态
                userRepository.updateLoginStatus(true)
                _isLoggedIn.value = true
                _operationResult.value = context.getString(R.string.simulate_login_success)
                Log.d(TAG, "Simulated login successful")
            } catch (e: Exception) {
                Log.e(TAG, "Error simulating login: ${e.message}")
                _operationResult.value = context.getString(R.string.simulate_login_failed, e.message)
            }
        }
    }

    /**
     * 模拟退出登录
     */
    fun simulateLogout() {
        viewModelScope.launch {
            try {
                // 更新登录状态
                userRepository.updateLoginStatus(false)
                _isLoggedIn.value = false
                _operationResult.value = context.getString(R.string.simulate_logout_success)
                Log.d(TAG, "Simulated logout successful")
            } catch (e: Exception) {
                Log.e(TAG, "Error simulating logout: ${e.message}")
                _operationResult.value = context.getString(R.string.simulate_logout_failed, e.message)
            }
        }
    }

    /**
     * 清除操作结果消息
     */
    fun clearOperationResult() {
        _operationResult.value = null
    }
}
