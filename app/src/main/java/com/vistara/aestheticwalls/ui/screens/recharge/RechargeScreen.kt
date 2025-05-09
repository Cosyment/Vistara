package com.vistara.aestheticwalls.ui.screens.recharge

import android.app.Activity
import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.DiamondProduct
import com.vistara.aestheticwalls.data.remote.api.PaymentMethod
import com.vistara.aestheticwalls.ui.components.PaymentMethodDialog
import com.vistara.aestheticwalls.ui.icons.AppIcons
import com.vistara.aestheticwalls.ui.theme.VistaraTheme
import com.vistara.aestheticwalls.ui.theme.stringResource

/**
 * 钻石充值页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeScreen(
    onBackPressed: () -> Unit,
    navController: NavController? = null,
    viewModel: RechargeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val diamondBalance by viewModel.diamondBalance.collectAsState()
    val diamondProducts by viewModel.diamondProducts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val billingConnectionState by viewModel.billingConnectionState.collectAsState()
    val purchaseState by viewModel.purchaseState.collectAsState()
    val productPrices by viewModel.productPrices.collectAsState()
    val apiProductsLoading by viewModel.apiProductsLoading.collectAsState()
    val apiProductsError by viewModel.apiProductsError.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()
    val showPaymentDialog by viewModel.showPaymentDialog.collectAsState()
    val paymentUrl by viewModel.paymentUrl.collectAsState()
    val orderCreationState by viewModel.orderCreationState.collectAsState()

    var showTransactions by remember { mutableStateOf(false) }

    // 连接计费服务
    LaunchedEffect(Unit) {
        viewModel.connectBillingService()
    }

    // 处理支付URL
    LaunchedEffect(paymentUrl) {
        paymentUrl?.let { url ->
            try {
                // 对URL进行编码
                val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
                val route = "webview?url=$encodedUrl"

                // 检查navController是否为null
                if (navController == null) {
                    Log.e("RechargeScreen", "NavController is null, cannot navigate to WebView")
                } else {
                    Log.d("RechargeScreen", "NavController is available, navigating to: $route")
                    // 导航到WebView页面，使用正确的路由格式
                    navController.navigate(route) {
                        // 导航选项
                        launchSingleTop = true
                    }
                    Log.d("RechargeScreen", "Navigation command executed for URL: $url")
                }
            } catch (e: Exception) {
                Log.e("RechargeScreen", "Error navigating to WebView: ${e.message}", e)
            }
            // 清除支付URL，避免重复导航
            viewModel.clearPaymentUrl()
        }
    }



    // 显示支付方式对话框
    if (showPaymentDialog && selectedProduct != null) {
        PaymentMethodDialog(
            amount =  "${selectedProduct?.diamondAmount}",
            paymentMethods = paymentMethods,
            isLoading = viewModel.paymentMethodsLoading.collectAsState().value,
            onDismiss = viewModel::hidePaymentDialog,
            onPaymentSelected = viewModel::handlePaymentMethodSelected
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.diamond_recharge)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showTransactions = !showTransactions }) {
                        Icon(
                            imageVector = AppIcons.History,
                            contentDescription = stringResource(R.string.transaction_history)
                        )
                    }
                })
        }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 主内容
            AnimatedVisibility(
                visible = !showTransactions,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                RechargeContent(
                    diamondBalance = diamondBalance,
                    diamondProducts = diamondProducts,
                    selectedProduct = selectedProduct,
                    productPrices = productPrices,
                    isLoading = apiProductsLoading,
                    errorMessage = apiProductsError,
                    onProductSelected = viewModel::selectProduct,
                    onPurchase = { viewModel.showPaymentDialog() })
            }

            // 交易记录
            AnimatedVisibility(
                visible = showTransactions,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                TransactionHistoryContent(
                    transactions = transactions, diamondBalance = diamondBalance
                )
            }
        }
    }
}

/**
 * 钻石充值内容
 */
@Composable
fun RechargeContent(
    diamondBalance: Int,
    diamondProducts: List<DiamondProduct>,
    selectedProduct: DiamondProduct?,
    productPrices: Map<String, String>,
    isLoading: Boolean,
    errorMessage: String?,
    onProductSelected: (DiamondProduct) -> Unit,
    onPurchase: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 钻石余额
        item {
            DiamondBalanceCard(diamondBalance)
        }

        // 首充奖励卡片
        item {
            FirstRechargeCard()
        }

        // 钻石商品列表
        item {
            Text(
                text = stringResource(R.string.select_diamond_package),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }

        // 加载状态
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.loading_products),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        // 错误提示
        else if (errorMessage != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.error_loading_products, errorMessage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // 商品列表
        if (!isLoading && errorMessage == null) {
            itemsIndexed(diamondProducts) { index, product ->
                // 获取价格，优先使用Google Play价格
                // 尝试使用productId获取价格
                val googlePriceFromProductId = product.productId?.let { productPrices[it] }
                // API返回的价格
                val apiPrice = "${product.currency} ${product.price}"
                // 优先使用productId获取的价格，其次使用API价格
                val displayPrice = googlePriceFromProductId ?: apiPrice

                // 记录价格信息，用于调试
                Log.d("RechargeScreen", "Product ${product.id}: " +
                        "productId=${product.productId}, " +
                        "Google price from productId=$googlePriceFromProductId, " +
                        "API price=$apiPrice, " +
                        "Display price=$displayPrice")

                DiamondProductCard(
                    index = index,
                    product = product,
                    isSelected = product == selectedProduct,
                    price = displayPrice,
                    onClick = {
                        onProductSelected(product)
                        onPurchase()
                    })
            }
        }

        // 购买按钮
        item {
            Spacer(modifier = Modifier.height(24.dp))

//            Button(
//                onClick = onPurchase,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(50.dp),
//                enabled = selectedProduct != null && billingConnectionState == BillingConnectionState.CONNECTED && purchaseState != PurchaseState.Pending,
//                shape = RoundedCornerShape(25.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color.Transparent,
//                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
//                ),
//                contentPadding = PaddingValues(0.dp)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(
//                            brush = Brush.linearGradient(
//                                colors = if (selectedProduct != null && billingConnectionState == BillingConnectionState.CONNECTED && purchaseState != PurchaseState.Pending) {
//                                    listOf(
//                                        Color(0xFF9F2BEE), // 紫色渐变起始色
//                                        Color(0xFF8545FF)  // 紫色渐变结束色
//                                    )
//                                } else {
//                                    listOf(
//                                        Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.3f)
//                                    )
//                                }
//                            )
//                        ), contentAlignment = Alignment.Center
//                ) {
//                    if (purchaseState == PurchaseState.Pending) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp
//                        )
//                    } else {
//                        Text(
//                            text = when {
//                                billingConnectionState != BillingConnectionState.CONNECTED -> stringResource(
//                                    R.string.connecting_payment
//                                )
//
//                                else -> stringResource(R.string.purchase_now)
//                            },
//                            style = MaterialTheme.typography.bodyLarge,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.White
//                        )
//                    }
//                }
//            }
        }
    }
}


@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL,
    device = "spec:width=1080px,height=2340px,dpi=440"
)
@Composable
fun RechargeScreenPreview() {
    VistaraTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // 注意：预览中不会显示真实数据，因为没有提供真实的ViewModel
            // 这里只是UI预览
            DiamondBalanceCard(100)
//            RechargeContent(
//                diamondBalance = 100,
//                diamondProducts = emptyList(),
//                selectedProduct = null,
//                productPrices = emptyMap(),
//                onProductSelected = {},
//                onPurchase = {})
        }
    }
}
