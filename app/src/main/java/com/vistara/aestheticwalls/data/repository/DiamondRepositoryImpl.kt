package com.vistara.aestheticwalls.data.repository

import android.text.format.DateUtils
import android.util.Log
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.billing.BillingManager
import com.vistara.aestheticwalls.data.local.DiamondDao
import com.vistara.aestheticwalls.data.model.DiamondAccount
import com.vistara.aestheticwalls.data.model.DiamondProduct
import com.vistara.aestheticwalls.data.model.DiamondTransaction
import com.vistara.aestheticwalls.data.model.DiamondTransactionType
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.remote.ApiSource
import com.vistara.aestheticwalls.data.remote.api.ApiService
import com.vistara.aestheticwalls.data.remote.api.CreateOrderRequest
import com.vistara.aestheticwalls.data.remote.api.CreateOrderResponse
import com.vistara.aestheticwalls.data.remote.api.PaymentMethod
import com.vistara.aestheticwalls.utils.StringProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * 钻石仓库实现类
 * 管理钻石相关的数据
 */
@Singleton
class DiamondRepositoryImpl @Inject constructor(
    private val diamondDao: DiamondDao,
    private val authRepository: AuthRepository,
    private val billingManagerProvider: Provider<BillingManager>,
    private val stringProvider: StringProvider,
    private val apiService: ApiService
) : DiamondRepository {

    // 延迟获取BillingManager实例
    private val billingManager: BillingManager
        get() = billingManagerProvider.get()

    companion object {
        private const val TAG = "DiamondRepositoryImpl"
        private const val DEFAULT_USER_ID = "default_user" // 未登录用户的默认ID
    }

    /**
     * 获取当前用户ID
     */
    private suspend fun getCurrentUserId(): String {
        return authRepository.userId.firstOrNull() ?: DEFAULT_USER_ID
    }

    /**
     * 获取用户钻石余额
     */
    override fun getDiamondBalance(): Flow<Int> {
        return authRepository.userId.map { userId ->
            val actualUserId = userId ?: DEFAULT_USER_ID
            val account = diamondDao.getAccount(actualUserId)
            account?.balance ?: 0
        }
    }

    /**
     * 获取用户钻石余额（非Flow）
     */
    override suspend fun getDiamondBalanceValue(): Int {
        val userId = getCurrentUserId()
        val account = diamondDao.getAccount(userId)
        return account?.balance ?: 0
    }

    /**
     * 更新钻石余额
     */
    override suspend fun updateDiamondBalance(
        amount: Int, type: DiamondTransactionType, description: String, relatedItemId: String?
    ): Boolean {
        try {
            val userId = getCurrentUserId()
            val currentAccount = diamondDao.getAccount(userId) ?: DiamondAccount(
                userId = userId, balance = 0
            )

            // 计算新余额
            val newBalance = currentAccount.balance + amount

            // 如果是消费，检查余额是否足够
            if (amount < 0 && newBalance < 0) {
                Log.e(
                    TAG,
                    "Insufficient diamond balance: current balance ${currentAccount.balance}, trying to consume ${amount}"
                )
                return false
            }

            // 创建新的账户对象
            val updatedAccount = currentAccount.copy(
                balance = newBalance, lastUpdated = System.currentTimeMillis()
            )

            // 创建交易记录
            val transaction = DiamondTransaction(
                userId = userId,
                amount = amount.toString(),
                type = type,
                description = description,
                relatedItemId = relatedItemId,
                timestamp = System.currentTimeMillis()
            )

            // 更新数据库
            diamondDao.updateBalanceAndAddTransaction(updatedAccount, transaction)
            // 使用字符串资源记录日志
            if (amount > 0) {
                Log.d(TAG, "Diamond recharge successful: $amount")
            } else {
                Log.d(TAG, "Diamond balance updated: $amount, new balance: $newBalance")
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update diamond balance", e)
            return false
        }
    }

    /**
     * 获取交易记录
     */
    override fun getTransactions(): Flow<List<DiamondTransaction>> {
        return authRepository.userId.map { userId ->
            val actualUserId = userId ?: DEFAULT_USER_ID
            diamondDao.getAllTransactions(actualUserId).firstOrNull() ?: emptyList()
        }
    }

    /**
     * 获取最近的交易记录
     */
    override suspend fun getRecentTransactions(limit: Int): List<DiamondTransaction> {
        val userId = getCurrentUserId()
        return diamondDao.getRecentTransactions(userId, limit)
    }

    /**
     * 获取本地钻石商品列表
     * 直接从Google Play获取钻石商品信息
     */
    override suspend fun getLocalDiamondProducts(): List<DiamondProduct> {
        val products = mutableListOf<DiamondProduct>()

        // 从BillingManager获取所有钻石商品SKU和对应的钻石数量
        BillingManager.DIAMOND_PRODUCTS.forEach { (productId, diamondAmount) ->
            // 获取商品价格
            val formattedPrice = billingManager.getProductPrice(productId)

            // 解析价格字符串，提取数值部分（移除货币符号等）
            // 注意：这里简化处理，实际应用中可能需要更复杂的解析逻辑
            val priceValue = try {
                formattedPrice.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
            } catch (e: Exception) {
                Log.e(TAG, "Price parsing failed: $formattedPrice", e)
                0.0
            }

            // 创建钻石商品对象
            val product = DiamondProduct(
                id = "diamond_$diamondAmount",
                name = stringProvider.getString(
                    R.string.diamond_purchase_description, diamondAmount
                ),
                diamondAmount = diamondAmount, price = priceValue,
                // 根据商品ID设置折扣
                discount = when {
                    productId.endsWith("off") -> 10 // 假设带"off"后缀的商品有10%折扣
                    diamondAmount >= 3000 -> 15     // 大额钻石包有15%折扣
                    diamondAmount >= 1000 -> 10     // 中额钻石包有10%折扣
                    diamondAmount >= 500 -> 5       // 小额钻石包有5%折扣
                    else -> 0
                },
                googlePlayProductId = productId,
                currency = "",
                itemName = "",
                payMethodId = 1,
                dollarPrice = "",
                productId = productId,
            )

            products.add(product)
        }

        // 按钻石数量排序
        return products.sortedBy { it.diamondAmount }
    }

    /**
     * 从API获取钻石商品列表
     * @return 成功返回商品列表，失败返回null
     */
    override suspend fun getRemoteDiamondProducts(): List<DiamondProduct>? {
        try {
            // 调用API获取商品数据
            val result = apiService.getProducts()

            // 处理API结果
            if (result.isSuccess) {

                // 记录API返回的商品数据
                result.data?.forEach { product ->
                    // 检查productId是否与BillingManager中的SKU匹配
                    if (product.productId != null) {
                        val isInSkuList = BillingManager.DIAMOND_SKUS.contains(product.productId)
                    }
                }

                // 将API返回的productId映射到googlePlayProductId
                val updatedProducts = result.data?.map { product ->
                    // 如果productId存在且在BillingManager.DIAMOND_SKUS中，则将其设置为googlePlayProductId
                    if (product.productId != null && BillingManager.DIAMOND_SKUS.contains(product.productId)) {
                        product
                    } else {
                        // 尝试根据钻石数量映射到Google Play商品ID
                        val googlePlayId = mapToGooglePlayProductId(product.diamondAmount)
                        if (googlePlayId != null) {
                            product.copy(productId = googlePlayId)
                        } else {
                            product
                        }
                    }
                }

                return updatedProducts?.sortedBy { it.diamondAmount }
            } else {
                Log.e(TAG, "API error:")
                return null
            }


        } catch (e: Exception) {
            // 发生异常
            Log.e(TAG, "Error loading products from API: ${e.message}", e)
            return null
        }
    }

    /**
     * 获取钻石商品列表（优先从API获取，失败则使用本地数据）
     */
    override suspend fun getDiamondProducts(): ApiResult<List<DiamondProduct>> {
        // 尝试从API获取商品数据
        val remoteProducts = getRemoteDiamondProducts()

        // 如果API获取成功，则返回API数据
        if (remoteProducts != null) {
            return ApiResult.Success(remoteProducts)
        }

        // 否则返回本地数据
        Log.d(TAG, "Using local diamond products")
        return ApiResult.Success(getLocalDiamondProducts())
    }

    /**
     * 从商品名称中提取钻石数量
     */
    private fun extractDiamondAmount(productName: String): Int {
        // 尝试从商品名称中提取数字
        val regex = "\\d+".toRegex()
        val matchResult = regex.find(productName)

        return matchResult?.value?.toIntOrNull() ?: 0
    }

    /**
     * 将钻石数量映射到Google Play商品ID
     */
    private fun mapToGooglePlayProductId(diamondAmount: Int): String? {
        return when (diamondAmount) {
            500 -> BillingManager.DIAMOND_500
            705 -> BillingManager.DIAMOND_705
            799 -> BillingManager.DIAMOND_799
            1411 -> BillingManager.DIAMOND_1411
            3528 -> BillingManager.DIAMOND_3528
            7058 -> BillingManager.DIAMOND_7058
            else -> null
        }
    }

    /**
     * 清除用户数据
     */
    override suspend fun clearUserData() {
        val userId = getCurrentUserId()
        diamondDao.deleteAccount(userId)
        diamondDao.deleteAllTransactions(userId)
        Log.d(TAG, "User diamond data cleared: $userId")
    }

    /**
     * 检查用户是否有足够的钻石
     */
    override suspend fun hasSufficientDiamonds(amount: Int): Boolean {
        val balance = getDiamondBalanceValue()
        return balance >= amount
    }

    /**
     * 消费钻石
     */
    override suspend fun consumeDiamonds(
        amount: Int, description: String, itemId: String?
    ): Boolean {
        if (amount <= 0) {
            Log.e(TAG, "Diamond amount must be positive: $amount")
            return false
        }

        // 检查余额是否足够
        if (!hasSufficientDiamonds(amount)) {
            Log.e(
                TAG,
                "Insufficient diamond balance: current balance ${getDiamondBalanceValue()}, trying to consume $amount"
            )
            return false
        }

        // 更新余额，消费为负数
        return updateDiamondBalance(
            amount = -amount,
            type = DiamondTransactionType.PURCHASE,
            description = description,
            relatedItemId = itemId
        )
    }

    /**
     * 获取支付方式列表
     */
    override suspend fun getPaymentMethods(itemName: String): ApiResult<List<PaymentMethod>> {
        return try {
            // 调用API获取支付方式
            val response = apiService.getPaymentMethods(itemName)

            if (response.isSuccess && response.data != null) {
                Log.d(TAG, "Payment methods loaded successfully: ${response.data.size} methods")
                ApiResult.Success(response.data)
            } else {
                Log.e(TAG, "Failed to load payment methods: ${response.msg}")
                ApiResult.Error(
                    code = response.code,
                    message = response.msg,
                    source = ApiSource.BACKEND
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading payment methods: ${e.message}", e)
            ApiResult.Error(
                code = null,
                message = e.message ?: "Unknown error",
                source = ApiSource.BACKEND
            )
        }
    }

    /**
     * 创建订单
     */
    override suspend fun createOrder(
        productId: String,
        paymentMethodId: String
    ): ApiResult<CreateOrderResponse> {
        return try {
            // 创建订单请求
            val request = CreateOrderRequest(
                priceId = productId,
                paymentMethodId = paymentMethodId
            )

            // 调用API创建订单
            val response = apiService.createOrder(request)

            if (response.isSuccess && response.data != null) {
                Log.d(TAG, "Order created successfully: ${response.data.id}")
                ApiResult.Success(response.data)
            } else {
                Log.e(TAG, "Failed to create order: ${response.msg}")
                ApiResult.Error(
                    code = response.code,
                    message = response.msg,
                    source = ApiSource.BACKEND
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating order: ${e.message}", e)
            ApiResult.Error(
                code = null,
                message = e.message ?: "Unknown error",
                source = ApiSource.BACKEND
            )
        }
    }

    /**
     * 检查订单状态
     */
    override suspend fun checkOrder(outTradeNo: String): ApiResult<String> {
        return try {
            // 调用API检查订单状态
            val response = apiService.checkOrder(outTradeNo)

            if (response.isSuccess) {
                Log.d(TAG, "Order check successful for order: $outTradeNo")
                ApiResult.Success(response.data ?: "")
            } else {
                Log.e(TAG, "Failed to check order $outTradeNo: ${response.msg}")
                ApiResult.Error(
                    code = response.code,
                    message = response.msg,
                    source = ApiSource.BACKEND
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking order $outTradeNo: ${e.message}", e)
            ApiResult.Error(
                code = null,
                message = e.message ?: "Unknown error",
                source = ApiSource.BACKEND
            )
        }
    }

    /**
     * 从API获取交易记录
     */
    override suspend fun getRemoteTransactions(): ApiResult<List<DiamondTransaction>> {
        return try {
            // 调用API获取交易记录
            val response = apiService.getOrders()

            if (response.isSuccess && response.rows != null) {
                Log.d(TAG, "Transactions loaded successfully from API: ${response.rows.size} transactions")

                // 将API返回的交易记录保存到本地数据库
                val userId = getCurrentUserId()
                response.rows.forEach { transaction ->
                    // 确保交易记录的userId是当前用户的ID
//                    val localTransaction = transaction.copy(userId = userId)
//                    diamondDao.insertTransaction(localTransaction)
                }

                ApiResult.Success(response.rows)
            } else {
                Log.e(TAG, "Failed to load transactions from API: ${response.msg}")
                ApiResult.Error(
                    code = response.code,
                    message = response.msg,
                    source = ApiSource.BACKEND
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading transactions from API: ${e.message}", e)
            ApiResult.Error(
                code = null,
                message = e.message ?: "Unknown error",
                source = ApiSource.BACKEND
            )
        }
    }
}
