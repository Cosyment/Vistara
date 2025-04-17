package com.vistara.aestheticwalls.ui.screens.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.AppLanguage
import com.vistara.aestheticwalls.data.repository.AuthRepository
import com.vistara.aestheticwalls.data.repository.UserPrefsRepository
import com.vistara.aestheticwalls.data.repository.UserRepository
import com.vistara.aestheticwalls.manager.LocaleManager
import com.vistara.aestheticwalls.utils.EventBus
import com.vistara.aestheticwalls.utils.StringProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import javax.inject.Inject

/**
 * 设置页面的ViewModel
 * 管理用户设置数据和状态
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPrefsRepository: UserPrefsRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val localeManager: LocaleManager,
    @ApplicationContext private val context: Context,
    private val stringProvider: StringProvider,
    private val eventBus: EventBus
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    // 深色主题
    private val _darkTheme = MutableStateFlow(false)
    val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()

    // 动态颜色
    private val _dynamicColors = MutableStateFlow(true)
    val dynamicColors: StateFlow<Boolean> = _dynamicColors.asStateFlow()

    // 应用语言
    private val _appLanguage = MutableStateFlow(AppLanguage.SYSTEM)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    // 下载通知
    private val _showDownloadNotification = MutableStateFlow(true)
    val showDownloadNotification: StateFlow<Boolean> = _showDownloadNotification.asStateFlow()

    // 壁纸更换通知
    private val _showWallpaperChangeNotification = MutableStateFlow(true)
    val showWallpaperChangeNotification: StateFlow<Boolean> = _showWallpaperChangeNotification.asStateFlow()

    // 下载原始质量
    private val _downloadOriginalQuality = MutableStateFlow(true)
    val downloadOriginalQuality: StateFlow<Boolean> = _downloadOriginalQuality.asStateFlow()

    // 缓存大小
    private val _cacheSize = MutableStateFlow("0 MB")
    val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

    // 清除缓存状态
    private val _isClearingCache = MutableStateFlow(false)
    val isClearingCache: StateFlow<Boolean> = _isClearingCache.asStateFlow()

    // 应用版本
    private val _appVersion = MutableStateFlow("1.0.0")
    val appVersion: StateFlow<String> = _appVersion.asStateFlow()

    // 通知权限状态
    private val _needNotificationPermission = MutableStateFlow(false)
    val needNotificationPermission: StateFlow<Boolean> = _needNotificationPermission.asStateFlow()

    // 登录状态
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // 退出登录状态
    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut.asStateFlow()

    // 需要登录的操作类型
    private val _needLoginAction = MutableStateFlow<LoginAction?>(null)
    val needLoginAction: StateFlow<LoginAction?> = _needLoginAction.asStateFlow()

    // 操作结果
    private val _operationResult = MutableStateFlow<String?>(null)
    val operationResult: StateFlow<String?> = _operationResult.asStateFlow()

    init {
        loadUserSettings()
        calculateCacheSize()
        loadAppVersion()
        checkLoginStatus()
    }

    /**
     * 检查登录状态
     */
    private fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                _isLoggedIn.value = userRepository.checkUserLoggedIn()
                Log.d(TAG, "Login status: ${_isLoggedIn.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error checking login status: ${e.message}")
                _isLoggedIn.value = false
            }
        }
    }

    /**
     * 退出登录
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                _isLoggingOut.value = true

                // 调用AuthRepository的signOut方法
                authRepository.signOut()

                // 更新登录状态
                _isLoggedIn.value = false
                _operationResult.value = context.getString(R.string.sign_out_success)

                Log.d(TAG, "User signed out successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error signing out: ${e.message}")
                _operationResult.value = context.getString(R.string.sign_out_failed, e.message)
            } finally {
                _isLoggingOut.value = false
            }
        }
    }

    /**
     * 清除操作结果
     */
    fun clearOperationResult() {
        _operationResult.value = null
    }

    /**
     * 加载用户设置
     */
    private fun loadUserSettings() {
        viewModelScope.launch {
            try {
                val userSettings = userPrefsRepository.getUserSettings()

                // 更新状态
                _darkTheme.value = userSettings.darkTheme
                _dynamicColors.value = userSettings.dynamicColors
                _appLanguage.value = userSettings.appLanguage
                _showDownloadNotification.value = userSettings.showDownloadNotification
                _showWallpaperChangeNotification.value = userSettings.showWallpaperChangeNotification
                _downloadOriginalQuality.value = userSettings.downloadOriginalQuality

                Log.d(TAG, "User settings loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user settings: ${e.message}")
            }
        }
    }

    /**
     * 更新深色主题设置
     */
    fun updateDarkTheme(enabled: Boolean) {
        _darkTheme.value = enabled
        saveSettings()
    }

    /**
     * 更新动态颜色设置
     */
    fun updateDynamicColors(enabled: Boolean) {
        _dynamicColors.value = enabled
        saveSettings()
    }

    /**
     * 更新应用语言设置
     */
    // 新增 StateFlow 通知 UI
    private val _needRecreate = MutableStateFlow(false)
    val needRecreate: StateFlow<Boolean> = _needRecreate.asStateFlow()

    // 语言更新状态，用于触发 UI 刷新
    private val _languageUpdated = MutableStateFlow(false)
    val languageUpdated: StateFlow<Boolean> = _languageUpdated.asStateFlow()

    fun onRecreateHandled() {
        _needRecreate.value = false
    }

    fun updateAppLanguage(language: AppLanguage) {
        Log.d(TAG, "updateAppLanguage called with language: $language")

        // 获取当前语言设置
        val currentLocale = AppCompatDelegate.getApplicationLocales()
        Log.d(TAG, "Current ApplicationLocales in ViewModel: $currentLocale, isEmpty: ${currentLocale.isEmpty}")
        Log.d(TAG, "Current default locale in ViewModel: ${Locale.getDefault()}")

        // 即使语言相同，也强制应用语言设置
        _appLanguage.value = language
        Log.d(TAG, "Saving language setting: $language")
        saveSettings()

        // 应用语言设置
        viewModelScope.launch {
            try {
                Log.d(TAG, "Calling localeManager.updateAppLanguage with: $language")
                val needRestart = localeManager.updateAppLanguage(language)
                Log.d(TAG, "Need restart: $needRestart")

                // 再次检查设置后的结果
                val afterLocale = AppCompatDelegate.getApplicationLocales()
                Log.d(TAG, "After updateAppLanguage, ApplicationLocales: $afterLocale, isEmpty: ${afterLocale.isEmpty}")
                Log.d(TAG, "After updateAppLanguage, default locale: ${Locale.getDefault()}")

                // 通知 UI 语言已更新，但不重启 Activity
                // 使用 stringProvider 而不是直接使用 context.getString 来确保使用更新后的语言资源
                _operationResult.value = stringProvider.getString(R.string.language_changed)

                // 发送语言变化事件，通知其他组件刷新
                eventBus.emitLanguageChanged()

                // 触发 UI 刷新
                _languageUpdated.value = true
                delay(100)
                _languageUpdated.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error updating language: ${e.message}")
                _operationResult.value = "Error updating language: ${e.message}"
            }
        }
    }



    /**
     * 更新下载通知设置
     */
    fun updateShowDownloadNotification(enabled: Boolean) {
        // 如果要开启通知，检查权限
        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // 需要请求权限
                _needNotificationPermission.value = true
                return
            }
        }

        _showDownloadNotification.value = enabled
        saveSettings()
    }

    /**
     * 更新壁纸更换通知设置
     */
    fun updateShowWallpaperChangeNotification(enabled: Boolean) {
        // 如果要开启通知，检查权限
        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // 需要请求权限
                _needNotificationPermission.value = true
                return
            }
        }

        _showWallpaperChangeNotification.value = enabled
        saveSettings()
    }

    /**
     * 权限授予后继续开启通知
     */
    fun onNotificationPermissionGranted(notificationType: NotificationType) {
        when (notificationType) {
            NotificationType.DOWNLOAD -> {
                _showDownloadNotification.value = true
                saveSettings()
            }

            NotificationType.WALLPAPER_CHANGE -> {
                _showWallpaperChangeNotification.value = true
                saveSettings()
            }
        }
        _needNotificationPermission.value = false
    }

    /**
     * 权限被拒绝
     */
    fun onNotificationPermissionDenied() {
        _needNotificationPermission.value = false
    }

    /**
     * 通知类型
     */
    enum class NotificationType {
        DOWNLOAD, WALLPAPER_CHANGE
    }

    /**
     * 需要登录的操作类型
     */
    enum class LoginAction {
        FAVORITES, DOWNLOADS, AUTO_WALLPAPER, SETTINGS, RATE_APP
    }

    /**
     * 更新下载原始质量设置
     */
    fun updateDownloadOriginalQuality(enabled: Boolean) {
        _downloadOriginalQuality.value = enabled
        saveSettings()
    }

    /**
     * 计算缓存大小
     */
    private fun calculateCacheSize() {
        viewModelScope.launch {
            try {
                val cacheDir = context.cacheDir
                val externalCacheDir = context.externalCacheDir

                var size = getDirSize(cacheDir)
                if (externalCacheDir != null) {
                    size += getDirSize(externalCacheDir)
                }

                _cacheSize.value = formatSize(size)
                Log.d(TAG, "Cache size calculated: ${_cacheSize.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error calculating cache size: ${e.message}")
                _cacheSize.value = "0 MB"
            }
        }
    }

    /**
     * 清除缓存
     */
    fun clearCache() {
        viewModelScope.launch {
            try {
                _isClearingCache.value = true

                // 清除应用缓存
                clearDir(context.cacheDir)
                context.externalCacheDir?.let { clearDir(it) }

                // 重新计算缓存大小
                calculateCacheSize()

                Log.d(TAG, "Cache cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing cache: ${e.message}")
            } finally {
                _isClearingCache.value = false
            }
        }
    }

    /**
     * 获取目录大小
     */
    private fun getDirSize(dir: File): Long {
        var size: Long = 0

        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) {
                getDirSize(file)
            } else {
                file.length()
            }
        }

        return size
    }

    /**
     * 清除目录
     */
    private fun clearDir(dir: File) {
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                clearDir(file)
            } else {
                file.delete()
            }
        }
    }

    /**
     * 格式化大小
     */
    private fun formatSize(size: Long): String {
        val kb = 1024.0
        val mb = kb * 1024
        val gb = mb * 1024

        return when {
            size >= gb -> String.format("%.2f GB", size / gb)
            size >= mb -> String.format("%.2f MB", size / mb)
            size >= kb -> String.format("%.2f KB", size / kb)
            else -> "$size B"
        }
    }

    /**
     * 加载应用版本
     */
    private fun loadAppVersion() {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName ?: "1.0.0"
            _appVersion.value = versionName
            Log.d(TAG, "App version loaded: ${_appVersion.value}")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading app version: ${e.message}")
            _appVersion.value = "1.0.0"
        }
    }

    /**
     * 保存所有设置
     */
    private fun saveSettings() {
        viewModelScope.launch {
            try {
                // 获取当前设置
                val currentSettings = userPrefsRepository.getUserSettings()

                // 创建更新后的设置对象
                val updatedSettings = currentSettings.copy(
                    darkTheme = _darkTheme.value,
                    dynamicColors = _dynamicColors.value,
                    appLanguage = _appLanguage.value,
                    showDownloadNotification = _showDownloadNotification.value,
                    showWallpaperChangeNotification = _showWallpaperChangeNotification.value,
                    downloadOriginalQuality = _downloadOriginalQuality.value
                )

                // 保存设置
                userPrefsRepository.saveUserSettings(updatedSettings)
                Log.d(TAG, "Settings saved successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving settings: ${e.message}")
            }
        }
    }
}
