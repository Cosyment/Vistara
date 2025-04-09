package com.vistara.aestheticwalls

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Vistara壁纸应用的Application类
 * 集成Hilt依赖注入框架
 */
@HiltAndroidApp
class VistaraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化全局组件
    }
} 