package com.vistara.aestheticwalls.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 字符串提供者，用于在非 UI 层获取字符串资源
 */
@Singleton
class StringProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * 获取字符串资源
     *
     * @param resId 字符串资源 ID
     * @return 字符串资源的值
     */
    fun getString(@StringRes resId: Int): String {
        // 获取当前语言设置
        val currentLocale = getCurrentLocale()

        // 创建一个新的 Configuration 对象
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(currentLocale)
        } else {
            config.locale = currentLocale
        }

        // 创建一个新的 Context，应用当前语言设置
        val updatedContext = context.createConfigurationContext(config)

        // 使用更新后的 Context 获取字符串资源
        return updatedContext.getString(resId)
    }

    /**
     * 获取带格式化参数的字符串资源
     *
     * @param resId 字符串资源 ID
     * @param formatArgs 格式化参数
     * @return 格式化后的字符串资源的值
     */
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        // 获取当前语言设置
        val currentLocale = getCurrentLocale()

        // 创建一个新的 Configuration 对象
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(currentLocale)
        } else {
            config.locale = currentLocale
        }

        // 创建一个新的 Context，应用当前语言设置
        val updatedContext = context.createConfigurationContext(config)

        // 使用更新后的 Context 获取字符串资源
        return updatedContext.getString(resId, *formatArgs)
    }

    /**
     * 获取当前语言设置
     */
    private fun getCurrentLocale(): Locale {
        // 获取当前语言设置
        val currentLocale = AppCompatDelegate.getApplicationLocales()
        return if (currentLocale.isEmpty) {
            // 如果没有设置语言，使用系统默认语言
            Locale.getDefault()
        } else {
            // 使用设置的语言
            currentLocale[0] ?: Locale.getDefault()
        }
    }
}
