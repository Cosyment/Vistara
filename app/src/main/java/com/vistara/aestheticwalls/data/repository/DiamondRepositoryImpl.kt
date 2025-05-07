package com.vistara.aestheticwalls.data.repository

import android.util.Log
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.billing.BillingManager
import com.vistara.aestheticwalls.data.local.DiamondDao
import com.vistara.aestheticwalls.data.model.DiamondAccount
import com.vistara.aestheticwalls.data.model.DiamondProduct
import com.vistara.aestheticwalls.data.model.DiamondTransaction
import com.vistara.aestheticwalls.data.model.DiamondTransactionType
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
    private val stringProvider: StringProvider
) : DiamondRepository {

    // 延迟获取BillingManager实例
    private val billingManager: BillingManager
        get() = billingManagerProvider.get()

    companion object {
        private const val TAG = "DiamondRepository"
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
        amount: Int,
        type: DiamondTransactionType,
        description: String,
        relatedItemId: String?
    ): Boolean {
        try {
            val userId = getCurrentUserId()
            val currentAccount = diamondDao.getAccount(userId) ?: DiamondAccount(
                userId = userId,
                balance = 0
            )

            // 计算新余额
            val newBalance = currentAccount.balance + amount

            // 如果是消费，检查余额是否足够
            if (amount < 0 && newBalance < 0) {
                Log.e(TAG, "Insufficient diamond balance: current balance ${currentAccount.balance}, trying to consume ${amount}")
                return false
            }

            // 创建新的账户对象
            val updatedAccount = currentAccount.copy(
                balance = newBalance,
                lastUpdated = System.currentTimeMillis()
            )

            // 创建交易记录
            val transaction = DiamondTransaction(
                userId = userId,
                amount = amount,
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
     * 获取钻石商品列表
     * 直接从Google Play获取钻石商品信息
     */
    override suspend fun getDiamondProducts(): List<DiamondProduct> {
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
                name = stringProvider.getString(R.string.diamond_purchase_description, diamondAmount),
                diamondAmount = diamondAmount,
                price = priceValue,
                // 根据商品ID设置折扣
                discount = when {
                    productId.endsWith("off") -> 10 // 假设带"off"后缀的商品有10%折扣
                    diamondAmount >= 3000 -> 15     // 大额钻石包有15%折扣
                    diamondAmount >= 1000 -> 10     // 中额钻石包有10%折扣
                    diamondAmount >= 500 -> 5       // 小额钻石包有5%折扣
                    else -> 0
                },
                googlePlayProductId = productId
            )

            products.add(product)
        }

        // 按钻石数量排序
        return products.sortedBy { it.diamondAmount }
    }

    /**
     * 清除用户数据
     */
    override suspend fun clearUserData() {
        val userId = getCurrentUserId()
        diamondDao.deleteAccount(userId)
        diamondDao.deleteAllTransactions(userId)
        Log.d(TAG, "User diamond data cleared: ${userId}")
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
    override suspend fun consumeDiamonds(amount: Int, description: String, itemId: String?): Boolean {
        if (amount <= 0) {
            Log.e(TAG, "Diamond amount must be positive: $amount")
            return false
        }

        // 检查余额是否足够
        if (!hasSufficientDiamonds(amount)) {
            Log.e(TAG, "Insufficient diamond balance: current balance ${getDiamondBalanceValue()}, trying to consume $amount")
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
}
