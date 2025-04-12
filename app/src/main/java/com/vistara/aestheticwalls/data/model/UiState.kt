package com.vistara.aestheticwalls.data.model

/**
 * UI状态密封类，用于表示 UI 层的数据加载状态
 * @param T 数据类型
 */
sealed class UiState<out T> {
    /**
     * 加载中状态
     */
    data object Loading : UiState<Nothing>()

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

    /**
     * 辅助函数，判断是否加载成功
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * 辅助函数，判断是否加载失败
     */
    val isError: Boolean get() = this is Error

    /**
     * 辅助函数，判断是否正在加载
     */
    val isLoading: Boolean get() = this is Loading

    /**
     * 辅助函数，安全获取成功数据
     * 如果加载失败，返回null
     */
    fun getOrNull(): T? = if (this is Success) data else null

    /**
     * 辅助函数，处理成功情况
     * @param action 加载成功时的回调
     */
    inline fun onSuccess(action: (T) -> Unit): UiState<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * 辅助函数，处理失败情况
     * @param action 加载失败时的回调
     */
    inline fun onError(action: (String) -> Unit): UiState<T> {
        if (this is Error) action(message)
        return this
    }

    /**
     * 辅助函数，处理加载情况
     * @param action 正在加载时的回调
     */
    inline fun onLoading(action: () -> Unit): UiState<T> {
        if (this is Loading) action()
        return this
    }
}
