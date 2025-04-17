package com.vistara.aestheticwalls.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 计费管理器
 * 负责处理Google Play Billing的集成
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository
) : PurchasesUpdatedListener, BillingClientStateListener {

    companion object {
        private const val TAG = "BillingManager"

        // 订阅SKU
        const val SUBSCRIPTION_WEEKLY = "vistara_premium_weekly"     // 周订阅
        const val SUBSCRIPTION_MONTHLY = "vistara_premium_monthly"   // 月订阅
        const val SUBSCRIPTION_QUARTERLY = "vistara_premium_quarterly" // 季度订阅
        const val SUBSCRIPTION_YEARLY = "vistara_premium_yearly"     // 年订阅

        // 一次性购买SKU
        const val PREMIUM_LIFETIME = "vistara_premium_lifetime"     // 终身会员
    }

    // 计费客户端
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
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

    // 初始化
    init {
        connectToPlayBilling()
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
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SUBSCRIPTION_WEEKLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SUBSCRIPTION_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SUBSCRIPTION_QUARTERLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SUBSCRIPTION_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        // 查询一次性购买商品
        val inappProductList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_LIFETIME)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        // 查询订阅商品详情
        val subscriptionParams = QueryProductDetailsParams.newBuilder()
            .setProductList(subscriptionProductList)
            .build()

        billingClient.queryProductDetailsAsync(subscriptionParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val productDetailsMap = _productDetails.value.toMutableMap()
                productDetailsList.forEach { productDetails ->
                    productDetailsMap[productDetails.productId] = productDetails
                }
                _productDetails.value = productDetailsMap
                Log.d(TAG, "Subscription product details: $productDetailsList")
            } else {
                Log.e(TAG, "Failed to query subscription product details: ${billingResult.debugMessage}")
            }
        }

        // 查询一次性购买商品详情
        val inappParams = QueryProductDetailsParams.newBuilder()
            .setProductList(inappProductList)
            .build()

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
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchasesList)
            } else {
                Log.e(TAG, "Failed to query subscription purchases: ${billingResult.debugMessage}")
            }
        }

        // 查询一次性购买历史
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
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
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                // 如果购买已完成但尚未确认，则确认购买
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase.purchaseToken)
                }

                // 更新用户的Premium状态
                CoroutineScope(Dispatchers.IO).launch {
                    userRepository.updatePremiumStatus(true)
                }
            }
        }
    }

    /**
     * 确认购买
     */
    private fun acknowledgePurchase(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

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
        val productType = when (productId) {
            SUBSCRIPTION_WEEKLY, SUBSCRIPTION_MONTHLY, SUBSCRIPTION_QUARTERLY, SUBSCRIPTION_YEARLY -> BillingClient.ProductType.SUBS
            PREMIUM_LIFETIME -> BillingClient.ProductType.INAPP
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
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            )
        } else {
            // 一次性购买商品
            builder.setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
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
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            // 处理购买
            processPurchases(purchases)
            _purchaseState.value = PurchaseState.Completed
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // 用户取消
            _purchaseState.value = PurchaseState.Cancelled
            Log.d(TAG, "Purchase cancelled")
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
        val productDetails = _productDetails.value[productId] ?: return context.getString(R.string.price_unknown)

        return when (productId) {
            SUBSCRIPTION_MONTHLY, SUBSCRIPTION_YEARLY -> {
                val offerDetails = productDetails.subscriptionOfferDetails?.firstOrNull()
                val pricingPhase = offerDetails?.pricingPhases?.pricingPhaseList?.firstOrNull()
                pricingPhase?.formattedPrice ?: context.getString(R.string.price_unknown)
            }
            PREMIUM_LIFETIME -> {
                productDetails.oneTimePurchaseOfferDetails?.formattedPrice ?: context.getString(R.string.price_unknown)
            }
            else -> context.getString(R.string.price_unknown)
        }
    }

    /**
     * 获取商品周期
     */
    fun getProductPeriod(productId: String): String {
        return when (productId) {
            SUBSCRIPTION_WEEKLY -> context.getString(R.string.subscription_weekly)
            SUBSCRIPTION_MONTHLY -> context.getString(R.string.subscription_monthly)
            SUBSCRIPTION_QUARTERLY -> context.getString(R.string.subscription_quarterly)
            SUBSCRIPTION_YEARLY -> context.getString(R.string.subscription_yearly)
            PREMIUM_LIFETIME -> context.getString(R.string.premium_lifetime)
            else -> ""
        }
    }
}

/**
 * 计费连接状态
 */
enum class BillingConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
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
