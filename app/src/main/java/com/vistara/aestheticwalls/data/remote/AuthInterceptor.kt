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
    private val userRepository: dagger.Lazy<UserRepository>,
    private val authRepository: dagger.Lazy<AuthRepository>
) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
        private const val HEADER_AUTH = "Authorization"
        private const val HEADER_EMAIL = "Email"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        // 获取token和邮箱
        val token = runBlocking {
            val t = userRepository.get().getServerToken()
            Log.d(TAG, "获取到token: ${if (t.isNullOrEmpty()) "" else t}")
            t
        }

        val email = runBlocking {
            try {
                val e = authRepository.get().userEmail.first()
                Log.d(TAG, "获取到邮箱: ${if (e.isNullOrEmpty()) "" else e}")
                e
            } catch (e: Exception) {
                Log.e(TAG, "获取邮箱失败: ${e.message}", e)
                null
            }
        }

        // 如果token为空，则不添加认证头
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest).also {
                Log.d(TAG, "请求完成，响应码: ${it.code}")
            }
        }

        // 构建新的请求，添加认证头和邮箱头
        val requestBuilder = originalRequest.newBuilder().header(HEADER_AUTH, token)

        // 如果邮箱不为空，添加邮箱头
        if (!email.isNullOrEmpty()) {
            requestBuilder.header(HEADER_EMAIL, email)
            Log.d(TAG, "添加邮箱头: $email")
        }

        val newRequest = requestBuilder.build()

        return chain.proceed(newRequest).also {
            if (!it.isSuccessful) {
                Log.e(TAG, "请求失败，响应码: ${it.code}, 消息: ${it.message}")
            }
        }
    }
}
