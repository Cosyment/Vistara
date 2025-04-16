package com.vistara.aestheticwalls.ui.screens.auth

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.vistara.aestheticwalls.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 认证页面的ViewModel
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    // 登录状态
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // 用户名
    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    // 用户头像URL
    private val _userPhotoUrl = MutableStateFlow<String?>(null)
    val userPhotoUrl: StateFlow<String?> = _userPhotoUrl.asStateFlow()

    // 用户邮箱
    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    // 登录结果
    private val _loginResult = MutableStateFlow<LoginResult?>(null)
    val loginResult: StateFlow<LoginResult?> = _loginResult.asStateFlow()

    init {
        checkLoginStatus()
        loadUserInfo()
    }

    /**
     * 检查登录状态
     */
    private fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                val isLoggedIn = authRepository.checkUserLoggedIn()
                _isLoggedIn.value = isLoggedIn
                Log.d(TAG, "User login status: $isLoggedIn")
            } catch (e: Exception) {
                Log.e(TAG, "Error checking login status", e)
                _isLoggedIn.value = false
            }
        }
    }

    /**
     * 加载用户信息
     */
    private fun loadUserInfo() {
        viewModelScope.launch {
            try {
                _userName.value = authRepository.userName.first()
                _userPhotoUrl.value = authRepository.userPhotoUrl.first()
                _userEmail.value = authRepository.userEmail.first()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user info", e)
            }
        }
    }

    /**
     * 处理Google登录结果
     */
    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val success = authRepository.handleSignInResult(task)
                if (success) {
                    _isLoggedIn.value = true
                    loadUserInfo()
                    _loginResult.value = LoginResult.Success("登录成功")
                } else {
                    _loginResult.value = LoginResult.Error("登录失败")
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Google sign in failed", e)
                _loginResult.value = LoginResult.Error("登录失败: ${e.statusCode}")
            } catch (e: Exception) {
                Log.e(TAG, "Error handling sign in result", e)
                _loginResult.value = LoginResult.Error("登录失败: ${e.message}")
            }
        }
    }

    /**
     * 退出登录
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _isLoggedIn.value = false
                _userName.value = null
                _userPhotoUrl.value = null
                _userEmail.value = null
                _loginResult.value = LoginResult.Success("已退出登录")
            } catch (e: Exception) {
                Log.e(TAG, "Error signing out", e)
                _loginResult.value = LoginResult.Error("退出登录失败: ${e.message}")
            }
        }
    }

    /**
     * 清除登录结果
     */
    fun clearLoginResult() {
        _loginResult.value = null
    }

    /**
     * 登录结果
     */
    sealed class LoginResult {
        data class Success(val message: String) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }
}
