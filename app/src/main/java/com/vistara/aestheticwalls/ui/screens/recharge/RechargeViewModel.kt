package com.vistara.aestheticwalls.ui.screens.recharge

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.billing.BillingConnectionState
import com.vistara.aestheticwalls.billing.BillingManager
import com.vistara.aestheticwalls.billing.PurchaseState
import com.vistara.aestheticwalls.data.model.DiamondProduct
import com.vistara.aestheticwalls.data.model.DiamondTransaction
import com.vistara.aestheticwalls.data.remote.api.CreateOrderResponse
import com.vistara.aestheticwalls.data.remote.api.PaymentMethod
import com.vistara.aestheticwalls.data.repository.DiamondRepository
import com.vistara.aestheticwalls.utils.ActivityProvider
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
class RechargeViewModel @Inject constructor(
    private val application: Application,
    private val diamondRepository: DiamondRepository,
    private val billingManager: BillingManager,
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "RechargeViewModel"
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
    val billingConnectionState: StateFlow<BillingConnectionState> =
        _billingConnectionState.asStateFlow()

    // 购买状态
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    // 商品价格
    private val _productPrices = MutableStateFlow<Map<String, String>>(emptyMap())
    val productPrices: StateFlow<Map<String, String>> = _productPrices.asStateFlow()

    // API商品加载状态
    private val _apiProductsLoading = MutableStateFlow(false)
    val apiProductsLoading: StateFlow<Boolean> = _apiProductsLoading.asStateFlow()

    // API商品加载错误
    private val _apiProductsError = MutableStateFlow<String?>(null)
    val apiProductsError: StateFlow<String?> = _apiProductsError.asStateFlow()

    // 支付方式列表
    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(emptyList())
    val paymentMethods: StateFlow<List<PaymentMethod>> = _paymentMethods.asStateFlow()

    // 支付方式加载状态
    private val _paymentMethodsLoading = MutableStateFlow(false)
    val paymentMethodsLoading: StateFlow<Boolean> = _paymentMethodsLoading.asStateFlow()

    // 支付方式加载错误
    private val _paymentMethodsError = MutableStateFlow<String?>(null)
    val paymentMethodsError: StateFlow<String?> = _paymentMethodsError.asStateFlow()

    // 显示支付方式对话框
    private val _showPaymentDialog = MutableStateFlow(false)
    val showPaymentDialog: StateFlow<Boolean> = _showPaymentDialog.asStateFlow()

    // 订单创建状态
    private val _orderCreationState = MutableStateFlow<OrderCreationState>(OrderCreationState.Idle)
    val orderCreationState: StateFlow<OrderCreationState> = _orderCreationState.asStateFlow()

    // 支付URL
    private val _paymentUrl = MutableStateFlow<String?>(null)
    val paymentUrl: StateFlow<String?> = _paymentUrl.asStateFlow()

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
            try {
                _apiProductsLoading.value = true
                val products = diamondRepository.getDiamondProducts()
                products?.onSuccess {
                    _diamondProducts.value = it
                }

                // 默认选中第一个商品
                if (products.getOrNull()?.isNotEmpty() == true && _selectedProduct.value == null) {
//                    _selectedProduct.value = products.getOrNull()?.firstOrNull()
                }
            } catch (e: Exception) {
                _apiProductsError.value = e.message
                Log.e(TAG, "Error loading diamond products: ${e.message}", e)
            } finally {
                _apiProductsLoading.value = false
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
            val price = billingManager.getProductPrice(productId)
            priceMap[productId] = price
            Log.d(TAG, "Google Play price for $productId: $price")
        }

        // 更新价格状态
        _productPrices.value = priceMap
        Log.d(TAG, "Updated product prices from Google Play: ${priceMap.size} items")
        Log.d(TAG, "All Google Play SKUs: ${BillingManager.DIAMOND_SKUS}")

        // 记录所有商品的ID和价格映射关系
        _diamondProducts.value.forEach { product ->
            val productId = product.productId
            val priceFromProductId = productId?.let { priceMap[it] }
            val apiPrice = "${product.currency} ${product.price}"

            Log.d(
                TAG,
                "Product ${product.id}: " + "productId=$productId, " + "price from productId=$priceFromProductId, " + "API price=$apiPrice"
            )

            // 检查productId是否在BillingManager.DIAMOND_SKUS中
            if (productId != null) {
                val isInSkuList = BillingManager.DIAMOND_SKUS.contains(productId)
                Log.d(
                    TAG,
                    "Product ${product.id} with productId=$productId " + "is ${if (isInSkuList) "in" else "NOT in"} BillingManager.DIAMOND_SKUS"
                )
            }
        }

        // 如果当前没有选中的商品，或者选中的商品不在列表中，则选择第一个商品
        val currentProducts = _diamondProducts.value
        if (currentProducts.isNotEmpty() && (_selectedProduct.value == null || !currentProducts.contains(
                _selectedProduct.value
            ))
        ) {
//            _selectedProduct.value = currentProducts[0]
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

        // 使用productId
        val billingProductId = product.productId

        if (billingProductId == null) {
            Log.e(TAG, "Cannot purchase product ${product.id}: no valid product ID")
            return
        }

        Log.d(
            TAG,
            "Launching billing flow for product ${product.id} with billing ID: $billingProductId"
        )

        // 启动购买流程
        billingManager.launchBillingFlow(activity, billingProductId)
    }

    /**
     * 连接计费服务
     */
    fun connectBillingService() {
        billingManager.connectToPlayBilling()
    }

    /**
     * 加载支付方式
     */
    fun loadPaymentMethods() {
        viewModelScope.launch {
            try {
                _paymentMethodsLoading.value = true
                _paymentMethodsError.value = null

                val result =
                    diamondRepository.getPaymentMethods(_selectedProduct.value?.itemName ?: "")
                result.onSuccess { methods ->
                    _paymentMethods.value = methods
                    Log.d(TAG, "Payment methods loaded: ${methods.size}")
                }.onError { code, errorMsg, source ->
                    _paymentMethodsError.value = errorMsg
                    Log.e(TAG, "Failed to load payment methods: $errorMsg")
                }
            } catch (e: Exception) {
                _paymentMethodsError.value = e.message
                Log.e(TAG, "Error loading payment methods: ${e.message}", e)
            } finally {
                _paymentMethodsLoading.value = false
            }
        }
    }

    /**
     * 显示支付方式对话框
     */
    fun showPaymentDialog() {
        // 加载支付方式
        loadPaymentMethods()
        // 显示对话框
        _showPaymentDialog.value = true
    }

    /**
     * 隐藏支付方式对话框
     */
    fun hidePaymentDialog() {
        _showPaymentDialog.value = false
    }

    /**
     * 处理支付方式选择
     */
    fun handlePaymentMethodSelected(paymentMethod: PaymentMethod) {
        // 获取当前选中的商品
        val product = _selectedProduct.value ?: return

        Log.d(
            TAG, "Selected payment method: ${product.id} ${product.productId} ${paymentMethod.name}"
        )

        // 设置订单创建状态为加载中
        _orderCreationState.value = OrderCreationState.Loading

        // 创建订单
        viewModelScope.launch {
            try {
                val result = diamondRepository.createOrder(
                    productId = paymentMethod.id, paymentMethodId = paymentMethod.id
                )

                result.onSuccess { orderResponse ->
                    // 订单创建成功
                    _orderCreationState.value = OrderCreationState.Success(orderResponse)
                    // 根据支付方式处理不同的支付逻辑
                    when {

                        // 如果是Google Play支付
                        orderResponse.isGooglePay -> {
                            // 隐藏对话框
                            hidePaymentDialog()
                            // 使用ActivityProvider获取Activity实例
                            val activity = ActivityProvider.getMainActivity()
                            if (activity != null) {
                                // 调用Google Play支付
                                Log.d(
                                    TAG,
                                    "Executing Google payment with activity from ActivityProvider"
                                )
                                purchaseDiamond(activity)
                            } else {
                                Log.e(
                                    TAG,
                                    "Activity is null from ActivityProvider, cannot execute Google payment"
                                )
                                _orderCreationState.value =
                                    OrderCreationState.Error("无法启动支付，请重试")
                            }
                        }
                        // 其他支付方式
                        else -> {
                            // 隐藏对话框
                            hidePaymentDialog()
                            // 设置支付URL
                            _paymentUrl.value = orderResponse.payUrl
                            Log.d(TAG, "Payment URL: ${orderResponse.payUrl}")
                        }
                    }
                }.onError { code, errorMsg, source ->
                    // 订单创建失败
                    _orderCreationState.value = OrderCreationState.Error(errorMsg)
                    Log.e(TAG, "Failed to create order: $errorMsg")
                }
            } catch (e: Exception) {
                // 订单创建异常
                _orderCreationState.value = OrderCreationState.Error(e.message ?: "Unknown error")
                Log.e(TAG, "Error creating order: ${e.message}", e)
            }
        }
    }

    /**
     * 清除支付URL
     */
    fun clearPaymentUrl() {
        _paymentUrl.value = null
    }
}

/**
 * 订单创建状态
 */
sealed class OrderCreationState {
    object Idle : OrderCreationState()
    object Loading : OrderCreationState()
    data class Success(val orderResponse: CreateOrderResponse) : OrderCreationState()

    data class Error(val message: String) : OrderCreationState()
}
