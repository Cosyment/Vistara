package com.vistara.aestheticwalls.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * 刷新工具类
 * 提供延迟结束刷新状态的功能，改善用户体验
 */
object RefreshUtil {
    /**
     * 延迟结束刷新状态
     * @param isRefreshing 刷新状态Flow
     * @param scope 协程作用域
     * @param delayMillis 延迟时间（毫秒）
     */
    fun delayedEndRefreshing(
        isRefreshing: MutableStateFlow<Boolean>,
        scope: CoroutineScope,
        delayMillis: Long = 800 // 默认延迟800毫秒
    ) {
        if (isRefreshing.value) {
            scope.launch {
                delay(delayMillis)
                isRefreshing.value = false
            }
        }
    }
}
