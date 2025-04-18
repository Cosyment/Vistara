package com.vistara.aestheticwalls.data.remote

import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.IOException
import android.util.Log
import retrofit2.HttpException
import javax.inject.Inject

/**
 * 统一的API结果处理类
 * 用于包装所有API请求的结果，提供统一的成功和失败处理
 */
sealed class ApiResult<out T> {
    /**
     * API请求成功
     * @param data 请求返回的数据
     */
    data class Success<T>(val data: T) : ApiResult<T>()

    /**
     * API请求失败
     * @param code 错误代码（可选）
     * @param message 错误信息
     * @param source 错误来源
     */
    data class Error(
        val code: Int? = null,
        val message: String,
        val source: ApiSource
    ) : ApiResult<Nothing>()

    /**
     * API请求加载中
     * 表示正在获取数据的临时状态
     */
    data object Loading : ApiResult<Nothing>()

    /**
     * 辅助函数，用于判断是否请求成功
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * 辅助函数，用于判断是否请求失败
     */
    val isError: Boolean get() = this is Error

    /**
     * 辅助函数，用于判断是否正在加载
     */
    val isLoading: Boolean get() = this is Loading

    /**
     * 辅助函数，安全获取成功数据
     * 如果请求失败，返回null
     */
    fun getOrNull(): T? = if (this is Success) data else null

    /**
     * 辅助函数，处理成功和失败情况
     * @param action 请求成功时的回调
     */
    inline fun onSuccess(action: (T) -> Unit): ApiResult<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * 辅助函数，处理失败情况
     * @param action 请求失败时的回调
     */
    inline fun onError(action: (code: Int?, message: String, source: ApiSource) -> Unit): ApiResult<T> {
        if (this is Error) action(code, message, source)
        return this
    }

    /**
     * 辅助函数，处理加载情况
     * @param action 请求加载时的回调
     */
    inline fun onLoading(action: () -> Unit): ApiResult<T> {
        if (this is Loading) action()
        return this
    }
}

/**
 * API来源枚举，用于标识错误来自哪个API
 */
enum class ApiSource {
    UNSPLASH,
    PEXELS,
    PIXABAY,
    WALLHAVEN
}

/**
 * API使用跟踪器
 * 用于跟踪API调用次数、成功率和错误情况
 */
class ApiCallHelper @Inject constructor(
    private val apiUsageTracker: ApiUsageTracker
) {
    /**
     * 安全API调用的辅助函数
     * 用于包装可能抛出异常的API调用
     * @param source API来源
     * @param maxRetries 最大重试次数
     * @param call 实际的API调用函数
     * @return ApiResult包装的结果
     */
    suspend fun <T> safeApiCall(
        source: ApiSource,
        maxRetries: Int = 2,
        call: suspend () -> T
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        val tag = "SafeApiCall"
        var retryCount = 0
        var lastException: Exception? = null

        // 检查是否处于速率限制状态
        if (apiUsageTracker.isApiRateLimited(source)) {
            Log.e(tag, "API ${source.name} is rate limited, skipping call")
            return@withContext ApiResult.Error(
                code = 429, // Too Many Requests
                message = "API rate limit exceeded, please try again later",
                source = source
            )
        }

        // 记录API调用
        apiUsageTracker.trackApiCall(source)

        while (retryCount <= maxRetries) {
            try {
                val result = call()
                apiUsageTracker.trackApiSuccess(source)
                return@withContext ApiResult.Success(result)
            } catch (e: HttpException) {
                // 检查是否是速率限制错误
                if (e.code() == 403 || e.code() == 429) {
                    apiUsageTracker.trackApiError(source, e.message(), e.code())
                    Log.e(tag, "API ${source.name} rate limit exceeded: ${e.message()}")
                    return@withContext ApiResult.Error(
                        code = e.code(),
                        message = "API rate limit exceeded, please try again later",
                        source = source
                    )
                }

                // 其他HTTP错误
                lastException = e
                retryCount++
                apiUsageTracker.trackApiError(source, e.message(), e.code())
                Log.w(tag, "API call to ${source.name} failed with HTTP ${e.code()} (attempt $retryCount/$maxRetries): ${e.message()}")

                if (retryCount <= maxRetries) {
                    val delayTime = 1000L * retryCount // 指数退避
                    Log.d(tag, "Retrying in $delayTime ms...")
                    delay(delayTime)
                    continue
                }
            } catch (e: IOException) {
                // 网络错误可以重试
                lastException = e
                retryCount++
                apiUsageTracker.trackApiError(source, e.message)
                Log.w(tag, "API call to ${source.name} failed (attempt $retryCount/$maxRetries): ${e.message}")

                if (retryCount <= maxRetries) {
                    val delayTime = 1000L * retryCount // 指数退避
                    Log.d(tag, "Retrying in $delayTime ms...")
                    delay(delayTime)
                    continue
                }
            } catch (e: Exception) {
                // 其他错误直接返回
                apiUsageTracker.trackApiError(source, e.message)
                Log.e(tag, "API call to ${source.name} failed with non-retryable error: ${e.message}")
                return@withContext ApiResult.Error(
                    message = e.message ?: "Unknown error",
                    source = source
                )
            }
        }

        // 重试失败
        Log.e(tag, "API call to ${source.name} failed after $maxRetries retries")
        return@withContext ApiResult.Error(
            message = lastException?.message ?: "Network error after retries",
            source = source
        )
    }
}

/**
 * 安全API调用的辅助函数
 * 用于包装可能抛出异常的API调用
 * 这是一个顶级函数，它会使用ApiCallHelper中的实现
 * @param source API来源
 * @param maxRetries 最大重试次数
 * @param call 实际的API调用函数
 * @return ApiResult包装的结果
 */
suspend fun <T> safeApiCall(
    source: ApiSource,
    maxRetries: Int = 2,
    call: suspend () -> T
): ApiResult<T> {
    // 获取ApiUsageTracker的单例实例
    val apiUsageTracker = ApiUsageTracker.getInstance()
    val apiCallHelper = ApiCallHelper(apiUsageTracker)

    // 使用ApiCallHelper中的实现
    return apiCallHelper.safeApiCall(source, maxRetries, call)
}