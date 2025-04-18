package com.vistara.aestheticwalls.data.remote

import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API使用统计跟踪器
 * 用于跟踪各API的调用次数、成功率和错误情况
 */
@Singleton
class ApiUsageTracker @Inject constructor() {

    companion object {
        @Volatile
        private var instance: ApiUsageTracker? = null

        /**
         * 获取ApiUsageTracker的单例实例
         */
        fun getInstance(): ApiUsageTracker {
            return instance ?: synchronized(this) {
                instance ?: ApiUsageTracker().also { instance = it }
            }
        }
    }
    private val tag = "ApiUsageTracker"

    // 使用线程安全的集合和计数器
    private val apiCallCounts = ConcurrentHashMap<ApiSource, AtomicInteger>()
    private val apiErrorCounts = ConcurrentHashMap<ApiSource, AtomicInteger>()
    private val apiSuccessCounts = ConcurrentHashMap<ApiSource, AtomicInteger>()

    // 跟踪API速率限制状态
    private val apiRateLimited = ConcurrentHashMap<ApiSource, AtomicBoolean>()
    private val apiRateLimitResetTime = ConcurrentHashMap<ApiSource, AtomicLong>()

    // 速率限制恢复时间（毫秒）
    private val RATE_LIMIT_RESET_DURATION = 600000L // 10分钟，缩短速率限制时间以便于测试

    // 初始化计数器
    init {
        ApiSource.values().forEach { source ->
            apiCallCounts[source] = AtomicInteger(0)
            apiErrorCounts[source] = AtomicInteger(0)
            apiSuccessCounts[source] = AtomicInteger(0)
            apiRateLimited[source] = AtomicBoolean(false)
            apiRateLimitResetTime[source] = AtomicLong(0)
        }
    }

    /**
     * 跟踪API调用
     * @param source API来源
     * @return 如果API已经被速率限制，返回true
     */
    fun trackApiCall(source: ApiSource): Boolean {
        // 检查是否处于速率限制状态
        if (isApiRateLimited(source)) {
            Log.w(tag, "API ${source.name} is rate limited, skipping call")
            return true
        }

        apiCallCounts[source]?.incrementAndGet()
        Log.d(tag, "API call to ${source.name}, total: ${apiCallCounts[source]?.get()}")
        return false
    }

    /**
     * 跟踪API错误
     * @param source API来源
     * @param errorMessage 错误信息
     * @param statusCode HTTP状态码
     */
    fun trackApiError(source: ApiSource, errorMessage: String? = null, statusCode: Int? = null) {
        apiErrorCounts[source]?.incrementAndGet()
        Log.w(tag, "API error from ${source.name}: $errorMessage, status code: $statusCode, total errors: ${apiErrorCounts[source]?.get()}")

        // 检查是否是速率限制错误
        if (statusCode == 403 || errorMessage?.contains("Rate Limit") == true || errorMessage?.contains("rate limit") == true) {
            setApiRateLimited(source)
            Log.e(tag, "API ${source.name} is now rate limited until ${apiRateLimitResetTime[source]?.get()}")
        }
    }

    /**
     * 跟踪API成功
     * @param source API来源
     */
    fun trackApiSuccess(source: ApiSource) {
        apiSuccessCounts[source]?.incrementAndGet()
        Log.d(tag, "API success from ${source.name}, total successes: ${apiSuccessCounts[source]?.get()}")
    }

    /**
     * 获取API使用统计
     * @return 各API的使用统计
     */
    fun getUsageStats(): Map<ApiSource, ApiStats> {
        return ApiSource.values().associateWith { source ->
            ApiStats(
                callCount = apiCallCounts[source]?.get() ?: 0,
                errorCount = apiErrorCounts[source]?.get() ?: 0,
                successCount = apiSuccessCounts[source]?.get() ?: 0
            )
        }
    }

    /**
     * 获取特定API的使用统计
     * @param source API来源
     * @return API使用统计
     */
    fun getApiStats(source: ApiSource): ApiStats {
        return ApiStats(
            callCount = apiCallCounts[source]?.get() ?: 0,
            errorCount = apiErrorCounts[source]?.get() ?: 0,
            successCount = apiSuccessCounts[source]?.get() ?: 0
        )
    }

    /**
     * 重置所有API使用统计
     */
    fun resetAllStats() {
        ApiSource.values().forEach { source ->
            apiCallCounts[source]?.set(0)
            apiErrorCounts[source]?.set(0)
            apiSuccessCounts[source]?.set(0)
        }
        Log.i(tag, "All API usage statistics have been reset")
    }

    /**
     * 重置所有API速率限制
     */
    fun resetAllRateLimits() {
        ApiSource.values().forEach { source ->
            apiRateLimited[source]?.set(false)
            apiRateLimitResetTime[source]?.set(0)
        }
        Log.i(tag, "All API rate limits have been reset")
    }

    /**
     * 重置特定API的使用统计
     * @param source API来源
     */
    fun resetApiStats(source: ApiSource) {
        apiCallCounts[source]?.set(0)
        apiErrorCounts[source]?.set(0)
        apiSuccessCounts[source]?.set(0)
        apiRateLimited[source]?.set(false)
        apiRateLimitResetTime[source]?.set(0)
        Log.i(tag, "API usage statistics for ${source.name} have been reset")
    }

    /**
     * 设置API速率限制状态
     * @param source API来源
     * @param resetDuration 可选的自定义重置时间（毫秒），如果为null则使用默认值
     */
    fun setApiRateLimited(source: ApiSource, resetDuration: Long? = null) {
        apiRateLimited[source]?.set(true)
        val duration = resetDuration ?: RATE_LIMIT_RESET_DURATION
        val resetTime = System.currentTimeMillis() + duration
        apiRateLimitResetTime[source]?.set(resetTime)
        Log.w(tag, "API ${source.name} is now rate limited until ${resetTime} (大约${duration/60000}分钟)")
    }

    /**
     * 检查API是否处于速率限制状态
     * @param source API来源
     * @return 如果处于速率限制状态返回true
     */
    fun isApiRateLimited(source: ApiSource): Boolean {
        val isLimited = apiRateLimited[source]?.get() ?: false
        if (!isLimited) {
            return false
        }

        // 检查速率限制是否已经过期
        val resetTime = apiRateLimitResetTime[source]?.get() ?: 0
        val currentTime = System.currentTimeMillis()

        if (currentTime > resetTime) {
            // 速率限制已过期，重置状态
            apiRateLimited[source]?.set(false)
            apiRateLimitResetTime[source]?.set(0)
            Log.i(tag, "API ${source.name} rate limit has expired")
            return false
        }

        return true
    }

    /**
     * API统计数据类
     */
    data class ApiStats(
        val callCount: Int,
        val errorCount: Int,
        val successCount: Int
    ) {
        /**
         * 计算成功率
         * @return 成功率百分比，如果没有调用则返回100
         */
        val successRate: Float
            get() = if (callCount > 0) (successCount.toFloat() / callCount) * 100 else 100f

        /**
         * 计算错误率
         * @return 错误率百分比，如果没有调用则返回0
         */
        val errorRate: Float
            get() = if (callCount > 0) (errorCount.toFloat() / callCount) * 100 else 0f
    }
}
