package com.vistara.aestheticwalls.data.util

/**
 * 网络请求结果的封装类
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
} 