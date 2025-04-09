package com.vistara.aestheticwalls.data.remote

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
 * 安全API调用的辅助函数
 * 用于包装可能抛出异常的API调用
 * @param source API来源
 * @param call 实际的API调用函数
 * @return ApiResult包装的结果
 */
suspend fun <T> safeApiCall(
    source: ApiSource,
    call: suspend () -> T
): ApiResult<T> = try {
    ApiResult.Success(call())
} catch (e: Exception) {
    ApiResult.Error(
        message = e.message ?: "Unknown error",
        source = source
    )
} 