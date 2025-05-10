package com.vistara.aestheticwalls.ui.screens.premium

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.billing.BillingConnectionState
import com.vistara.aestheticwalls.billing.BillingManager
import com.vistara.aestheticwalls.billing.PurchaseState
import com.vistara.aestheticwalls.data.model.DiamondProduct
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.remote.api.PaymentMethod
import com.vistara.aestheticwalls.data.repository.DiamondRepository
import com.vistara.aestheticwalls.data.repository.UserRepository
import com.vistara.aestheticwalls.manager.ThemeManager
import com.vistara.aestheticwalls.ui.screens.recharge.OrderCreationState
import com.vistara.aestheticwalls.utils.ActivityProvider
import com.vistara.aestheticwalls.utils.Constants.PRIVACY_POLICY_URL
import com.vistara.aestheticwalls.utils.Constants.TERMS_OF_SERVICE_URL
import com.vistara.aestheticwalls.utils.StringProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.URLEncoder
import javax.inject.Inject

/**
 * 升级页面的ViewModel
 */
@HiltViewModel
class PremiumViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val billingManager: BillingManager,
    private val stringProvider: StringProvider,
    private val themeManager: ThemeManager,
    private val diamondRepository: DiamondRepository
) : ViewModel() {

    companion object {
        private const val TAG = "PremiumViewModel"
    }

    // 导航控制器
    private var navController: NavController? = null

    private val _darkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()

    private val _canPayment = MutableStateFlow(false)
    val canPayment: StateFlow<Boolean> = _canPayment.asStateFlow()

    // 高级用户状态
    private val _isPremiumUser = MutableStateFlow(false)
    val isPremiumUser: StateFlow<Boolean> = _isPremiumUser.asStateFlow()

    // 升级中状态
    private val _isUpgrading = MutableStateFlow(false)
    val isUpgrading: StateFlow<Boolean> = _isUpgrading.asStateFlow()

    // 升级结果
    private val _upgradeResult = MutableStateFlow<UpgradeResult?>(null)
    val upgradeResult: StateFlow<UpgradeResult?> = _upgradeResult.asStateFlow()

    // 选中的套餐，初始为null表示没有选中任何套餐
    private val _selectedPlan = MutableStateFlow<PremiumPlan?>(null)
    val selectedPlan: StateFlow<PremiumPlan?> = _selectedPlan.asStateFlow()

    // 计费连接状态
    private val _billingConnectionState = MutableStateFlow(BillingConnectionState.DISCONNECTED)
    val billingConnectionState: StateFlow<BillingConnectionState> =
        _billingConnectionState.asStateFlow()

    // 商品价格
    private val _productPrices = MutableStateFlow<Map<String, String>>(emptyMap())
    val productPrices: StateFlow<Map<String, String>> = _productPrices.asStateFlow()

    // 订阅商品列表
    private val _subscriptionProducts = MutableStateFlow<List<DiamondProduct>>(emptyList())
    val subscriptionProducts: StateFlow<List<DiamondProduct>> = _subscriptionProducts.asStateFlow()

    // API商品加载状态
    private val _apiProductsLoading = MutableStateFlow(false)
    val apiProductsLoading: StateFlow<Boolean> = _apiProductsLoading.asStateFlow()

    // API商品加载错误
    private val _apiProductsError = MutableStateFlow<String?>(null)
    val apiProductsError: StateFlow<String?> = _apiProductsError.asStateFlow()

    // 支付方式相关
    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(emptyList())
    val paymentMethods: StateFlow<List<PaymentMethod>> = _paymentMethods.asStateFlow()

    private val _paymentMethodsLoading = MutableStateFlow(false)
    val paymentMethodsLoading: StateFlow<Boolean> = _paymentMethodsLoading.asStateFlow()

    private val _paymentMethodsError = MutableStateFlow<String?>(null)
    val paymentMethodsError: StateFlow<String?> = _paymentMethodsError.asStateFlow()

    // 支付对话框
    private val _showPaymentDialog = MutableStateFlow(false)
    val showPaymentDialog: StateFlow<Boolean> = _showPaymentDialog.asStateFlow()

    // 订单创建状态
    private val _orderCreationState = MutableStateFlow<OrderCreationState>(OrderCreationState.Idle)
    val orderCreationState: StateFlow<OrderCreationState> = _orderCreationState.asStateFlow()

    // 支付URL
    private val _paymentUrl = MutableStateFlow<String?>(null)
    val paymentUrl: StateFlow<String?> = _paymentUrl.asStateFlow()

    init {
        // 初始化时清除升级结果，避免显示旧的错误消息
        _upgradeResult.value = null

        checkPremiumStatus()
        observePaymentStatus()
        observeBillingState()
        observePurchaseState()
        observeDarkTheme()
        loadSubscriptionProducts()
    }

    private fun observePaymentStatus() {
        viewModelScope.launch {
            _canPayment.value =
                userRepository.getCachedUserProfile()?.isWhitelisted == true || (!_isPremiumUser.value && !_isUpgrading.value && _billingConnectionState.value == BillingConnectionState.CONNECTED && _orderCreationState != OrderCreationState.Loading)
        }
    }

    private fun observeDarkTheme() {
        viewModelScope.launch {
            _darkTheme.value = themeManager.isDarkTheme().first()
        }
    }

    /**
     * 检查高级用户状态
     */
    private fun checkPremiumStatus() {
        viewModelScope.launch {
            try {
                val isPremium = userRepository.isPremiumUser.first()
                _isPremiumUser.value = isPremium
                Log.d(TAG, "Premium status: $isPremium")
            } catch (e: Exception) {
                Log.e(TAG, "Error checking premium status: ${e.message}")
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
                Log.d(TAG, "Billing connection state: $state")

                if (state == BillingConnectionState.CONNECTED) {
                    updateProductPrices()
                }
            }
        }
    }

    /**
     * 观察购买状态
     */
    private fun observePurchaseState() {
        viewModelScope.launch {
            billingManager.purchaseState.collectLatest { state ->
                when (state) {
                    is PurchaseState.Pending -> {
                        _isUpgrading.value = true
                    }

                    is PurchaseState.Completed -> {
                        _isUpgrading.value = false
                        _isPremiumUser.value = true
                        _upgradeResult.value =
                            UpgradeResult.Success(stringProvider.getString(R.string.upgrade_success))
                    }

                    is PurchaseState.Failed -> {
                        _isUpgrading.value = false
                        _upgradeResult.value = UpgradeResult.Error(
                            stringProvider.getString(
                                R.string.upgrade_failed, state.message
                            )
                        )
                    }

                    is PurchaseState.Cancelled -> {
                        _isUpgrading.value = false
                        _upgradeResult.value =
                            UpgradeResult.Error(stringProvider.getString(R.string.upgrade_cancelled))
                    }

                    is PurchaseState.Restoring -> {
                        _isUpgrading.value = true
                    }

                    else -> {
                        _isUpgrading.value = false
                    }
                }
            }
        }
    }

    /**
     * 更新商品价格
     */
    private fun updateProductPrices() {
        val prices = mutableMapOf<String, String>()

        // 月度套餐
        prices[BillingManager.SUBSCRIPTION_WEEKLY] =
            billingManager.getProductPrice(BillingManager.SUBSCRIPTION_WEEKLY)
        // 周套餐
        prices[BillingManager.SUBSCRIPTION_MONTHLY] =
            billingManager.getProductPrice(BillingManager.SUBSCRIPTION_MONTHLY)
        // 季度套餐
        prices[BillingManager.SUBSCRIPTION_QUARTERLY] =
            billingManager.getProductPrice(BillingManager.SUBSCRIPTION_QUARTERLY)
        // 年度套餐
//        prices[BillingManager.SUBSCRIPTION_YEARLY] = billingManager.getProductPrice(BillingManager.SUBSCRIPTION_YEARLY)

        // 终身套餐
//        prices[BillingManager.PREMIUM_LIFETIME] = billingManager.getProductPrice(BillingManager.PREMIUM_LIFETIME)

        _productPrices.value = prices
    }

    /**
     * 加载订阅商品列表
     */
    private fun loadSubscriptionProducts() {
        viewModelScope.launch {
            try {
                _apiProductsLoading.value = true
                _apiProductsError.value = null

                // 从API获取钻石商品列表
                val result = diamondRepository.getDiamondProducts()
                result.onSuccess { products ->
                    // 过滤出订阅商品（这里假设订阅商品的productId包含"subscription"）
                    val subscriptionProducts = products.filter { product ->
                        product.priceType == "2"
                    }

                    // 根据套餐类型排序：周、月、季
                    val sortedProducts = subscriptionProducts.sortedBy { product ->
                        when {
                            product.productId?.contains(
                                "vistara_sub_week", ignoreCase = true
                            ) == true -> 0

                            product.productId?.contains(
                                "vistara_sub_month", ignoreCase = true
                            ) == true -> 1

                            product.productId?.contains(
                                "vistara_sub_quarter", ignoreCase = true
                            ) == true -> 2

                            else -> 3
                        }
                    }

                    _subscriptionProducts.value = sortedProducts
                    Log.d(TAG, "Loaded ${sortedProducts.size} subscription products")

                    // 如果没有从API获取到订阅商品，则创建默认的订阅商品
                    if (sortedProducts.isEmpty()) {
//                        createDefaultSubscriptionProducts()
                    } else {
                        // 如果有订阅商品，默认选择第一个
                        sortedProducts.firstOrNull()?.let { firstProduct ->
//                            val plan = when {
//                                firstProduct.productId?.contains("subweek", ignoreCase = true) == true -> PremiumPlan.WEEKLY
//                                firstProduct.productId?.contains("submonth", ignoreCase = true) == true -> PremiumPlan.MONTHLY
//                                firstProduct.productId?.contains("sub3month", ignoreCase = true) == true -> PremiumPlan.QUARTERLY
//                                else -> PremiumPlan.MONTHLY // 默认为月度套餐
//                            }
                            _selectedPlan.value = PremiumPlan.WEEKLY
                        }
                    }
                }.onError { code, message, source ->
                    _apiProductsError.value = message
                    Log.e(TAG, "Error loading subscription products: $message")
                    // 创建默认的订阅商品
                    createDefaultSubscriptionProducts()
                }
            } catch (e: Exception) {
                _apiProductsError.value = e.message
                Log.e(TAG, "Exception loading subscription products: ${e.message}", e)
                // 创建默认的订阅商品
                createDefaultSubscriptionProducts()
            } finally {
                _apiProductsLoading.value = false
            }
        }
    }

    /**
     * 创建默认的订阅商品
     */
    private fun createDefaultSubscriptionProducts() {
        // 创建默认的订阅商品
        val defaultProducts = listOf(
            DiamondProduct(
                id = "subscription_weekly",
                name = stringProvider.getString(R.string.subscription_title_week),
                itemName = stringProvider.getString(R.string.subscription_title_week),
                diamondAmount = 0,
                price = 19.99,
                currency = "¥",
                productId = BillingManager.SUBSCRIPTION_WEEKLY,
                discount = 0,
                payMethodId = 0,
                dollarPrice = null,
                googlePlayProductId = null
            ), DiamondProduct(
                id = "subscription_monthly",
                name = stringProvider.getString(R.string.subscription_title_month),
                itemName = stringProvider.getString(R.string.subscription_title_month),
                diamondAmount = 0,
                price = 49.99,
                currency = "¥",
                productId = BillingManager.SUBSCRIPTION_MONTHLY,
                discount = 0,
                payMethodId = 0,
                dollarPrice = null,
                googlePlayProductId = null
            ), DiamondProduct(
                id = "subscription_quarterly",
                name = stringProvider.getString(R.string.subscription_title_quarter),
                itemName = stringProvider.getString(R.string.subscription_title_quarter),
                diamondAmount = 0,
                price = 129.99,
                currency = "¥",
                productId = BillingManager.SUBSCRIPTION_QUARTERLY,
                discount = 0,
                payMethodId = 0,
                dollarPrice = null,
                googlePlayProductId = null
            )
        )

        _subscriptionProducts.value = defaultProducts
        Log.d(TAG, "Created ${defaultProducts.size} default subscription products")

        // 默认选择第一个产品
        defaultProducts.firstOrNull()?.let {
            _selectedPlan.value = PremiumPlan.WEEKLY
            Log.d(TAG, "Default selected plan from default products: ${_selectedPlan.value}")
        }
    }

    /**
     * 加载支付方式
     */
    private fun loadPaymentMethods() {
        viewModelScope.launch {
            _paymentMethodsLoading.value = true
            _paymentMethodsError.value = null

            try {
                // 如果没有选择套餐，默认使用月度套餐
                if (_selectedPlan.value == null) {
                    _selectedPlan.value = PremiumPlan.MONTHLY
                }

                // 根据选择的套餐确定商品ID
                val productId = when (_selectedPlan.value) {
                    PremiumPlan.WEEKLY -> BillingManager.SUBSCRIPTION_WEEKLY
                    PremiumPlan.MONTHLY -> BillingManager.SUBSCRIPTION_MONTHLY
                    PremiumPlan.QUARTERLY -> BillingManager.SUBSCRIPTION_QUARTERLY
                    PremiumPlan.YEARLY -> BillingManager.SUBSCRIPTION_MONTHLY // 暂时使用月度套餐
                    PremiumPlan.LIFETIME -> BillingManager.SUBSCRIPTION_QUARTERLY // 暂时使用季度套餐
                    null -> BillingManager.SUBSCRIPTION_MONTHLY // 默认使用月度套餐
                }

                val itemName =
                    subscriptionProducts.value.find { it.productId == productId }?.itemName ?: ""

                val result = diamondRepository.getPaymentMethods(itemName)
                if (result is ApiResult.Success) {
                    _paymentMethods.value = result.data
                    Log.d(TAG, "Payment methods loaded: ${result.data.size}")
                } else if (result is ApiResult.Error) {
                    _paymentMethodsError.value = result.message
                    Log.e(TAG, "Error loading payment methods: ${result.message}")
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
        viewModelScope.launch {
            _showPaymentDialog.value = userRepository.getCachedUserProfile()?.isWhitelisted == true
            if (_showPaymentDialog.value) {
                // 加载支付方式
                loadPaymentMethods()
            } else {
                if (_billingConnectionState.value != BillingConnectionState.CONNECTED) {
                    _upgradeResult.value =
                        UpgradeResult.Error(stringProvider.getString(R.string.payment_service_not_connected))
                } else {
                    //非白名单用户直接走Google Play支付
                    handlePaymentMethodSelected()
                }
            }
        }
    }

    /**
     * 隐藏支付方式对话框
     */
    fun hidePaymentDialog() {
        _showPaymentDialog.value = false
    }

    /**
     * 选择套餐
     */
    fun selectPlan(plan: PremiumPlan) {
        Log.d(TAG, "Selecting plan: $plan")
        _selectedPlan.value = plan
    }

    /**
     * 升级到高级版
     */
    fun upgrade(activity: Activity?) {
        if (_isPremiumUser.value) {
            _upgradeResult.value =
                UpgradeResult.Error(stringProvider.getString(R.string.already_premium_user))
            return
        }

        // 显示支付方式对话框或直接创建订单
        showPaymentDialog()
    }

    /**
     * 处理支付方式选择
     */
    fun handlePaymentMethodSelected(paymentMethodId: String? = null) {
        // 如果没有选择套餐，默认使用月度套餐
        if (_selectedPlan.value == null) {
            _selectedPlan.value = PremiumPlan.MONTHLY
        }

        // 根据选择的套餐确定商品ID
        val productId = when (_selectedPlan.value) {
            PremiumPlan.WEEKLY -> BillingManager.SUBSCRIPTION_WEEKLY
            PremiumPlan.MONTHLY -> BillingManager.SUBSCRIPTION_MONTHLY
            PremiumPlan.QUARTERLY -> BillingManager.SUBSCRIPTION_QUARTERLY
            PremiumPlan.YEARLY -> BillingManager.SUBSCRIPTION_MONTHLY // 暂时使用月度套餐
            PremiumPlan.LIFETIME -> BillingManager.SUBSCRIPTION_QUARTERLY // 暂时使用季度套餐
            null -> BillingManager.SUBSCRIPTION_MONTHLY // 默认使用月度套餐
        }

        Log.d(
            TAG, "Selected payment method: $productId"
        )

        // 设置订单创建状态为加载中
        _orderCreationState.value = OrderCreationState.Loading

        // 创建订单
        viewModelScope.launch {
            try {
                val result = diamondRepository.createOrder(
                    productId = paymentMethodId
                        ?: subscriptionProducts.value.find { it.productId == productId }?.id ?: "",
                    paymentMethodId = productId
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
                            // 使用当前Activity实例
                            val currentActivity = ActivityProvider.getMainActivity()
                            if (currentActivity != null) {
                                // 调用Google Play支付
                                Log.d(
                                    TAG, "Executing Google payment with current activity"
                                )
                                // 启动购买流程
                                billingManager.launchBillingFlow(currentActivity, productId)
                            } else {
                                Log.e(
                                    TAG, "Activity is null, cannot execute Google payment"
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
     * 恢复购买
     */
    fun restorePurchases() {
        if (_billingConnectionState.value != BillingConnectionState.CONNECTED) {
            _upgradeResult.value =
                UpgradeResult.Error(stringProvider.getString(R.string.payment_service_not_connected))
            return
        }

        billingManager.restorePurchases()
    }

    /**
     * 清除升级结果
     */
    fun clearUpgradeResult() {
        _upgradeResult.value = null
    }

    /**
     * 清除支付URL
     */
    fun clearPaymentUrl() {
        _paymentUrl.value = null
    }

    /**
     * 设置导航控制器
     */
    fun setNavController(controller: NavController) {
        navController = controller
    }

    /**
     * 打开隐私政策
     */
    fun openPrivacyPolicy() {
        openInWebView(PRIVACY_POLICY_URL, context.getString(R.string.privacy_policy))
    }

    /**
     * 打开服务条款
     */
    fun openTermsOfService() {
        openInWebView(TERMS_OF_SERVICE_URL, context.getString(R.string.terms_of_use))
    }

    /**
     * 在WebView中打开URL
     */
    private fun openInWebView(url: String, title: String) {
        try {
            navController?.let { nav ->
                // 使用 URLEncoder 对 URL 和标题进行编码
                val encodedUrl = URLEncoder.encode(url, "UTF-8")
                val encodedTitle = URLEncoder.encode(title, "UTF-8")
                val route = "webview?url=$encodedUrl"
                Log.d(TAG, "Navigating to WebView with route: $route")
                nav.navigate(route)
            } ?: run {
                Log.d(TAG, "NavController is null, opening URL in external browser: $url")
                openExternalUrl(url) // 如果没有导航控制器，则使用外部浏览器
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to WebView: $url, ${e.message}")
            openExternalUrl(url) // 如果导航失败，则使用外部浏览器
        }
    }

    /**
     * 在外部浏览器中打开URL
     */
    private fun openExternalUrl(url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening URL in external browser: $url, ${e.message}")
            false
        }
    }

    /**
     * 打开订阅管理页面
     * 跳转到 Google Play 商店的订阅管理页面
     * @param activity 当前 Activity
     */
    fun openSubscriptionManagementPage(activity: Activity?) {
        if (activity == null) {
            _upgradeResult.value =
                UpgradeResult.Error(stringProvider.getString(R.string.unknown_error))
            return
        }

        try {
            // 打开 Google Play 商店的订阅管理页面
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = "https://play.google.com/store/account/subscriptions".toUri()
                setPackage("com.android.vending") // Google Play 商店包名
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening subscription management page: ${e.message}")

            // 如果无法打开特定页面，则打开 Google Play 商店主页
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = "https://play.google.com/store/account".toUri()
                }
                activity.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "Error opening Google Play: ${e2.message}")
                _upgradeResult.value =
                    UpgradeResult.Error(stringProvider.getString(R.string.unknown_error))
            }
        }
    }
}

/**
 * 升级套餐
 */
enum class PremiumPlan(
    val titleResId: Int, val descriptionResId: Int, val discountResId: Int? = null
) {
    WEEKLY(R.string.weekly_plan, R.string.weekly_plan_description), MONTHLY(
        R.string.monthly_plan, R.string.monthly_plan_description
    ),

    QUARTERLY(
        R.string.quarterly_plan, R.string.quarterly_plan_description
    ),
    YEARLY(
        R.string.yearly_plan, R.string.yearly_plan_description, R.string.save_about
    ),
    LIFETIME(R.string.lifetime_plan, R.string.lifetime_plan_description)
}

/**
 * 升级结果
 */
sealed class UpgradeResult {
    data class Success(val message: String) : UpgradeResult()
    data class Error(val message: String) : UpgradeResult()
}
