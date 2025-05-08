package com.vistara.aestheticwalls.data.remote

import android.util.Log
import com.vistara.aestheticwalls.data.repository.AuthRepository
import com.vistara.aestheticwalls.data.repository.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 认证拦截器
 * 用于在请求头中添加token和邮箱信息
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
        private const val HEADER_AUTH = "Authorization"
        private const val HEADER_EMAIL = "X-User-Email"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 获取token和邮箱
        val token = runBlocking { userRepository.getServerToken() }
        val email = runBlocking {
            try {
                authRepository.userEmail.first()
            } catch (e: Exception) {
                Log.e(TAG, "获取邮箱失败: ${e.message}")
                null
            }
        }

        // 如果token为空，则不添加认证头
        if (token.isNullOrEmpty()) {
            Log.d(TAG, "Token为空，不添加认证头")
            return chain.proceed(originalRequest)
        }

        // 构建新的请求，添加认证头和邮箱头
        val requestBuilder = originalRequest.newBuilder()
            .header(HEADER_AUTH, "Bearer $token")

        // 如果邮箱不为空，添加邮箱头
        if (!email.isNullOrEmpty()) {
            requestBuilder.header(HEADER_EMAIL, email)
            Log.d(TAG, "添加邮箱头: $email")
        }

        val newRequest = requestBuilder.build()
        Log.d(TAG, "添加认证头: Bearer $token")

        return chain.proceed(newRequest)
    }
}
