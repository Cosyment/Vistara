package com.vistara.aestheticwalls.ui.test

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vistara.aestheticwalls.ui.theme.VistaraTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 测试启动器Activity
 * 用于启动各种测试Activity
 */
@AndroidEntryPoint
class TestLauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VistaraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    TestLauncherScreen(
                        onLaunchApiTest = {
                            startActivity(Intent(this, ApiTestActivity::class.java))
                        })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestLauncherScreen(
    onLaunchApiTest: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("测试工具") })
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "API测试", style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = onLaunchApiTest, modifier = Modifier.fillMaxWidth()
            ) {
                Text("启动Pexels API测试")
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 可以在这里添加更多测试入口
        }
    }
}
