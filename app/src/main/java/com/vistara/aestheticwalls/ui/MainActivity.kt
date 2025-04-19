package com.vistara.aestheticwalls.ui

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.vistara.aestheticwalls.data.model.AppLanguage
import com.vistara.aestheticwalls.manager.LocaleManager
import com.vistara.aestheticwalls.manager.ThemeManager
import com.vistara.aestheticwalls.receiver.UnlockWallpaperReceiver
import com.vistara.aestheticwalls.ui.navigation.MainNavigation
import com.vistara.aestheticwalls.ui.theme.VistaraTheme
import com.vistara.aestheticwalls.utils.ActivityProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 应用程序的主活动
 * 作为应用的入口点
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // 保存当前导航路径
    private var initialNavigation: String? = null

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var localeManager: LocaleManager

    // 解锁屏幕广播接收器
    private lateinit var unlockReceiver: UnlockWallpaperReceiver
    private var isReceiverRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 注册Activity实例到ActivityProvider
        ActivityProvider.setMainActivity(this)

        // 应用语言设置 - 在 attachBaseContext 中已经处理
        // 使用 LocaleProvider 和 staticCompositionLocalOf 确保语言变化时整个应用都能更新

        // 处理导航意图
        handleNavigationIntent(intent)

        // 注册解锁屏幕广播接收器
        registerUnlockReceiver()

        recreateContent()
    }

    private fun recreateContent() {
        setContent {
            // 使用用户设置的主题
            val darkTheme by themeManager.darkTheme()
            val dynamicColors by themeManager.dynamicColors()

            // 监听语言设置变化
            // 由于 appLanguageFlow 是 Flow，当值变化时会自动触发 UI 重组
            val language by localeManager.appLanguageFlow.collectAsState(initial = AppLanguage.SYSTEM)

            // 使用 LocaleProvider 提供本地化资源
            com.vistara.aestheticwalls.ui.theme.LocaleProvider(language = language) {
                VistaraTheme(darkTheme = darkTheme, dynamicColor = dynamicColors) {
                    val navController = rememberNavController()
                    var startDestination by rememberSaveable { mutableStateOf(initialNavigation) }

                    // 如果有初始导航路径，导航到该路径
                    if (startDestination != null) {
                        LaunchedEffect(startDestination) {
                            navController.navigate(startDestination!!) {
                                launchSingleTop = true
                            }
                            // 重置初始导航，避免重复导航
                            startDestination = null
                            initialNavigation = null
                        }
                    }

                    MainNavigation(navController = navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNavigationIntent(intent)
    }

    override fun onDestroy() {
        // 取消注册广播接收器
        unregisterUnlockReceiver()

        // 清除ActivityProvider中的引用
        ActivityProvider.clearMainActivity()

        super.onDestroy()
    }

    override fun attachBaseContext(newBase: android.content.Context) {
        // 在创建 Activity 之前应用语言设置
        val localeManager =
            (newBase.applicationContext as com.vistara.aestheticwalls.VistaraApp).localeManager
        val settings = kotlinx.coroutines.runBlocking { localeManager.appLanguageFlow.first() }

        // 创建带有语言设置的新 Context
        val config = android.content.res.Configuration(newBase.resources.configuration)
        if (settings != AppLanguage.SYSTEM) {
            val locale = java.util.Locale(settings.code)
            java.util.Locale.setDefault(locale)
            val localeList = android.os.LocaleList(locale)
            config.setLocales(localeList)
        } else {
            config.setLocales(android.os.LocaleList.getDefault())
        }

        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)

        Log.d("MainActivity", "attachBaseContext with language: $settings")
    }

    /**
     * 处理导航意图
     */
    private fun handleNavigationIntent(intent: Intent) {
        // 从意图中获取导航路径
        val navigation = intent.getStringExtra("navigation")
        if (!navigation.isNullOrEmpty()) {
            // 如果是设置页面，则导航到设置页面
            initialNavigation = if (navigation == "settings") {
                "settings"
            } else {
                navigation
            }
        }
    }

    /**
     * 注册解锁屏幕广播接收器
     */
    private fun registerUnlockReceiver() {
        try {
            // 初始化广播接收器
            unlockReceiver = UnlockWallpaperReceiver()

            // 注册广播接收器
            val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
            registerReceiver(unlockReceiver, filter)
            isReceiverRegistered = true

            Log.d("MainActivity", "解锁屏幕广播接收器已动态注册")
        } catch (e: Exception) {
            Log.e("MainActivity", "注册解锁屏幕广播接收器失败: ${e.message}")
        }
    }

    /**
     * 取消注册解锁屏幕广播接收器
     */
    private fun unregisterUnlockReceiver() {
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(unlockReceiver)
                isReceiverRegistered = false
                Log.d("MainActivity", "解锁屏幕广播接收器已取消注册")
            } catch (e: Exception) {
                Log.e("MainActivity", "取消注册解锁屏幕广播接收器失败: ${e.message}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainActivityPreview() {
    VistaraTheme {
        MainNavigation()
    }
}