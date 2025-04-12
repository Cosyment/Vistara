package com.vistara.aestheticwalls.data.remote

import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * API负载均衡器
 * 用于在多个API之间进行负载均衡，避免单个API使用过度
 */
@Singleton
class ApiLoadBalancer @Inject constructor(
    private val apiUsageTracker: ApiUsageTracker
) {
    private val tag = "ApiLoadBalancer"
    
    // 各API的使用计数
    private val apiUsageCount = ConcurrentHashMap<ApiSource, AtomicInteger>()
    
    // 各API的限制
    private val apiLimits = mapOf(
        ApiSource.UNSPLASH to 50,  // 每小时50次请求
        ApiSource.PEXELS to 200,   // 每小时200次请求
        ApiSource.PIXABAY to 100,  // 每小时100次请求
        ApiSource.WALLHAVEN to 45  // 每小时45次请求
    )
    
    // 初始化计数器
    init {
        ApiSource.values().forEach { source ->
            apiUsageCount[source] = AtomicInteger(0)
        }
        
        // 每小时重置计数
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(TimeUnit.HOURS.toMillis(1))
                resetUsageCounts()
            }
        }
    }
    
    /**
     * 获取下一个要使用的API来源
     * 基于当前使用情况和限制选择最合适的API
     * @return API来源
     */
    fun getNextApiSource(): ApiSource {
        // 获取所有未达到限制的API
        val availableApis = ApiSource.values().filter { source ->
            val currentUsage = apiUsageCount[source]?.get() ?: 0
            val limit = apiLimits[source] ?: Int.MAX_VALUE
            currentUsage < limit
        }
        
        if (availableApis.isEmpty()) {
            // 如果所有API都达到限制，选择限制最高的
            Log.w(tag, "All APIs have reached their limits, using the one with highest limit")
            return apiLimits.entries.maxByOrNull { it.value }?.key ?: ApiSource.UNSPLASH
        }
        
        // 选择使用率最低的API
        val selectedApi = availableApis.minByOrNull { source ->
            val currentUsage = apiUsageCount[source]?.get() ?: 0
            val limit = apiLimits[source] ?: Int.MAX_VALUE
            currentUsage.toFloat() / limit
        } ?: ApiSource.UNSPLASH
        
        // 增加使用计数
        apiUsageCount[selectedApi]?.incrementAndGet()
        
        Log.d(tag, "Selected API: ${selectedApi.name}, usage: ${apiUsageCount[selectedApi]?.get()}/${apiLimits[selectedApi]}")
        
        return selectedApi
    }
    
    /**
     * 重置所有API的使用计数
     */
    fun resetUsageCounts() {
        ApiSource.values().forEach { source ->
            apiUsageCount[source]?.set(0)
        }
        Log.i(tag, "API usage counts have been reset")
    }
    
    /**
     * 获取API的当前使用情况
     * @return API使用情况映射
     */
    fun getApiUsage(): Map<ApiSource, ApiUsage> {
        return ApiSource.values().associateWith { source ->
            val currentUsage = apiUsageCount[source]?.get() ?: 0
            val limit = apiLimits[source] ?: Int.MAX_VALUE
            ApiUsage(currentUsage, limit)
        }
    }
    
    /**
     * API使用情况数据类
     */
    data class ApiUsage(
        val currentUsage: Int,
        val limit: Int
    ) {
        /**
         * 计算使用率
         * @return 使用率百分比
         */
        val usageRate: Float
            get() = (currentUsage.toFloat() / limit) * 100
            
        /**
         * 检查是否达到限制
         * @return 是否达到限制
         */
        val isLimitReached: Boolean
            get() = currentUsage >= limit
    }
}
