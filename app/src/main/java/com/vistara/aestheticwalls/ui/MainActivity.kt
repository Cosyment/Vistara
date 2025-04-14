package com.vistara.aestheticwalls.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.vistara.aestheticwalls.ui.navigation.MainNavigation
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
                MainNavigation()
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