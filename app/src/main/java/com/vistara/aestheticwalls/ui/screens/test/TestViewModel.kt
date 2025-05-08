package com.vistara.aestheticwalls.ui.screens.test

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.DiamondTransactionType
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.remote.api.ApiService
import com.vistara.aestheticwalls.data.remote.api.LoginRequest
import com.vistara.aestheticwalls.data.repository.AuthRepository
import com.vistara.aestheticwalls.data.repository.DiamondRepository
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
    private val diamondRepository: DiamondRepository,
    private val authRepository: AuthRepository,
    private val apiService: ApiService,
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

    // 钻石测试开关状态
    private val _isDiamondTestEnabled = MutableStateFlow(false)
    val isDiamondTestEnabled: StateFlow<Boolean> = _isDiamondTestEnabled.asStateFlow()

    // 当前钻石余额
    private val _currentDiamondBalance = MutableStateFlow(0)
    val currentDiamondBalance: StateFlow<Int> = _currentDiamondBalance.asStateFlow()

    // 操作结果
    private val _operationResult = MutableStateFlow<String?>(null)
    val operationResult: StateFlow<String?> = _operationResult.asStateFlow()

    // 登录状态
    private val _isLoginLoading = MutableStateFlow(false)
    val isLoginLoading: StateFlow<Boolean> = _isLoginLoading.asStateFlow()

    // 显示登录对话框
    private val _showLoginDialog = MutableStateFlow(false)
    val showLoginDialog: StateFlow<Boolean> = _showLoginDialog.asStateFlow()

    init {
        checkPremiumStatus()
        checkLoginStatus()
        checkDiamondBalance()
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
                _operationResult.value =
                    context.getString(R.string.check_login_status_failed, e.message)
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
                _operationResult.value =
                    context.getString(R.string.check_premium_status_failed, e.message)
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
                _operationResult.value =
                    context.getString(R.string.disable_premium_failed, e.message)
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
                _operationResult.value =
                    context.getString(R.string.simulate_login_failed, e.message)
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
                _operationResult.value =
                    context.getString(R.string.simulate_logout_failed, e.message)
            }
        }
    }

    /**
     * 检查钻石余额
     */
    private fun checkDiamondBalance() {
        viewModelScope.launch {
            try {
                val balance = diamondRepository.getDiamondBalanceValue()
                _currentDiamondBalance.value = balance
                Log.d(TAG, "Diamond balance: $balance")
            } catch (e: Exception) {
                Log.e(TAG, "Error checking diamond balance: ${e.message}")
                _operationResult.value = "检查钻石余额失败: ${e.message}"
            }
        }
    }

    /**
     * 切换钻石测试开关
     */
    fun toggleDiamondTest() {
        val newState = !_isDiamondTestEnabled.value
        _isDiamondTestEnabled.value = newState

        viewModelScope.launch {
            try {
                if (newState) {
                    // 开启测试模式，增加200钻石
                    val success = diamondRepository.updateDiamondBalance(
                        amount = 200,
                        type = DiamondTransactionType.REWARD,
                        description = "测试模式奖励"
                    )
                    if (success) {
                        _operationResult.value = "测试模式已开启，已增加200钻石"
                        checkDiamondBalance()
                    } else {
                        _operationResult.value = "增加钻石失败"
                    }
                } else {
                    // 关闭测试模式，清空钻石
                    val currentBalance = diamondRepository.getDiamondBalanceValue()
                    if (currentBalance > 0) {
                        val success = diamondRepository.updateDiamondBalance(
                            amount = -currentBalance,
                            type = DiamondTransactionType.PURCHASE,
                            description = "测试模式关闭，清空钻石"
                        )
                        if (success) {
                            _operationResult.value = "测试模式已关闭，钻石已清空"
                            checkDiamondBalance()
                        } else {
                            _operationResult.value = "清空钻石失败"
                        }
                    } else {
                        _operationResult.value = "测试模式已关闭"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling diamond test: ${e.message}")
                _operationResult.value = "切换钻石测试模式失败: ${e.message}"
            }
        }
    }

    /**
     * 清除操作结果消息
     */
    fun clearOperationResult() {
        _operationResult.value = null
    }

    /**
     * 显示登录对话框
     */
    fun showLoginDialog() {
        _showLoginDialog.value = true
    }

    /**
     * 隐藏登录对话框
     */
    fun hideLoginDialog() {
        _showLoginDialog.value = false
    }

    /**
     * 使用邮箱登录，模拟Google登录
     * 随机生成nickname和token
     */
    fun loginWithEmail(email: String) {
        viewModelScope.launch {
            try {
                _isLoginLoading.value = true

                // 生成随机nickname
                val randomNickname = generateRandomNickname()

                // 生成随机token
                val randomToken = generateRandomToken()

                Log.d(TAG, "随机生成nickname: $randomNickname, token: $randomToken")

                // 创建登录请求
                val loginRequest = LoginRequest(
                    nickname = randomNickname,
                    email = email,
                    avatar = "https://api.dicebear.com/7.x/micah/png?seed=${email}",
                    token = randomToken
                )

                // 调用登录接口
                val result = apiService.login(loginRequest)

                // 处理API结果
                when (result) {
                    is ApiResult.Success -> {
                        val loginResponse = result.data

                        // 保存token
                        userRepository.saveServerToken(loginResponse.token)

                        // 更新登录状态
                        userRepository.updateLoginStatus(true)
                        _isLoggedIn.value = true

                        // 保存用户信息
                        saveUserInfo(
                            userId = "test_${System.currentTimeMillis()}",
                            userName = randomNickname,
                            userEmail = email,
                            userPhotoUrl = "https://api.dicebear.com/7.x/micah/png?seed=${email}"
                        )

                        // 更新高级状态（如果服务器返回了这个信息）
                        loginResponse.isPremium?.let { isPremium ->
                            userRepository.updatePremiumStatus(isPremium)
                            _isPremiumUser.value = isPremium
                        }

                        _operationResult.value = context.getString(R.string.login_success)
                        hideLoginDialog()
                        Log.d(TAG, "Login successful")
                    }

                    is ApiResult.Error -> {
                        _operationResult.value = context.getString(R.string.login_failed) + ": ${result.message}"
                        Log.e(TAG, "Login failed: ${result.code} ${result.message}")
                    }

                    is ApiResult.Loading -> {
                        _operationResult.value = context.getString(R.string.login_loading)
                        Log.d(TAG, "Login loading")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during login: ${e.message}")
                _operationResult.value = context.getString(R.string.login_failed)
            } finally {
                _isLoginLoading.value = false
            }
        }
    }

    /**
     * 生成随机昵称
     */
    private fun generateRandomNickname(): String {
        val adjectives = listOf(
            "快乐的",
            "聪明的",
            "勇敢的",
            "可爱的",
            "友善的",
            "活泼的",
            "机智的",
            "温柔的",
            "善良的",
            "幽默的"
        )
        val nouns =
            listOf("熊猫", "老虎", "狮子", "猫咪", "狗狗", "兔子", "松鼠", "大象", "长颈鹿", "猴子")

        val randomAdjective = adjectives.random()
        val randomNoun = nouns.random()
        val randomNumber = (1000..9999).random()

        return "$randomAdjective$randomNoun$randomNumber"
    }

    /**
     * 生成随机token
     */
    private fun generateRandomToken(): String {
        val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..32).map { charPool.random() }.joinToString("")
    }

    /**
     * 保存用户信息
     */
    private suspend fun saveUserInfo(
        userId: String, userName: String, userEmail: String, userPhotoUrl: String
    ) {
        try {
            // 使用AuthRepository保存用户信息
            authRepository.saveUserInfo(userId, userName, userEmail, userPhotoUrl)
            Log.d(TAG, "用户信息保存成功: $userName, $userEmail")
        } catch (e: Exception) {
            Log.e(TAG, "保存用户信息失败: ${e.message}")
        }
    }
}
