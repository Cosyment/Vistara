package com.vistara.aestheticwalls.utils

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 事件总线
 * 用于在不同组件之间传递事件
 */
@Singleton
class EventBus @Inject constructor() {
    // 语言变化事件
    private val _languageChangedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val languageChangedEvent = _languageChangedEvent.asSharedFlow()

    // 用于非挂起函数的协程作用域
    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * 发送语言变化事件（非挂起函数版本）
     * 可以在非协程上下文中调用
     */
    fun emitLanguageChanged() {
        Log.d("EventBus", "发送语言变化事件（非挂起函数版本）")
        scope.launch {
            _languageChangedEvent.emit(Unit)
        }
    }
}
