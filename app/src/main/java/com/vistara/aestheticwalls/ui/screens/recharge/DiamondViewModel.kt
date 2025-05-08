package com.vistara.aestheticwalls.ui.screens.recharge

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.billing.BillingConnectionState
import com.vistara.aestheticwalls.billing.BillingManager
import com.vistara.aestheticwalls.billing.PurchaseState
import com.vistara.aestheticwalls.data.model.DiamondProduct
import com.vistara.aestheticwalls.data.model.DiamondTransaction
import com.vistara.aestheticwalls.data.repository.DiamondRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 钻石页面ViewModel
 */
@HiltViewModel
class DiamondViewModel @Inject constructor(
    private val diamondRepository: DiamondRepository,
    private val billingManager: BillingManager
) : ViewModel() {

    companion object {
        private const val TAG = "DiamondViewModel"
    }

    // 钻石余额
    private val _diamondBalance = MutableStateFlow(0)
    val diamondBalance: StateFlow<Int> = _diamondBalance.asStateFlow()

    // 钻石商品列表
    private val _diamondProducts = MutableStateFlow<List<DiamondProduct>>(emptyList())
    val diamondProducts: StateFlow<List<DiamondProduct>> = _diamondProducts.asStateFlow()

    // 交易记录
    private val _transactions = MutableStateFlow<List<DiamondTransaction>>(emptyList())
    val transactions: StateFlow<List<DiamondTransaction>> = _transactions.asStateFlow()

    // 选中的商品
    private val _selectedProduct = MutableStateFlow<DiamondProduct?>(null)
    val selectedProduct: StateFlow<DiamondProduct?> = _selectedProduct.asStateFlow()

    // 计费连接状态
    private val _billingConnectionState = MutableStateFlow(BillingConnectionState.DISCONNECTED)
    val billingConnectionState: StateFlow<BillingConnectionState> = _billingConnectionState.asStateFlow()

    // 购买状态
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    // 商品价格
    private val _productPrices = MutableStateFlow<Map<String, String>>(emptyMap())
    val productPrices: StateFlow<Map<String, String>> = _productPrices.asStateFlow()

    init {
        loadData()
        observeBillingState()
    }

    /**
     * 加载数据
     */
    private fun loadData() {
        // 获取钻石余额（使用单独的协程以避免被其他协程取消）
        viewModelScope.launch {
            diamondRepository.getDiamondBalance().collectLatest { balance ->
                _diamondBalance.value = balance
                Log.d(TAG, "Diamond balance updated: $balance")
            }
        }

        // 获取钻石商品列表
        viewModelScope.launch {
            val products = diamondRepository.getDiamondProducts()
            _diamondProducts.value = products

            // 默认选中第一个商品
            if (products.isNotEmpty() && _selectedProduct.value == null) {
//                _selectedProduct.value = products[0]
            }
        }

        // 获取交易记录
        viewModelScope.launch {
            diamondRepository.getTransactions().collectLatest { transactions ->
                _transactions.value = transactions
            }
        }
    }

    /**
     * 刷新钻石余额
     * 在购买完成后调用此方法以立即获取最新余额
     */
    private fun refreshDiamondBalance() {
        viewModelScope.launch {
            try {
                val balance = diamondRepository.getDiamondBalanceValue()
                _diamondBalance.value = balance
                Log.d(TAG, "Diamond balance refreshed: $balance")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh diamond balance", e)
            }
        }
    }

    /**
     * 观察计费状态
     */
    private fun observeBillingState() {
        viewModelScope.launch {
            billingManager.connectionState.collectLatest { state ->
                _billingConnectionState.value = state

                // 如果连接成功，获取商品价格
                if (state == BillingConnectionState.CONNECTED) {
                    updateProductPrices()
                }
            }
        }

        viewModelScope.launch {
            billingManager.purchaseState.collectLatest { state ->
                _purchaseState.value = state

                // 如果购买完成，立即刷新钻石余额
                if (state == PurchaseState.Completed) {
                    refreshDiamondBalance()
                    // 延迟一点时间后再次刷新，确保数据库更新完成
                    delay(500)
                    refreshDiamondBalance()
                }
            }
        }
    }

    /**
     * 更新商品价格
     */
    private fun updateProductPrices() {
        val priceMap = mutableMapOf<String, String>()

        // 获取所有钻石商品的价格
        BillingManager.DIAMOND_SKUS.forEach { productId ->
            priceMap[productId] = billingManager.getProductPrice(productId)
        }

        _productPrices.value = priceMap

        // 重新加载钻石商品列表，以获取最新的价格信息
        viewModelScope.launch {
            val products = diamondRepository.getDiamondProducts()
            _diamondProducts.value = products

            // 如果当前没有选中的商品，或者选中的商品不在列表中，则选择第一个商品
            if (_selectedProduct.value == null || !products.contains(_selectedProduct.value)) {
                if (products.isNotEmpty()) {
                    _selectedProduct.value = products[0]
                }
            }
        }
    }

    /**
     * 选择商品
     */
    fun selectProduct(product: DiamondProduct) {
        _selectedProduct.value = product
    }

    /**
     * 购买钻石
     */
    fun purchaseDiamond(activity: Activity?) {
        val product = _selectedProduct.value ?: return

        // 启动购买流程
        billingManager.launchBillingFlow(activity, product.googlePlayProductId ?: return)
    }

    /**
     * 连接计费服务
     */
    fun connectBillingService() {
        billingManager.connectToPlayBilling()
    }
}
