package com.vistara.aestheticwalls.ui.screens.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.utils.Constants.PRIVACY_POLICY_URL
import com.vistara.aestheticwalls.utils.Constants.TERMS_OF_SERVICE_URL
import com.vistara.aestheticwalls.utils.Constants.USER_AGREEMENT_URL
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URLEncoder
import javax.inject.Inject

/**
 * 关于页面的ViewModel
 */
@HiltViewModel
class AboutViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "AboutViewModel"

    }

    // 应用版本
    private val _appVersion = MutableStateFlow(getAppVersion())
    val appVersion: StateFlow<String> = _appVersion.asStateFlow()

    // 开源库列表
    private val _openSourceLibraries = MutableStateFlow(getOpenSourceLibraries())
    val openSourceLibraries: StateFlow<List<Library>> = _openSourceLibraries.asStateFlow()

    /**
     * 获取应用版本
     */
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "Ver ${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app version: ${e.message}")
            "Ver 1.0.0"
        }
    }

    /**
     * 获取开源库列表
     */
    private fun getOpenSourceLibraries(): List<Library> {
        return listOf(
            Library(
                name = "Jetpack Compose",
                description = "Modern UI toolkit for Android",
                url = "https://developer.android.com/jetpack/compose"
            ),
            Library(
                name = "Kotlin Coroutines",
                description = "Asynchronous programming library for Kotlin",
                url = "https://github.com/Kotlin/kotlinx.coroutines"
            ),
            Library(
                name = "Hilt",
                description = "Dependency injection library for Android",
                url = "https://dagger.dev/hilt/"
            ),
            Library(
                name = "Coil",
                description = "Image loading library for Kotlin",
                url = "https://github.com/coil-kt/coil"
            ),
            Library(
                name = "Retrofit",
                description = "Type-safe HTTP client for Android and Java",
                url = "https://square.github.io/retrofit/"
            ),
            Library(
                name = "OkHttp",
                description = "HTTP client for Android and Java",
                url = "https://square.github.io/okhttp/"
            ),
            Library(
                name = "Room",
                description = "SQLite object mapping library for Android",
                url = "https://developer.android.com/training/data-storage/room"
            ),
            Library(
                name = "ExoPlayer",
                description = "Media player for Android",
                url = "https://github.com/google/ExoPlayer"
            )
        )
    }

    // 导航控制器
    private var navController: NavController? = null

    /**
     * 设置导航控制器
     */
    fun setNavController(controller: NavController) {
        navController = controller
    }

    /**
     * 打开隐私政策
     */
    fun openPrivacyPolicy() {
        openInWebView(PRIVACY_POLICY_URL, context.getString(R.string.privacy_policy))
    }

    /**
     * 打开服务条款
     */
    fun openTermsOfService() {
        openInWebView(TERMS_OF_SERVICE_URL, context.getString(R.string.terms_of_service))
    }

    /**
     * 打开用户协议
     */
    fun openUserAgreement() {
        openInWebView(USER_AGREEMENT_URL, context.getString(R.string.user_agreement))
    }

    /**
     * 在WebView中打开URL
     */
    private fun openInWebView(url: String, title: String) {
        try {
            navController?.let { nav ->
                // 使用 URLEncoder 对 URL 和标题进行编码
                val encodedUrl = URLEncoder.encode(url, "UTF-8")
                val encodedTitle = URLEncoder.encode(title, "UTF-8")
                val route = "webview?url=$encodedUrl"
                Log.d(TAG, "Navigating to WebView with route: $route")
                nav.navigate(route)
            } ?: run {
                Log.d(TAG, "NavController is null, opening URL in external browser: $url")
                openExternalUrl(url) // 如果没有导航控制器，则使用外部浏览器
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to WebView: $url, ${e.message}")
            openExternalUrl(url) // 如果导航失败，则使用外部浏览器
        }
    }

    /**
     * 在外部浏览器中打开URL
     */
    private fun openExternalUrl(url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening URL in external browser: $url, ${e.message}")
            false
        }
    }

    /**
     * 打开开源库URL
     */
    fun openLibraryUrl(url: String) {
        openInWebView(url, context.getString(R.string.open_source_libraries))
    }
}

/**
 * 开源库数据类
 */
data class Library(
    val name: String,
    val description: String,
    val url: String
)
