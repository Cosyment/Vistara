package com.vistara.aestheticwalls.data.remote.api

import com.vistara.aestheticwalls.data.model.DiamondProduct
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("google/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @GET("/system/user/getUserInfo")
    suspend fun getProfile(): ApiResponse<ProfileResponse>

    @POST("/system/order/add")
    suspend fun createOrder(@Body request: CreateOrderRequest): ApiResponse<CreateOrderResponse>

    @GET("/system/price/getList")
    suspend fun getProducts(): ApiResponse<List<DiamondProduct>>

    @GET("/system/method/getPayMethod")
    suspend fun getPaymentMethods(): ApiResponse<PaymentMethodsResponse>
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
    val token: String,
    val isPremium: Boolean? = false
)

data class ProfileResponse(
    val nickname: String,
    val email: String,
    val avatar: String,
    val diamond: Int
)

data class CreateOrderRequest(
    val productId: String, val quantity: Int = 1, val paymentMethodId: String
)

data class CreateOrderResponse(
    val orderId: String, val status: String, val paymentUrl: String? = null, val msg: String? = null
)

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String = "CNY",
    val imageUrl: String? = null
)

data class ProductsResponse(
    val products: List<Product>?, val msg: String? = null
)

data class PaymentMethod(
    val id: String, val name: String, val icon: String? = null, val isAvailable: Boolean = true
)

data class PaymentMethodsResponse(
    val methods: List<PaymentMethod>, val msg: String? = null
)