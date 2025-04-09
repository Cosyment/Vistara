package com.vistara.aestheticwalls.data.model

/**
 * UI状态密封类，用于表示数据加载状态
 * @param T 数据类型
 */
sealed class UiState<out T> {
    /**
     * 加载中状态
     */
    object Loading : UiState<Nothing>()

    /**
     * 成功状态，携带数据
     * @param data 加载的数据
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * 错误状态，携带错误信息
     * @param message 错误信息
     */
    data class Error(val message: String) : UiState<Nothing>()
}
