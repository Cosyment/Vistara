package com.vistara.aestheticwalls.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.vistara.aestheticwalls.data.model.DiamondAccount
import com.vistara.aestheticwalls.data.model.DiamondTransaction
import kotlinx.coroutines.flow.Flow

/**
 * 钻石数据访问对象接口
 * 处理所有与钻石相关的本地数据库操作
 */
@Dao
interface DiamondDao {
    // 钻石账户相关操作
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAccount(account: DiamondAccount)

    @Query("SELECT * FROM diamond_accounts WHERE userId = :userId")
    suspend fun getAccount(userId: String): DiamondAccount?

    @Query("SELECT * FROM diamond_accounts WHERE userId = :userId")
    fun getAccountFlow(userId: String): Flow<DiamondAccount?>

    // 钻石交易记录相关操作
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: DiamondTransaction)

    @Query("SELECT * FROM diamond_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllTransactions(userId: String): Flow<List<DiamondTransaction>>

    @Query("SELECT * FROM diamond_transactions WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentTransactions(userId: String, limit: Int): List<DiamondTransaction>

    @Query("SELECT * FROM diamond_transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: String): DiamondTransaction?

    // 更新钻石余额并添加交易记录
    @Transaction
    suspend fun updateBalanceAndAddTransaction(account: DiamondAccount, transaction: DiamondTransaction) {
        insertOrUpdateAccount(account)
        insertTransaction(transaction)
    }

    // 清除用户数据
    @Query("DELETE FROM diamond_accounts WHERE userId = :userId")
    suspend fun deleteAccount(userId: String)

    @Query("DELETE FROM diamond_transactions WHERE userId = :userId")
    suspend fun deleteAllTransactions(userId: String)
}
