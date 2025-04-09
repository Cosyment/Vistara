package com.vistara.aestheticwalls

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.vistara.aestheticwalls.ui.screens.home.HomeScreen
import com.vistara.aestheticwalls.ui.theme.VistaraTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 应用程序的主活动
 * 作为应用的入口点
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VistaraTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // 使用HomeScreen代替临时首页
                    HomeScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainActivityPreview() {
    VistaraTheme {
        HomeScreen()
    }
} 