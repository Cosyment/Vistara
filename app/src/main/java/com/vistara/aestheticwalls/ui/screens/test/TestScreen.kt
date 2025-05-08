package com.vistara.aestheticwalls.ui.screens.test

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vistara.aestheticwalls.ui.theme.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.ui.components.PaymentMethod
import com.vistara.aestheticwalls.ui.components.PaymentMethodDialog
import com.vistara.aestheticwalls.ui.theme.VistaraTheme

/**
 * 测试工具屏幕
 * 提供各种测试功能入口和用户状态测试
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(
    onBackPressed: () -> Unit,
    onNavigateToApiTest: () -> Unit,
    viewModel: TestViewModel = hiltViewModel()
) {
    // 获取用户状态
    val isPremiumUser by viewModel.isPremiumUser.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isDiamondTestEnabled by viewModel.isDiamondTestEnabled.collectAsState()
    val currentDiamondBalance by viewModel.currentDiamondBalance.collectAsState()
    val operationResult by viewModel.operationResult.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 显示操作结果
    LaunchedEffect(operationResult) {
        operationResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearOperationResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.test_tools)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
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
                text = stringResource(R.string.api_test),
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = onNavigateToApiTest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.launch_pexels_api_test))
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // 登录状态测试
            Text(
                text = stringResource(R.string.login_status_test),
                style = MaterialTheme.typography.titleMedium
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
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
                            text = stringResource(R.string.login_status),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Switch(
                            checked = isLoggedIn,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    viewModel.simulateLogin()
                                } else {
                                    viewModel.simulateLogout()
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isLoggedIn) stringResource(R.string.current_status_logged_in) else stringResource(R.string.current_status_logged_out),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )


            // 用户状态测试
            Text(
                text = stringResource(R.string.user_status_test),
                style = MaterialTheme.typography.titleMedium
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
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
                            text = stringResource(R.string.premium_user_status),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Switch(
                            checked = isPremiumUser,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    viewModel.enablePremiumUser()
                                } else {
                                    viewModel.disablePremiumUser()
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isPremiumUser) stringResource(R.string.current_status_premium) else stringResource(R.string.current_status_regular),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // 钻石测试
            Text(
                text = stringResource(R.string.diamond_test),
                style = MaterialTheme.typography.titleMedium
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
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
                            text = stringResource(R.string.diamond_test_mode),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Switch(
                            checked = isDiamondTestEnabled,
                            onCheckedChange = { _ ->
                                viewModel.toggleDiamondTest()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.current_diamond_balance, currentDiamondBalance),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isDiamondTestEnabled)
                            stringResource(R.string.test_mode_enabled)
                        else
                            stringResource(R.string.test_mode_disabled),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // 支付弹框测试
            Text(
                text = "支付弹框测试",
                style = MaterialTheme.typography.titleMedium
            )

            // 使用by语法创建状态
            var showPaymentDialog by remember { mutableStateOf(false) }
            var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }

            Button(
                onClick = { showPaymentDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("打开支付弹框")
            }

            // 显示选择的支付方式
            selectedPaymentMethod?.let {
                Text(
                    text = "已选择支付方式: ${it.displayName}",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // 支付弹框
            if (showPaymentDialog) {
                PaymentMethodDialog(
                    amount = "5600",
                    onDismiss = { showPaymentDialog = false },
                    onPaymentSelected = {
                        selectedPaymentMethod = it
                        showPaymentDialog = false
                    }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // 可以在这里添加更多测试入口
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun TestScreenPreview() {
    VistaraTheme {
        Surface {
            TestScreen(
                onBackPressed = {},
                onNavigateToApiTest = {}
            )
        }
    }
}
