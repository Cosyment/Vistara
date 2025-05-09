package com.vistara.aestheticwalls.ui.screens.webview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.repository.DiamondRepository
import com.vistara.aestheticwalls.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * WebView页面的ViewModel
 */
@HiltViewModel
class WebViewViewModel @Inject constructor(
    private val diamondRepository: DiamondRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    companion object {
        private const val TAG = "WebViewViewModel"
    }

    // 支付处理状态
    private val _paymentProcessingState = MutableStateFlow<PaymentProcessingState>(PaymentProcessingState.Idle)
    val paymentProcessingState: StateFlow<PaymentProcessingState> = _paymentProcessingState.asStateFlow()

    /**
     * 处理支付成功的URL
     * 如果URL包含status=SUCCESS参数，则调用checkOrder接口
     * @param url 重定向的URL
     * @return 是否处理了支付成功
     */
    fun handlePaymentSuccessUrl(url: String): Boolean {
        // 检查URL是否包含status=SUCCESS参数
        if (url.contains("status=SUCCESS", ignoreCase = true)) {
            Log.d(TAG, "Payment success URL detected: $url")

            // 提取outTradeNo参数
            val outTradeNo = extractOutTradeNo(url)
            if (outTradeNo != null) {
                Log.d(TAG, "Extracted outTradeNo: $outTradeNo")
                // 调用checkOrder接口
                checkOrderStatus(outTradeNo)
                return true
            } else {
                Log.e(TAG, "Failed to extract outTradeNo from URL: $url")
            }
        }
        return false
    }

    /**
     * 从URL中提取outTradeNo参数
     * @param url 包含outTradeNo参数的URL
     * @return outTradeNo参数值，如果不存在则返回null
     */
    private fun extractOutTradeNo(url: String): String? {
        // 使用正则表达式提取outTradeNo参数
        val regex = "outTradeNo=([^&]+)".toRegex()
        val matchResult = regex.find(url)
        return matchResult?.groupValues?.getOrNull(1)
    }

    /**
     * 检查订单状态
     * @param outTradeNo 订单号
     */
    private fun checkOrderStatus(outTradeNo: String) {
        viewModelScope.launch {
            try {
                _paymentProcessingState.value = PaymentProcessingState.CheckingOrder

                // 调用checkOrder接口
                val result = diamondRepository.checkOrder(outTradeNo)

                if (result is ApiResult.Success) {
                    Log.d(TAG, "Order check successful: ${result.data}")
                    _paymentProcessingState.value = PaymentProcessingState.OrderCheckSuccess

                    // 刷新用户信息
                    refreshUserProfile()
                } else if (result is ApiResult.Error) {
                    Log.e(TAG, "Order check failed: ${result.message}")
                    _paymentProcessingState.value = PaymentProcessingState.OrderCheckFailed(result.message)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking order: ${e.message}", e)
                _paymentProcessingState.value = PaymentProcessingState.OrderCheckFailed(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * 刷新用户信息
     */
    private suspend fun refreshUserProfile() {
        try {
            _paymentProcessingState.value = PaymentProcessingState.RefreshingUserProfile

            // 刷新用户信息
            userRepository.refreshUserProfile()

            _paymentProcessingState.value = PaymentProcessingState.RefreshUserProfileSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing user profile: ${e.message}", e)
            _paymentProcessingState.value = PaymentProcessingState.RefreshUserProfileFailed(e.message ?: "Unknown error")
        }
    }

    /**
     * 重置支付处理状态
     */
    fun resetPaymentProcessingState() {
        _paymentProcessingState.value = PaymentProcessingState.Idle
    }
}

/**
 * 支付处理状态
 */
sealed class PaymentProcessingState {
    object Idle : PaymentProcessingState()
    object CheckingOrder : PaymentProcessingState()
    object OrderCheckSuccess : PaymentProcessingState()
    data class OrderCheckFailed(val error: String) : PaymentProcessingState()
    object RefreshingUserProfile : PaymentProcessingState()
    object RefreshUserProfileSuccess : PaymentProcessingState()
    data class RefreshUserProfileFailed(val error: String) : PaymentProcessingState()
}
