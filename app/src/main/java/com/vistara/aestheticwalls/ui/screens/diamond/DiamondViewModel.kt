package com.vistara.aestheticwalls.ui.screens.diamond

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
        viewModelScope.launch {
            // 获取钻石余额
            diamondRepository.getDiamondBalance().collectLatest { balance ->
                _diamondBalance.value = balance
            }
        }

        viewModelScope.launch {
            // 获取钻石商品列表
            val products = diamondRepository.getDiamondProducts()
            _diamondProducts.value = products

            // 默认选中第一个商品
            if (products.isNotEmpty()) {
                _selectedProduct.value = products[0]
            }
        }

        viewModelScope.launch {
            // 获取交易记录
            diamondRepository.getTransactions().collectLatest { transactions ->
                _transactions.value = transactions
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

                // 如果购买完成，刷新数据
                if (state == PurchaseState.Completed) {
                    loadData()
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
        priceMap[BillingManager.DIAMOND_80] = billingManager.getProductPrice(BillingManager.DIAMOND_80)
        priceMap[BillingManager.DIAMOND_140] = billingManager.getProductPrice(BillingManager.DIAMOND_140)
        priceMap[BillingManager.DIAMOND_199] = billingManager.getProductPrice(BillingManager.DIAMOND_199)
        priceMap[BillingManager.DIAMOND_352] = billingManager.getProductPrice(BillingManager.DIAMOND_352)
        priceMap[BillingManager.DIAMOND_500] = billingManager.getProductPrice(BillingManager.DIAMOND_500)
        priceMap[BillingManager.DIAMOND_705] = billingManager.getProductPrice(BillingManager.DIAMOND_705)
        priceMap[BillingManager.DIAMOND_799] = billingManager.getProductPrice(BillingManager.DIAMOND_799)
        priceMap[BillingManager.DIAMOND_1411] = billingManager.getProductPrice(BillingManager.DIAMOND_1411)
        priceMap[BillingManager.DIAMOND_3528] = billingManager.getProductPrice(BillingManager.DIAMOND_3528)
        priceMap[BillingManager.DIAMOND_7058] = billingManager.getProductPrice(BillingManager.DIAMOND_7058)

        _productPrices.value = priceMap
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
