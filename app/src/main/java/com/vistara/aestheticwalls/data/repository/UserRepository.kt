package com.vistara.aestheticwalls.data.repository

import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val isPremiumUser: Flow<Boolean>
    suspend fun checkPremiumStatus(): Boolean
    suspend fun updatePremiumStatus(isPremium: Boolean)
    suspend fun clearUserData()
    suspend fun checkUserLoggedIn(): Boolean
    suspend fun updateLoginStatus(isLoggedIn: Boolean)
    suspend fun saveServerToken(token: String)
    suspend fun getServerToken(): String?
}