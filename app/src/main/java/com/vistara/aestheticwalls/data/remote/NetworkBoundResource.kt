package com.vistara.aestheticwalls.data.remote

import kotlinx.coroutines.flow.flow

/**
 * 网络绑定资源辅助类
 * 用于统一处理网络请求和缓存逻辑，实现离线优先策略
 */
inline fun <ResultType, RequestType> networkBoundResource(
    // 从数据库获取缓存数据的函数
    crossinline loadFromDb: suspend () -> ResultType?,
    // 判断缓存数据是否过期/有效的函数
    crossinline shouldFetch: (ResultType?) -> Boolean = { true },
    // 从网络获取新数据的函数
    crossinline fetchFromNetwork: suspend () -> ApiResult<RequestType>,
    // 保存网络结果到本地的函数
    crossinline saveNetworkResult: suspend (RequestType) -> Unit,
    // 将网络响应映射为本地数据类型的函数
    crossinline mapNetworkResult: (RequestType) -> ResultType,
    // 获取缓存数据作为结果的函数
    crossinline onFetchFailed: suspend (Throwable) -> Unit = { }
) = flow<ApiResult<ResultType>> {
    
    // 1. 首先从缓存加载数据
    val data = loadFromDb()
    
    // 2. 发送缓存数据（如果有）
    if (data != null) {
        emit(ApiResult.Success(data))
    } else {
        // 没有缓存数据时发送加载状态
        emit(ApiResult.Loading)
    }
    
    // 3. 确定是否需要从网络获取新数据
    val shouldFetchFromNetwork = shouldFetch(data)
    
    if (shouldFetchFromNetwork) {
        try {
            // 4. 从网络获取新数据
            val networkResult = fetchFromNetwork()
            
            when (networkResult) {
                is ApiResult.Success -> {
                    // 5. 保存网络结果到数据库
                    saveNetworkResult(networkResult.data)
                    
                    // 6. 重新从数据库获取数据，以保证统一的数据源
                    val mappedResult = mapNetworkResult(networkResult.data)
                    emit(ApiResult.Success(mappedResult))
                }
                is ApiResult.Error -> {
                    // 如果网络请求失败，但有缓存数据，仍然发送错误
                    emit(ApiResult.Error(
                        code = networkResult.code,
                        message = networkResult.message,
                        source = networkResult.source
                    ))
                }
                is ApiResult.Loading -> {
                    // 网络请求中，保持现有状态
                    // 通常不会从网络请求中返回Loading状态
                }
            }
        } catch (e: Exception) {
            // 7. 处理异常情况
            onFetchFailed(e)
            
            // 8. 发送错误消息
            emit(ApiResult.Error(
                code = null,
                message = e.message ?: "网络请求失败",
                source = ApiSource.UNSPLASH // 默认来源，实际应该传入正确的来源
            ))
        }
    }
} 