package com.vistara.aestheticwalls.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 网络状态类型
 */
enum class NetworkType {
    WIFI,       // WiFi网络
    CELLULAR,   // 移动数据网络
    OTHER,      // 其他网络类型
    NONE        // 无网络连接
}

/**
 * 网络状态监听器
 * 用于监控网络连接状态和类型
 */
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager = 
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * 检查当前是否有网络连接
     * @return 是否有网络连接
     */
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    /**
     * 获取当前网络类型
     * @return 网络类型
     */
    fun getNetworkType(): NetworkType {
        val network = connectivityManager.activeNetwork ?: return NetworkType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.NONE
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.OTHER
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> NetworkType.OTHER
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkType.OTHER
            else -> NetworkType.NONE
        }
    }
    
    /**
     * 监听网络状态变化
     * @return 网络状态Flow
     */
    fun networkStatus(): Flow<NetworkType> = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                val networkType = when {
                    capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
                    capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.CELLULAR
                    capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkType.OTHER
                    capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) == true -> NetworkType.OTHER
                    capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true -> NetworkType.OTHER
                    else -> NetworkType.NONE
                }
                trySend(networkType)
            }
            
            override fun onLost(network: Network) {
                trySend(NetworkType.NONE)
            }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(request, networkCallback)
        
        // 发送初始状态
        trySend(getNetworkType())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged()
}
