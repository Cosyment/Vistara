package com.vistara.aestheticwalls.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("google/login")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<LoginResponse>
}

data class GoogleLoginRequest(
    val nickname: String,
    val email: String,
    val avatar: String,
    val token: String
)

data class LoginResponse(
    val token: String,
    val isPremium: Boolean? = false,
    val msg: String? = null
) 