package com.vistara.aestheticwalls.manager

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.vistara.aestheticwalls.data.model.AppLanguage
import com.vistara.aestheticwalls.data.repository.UserPrefsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
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
    val appLanguageFlow: Flow<AppLanguage> =
        userPrefsRepository.getUserSettingsFlow().map { it.appLanguage }

    /**
     * 更新应用语言设置
     * @param language 要设置的语言
     */
    suspend fun updateAppLanguage(language: AppLanguage) {
        // 只更新 UserPrefsRepository 中的值，不做其他操作
        // 由于 appLanguageFlow 是 Flow，当值变化时会自动触发 UI 重组
        val currentSettings = userPrefsRepository.getUserSettings()
        val updatedSettings = currentSettings.copy(appLanguage = language)
        userPrefsRepository.saveUserSettings(updatedSettings)

        // 应用语言设置
        applyLanguage(language)
    }

    /**
     * 应用语言设置
     * @param language 要应用的语言
     * @return 是否需要重启应用
     */
    fun applyLanguage(language: AppLanguage): Boolean {

        // 获取当前语言设置
        val currentLocale = AppCompatDelegate.getApplicationLocales()

        // 获取当前设置的语言
        val settings = runBlocking { userPrefsRepository.getUserSettings() }
        val savedLanguage = settings.appLanguage

        // 使用保存的语言设置作为当前语言
        val currentLanguage = savedLanguage

        Log.d(TAG, "当前语言设置: $currentLanguage")
        Log.d(TAG, "当前应用语言: $currentLocale")

        // 强制应用语言设置，即使语言没有变化
        // if (language == currentLanguage) {
        //     Log.d(TAG, "Language unchanged, no need to restart")
        //     return false
        // }

        try {
            if (language == AppLanguage.SYSTEM) {
                // 使用系统默认语言
                Log.d(TAG, "Setting to SYSTEM language")

                // 使用新的强制系统语言方法
                forceSystemLanguage()
            } else {
                // 设置指定语言
                val locale = Locale(language.code)

                // 先设置默认语言
                Locale.setDefault(locale)

                // 然后设置应用语言
                val localeList = LocaleListCompat.create(locale)
                AppCompatDelegate.setApplicationLocales(localeList)

                // 使用强制方式设置语言
                try {
                    val config = Resources.getSystem().configuration
                    val localeList = android.os.LocaleList(locale)
                    config.setLocales(localeList)
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting locale: ${e.message}")
                }
            }

            // 检查设置后的结果
            val afterLocale = AppCompatDelegate.getApplicationLocales()

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
        val settings = userPrefsRepository.getUserSettings()
        val language = settings.appLanguage

        // 获取当前语言设置
        val currentLocale = AppCompatDelegate.getApplicationLocales()

        try {
            // 强制应用语言设置，即使当前语言与设置的语言相同
            if (language == AppLanguage.SYSTEM) {
                // 使用系统默认语言
                Log.d(TAG, "Applying SYSTEM language at startup")

                // 使用新的强制系统语言方法
//                forceSystemLanguage()
            } else {
                // 设置指定语言
                val locale = Locale(language.code)

                // 然后设置应用语言
                val localeList = LocaleListCompat.create(locale)
                AppCompatDelegate.setApplicationLocales(localeList)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error applying current language: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 获取真正的设备系统语言
     * @return 系统语言代码
     */
    fun getSystemLanguage(context: Context): String {
        return getSystemLocale().language
    }

    /**
     * 强制重置应用语言到系统语言
     * 使用简化的逻辑确保应用使用系统语言
     */
    fun forceSystemLanguage() {
        Log.d(TAG, "强制重置应用语言到系统语言")

        try {
            // 获取真正的系统语言
            val systemLocale = getSystemLocale()
            Log.d(TAG, "获取到系统语言: $systemLocale")

            // 设置空的语言列表，这会使应用使用系统语言
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            Log.d(TAG, "设置空的语言列表，使应用使用系统语言")

            // 检查设置后的结果
            val afterLocales = AppCompatDelegate.getApplicationLocales()
            Log.d(TAG, "设置后的应用语言: $afterLocales, isEmpty: ${afterLocales.isEmpty}")
        } catch (e: Exception) {
            Log.e(TAG, "强制设置系统语言时出错: ${e.message}")
        }
    }

    /**
     * 获取真正的设备系统语言区域设置
     * @return 系统语言区域
     */
    fun getSystemLocale(): Locale {
        // 使用 Resources.getSystem() 获取系统资源，不受应用语言设置影响
        val configuration = Resources.getSystem().configuration
        val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION") configuration.locale
        }

        Log.d(TAG, "从 Resources.getSystem() 获取的系统语言: $systemLocale")
        return systemLocale
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
            @Suppress("DEPRECATION") config.locale = locale
        }

        return baseContext.createConfigurationContext(config)
    }
}
