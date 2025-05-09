package com.vistara.aestheticwalls.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.remote.ApiSource
import com.vistara.aestheticwalls.data.remote.api.ApiService
import com.vistara.aestheticwalls.data.remote.api.ProfileResponse
import com.vistara.aestheticwalls.data.remote.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户仓库实现类
 * 管理用户数据和状态
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val apiService: ApiService,
    private val diamondRepository: dagger.Lazy<DiamondRepository>
) : UserRepository {

    companion object {
        private const val TAG = "UserRepositoryImpl"
        private val IS_PREMIUM_USER = booleanPreferencesKey("is_premium_user")
        private val PREMIUM_EXPIRY_DATE = longPreferencesKey("premium_expiry_date")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val SERVER_TOKEN = stringPreferencesKey("server_token")
        private val USER_DIAMOND_BALANCE = intPreferencesKey("user_diamond_balance")
    }

    override val isPremiumUser: Flow<Boolean> = dataStore.data.map { preferences ->
        val isPremium = preferences[IS_PREMIUM_USER] == true
        val expiryDate = preferences[PREMIUM_EXPIRY_DATE] ?: 0L

        // 如果有过期时间，检查是否已过期
        if (expiryDate > 0) {
            isPremium && System.currentTimeMillis() < expiryDate
        } else {
            isPremium
        }
    }

    override suspend fun checkPremiumStatus(): Boolean {
        return dataStore.data.map { preferences ->
            val isPremium = preferences[IS_PREMIUM_USER] == true
            val expiryDate = preferences[PREMIUM_EXPIRY_DATE] ?: 0L

            // 如果有过期时间，检查是否已过期
            if (expiryDate > 0) {
                isPremium && System.currentTimeMillis() < expiryDate
            } else {
                isPremium
            }
        }.first()
    }

    override suspend fun updatePremiumStatus(isPremium: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_PREMIUM_USER] = isPremium

            // 如果是升级为高级用户，设置过期时间为一年后
            if (isPremium) {
                val oneYearInMillis = 365L * 24 * 60 * 60 * 1000
                preferences[PREMIUM_EXPIRY_DATE] = System.currentTimeMillis() + oneYearInMillis
            } else {
                // 如果是取消高级用户，清除过期时间
                preferences.remove(PREMIUM_EXPIRY_DATE)
            }
        }
    }

    override suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.remove(IS_PREMIUM_USER)
            preferences.remove(PREMIUM_EXPIRY_DATE)
            preferences.remove(IS_LOGGED_IN)
            preferences.remove(SERVER_TOKEN)
        }
    }

    override suspend fun checkUserLoggedIn(): Boolean {
        return dataStore.data.map { preferences ->
            preferences[IS_LOGGED_IN] == true
        }.first()
    }

    /**
     * 更新用户登录状态
     */
    override suspend fun updateLoginStatus(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn
        }
    }

    /**
     * 保存服务器返回的token
     */
    override suspend fun saveServerToken(token: String) {
        android.util.Log.d("UserRepositoryImpl", "保存服务器token: $token")
        dataStore.edit { preferences ->
            preferences[SERVER_TOKEN] = token
        }
    }

    /**
     * 获取服务器token
     */
    override suspend fun getServerToken(): String? {
        val token = dataStore.data.map<Preferences, String?> { preferences ->
            preferences[SERVER_TOKEN]
        }.first()
        Log.d(TAG, "获取服务器token: $token")
        return token
    }

    /**
     * 获取用户个人资料
     */
    override suspend fun getUserProfile(): ApiResult<ProfileResponse> {
        // 检查用户是否已登录
        val isLoggedIn = checkUserLoggedIn()
        if (!isLoggedIn) {
            Log.d(TAG, "用户未登录，无法获取个人资料")
            return ApiResult.Error(
                code = 401,
                message = "用户未登录",
                source = ApiSource.BACKEND
            )
        }

        return safeApiCall(ApiSource.BACKEND) {
            val response = apiService.getProfile()
            if (response.isSuccess && response.data != null) {
                // 更新本地钻石余额
                response.data.diamond.let { diamond ->
                    updateUserDiamondBalance(diamond)
                }
                response.data
            } else {
                throw Exception(response.msg)
            }
        }
    }

    /**
     * 更新用户钻石余额
     */
    private suspend fun updateUserDiamondBalance(amount: Int) {
        dataStore.edit { preferences ->
            preferences[USER_DIAMOND_BALANCE] = amount
        }

        // 同步更新DiamondRepository中的钻石余额
        try {
            val currentBalance = diamondRepository.get().getDiamondBalanceValue()
            if (currentBalance != amount) {
                Log.d(TAG, "同步钻石余额: 从 $currentBalance 到 $amount")
                diamondRepository.get().updateDiamondBalance(
                    amount = amount - currentBalance,
                    type = com.vistara.aestheticwalls.data.model.DiamondTransactionType.RECHARGE,
                    description = "同步服务器钻石余额"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "同步钻石余额失败: ${e.message}", e)
        }
    }

    /**
     * 刷新用户个人资料
     * 从服务器获取最新的用户信息并更新本地缓存
     */
    override suspend fun refreshUserProfile() {
        // 检查用户是否已登录
        val isLoggedIn = checkUserLoggedIn()
        if (!isLoggedIn) {
            Log.d(TAG, "用户未登录，无法刷新个人资料")
            return
        }

        try {
            val result = getUserProfile()
            if (result is ApiResult.Success) {
                Log.d(TAG, "用户个人资料刷新成功: ${result.data}")
            } else if (result is ApiResult.Error) {
                Log.e(TAG, "刷新用户个人资料失败: ${result.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "刷新用户个人资料异常: ${e.message}", e)
        }
    }
}
