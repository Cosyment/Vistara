package com.vistara.aestheticwalls.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.vistara.aestheticwalls.data.remote.api.ApiService
import com.vistara.aestheticwalls.data.remote.api.LoginRequest
import com.vistara.aestheticwalls.data.remote.api.LoginResponse
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import retrofit2.Response

@Suppress("DEPRECATION", "UNCHECKED_CAST")
class AuthRepositoryTest {

    private lateinit var context: Context
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var apiService: ApiService
    private lateinit var userRepository: UserRepository
    private lateinit var authRepository: AuthRepositoryImpl

    @Before
    fun setup() {
        context = mock()
        dataStore = mock()
        apiService = mock()
        userRepository = mock()

        // Mock DataStore
        val preferences: Preferences = mock()
        whenever(dataStore.data).thenReturn(flowOf(preferences))

        authRepository = AuthRepositoryImpl(context, dataStore, userRepository, apiService)
    }

    @Test
    fun `handleSignInResult success`() = runTest {
        // 准备测试数据
        val account: GoogleSignInAccount = mock {
            on { id } doReturn "test_id"
            on { displayName } doReturn "Test User"
            on { email } doReturn "test@example.com"
            on { photoUrl } doReturn null
            on { idToken } doReturn "test_token"
        }

        val task: Task<GoogleSignInAccount> = mock {
            on { getResult(Exception::class.java) } doReturn account
        }

        val loginResponse = LoginResponse("server_token", true, "Success")
        whenever(apiService.login(any())).thenReturn(Response.success(loginResponse))

        // 执行测试
        val result = authRepository.handleSignInResult(task)

        // 验证结果
        assert(result)
        verify(userRepository).updateLoginStatus(true)
        verify(userRepository).updatePremiumStatus(true)
        
        // 验证请求参数
        verify(apiService).login(eq(LoginRequest(
            nickname = "Test User",
            email = "test@example.com",
            avatar = "",
            token = "test_token"
        )))
    }

    @Test
    fun `handleSignInResult failure`() = runTest {
        // 准备测试数据
        val account: GoogleSignInAccount = mock {
            on { id } doReturn "test_id"
            on { displayName } doReturn "Test User"
            on { email } doReturn "test@example.com"
            on { photoUrl } doReturn null
            on { idToken } doReturn "test_token"
        }

        val task: Task<GoogleSignInAccount> = mock {
            on { getResult(Exception::class.java) } doReturn account
        }

        val responseBody: ResponseBody = mock()
        whenever(apiService.login(any())).thenReturn(Response.error(400, responseBody))

        // 执行测试
        val result = authRepository.handleSignInResult(task)

        // 验证结果
        assert(!result)
        
        // 验证请求参数
        verify(apiService).login(eq(LoginRequest(
            nickname = "Test User",
            email = "test@example.com",
            avatar = "",
            token = "test_token"
        )))
    }
} 