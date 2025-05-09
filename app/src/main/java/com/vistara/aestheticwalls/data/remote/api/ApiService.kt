package com.vistara.aestheticwalls.data.remote.api

import com.vistara.aestheticwalls.data.model.DiamondProduct
import com.vistara.aestheticwalls.data.model.DiamondTransaction
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("google/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @GET("/system/user/getUserInfo")
    suspend fun getProfile(): ApiResponse<ProfileResponse>

    @POST("/system/order/add")
    suspend fun createOrder(@Body request: CreateOrderRequest): ApiResponse<CreateOrderResponse>

    @GET("/system/price/getList")
    suspend fun getProducts(): ApiResponse<List<DiamondProduct>>

    @GET("/system/method/getPayMethod/{itemName}")
    suspend fun getPaymentMethods(@Path("itemName") itemName: String): ApiResponse<List<PaymentMethod>>

    @GET("/system/order/list")
    suspend fun getOrders(): ApiResponse<List<DiamondTransaction>>

    @POST("/system/order/myCallback/{outTradeNo}")
    suspend fun checkOrder(@Path("outTradeNo") outTradeNo: String): ApiResponse<String>
}

data class ApiResponse<T>(
    val code: Int, val msg: String, val data: T?
) {

    val isSuccess: Boolean
        get() = code == 200
}

data class LoginRequest(
    val nickname: String, val email: String, val avatar: String, val token: String
)

data class LoginResponse(
    val token: String, val isPremium: Boolean? = false
)

data class ProfileResponse(
    val nickname: String,
    val email: String,
    val avatar: String,
    val expireTime: String? = null,
    val diamond: Int?,
    val isWhiteList: String
) {
    val isPremium: Boolean get() = isWhitelisted
    val isWhitelisted: Boolean get() = isWhiteList == "0"
}

data class CreateOrderRequest(
    val priceId: String, val paymentMethodId: String
)

data class CreateOrderResponse(
    val id: String,
    val status: String,
    val payUrl: String? = null,
    val priceId: String,
    val diamondNum: Int,
    val payMethodId: Int
) {
    val isGooglePay: Boolean get() = payMethodId == 1
}


data class PaymentMethod(
    val id: String,
    val name: String,
    val price: String,
    val dollarPrice: String,
    val currency: String,
    val productId: String,
    val payMethodId: Int,
    val payTypeMessage: String
)
