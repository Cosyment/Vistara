package com.vistara.aestheticwalls.ui.test

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vistara.aestheticwalls.ui.theme.VistaraTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 测试启动器Activity
 * 用于启动各种测试Activity
 */
@AndroidEntryPoint
class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VistaraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    TestScreen(
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
fun TestScreen(
    onLaunchApiTest: () -> Unit, viewModel: TestViewModel = hiltViewModel()
) {
    val context = LocalActivity.current
    val onBackPressed: () -> Unit = { context?.finish() }

    // 获取用户状态
    val isPremiumUser by viewModel.isPremiumUser.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val operationResult by viewModel.operationResult.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 显示操作结果
    LaunchedEffect(operationResult) {
        operationResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearOperationResult()
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("测试工具") }, navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回"
                )
            }
        })
    }, snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
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

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp), thickness = DividerDefaults.Thickness, color = DividerDefaults.color
            )

            // 用户状态测试
            Text(
                text = "用户状态测试", style = MaterialTheme.typography.titleMedium
            )

            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "高级用户状态", style = MaterialTheme.typography.bodyLarge
                        )

                        Switch(
                            checked = isPremiumUser, onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    viewModel.enablePremiumUser()
                                } else {
                                    viewModel.disablePremiumUser()
                                }
                            })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isPremiumUser) "当前状态: 高级用户" else "当前状态: 普通用户",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp), thickness = DividerDefaults.Thickness, color = DividerDefaults.color
            )

            // 登录状态测试
            Text(
                text = "登录状态测试", style = MaterialTheme.typography.titleMedium
            )

            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "登录状态", style = MaterialTheme.typography.bodyLarge
                        )

                        Switch(
                            checked = isLoggedIn, onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    viewModel.simulateLogin()
                                } else {
                                    viewModel.simulateLogout()
                                }
                            })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isLoggedIn) "当前状态: 已登录" else "当前状态: 未登录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp), thickness = DividerDefaults.Thickness, color = DividerDefaults.color
            )

            // 可以在这里添加更多测试入口
        }
    }
}
