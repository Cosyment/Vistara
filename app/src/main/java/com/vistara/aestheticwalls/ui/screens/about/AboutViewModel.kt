package com.vistara.aestheticwalls.ui.screens.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        private const val PRIVACY_POLICY_URL = "https://www.vistara.com/privacy-policy"
        private const val TERMS_OF_SERVICE_URL = "https://www.vistara.com/terms-of-service"
        private const val GITHUB_REPO_URL = "https://github.com/Cosyment/Vistara"
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
            "版本 ${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app version: ${e.message}")
            "版本 1.0.0"
        }
    }

    /**
     * 获取开源库列表
     */
    private fun getOpenSourceLibraries(): List<Library> {
        return listOf(
            Library(
                name = "Jetpack Compose",
                description = "Android的现代UI工具包",
                url = "https://developer.android.com/jetpack/compose"
            ),
            Library(
                name = "Kotlin Coroutines",
                description = "Kotlin的异步编程库",
                url = "https://github.com/Kotlin/kotlinx.coroutines"
            ),
            Library(
                name = "Hilt",
                description = "Android的依赖注入库",
                url = "https://dagger.dev/hilt/"
            ),
            Library(
                name = "Coil",
                description = "Kotlin的图片加载库",
                url = "https://github.com/coil-kt/coil"
            ),
            Library(
                name = "Retrofit",
                description = "类型安全的HTTP客户端",
                url = "https://square.github.io/retrofit/"
            ),
            Library(
                name = "OkHttp",
                description = "HTTP客户端",
                url = "https://square.github.io/okhttp/"
            ),
            Library(
                name = "Room",
                description = "SQLite对象映射库",
                url = "https://developer.android.com/training/data-storage/room"
            ),
            Library(
                name = "ExoPlayer",
                description = "媒体播放器",
                url = "https://github.com/google/ExoPlayer"
            )
        )
    }

    /**
     * 打开隐私政策
     */
    fun openPrivacyPolicy(): Boolean {
        return openUrl(PRIVACY_POLICY_URL)
    }

    /**
     * 打开服务条款
     */
    fun openTermsOfService(): Boolean {
        return openUrl(TERMS_OF_SERVICE_URL)
    }

    /**
     * 打开GitHub仓库
     */
    fun openGitHubRepo(): Boolean {
        return openUrl(GITHUB_REPO_URL)
    }

    /**
     * 打开URL
     */
    private fun openUrl(url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening URL: $url, ${e.message}")
            false
        }
    }

    /**
     * 打开开源库URL
     */
    fun openLibraryUrl(url: String): Boolean {
        return openUrl(url)
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
