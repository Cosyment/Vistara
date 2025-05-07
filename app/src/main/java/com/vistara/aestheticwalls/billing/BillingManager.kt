package com.vistara.aestheticwalls.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.DiamondTransactionType
import com.vistara.aestheticwalls.data.repository.DiamondRepository
import com.vistara.aestheticwalls.data.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 计费管理器
 * 负责处理Google Play Billing的集成
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val diamondRepository: DiamondRepository
) : PurchasesUpdatedListener, BillingClientStateListener {

    companion object {
        private const val TAG = "BillingManager"

        // 订阅SKU
        const val SUBSCRIPTION_WEEKLY = "vistara_sub_week"     // 周订阅
        const val SUBSCRIPTION_MONTHLY = "vistara_sub_month"   // 月订阅
        const val SUBSCRIPTION_QUARTERLY = "vistara_sub_quarter" // 季度订阅
//        const val SUBSCRIPTION_YEARLY = "vistara_premium_yearly"     // 年订阅

        // 一次性购买SKU
//        const val PREMIUM_LIFETIME = "vistara_premium_lifetime"     // 终身会员

        // 钻石商品SKU常量，方便引用
        const val DIAMOND_80 = "dm99"
        const val DIAMOND_140 = "dm249"
        const val DIAMOND_199 = "dm1999"
        const val DIAMOND_352 = "dm499"
        const val DIAMOND_500 = "dm499off"
        const val DIAMOND_705 = "dm999"
        const val DIAMOND_799 = "dm999off"
        const val DIAMOND_1411 = "dm1999"
        const val DIAMOND_3528 = "dm4999"
        const val DIAMOND_7058 = "dm9999"

        // 钻石商品映射表，包含SKU和对应的钻石数量
        val DIAMOND_PRODUCTS = mapOf(
            DIAMOND_80 to 80,
            DIAMOND_140 to 140,
            DIAMOND_199 to 199,
            DIAMOND_352 to 352,
            DIAMOND_500 to 500,
            DIAMOND_705 to 705,
            DIAMOND_799 to 799,
            DIAMOND_1411 to 1411,
            DIAMOND_3528 to 3528,
            DIAMOND_7058 to 7058
        )

        // 所有钻石商品SKU列表
        val DIAMOND_SKUS = DIAMOND_PRODUCTS.keys.toList()
    }

    // 计费客户端
    private val billingClient: BillingClient =
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases() // 启用待处理购买支持
            .build()

    // 连接状态
    private val _connectionState = MutableStateFlow(BillingConnectionState.DISCONNECTED)
    val connectionState: StateFlow<BillingConnectionState> = _connectionState.asStateFlow()

    // 商品详情
    private val _productDetails = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetails: StateFlow<Map<String, ProductDetails>> = _productDetails.asStateFlow()

    // 购买状态
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    // 已处理的购买记录，用于避免重复处理
    private val processedPurchases = mutableSetOf<String>()

    // 初始化
    init {
        // 从SharedPreferences加载已处理的购买记录
        loadProcessedPurchases()
        connectToPlayBilling()
    }

    /**
     * 从SharedPreferences加载已处理的购买记录
     */
    private fun loadProcessedPurchases() {
        val sharedPrefs = context.getSharedPreferences("billing_prefs", Context.MODE_PRIVATE)
        val processedPurchasesSet = sharedPrefs.getStringSet("processed_purchases", emptySet()) ?: emptySet()
        processedPurchases.addAll(processedPurchasesSet)
        Log.d(TAG, "Loaded ${processedPurchases.size} processed purchases from SharedPreferences")
    }

    /**
     * 保存已处理的购买记录到SharedPreferences
     */
    private fun saveProcessedPurchases() {
        val sharedPrefs = context.getSharedPreferences("billing_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putStringSet("processed_purchases", processedPurchases).apply()
        Log.d(TAG, "Saved ${processedPurchases.size} processed purchases to SharedPreferences")
    }

    /**
     * 连接到Google Play Billing
     */
    fun connectToPlayBilling() {
        if (_connectionState.value == BillingConnectionState.CONNECTING) {
            return
        }

        _connectionState.value = BillingConnectionState.CONNECTING
        billingClient.startConnection(this)
    }

    /**
     * 断开与Google Play Billing的连接
     */
    fun disconnectFromPlayBilling() {
        // 保存已处理的购买记录
        saveProcessedPurchases()
        billingClient.endConnection()
        _connectionState.value = BillingConnectionState.DISCONNECTED
    }

    /**
     * 查询商品详情
     */
    fun queryProductDetails() {
        if (_connectionState.value != BillingConnectionState.CONNECTED) {
            Log.e(TAG, "Billing client is not connected")
            return
        }

        // 查询订阅商品
        val subscriptionProductList = listOf(
            QueryProductDetailsParams.Product.newBuilder().setProductId(SUBSCRIPTION_WEEKLY)
                .setProductType(BillingClient.ProductType.SUBS).build(),
            QueryProductDetailsParams.Product.newBuilder().setProductId(SUBSCRIPTION_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS).build(),
            QueryProductDetailsParams.Product.newBuilder().setProductId(SUBSCRIPTION_QUARTERLY)
                .setProductType(BillingClient.ProductType.SUBS).build(),
//            QueryProductDetailsParams.Product.newBuilder()
//                .setProductId(SUBSCRIPTION_YEARLY)
//                .setProductType(BillingClient.ProductType.SUBS)
//                .build()
        )

        // 查询一次性购买商品
        val inappProductList = DIAMOND_SKUS.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        // 查询订阅商品详情
        val subscriptionParams =
            QueryProductDetailsParams.newBuilder().setProductList(subscriptionProductList).build()

        billingClient.queryProductDetailsAsync(subscriptionParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val productDetailsMap = _productDetails.value.toMutableMap()
                productDetailsList.forEach { productDetails ->
                    productDetailsMap[productDetails.productId] = productDetails
                }
                _productDetails.value = productDetailsMap
                Log.d(TAG, "Subscription product details: $productDetailsList")
            } else {
                Log.e(
                    TAG,
                    "Failed to query subscription product details: ${billingResult.debugMessage}"
                )
            }
        }

        // 查询一次性购买商品详情
        val inappParams =
            QueryProductDetailsParams.newBuilder().setProductList(inappProductList).build()

        billingClient.queryProductDetailsAsync(inappParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val productDetailsMap = _productDetails.value.toMutableMap()
                productDetailsList.forEach { productDetails ->
                    productDetailsMap[productDetails.productId] = productDetails
                }
                _productDetails.value = productDetailsMap
                Log.d(TAG, "Inapp product details: $productDetailsList")
            } else {
                Log.e(TAG, "Failed to query inapp product details: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * 查询购买历史
     */
    fun queryPurchases() {
        if (_connectionState.value != BillingConnectionState.CONNECTED) {
            Log.e(TAG, "Billing client is not connected")
            return
        }

        // 查询订阅购买历史
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchasesList)
            } else {
                Log.e(TAG, "Failed to query subscription purchases: ${billingResult.debugMessage}")
            }
        }

        // 查询一次性购买历史
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchasesList)
            } else {
                Log.e(TAG, "Failed to query inapp purchases: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * 处理购买
     */
    private fun processPurchases(purchases: List<Purchase>) {
        if (purchases.isEmpty()) {
            Log.d(TAG, "No purchases found")
            return
        }

        Log.d(TAG, "Processing ${purchases.size} purchases")

        // 处理每个购买
        for (purchase in purchases) {
            // 生成唯一的购买标识符，用于检查是否已处理过
            val purchaseKey = "${purchase.orderId}_${purchase.purchaseToken}"

            // 如果这个购买已经处理过，跳过
            if (processedPurchases.contains(purchaseKey)) {
                Log.d(TAG, "Purchase already processed: $purchaseKey")
                continue
            }

            when (purchase.purchaseState) {
                Purchase.PurchaseState.PURCHASED -> {
                    // 如果购买已完成但尚未确认，则确认购买
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase.purchaseToken)
                    }

                    // 处理购买的商品
                    val productIds = purchase.products
                    for (productId in productIds) {
                        when {
                            // 处理订阅商品
                            productId in listOf(
                                SUBSCRIPTION_WEEKLY, SUBSCRIPTION_MONTHLY, SUBSCRIPTION_QUARTERLY
                            ) -> {
                                // 更新用户的Premium状态
                                CoroutineScope(Dispatchers.IO).launch {
                                    userRepository.updatePremiumStatus(true)
                                }
                            }
                            // 处理钻石商品
                            productId.startsWith("dm") -> {
                                processDiamondPurchase(productId, purchase.purchaseToken)
                            }
                        }
                    }

                    // 将此购买标记为已处理
                    processedPurchases.add(purchaseKey)
                    // 保存已处理的购买记录
                    saveProcessedPurchases()
                    Log.d(TAG, "Marked purchase as processed: $purchaseKey")
                }

                Purchase.PurchaseState.PENDING -> {
                    // 处理待处理的购买
                    Log.d(TAG, "Purchase is pending: ${purchase.products}")
                    // 对于待处理的购买，我们只需记录它们，实际处理会在购买完成后进行
                }

                Purchase.PurchaseState.UNSPECIFIED_STATE -> {
                    Log.d(TAG, "Purchase state is unspecified: ${purchase.products}")
                }
            }
        }
    }

    /**
     * 处理钻石购买
     */
    private fun processDiamondPurchase(productId: String, purchaseToken: String? = null) {
        val diamondAmount = DIAMOND_PRODUCTS[productId] ?: 0

        if (diamondAmount > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                val success = diamondRepository.updateDiamondBalance(
                    amount = diamondAmount,
                    type = DiamondTransactionType.RECHARGE,
                    description = "购买${diamondAmount}钻石"
                )

                if (success) {
                    Log.d(TAG, "钻石充值成功: $diamondAmount")

                    // 如果提供了购买令牌，消耗这个购买，以便用户可以再次购买
                    if (purchaseToken != null) {
                        consumePurchase(purchaseToken)
                    }
                } else {
                    Log.e(TAG, "钻石充值失败: $diamondAmount")
                }
            }
        }
    }

    /**
     * 消耗购买
     * 对于一次性购买的商品（如钻石包），需要消耗购买，以便用户可以再次购买
     */
    private fun consumePurchase(purchaseToken: String) {
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        billingClient.consumeAsync(params) { billingResult, outToken ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase consumed successfully: $outToken")
            } else {
                Log.e(TAG, "Failed to consume purchase: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * 确认购买
     */
    private fun acknowledgePurchase(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build()

        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged")
            } else {
                Log.e(TAG, "Failed to acknowledge purchase: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * 启动购买流程
     */
    fun launchBillingFlow(activity: Activity?, productId: String) {
        if (activity == null) {
            Log.e(TAG, "Activity is null")
            _purchaseState.value = PurchaseState.Failed("Activity is null")
            return
        }

        if (_connectionState.value != BillingConnectionState.CONNECTED) {
            Log.e(TAG, "Billing client is not connected")
            _purchaseState.value = PurchaseState.Failed("Billing client is not connected")
            return
        }

        val productDetails = _productDetails.value[productId]
        if (productDetails == null) {
            Log.e(TAG, "Product details not found for $productId")
            _purchaseState.value = PurchaseState.Failed("Product details not found")
            return
        }

        _purchaseState.value = PurchaseState.Pending

        // 根据商品类型构建购买参数
        val productType = when {
            productId in listOf(SUBSCRIPTION_WEEKLY, SUBSCRIPTION_MONTHLY, SUBSCRIPTION_QUARTERLY) -> BillingClient.ProductType.SUBS
            productId in DIAMOND_SKUS -> BillingClient.ProductType.INAPP
            else -> {
                Log.e(TAG, "Unknown product ID: $productId")
                _purchaseState.value = PurchaseState.Failed("Unknown product ID")
                return
            }
        }

        // 构建购买参数
        val builder = BillingFlowParams.newBuilder()

        if (productType == BillingClient.ProductType.SUBS) {
            // 订阅商品
            val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            if (offerToken == null) {
                Log.e(TAG, "Offer token not found for $productId")
                _purchaseState.value = PurchaseState.Failed("Offer token not found")
                return
            }

            builder.setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails).setOfferToken(offerToken).build()
                )
            )
        } else {
            // 一次性购买商品
            builder.setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails).build()
                )
            )
        }

        // 启动购买流程
        val billingResult = billingClient.launchBillingFlow(activity, builder.build())

        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Failed to launch billing flow: ${billingResult.debugMessage}")
            _purchaseState.value = PurchaseState.Failed(billingResult.debugMessage)
        }
    }

    /**
     * 恢复购买
     */
    fun restorePurchases() {
        if (_connectionState.value != BillingConnectionState.CONNECTED) {
            Log.e(TAG, "Billing client is not connected")
            return
        }

        _purchaseState.value = PurchaseState.Restoring

        // 查询所有购买
        queryPurchases()
    }

    /**
     * 计费客户端连接状态回调
     */
    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _connectionState.value = BillingConnectionState.CONNECTED
            Log.d(TAG, "Billing client connected")

            // 连接成功后查询商品详情和购买历史
            queryProductDetails()
            queryPurchases()

            // 查询并处理待处理的购买
            handlePendingPurchases()
        } else {
            _connectionState.value = BillingConnectionState.DISCONNECTED
            Log.e(TAG, "Billing client setup failed: ${billingResult.debugMessage}")
        }
    }

    /**
     * 计费客户端连接断开回调
     */
    override fun onBillingServiceDisconnected() {
        _connectionState.value = BillingConnectionState.DISCONNECTED
        Log.d(TAG, "Billing service disconnected")

        // 尝试重新连接
        connectToPlayBilling()
    }

    /**
     * 购买更新回调
     */
    override fun onPurchasesUpdated(
        billingResult: BillingResult, purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            // 检查是否有待处理的购买
            var hasPendingPurchases = false
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                    hasPendingPurchases = true
                    Log.d(TAG, "Purchase is pending: ${purchase.products}")
                }
            }

            // 对于新的购买，我们需要清除已处理的购买记录集合，确保它们能被处理
            // 这是因为这个回调是用户刚刚完成的新购买
            Log.d(TAG, "New purchase detected, clearing processed purchases set for these new purchases")

            // 处理购买
            processPurchases(purchases)

            if (hasPendingPurchases) {
                _purchaseState.value = PurchaseState.Pending
                Log.d(TAG, "Purchase is pending, waiting for completion")
            } else {
                _purchaseState.value = PurchaseState.Completed
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // 用户取消
            _purchaseState.value = PurchaseState.Cancelled
            Log.d(TAG, "Purchase cancelled")
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            // 商品已拥有，查询购买历史
            Log.d(TAG, "Item already owned, querying purchases")
            queryPurchases()
            _purchaseState.value = PurchaseState.Completed
        } else {
            // 购买失败
            _purchaseState.value = PurchaseState.Failed(billingResult.debugMessage)
            Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
        }
    }

    /**
     * 获取商品价格
     */
    fun getProductPrice(productId: String): String {
        val productDetails =
            _productDetails.value[productId] ?: return context.getString(R.string.price_unknown)

        return when (productId) {
            SUBSCRIPTION_WEEKLY, SUBSCRIPTION_MONTHLY, SUBSCRIPTION_QUARTERLY -> {
                val offerDetails = productDetails.subscriptionOfferDetails?.firstOrNull()
                val pricingPhase = offerDetails?.pricingPhases?.pricingPhaseList?.firstOrNull()
                pricingPhase?.formattedPrice ?: context.getString(R.string.price_unknown)
            }

            in DIAMOND_SKUS -> {
                productDetails.oneTimePurchaseOfferDetails?.formattedPrice
                    ?: context.getString(R.string.price_unknown)
            }

            else -> context.getString(R.string.price_unknown)
        }
    }

    /**
     * 获取钻石商品的钻石数量
     */
    fun getDiamondAmount(productId: String): Int {
        return DIAMOND_PRODUCTS[productId] ?: 0
    }

    /**
     * 获取商品周期
     */
    fun getProductPeriod(productId: String): String {
        return when (productId) {
            SUBSCRIPTION_WEEKLY -> context.getString(R.string.subscription_weekly)
            SUBSCRIPTION_MONTHLY -> context.getString(R.string.subscription_monthly)
            SUBSCRIPTION_QUARTERLY -> context.getString(R.string.subscription_quarterly)
//            SUBSCRIPTION_YEARLY -> context.getString(R.string.subscription_yearly)
//            PREMIUM_LIFETIME -> context.getString(R.string.premium_lifetime)
            else -> ""
        }
    }

    /**
     * 处理待处理的购买
     * 这是为了解决"Pending purchases for one-time products must be supported"错误
     */
    private fun handlePendingPurchases() {
        if (_connectionState.value != BillingConnectionState.CONNECTED) {
            Log.e(TAG, "Billing client is not connected")
            return
        }

        // 查询一次性购买的待处理购买
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // 处理待处理的购买
                for (purchase in purchasesList) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                        Log.d(TAG, "Found pending purchase: ${purchase.products}")
                        // 对于待处理的购买，我们只需记录它们，实际处理会在购买完成后进行
                        // 这里不需要确认购买，因为它们还处于待处理状态
                    }
                }
            } else {
                Log.e(TAG, "Failed to query pending purchases: ${billingResult.debugMessage}")
            }
        }
    }
}

/**
 * 计费连接状态
 */
enum class BillingConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED
}

/**
 * 购买状态
 */
sealed class PurchaseState {
    object Idle : PurchaseState()
    object Pending : PurchaseState()
    object Completed : PurchaseState()
    object Cancelled : PurchaseState()
    object Restoring : PurchaseState()
    data class Failed(val message: String) : PurchaseState()
}
