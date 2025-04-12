package com.vistara.aestheticwalls.data.remote

import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API使用统计跟踪器
 * 用于跟踪各API的调用次数、成功率和错误情况
 */
@Singleton
class ApiUsageTracker @Inject constructor() {
    private val tag = "ApiUsageTracker"
    
    // 使用线程安全的集合和计数器
    private val apiCallCounts = ConcurrentHashMap<ApiSource, AtomicInteger>()
    private val apiErrorCounts = ConcurrentHashMap<ApiSource, AtomicInteger>()
    private val apiSuccessCounts = ConcurrentHashMap<ApiSource, AtomicInteger>()
    
    // 初始化计数器
    init {
        ApiSource.values().forEach { source ->
            apiCallCounts[source] = AtomicInteger(0)
            apiErrorCounts[source] = AtomicInteger(0)
            apiSuccessCounts[source] = AtomicInteger(0)
        }
    }
    
    /**
     * 跟踪API调用
     * @param source API来源
     */
    fun trackApiCall(source: ApiSource) {
        apiCallCounts[source]?.incrementAndGet()
        Log.d(tag, "API call to ${source.name}, total: ${apiCallCounts[source]?.get()}")
    }
    
    /**
     * 跟踪API错误
     * @param source API来源
     * @param errorMessage 错误信息
     */
    fun trackApiError(source: ApiSource, errorMessage: String? = null) {
        apiErrorCounts[source]?.incrementAndGet()
        Log.w(tag, "API error from ${source.name}: $errorMessage, total errors: ${apiErrorCounts[source]?.get()}")
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
     * 重置特定API的使用统计
     * @param source API来源
     */
    fun resetApiStats(source: ApiSource) {
        apiCallCounts[source]?.set(0)
        apiErrorCounts[source]?.set(0)
        apiSuccessCounts[source]?.set(0)
        Log.i(tag, "API usage statistics for ${source.name} have been reset")
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
