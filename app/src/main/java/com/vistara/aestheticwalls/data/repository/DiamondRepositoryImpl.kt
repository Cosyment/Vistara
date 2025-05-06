package com.vistara.aestheticwalls.data.repository

import android.util.Log
import com.vistara.aestheticwalls.data.local.DiamondDao
import com.vistara.aestheticwalls.data.model.DiamondAccount
import com.vistara.aestheticwalls.data.model.DiamondProduct
import com.vistara.aestheticwalls.data.model.DiamondTransaction
import com.vistara.aestheticwalls.data.model.DiamondTransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 钻石仓库实现类
 * 管理钻石相关的数据
 */
@Singleton
class DiamondRepositoryImpl @Inject constructor(
    private val diamondDao: DiamondDao,
    private val authRepository: AuthRepository
) : DiamondRepository {

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
                Log.e(TAG, "钻石余额不足: 当前余额 ${currentAccount.balance}, 尝试消费 ${-amount}")
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
            Log.d(TAG, "钻石余额更新成功: $amount, 新余额: $newBalance")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "更新钻石余额失败", e)
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
     * 这里返回预定义的商品列表，实际应用中可能从服务器获取
     */
    override suspend fun getDiamondProducts(): List<DiamondProduct> {
        return listOf(
            DiamondProduct(
                id = "diamond_60",
                name = "60钻石",
                diamondAmount = 60,
                price = 6.0,
                googlePlayProductId = "vistara_diamond_60"
            ),
            DiamondProduct(
                id = "diamond_300",
                name = "300钻石",
                diamondAmount = 300,
                price = 30.0,
                discount = 0,
                googlePlayProductId = "vistara_diamond_300"
            ),
            DiamondProduct(
                id = "diamond_980",
                name = "980钻石",
                diamondAmount = 980,
                price = 98.0,
                discount = 5,
                googlePlayProductId = "vistara_diamond_980"
            ),
            DiamondProduct(
                id = "diamond_1980",
                name = "1980钻石",
                diamondAmount = 1980,
                price = 198.0,
                discount = 10,
                googlePlayProductId = "vistara_diamond_1980"
            ),
            DiamondProduct(
                id = "diamond_3280",
                name = "3280钻石",
                diamondAmount = 3280,
                price = 328.0,
                discount = 15,
                googlePlayProductId = "vistara_diamond_3280"
            )
        )
    }

    /**
     * 清除用户数据
     */
    override suspend fun clearUserData() {
        val userId = getCurrentUserId()
        diamondDao.deleteAccount(userId)
        diamondDao.deleteAllTransactions(userId)
        Log.d(TAG, "用户钻石数据已清除: $userId")
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
            Log.e(TAG, "消费金额必须为正数: $amount")
            return false
        }

        // 检查余额是否足够
        if (!hasSufficientDiamonds(amount)) {
            Log.e(TAG, "钻石余额不足: 当前余额 ${getDiamondBalanceValue()}, 尝试消费 $amount")
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
