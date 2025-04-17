package com.vistara.aestheticwalls

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.vistara.aestheticwalls.manager.LocaleManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Vistara壁纸应用的Application类
 * 集成Hilt依赖注入框架
 */
@HiltAndroidApp
class VistaraApp : Application() {

    @Inject
    lateinit var localeManager: LocaleManager

    override fun onCreate() {
        super.onCreate()

        // 初始化全局组件

        // 启用应用内语言切换
        AppCompatDelegate.setApplicationLocales(AppCompatDelegate.getApplicationLocales())

        // 应用保存的语言设置
        applyLanguageSettings()
    }

    /**
     * 应用保存的语言设置
     * 使用协程异步加载语言设置并应用
     */
    private fun applyLanguageSettings() {
        // 使用协程异步加载语言设置
        MainScope().launch {
            try {
                // 先获取当前设置的语言
                val settings = localeManager.appLanguageFlow.first()
                Log.d("VistaraApp", "Applying language settings: $settings")

                // 应用语言设置
                localeManager.applyCurrentLanguage()
            } catch (e: Exception) {
                Log.e("VistaraApp", "Failed to apply language settings: ${e.message}")
            }
        }
    }
}