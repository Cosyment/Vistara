package com.vistara.aestheticwalls.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    /**
     * 发送语言变化事件
     */
    suspend fun emitLanguageChanged() {
        _languageChangedEvent.emit(Unit)
    }
}
