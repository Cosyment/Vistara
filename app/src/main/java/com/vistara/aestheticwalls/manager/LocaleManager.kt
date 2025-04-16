package com.vistara.aestheticwalls.manager

import android.content.Context
import android.content.res.Configuration
import android.os.Build
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
    /**
     * 获取当前语言设置流
     */
    val appLanguageFlow: Flow<AppLanguage> = userPrefsRepository.getUserSettingsFlow()
        .map { it.appLanguage }

    /**
     * 更新应用语言设置
     * @param language 要设置的语言
     */
    suspend fun updateAppLanguage(language: AppLanguage) {
        val currentSettings = userPrefsRepository.getUserSettings()
        val updatedSettings = currentSettings.copy(appLanguage = language)
        userPrefsRepository.saveUserSettings(updatedSettings)
        applyLanguage(language)
    }

    /**
     * 应用语言设置
     * @param language 要应用的语言
     */
    fun applyLanguage(language: AppLanguage) {
        if (language == AppLanguage.SYSTEM) {
            // 使用系统默认语言
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            // 设置指定语言
            val localeList = LocaleListCompat.create(Locale(language.code))
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }

    /**
     * 应用保存的语言设置
     * 在应用启动时调用
     */
    suspend fun applyCurrentLanguage() {
        val settings = userPrefsRepository.getUserSettings()
        applyLanguage(settings.appLanguage)
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

        val locale = Locale(language.code)
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
