package com.vistara.aestheticwalls.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("google/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/system/order/add")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<CreateOrderResponse>

    @GET("products")
    suspend fun getProducts(): Response<ProductsResponse>

    @GET("/system/method/getPayMethod")
    suspend fun getPaymentMethods(): Response<PaymentMethodsResponse>
}

data class LoginRequest(
    val nickname: String, val email: String, val avatar: String, val token: String
)

data class LoginResponse(
    val token: String, val isPremium: Boolean? = false, val msg: String? = null
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
    val products: List<Product>, val msg: String? = null
)

data class PaymentMethod(
    val id: String, val name: String, val icon: String? = null, val isAvailable: Boolean = true
)

data class PaymentMethodsResponse(
    val methods: List<PaymentMethod>, val msg: String? = null
)