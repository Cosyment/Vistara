package com.vistara.aestheticwalls.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.vistara.aestheticwalls.data.remote.ApiService
import com.vistara.aestheticwalls.data.remote.GoogleLoginRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 认证仓库实现类
 * 负责处理用户认证相关的操作
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val userRepository: UserRepository,
    private val apiService: ApiService
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepositoryImpl"
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_PHOTO_URL = stringPreferencesKey("user_photo_url")
    }

    // Google登录客户端
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    override val isUserLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    override val userId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ID]
    }

    override val userName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_NAME]
    }

    override val userEmail: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_EMAIL]
    }

    override val userPhotoUrl: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_PHOTO_URL]
    }

    override suspend fun handleSignInResult(completedTask: Task<GoogleSignInAccount>): Boolean {
        return try {
            val account = completedTask.getResult(Exception::class.java)
            if (account != null) {
                // 保存用户信息
                saveUserInfo(
                    userId = account.id ?: "",
                    userName = account.displayName ?: "",
                    userEmail = account.email ?: "",
                    userPhotoUrl = account.photoUrl?.toString() ?: ""
                )
                
                try {
                    // 构建请求体
                    val requestBody = GoogleLoginRequest(
                        nickname = account.displayName ?: "",
                        email = account.email ?: "",
                        avatar = account.photoUrl?.toString() ?: "",
                        token = account.idToken ?: ""
                    )
                    
                    // 发起后端登录请求
                    val response = apiService.googleLogin(requestBody)
                    if (response.isSuccessful && response.body() != null) {
                        val loginResponse = response.body()!!
                        // 保存后端返回的token等信息
//                        userRepository.saveServerToken(loginResponse.token)
                        userRepository.updateLoginStatus(true)
                        userRepository.updatePremiumStatus(loginResponse.isPremium ?: false)
                        true
                    } else {
                        Log.e(TAG, "Backend login failed: ${response.errorBody()?.string()}")
                        false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Backend login request failed", e)
                    false
                }
            } else {
                Log.e(TAG, "Google sign in failed: account is null")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google sign in failed", e)
            false
        }
    }

    override suspend fun signOut() {
        // 清除本地存储的用户信息
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = false
            preferences.remove(USER_ID)
            preferences.remove(USER_NAME)
            preferences.remove(USER_EMAIL)
            preferences.remove(USER_PHOTO_URL)
        }

        // 清除用户数据
        userRepository.clearUserData()

        // 退出Google登录
        googleSignInClient.signOut()
    }

    override suspend fun checkUserLoggedIn(): Boolean {
        // 检查本地存储的登录状态
        val isLoggedIn = isUserLoggedIn.first()

        // 检查Google登录状态
        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(context)

        // 如果本地存储的状态与Google登录状态不一致，以Google登录状态为准
        if (isLoggedIn && lastSignedInAccount == null) {
            // 本地显示已登录，但Google显示未登录，清除本地登录状态
            signOut()
            return false
        } else if (!isLoggedIn && lastSignedInAccount != null) {
            // 本地显示未登录，但Google显示已登录，更新本地登录状态
            saveUserInfo(
                userId = lastSignedInAccount.id ?: "",
                userName = lastSignedInAccount.displayName ?: "",
                userEmail = lastSignedInAccount.email ?: "",
                userPhotoUrl = lastSignedInAccount.photoUrl?.toString() ?: ""
            )
            return true
        }

        return isLoggedIn
    }

    /**
     * 保存用户信息
     */
    private suspend fun saveUserInfo(
        userId: String,
        userName: String,
        userEmail: String,
        userPhotoUrl: String
    ) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_ID] = userId
            preferences[USER_NAME] = userName
            preferences[USER_EMAIL] = userEmail
            preferences[USER_PHOTO_URL] = userPhotoUrl
        }
    }
}
