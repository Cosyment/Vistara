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
     * 这个方法会尝试多种方式来确保应用使用系统语言
     */
    fun forceSystemLanguage() {
        Log.d(TAG, "强制重置应用语言到系统语言")

        // 获取真正的系统语言
        var systemLocale = getSystemLocale()
        Log.d(TAG, "强制设置系统语言(原始): $systemLocale")

        // 处理英语区域设置，将 en_US 转换为 en
        if (systemLocale.language == "en") {
            // 创建一个新的只有语言代码的 Locale
            systemLocale = Locale("en")
            Log.d(TAG, "处理英语区域设置，转换为: $systemLocale")
        }

        // 检查语言是否合理
        if (systemLocale.language != "en" && systemLocale.language != Locale.getDefault().language) {
            Log.d(TAG, "检测到的系统语言不合理，尝试使用英语")
            // 如果系统语言不合理，则使用英语
            val englishLocale = Locale("en")

            // 先设置默认语言
            Locale.setDefault(englishLocale)
            Log.d(TAG, "设置默认语言为英语: ${Locale.getDefault()}")

            // 然后设置应用语言
            val englishLocaleList = LocaleListCompat.create(englishLocale)
            AppCompatDelegate.setApplicationLocales(englishLocaleList)
            Log.d(TAG, "设置应用语言为英语: $englishLocaleList")

            // 尝试多种方式强制更新资源配置
            try {
                // 方式1: 设置空的 LocaleList 然后再设置英语
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                Log.d(TAG, "设置空的 LocaleList")

                // 等待一下再设置英语
                val englishLocaleList = LocaleListCompat.create(englishLocale)
                AppCompatDelegate.setApplicationLocales(englishLocaleList)
                Log.d(TAG, "再次设置英语: $englishLocaleList")

                // 方式2: 尝试更新系统资源配置
                val config = Resources.getSystem().configuration
                val localeList = android.os.LocaleList(englishLocale)
                config.setLocales(localeList)
                Log.d(TAG, "强制更新资源配置为英语")
            } catch (e: Exception) {
                Log.e(TAG, "强制更新资源配置时出错: ${e.message}")
            }

            // 检查设置后的结果
            val afterLocales = AppCompatDelegate.getApplicationLocales()
            Log.d(TAG, "设置后的应用语言(英语): $afterLocales, isEmpty: ${afterLocales.isEmpty}")
        } else {
            // 先设置默认语言
            Locale.setDefault(systemLocale)
            Log.d(TAG, "设置默认语言为系统语言: ${Locale.getDefault()}")

            // 然后设置应用语言
            val systemLocaleList = LocaleListCompat.create(systemLocale)
            AppCompatDelegate.setApplicationLocales(systemLocaleList)
            Log.d(TAG, "设置应用语言为系统语言: $systemLocaleList")

            // 尝试多种方式强制更新资源配置
            try {
                // 方式1: 设置空的 LocaleList 然后再设置系统语言
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                Log.d(TAG, "设置空的 LocaleList")

                // 等待一下再设置系统语言
                val systemLocaleList = LocaleListCompat.create(systemLocale)
                AppCompatDelegate.setApplicationLocales(systemLocaleList)
                Log.d(TAG, "再次设置系统语言: $systemLocaleList")

                // 方式2: 尝试更新系统资源配置
                val config = Resources.getSystem().configuration
                val localeList = android.os.LocaleList(systemLocale)
                config.setLocales(localeList)
                Log.d(TAG, "强制更新资源配置为系统语言")
            } catch (e: Exception) {
                Log.e(TAG, "强制更新资源配置时出错: ${e.message}")
            }

            // 检查设置后的结果
            val afterLocales = AppCompatDelegate.getApplicationLocales()
            Log.d(TAG, "设置后的应用语言: $afterLocales, isEmpty: ${afterLocales.isEmpty}")
        }
    }

    /**
     * 获取真正的设备系统语言区域设置
     * @return 系统语言区域
     */
    fun getSystemLocale(): Locale {
        // 尝试多种方式获取真正的系统语言
        Log.d(TAG, "开始获取真正的系统语言...")

        // 记录所有可能的系统语言来源
        val javaLocale = Locale.getDefault()
        Log.d(TAG, "从 Java Locale.getDefault() 获取的语言: $javaLocale")


        val configuration = Resources.getSystem().configuration
        val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION") configuration.locale
        }

        Log.e(TAG, " $systemLocale  ${Locale.getDefault()}")
        Log.d(TAG, "从 Resources.getSystem() 获取的系统语言: $systemLocale")

        // 如果所有方式都失败，则使用 Java 的 Locale.getDefault()
        if (systemLocale.language != "en" && systemLocale.language != javaLocale.language) {
            Log.d(TAG, "所有方式都失败，使用 Java 的 Locale.getDefault(): $javaLocale")
            return javaLocale
        }

        return javaLocale
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
