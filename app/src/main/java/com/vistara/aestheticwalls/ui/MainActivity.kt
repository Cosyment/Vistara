package com.vistara.aestheticwalls.ui

import android.content.Intent
import android.os.Bundle
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vistara.aestheticwalls.manager.ThemeManager
import com.vistara.aestheticwalls.ui.navigation.MainNavigation
import com.vistara.aestheticwalls.ui.theme.VistaraTheme
import dagger.hilt.android.AndroidEntryPoint
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 处理导航意图
        handleNavigationIntent(intent)

        setContent {
            // 使用用户设置的主题
            val darkTheme by themeManager.darkTheme()
            val dynamicColors by themeManager.dynamicColors()

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNavigationIntent(intent)
    }

    /**
     * 处理导航意图
     */
    private fun handleNavigationIntent(intent: Intent) {
        // 从意图中获取导航路径
        val navigation = intent.getStringExtra("navigation")
        if (!navigation.isNullOrEmpty()) {
            initialNavigation = navigation
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