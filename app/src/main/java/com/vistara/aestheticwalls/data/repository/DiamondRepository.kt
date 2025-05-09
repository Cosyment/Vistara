package com.vistara.aestheticwalls.data.repository

import com.vistara.aestheticwalls.data.model.DiamondProduct
import com.vistara.aestheticwalls.data.model.DiamondTransaction
import com.vistara.aestheticwalls.data.model.DiamondTransactionType
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.remote.api.CreateOrderResponse
import com.vistara.aestheticwalls.data.remote.api.PaymentMethod
import kotlinx.coroutines.flow.Flow

/**
 * 钻石仓库接口
 * 负责管理钻石相关的数据
 */
interface DiamondRepository {
    /**
     * 获取用户钻石余额
     */
    fun getDiamondBalance(): Flow<Int>

    /**
     * 获取用户钻石余额（非Flow）
     */
    suspend fun getDiamondBalanceValue(): Int

    /**
     * 更新钻石余额
     * @param amount 变动金额，正数为增加，负数为减少
     * @param type 交易类型
     * @param description 交易描述
     * @param relatedItemId 关联的项目ID，如壁纸ID
     * @return 是否更新成功
     */
    suspend fun updateDiamondBalance(
        amount: Int,
        type: DiamondTransactionType,
        description: String,
        relatedItemId: String? = null
    ): Boolean

    /**
     * 获取交易记录
     */
    fun getTransactions(): Flow<List<DiamondTransaction>>

    /**
     * 获取最近的交易记录
     * @param limit 限制数量
     */
    suspend fun getRecentTransactions(limit: Int): List<DiamondTransaction>

    /**
     * 获取本地钻石商品列表
     */
    suspend fun getLocalDiamondProducts(): List<DiamondProduct>

    /**
     * 从API获取钻石商品列表
     * @return 成功返回商品列表，失败返回null
     */
    suspend fun getRemoteDiamondProducts(): List<DiamondProduct>?

    /**
     * 获取钻石商品列表（优先从API获取，失败则使用本地数据）
     */
    suspend fun getDiamondProducts(): ApiResult<List<DiamondProduct>>

    /**
     * 清除用户数据
     */
    suspend fun clearUserData()

    /**
     * 检查用户是否有足够的钻石
     * @param amount 需要的钻石数量
     */
    suspend fun hasSufficientDiamonds(amount: Int): Boolean

    /**
     * 消费钻石
     * @param amount 消费数量
     * @param description 消费描述
     * @param itemId 关联的项目ID
     * @return 是否消费成功
     */
    suspend fun consumeDiamonds(amount: Int, description: String, itemId: String? = null): Boolean

    /**
     * 获取支付方式列表
     * @return 支付方式列表的API结果
     */
    suspend fun getPaymentMethods(itemName: String): ApiResult<List<PaymentMethod>>

    /**
     * 创建订单
     * @param productId 商品ID
     * @param paymentMethodId 支付方式ID
     * @return 创建订单的API结果
     */
    suspend fun createOrder(productId: String, paymentMethodId: String): ApiResult<CreateOrderResponse>
}
