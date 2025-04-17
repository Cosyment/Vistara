package com.vistara.aestheticwalls.ui.theme

import android.content.Context
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.vistara.aestheticwalls.data.model.AppLanguage
import java.util.Locale

/**
 * 用于在 Compose 中提供本地化资源的 CompositionLocal
 */
val LocalAppResources = compositionLocalOf<Resources> { error("No Resources provided") }

/**
 * 创建带有指定语言的 Resources 对象
 */
fun createLocalizedResources(context: Context, language: AppLanguage): Resources {
    val config = android.content.res.Configuration(context.resources.configuration)
    
    if (language == AppLanguage.SYSTEM) {
        // 使用系统默认语言
        config.setLocales(android.os.LocaleList.getDefault())
    } else {
        // 使用指定语言
        val locale = Locale(language.code)
        val localeList = android.os.LocaleList(locale)
        config.setLocales(localeList)
    }
    
    // 创建新的 Resources 对象
    val localizedContext = context.createConfigurationContext(config)
    return localizedContext.resources
}

/**
 * 提供本地化资源的 Composable 函数
 */
@Composable
fun LocaleProvider(
    language: AppLanguage,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    
    // 创建本地化的 Resources
    val resources = remember(language, configuration) {
        createLocalizedResources(context, language)
    }
    
    // 提供本地化的 Resources
    CompositionLocalProvider(LocalAppResources provides resources) {
        content()
    }
}
