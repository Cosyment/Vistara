package com.vistara.aestheticwalls.ui.screens.recharge

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.billing.BillingConnectionState
import com.vistara.aestheticwalls.billing.PurchaseState
import com.vistara.aestheticwalls.data.model.DiamondProduct
import com.vistara.aestheticwalls.ui.icons.AppIcons
import com.vistara.aestheticwalls.ui.theme.VistaraTheme
import com.vistara.aestheticwalls.ui.theme.stringResource

/**
 * 钻石充值页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeScreen(
    onBackPressed: () -> Unit, viewModel: RechargeViewModel = hiltViewModel()
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

    var showTransactions by remember { mutableStateOf(false) }

    // 连接计费服务
    LaunchedEffect(Unit) {
        viewModel.connectBillingService()
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
                DiamondRechargeContent(
                    diamondBalance = diamondBalance,
                    diamondProducts = diamondProducts,
                    selectedProduct = selectedProduct,
                    billingConnectionState = billingConnectionState,
                    purchaseState = purchaseState,
                    productPrices = productPrices,
                    onProductSelected = viewModel::selectProduct,
                    onPurchase = { activity?.let { viewModel.purchaseDiamond(it) } })
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
fun DiamondRechargeContent(
    diamondBalance: Int,
    diamondProducts: List<DiamondProduct>,
    selectedProduct: DiamondProduct?,
    billingConnectionState: BillingConnectionState,
    purchaseState: PurchaseState,
    productPrices: Map<String, String>,
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

        itemsIndexed(diamondProducts) { index, product ->
            DiamondProductCard(
                index = index,
                product = product,
                isSelected = product == selectedProduct,
                price = product.googlePlayProductId?.let { productPrices[it] }
                    ?: stringResource(R.string.loading),
                onClick = {
                    onProductSelected(product)
                    onPurchase
                })
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
fun DiamondScreenPreview() {
    VistaraTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // 注意：预览中不会显示真实数据，因为没有提供真实的ViewModel
            // 这里只是UI预览
            RechargeScreen(onBackPressed = {})
        }
    }
}
