package com.vistara.aestheticwalls.data.repository

import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.remote.api.ProfileResponse
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val isPremiumUser: Flow<Boolean>
    suspend fun checkPremiumStatus(): Boolean
    suspend fun updatePremiumStatus(isPremium: Boolean)
    suspend fun getLocalUser()
    suspend fun clearUserData()
    suspend fun checkUserLoggedIn(): Boolean
    suspend fun updateLoginStatus(isLoggedIn: Boolean)
    suspend fun saveServerToken(token: String)
    suspend fun getServerToken(): String?

    /**
     * 获取用户个人资料
     * @return 用户个人资料的API结果
     */
    suspend fun getUserProfile(): ApiResult<ProfileResponse>

    /**
     * 刷新用户个人资料
     * 从服务器获取最新的用户信息并更新本地缓存
     */
    suspend fun refreshUserProfile()

    /**
     * 获取缓存的用户个人资料
     * @return 缓存的用户个人资料，如果没有缓存则返回null
     */
    suspend fun getCachedUserProfile(): ProfileResponse?

    /**
     * 缓存用户个人资料
     * @param profile 用户个人资料
     */
    suspend fun cacheUserProfile(profile: ProfileResponse)

    /**
     * 获取用户昵称
     * @return 用户昵称的Flow
     */
    val userNickname: Flow<String?>

    /**
     * 获取用户头像
     * @return 用户头像URL的Flow
     */
    val userAvatar: Flow<String?>

    /**
     * 获取用户邮箱
     * @return 用户邮箱的Flow
     */
    val userEmail: Flow<String?>
}