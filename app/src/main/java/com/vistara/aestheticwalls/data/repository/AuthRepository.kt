package com.vistara.aestheticwalls.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.Flow

/**
 * 认证仓库接口
 * 负责处理用户认证相关的操作
 */
interface AuthRepository {
    /**
     * 获取当前用户是否已登录
     */
    val isUserLoggedIn: Flow<Boolean>

    /**
     * 获取当前用户ID
     */
    val userId: Flow<String?>

    /**
     * 获取当前用户名
     */
    val userName: Flow<String?>

    /**
     * 获取当前用户头像URL
     */
    val userPhotoUrl: Flow<String?>

    /**
     * 获取当前用户邮箱
     */
    val userEmail: Flow<String?>

    /**
     * 处理Google登录结果
     */
    suspend fun handleSignInResult(completedTask: Task<GoogleSignInAccount>): Boolean

    /**
     * 退出登录
     */
    suspend fun signOut()

    /**
     * 检查用户是否已登录
     */
    suspend fun checkUserLoggedIn(): Boolean
}
