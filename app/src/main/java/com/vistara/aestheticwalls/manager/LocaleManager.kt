package com.vistara.aestheticwalls.manager

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.vistara.aestheticwalls.data.model.AppLanguage
import com.vistara.aestheticwalls.data.repository.UserPrefsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 语言管理器
 * 负责管理应用的语言设置
 */
@Singleton
class LocaleManager @Inject constructor(
    private val userPrefsRepository: UserPrefsRepository
) {
    companion object {
        private const val TAG = "VistaraLocaleManager"
    }
    /**
     * 获取当前语言设置流
     */
    val appLanguageFlow: Flow<AppLanguage> = userPrefsRepository.getUserSettingsFlow()
        .map { it.appLanguage }

    /**
     * 更新应用语言设置
     * @param language 要设置的语言
     * @return 是否需要重启应用
     */
    suspend fun updateAppLanguage(language: AppLanguage): Boolean {
        val currentSettings = userPrefsRepository.getUserSettings()
        val updatedSettings = currentSettings.copy(appLanguage = language)
        userPrefsRepository.saveUserSettings(updatedSettings)
        return applyLanguage(language)
    }

    /**
     * 应用语言设置
     * @param language 要应用的语言
     * @return 是否需要重启应用
     */
    fun applyLanguage(language: AppLanguage): Boolean {
        Log.d(TAG, "applyLanguage called with language: $language")

        // 获取当前语言设置
        val currentLocale = AppCompatDelegate.getApplicationLocales()
        Log.d(TAG, "Current ApplicationLocales: $currentLocale, isEmpty: ${currentLocale.isEmpty}")

        val currentLanguage = if (currentLocale.isEmpty) {
            AppLanguage.SYSTEM
        } else {
            val localeCode = currentLocale[0]?.language ?: ""
            Log.d(TAG, "Current locale code from ApplicationLocales: $localeCode")
            AppLanguage.values().find { it.code == localeCode } ?: AppLanguage.SYSTEM
        }

        Log.d(TAG, "Current language detected: $currentLanguage")

        // 强制应用语言设置，即使语言没有变化
        // if (language == currentLanguage) {
        //     Log.d(TAG, "Language unchanged, no need to restart")
        //     return false
        // }

        try {
            if (language == AppLanguage.SYSTEM) {
                // 使用系统默认语言
                Log.d(TAG, "Setting to SYSTEM language")
                // 先设置默认语言
                val systemLocale = Locale.getDefault()
                Locale.setDefault(systemLocale)
                Log.d(TAG, "Set default locale to system locale: $systemLocale")

                // 然后设置应用语言
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                Log.d(TAG, "Set application locales to empty list")

                // 使用强制方式设置语言
                try {
                    val config = Resources.getSystem().configuration
                    config.setLocales(android.os.LocaleList.getDefault())
                    Log.d(TAG, "Forced system locale: ${config.locales[0]}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting system locale: ${e.message}")
                }
            } else {
                // 设置指定语言
                val locale = Locale(language.code)
                Log.d(TAG, "Setting to specific language: $language, locale: $locale")

                // 先设置默认语言
                Locale.setDefault(locale)
                Log.d(TAG, "Set default locale to: $locale")

                // 然后设置应用语言
                val localeList = LocaleListCompat.create(locale)
                AppCompatDelegate.setApplicationLocales(localeList)
                Log.d(TAG, "Set application locales to: $localeList")

                // 使用强制方式设置语言
                try {
                    val config = Resources.getSystem().configuration
                    val localeList = android.os.LocaleList(locale)
                    config.setLocales(localeList)
                    Log.d(TAG, "Forced locale: ${config.locales[0]}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting locale: ${e.message}")
                }
            }

            // 检查设置后的结果
            val afterLocale = AppCompatDelegate.getApplicationLocales()
            Log.d(TAG, "After setting, ApplicationLocales: $afterLocale, isEmpty: ${afterLocale.isEmpty}")
            Log.d(TAG, "After setting, default locale: ${Locale.getDefault()}")

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error applying language: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    /**
     * 应用保存的语言设置
     * 在应用启动时调用
     */
    suspend fun applyCurrentLanguage() {
        Log.d(TAG, "applyCurrentLanguage called")

        val settings = userPrefsRepository.getUserSettings()
        val language = settings.appLanguage
        Log.d(TAG, "Saved language setting: $language")

        // 获取当前语言设置
        val currentLocale = AppCompatDelegate.getApplicationLocales()
        Log.d(TAG, "Current ApplicationLocales before applying: $currentLocale, isEmpty: ${currentLocale.isEmpty}")

        try {
            // 强制应用语言设置，即使当前语言与设置的语言相同
            if (language == AppLanguage.SYSTEM) {
                // 使用系统默认语言
                Log.d(TAG, "Applying SYSTEM language in applyCurrentLanguage")
                // 先设置默认语言
                val systemLocale = Locale.getDefault()
                Locale.setDefault(systemLocale)
                Log.d(TAG, "Set default locale to system locale: $systemLocale")

                // 然后设置应用语言
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                Log.d(TAG, "Set application locales to empty list")

                // 使用强制方式设置语言
                try {
                    val config = Resources.getSystem().configuration
                    config.setLocales(android.os.LocaleList.getDefault())
                    Log.d(TAG, "Forced system locale: ${config.locales[0]}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting system locale: ${e.message}")
                }
            } else {
                // 设置指定语言
                val locale = Locale(language.code)
                Log.d(TAG, "Applying specific language in applyCurrentLanguage: $language, locale: $locale")

                // 先设置默认语言
                Locale.setDefault(locale)
                Log.d(TAG, "Set default locale to: $locale")

                // 然后设置应用语言
                val localeList = LocaleListCompat.create(locale)
                AppCompatDelegate.setApplicationLocales(localeList)
                Log.d(TAG, "Set application locales to: $localeList")

                // 使用强制方式设置语言
                try {
                    val config = Resources.getSystem().configuration
                    val localeList = android.os.LocaleList(locale)
                    config.setLocales(localeList)
                    Log.d(TAG, "Forced locale: ${config.locales[0]}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting locale: ${e.message}")
                }
            }

            // 检查设置后的结果
            val afterLocale = AppCompatDelegate.getApplicationLocales()
            Log.d(TAG, "After applying in applyCurrentLanguage, ApplicationLocales: $afterLocale, isEmpty: ${afterLocale.isEmpty}")
            Log.d(TAG, "After applying in applyCurrentLanguage, default locale: ${Locale.getDefault()}")

            // 打印当前资源语言
            val resources = Resources.getSystem()
            val config = resources.configuration
            Log.d(TAG, "Current system resources locale: ${config.locales[0]}")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying current language: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 获取当前系统语言
     * @return 系统语言代码
     */
    fun getSystemLanguage(context: Context): String {
        val config = context.resources.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.locales[0].language
        } else {
            @Suppress("DEPRECATION")
            config.locale.language
        }
    }

    /**
     * 创建配置了语言的Context
     * 用于在特定组件中应用语言设置
     * @param baseContext 基础Context
     * @param language 要应用的语言
     * @return 配置了语言的新Context
     */
    fun createConfigurationContext(baseContext: Context, language: AppLanguage): Context {
        if (language == AppLanguage.SYSTEM) {
            return baseContext
        }

        val locale = Locale.Builder().setLanguage(language.code).build()
        Locale.setDefault(locale)

        val config = Configuration(baseContext.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        return baseContext.createConfigurationContext(config)
    }
}
