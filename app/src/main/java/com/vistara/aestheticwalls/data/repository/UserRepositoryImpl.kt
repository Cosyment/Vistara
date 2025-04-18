package com.vistara.aestheticwalls.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
    private val dataStore: DataStore<Preferences>
) : UserRepository {

    companion object {
        private val IS_PREMIUM_USER = booleanPreferencesKey("is_premium_user")
        private val PREMIUM_EXPIRY_DATE = longPreferencesKey("premium_expiry_date")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val SERVER_TOKEN = stringPreferencesKey("server_token")
    }

    override val isPremiumUser: Flow<Boolean> = dataStore.data.map { preferences ->
        val isPremium = preferences[IS_PREMIUM_USER] ?: false
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
            val isPremium = preferences[IS_PREMIUM_USER] ?: false
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
            preferences[IS_LOGGED_IN] ?: false
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
        android.util.Log.d("UserRepositoryImpl", "获取服务器token: $token")
        return token
    }
}
