package com.vistara.aestheticwalls.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.UUID

/**
 * 钻石账户数据模型
 * 记录用户的钻石余额
 */
@Entity(tableName = "diamond_accounts")
data class DiamondAccount(
    @PrimaryKey val userId: String,
    val balance: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * 钻石交易类型
 */
enum class DiamondTransactionType {
    RECHARGE,  // 充值
    PURCHASE,  // 购买壁纸等
    REWARD,    // 奖励
    REFUND     // 退款
}

/**
 * 钻石交易记录数据模型
 * 记录钻石的充值和消费
 */
@Entity(tableName = "diamond_transactions")
data class DiamondTransaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val amount: Int,  // 正数表示充值，负数表示消费
    val type: DiamondTransactionType,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val relatedItemId: String? = null  // 关联的壁纸ID或其他项目ID
)

/**
 * 钻石商品数据模型
 * 表示可购买的钻石套餐
 */
data class DiamondProduct(
    val id: String,
    val name: String,
    @SerializedName("num") val diamondAmount: Int,
    val price: Double,
    val priceType: String? = null,
    val currency: String = "CNY",
    val itemName: String = "",
    val payMethodId: Int = 0,
    val dollarPrice: String? = null,
    val productId: String? = null,
    val discount: Int = 0,  // 折扣百分比
    val googlePlayProductId: String? = null // Google Play商品ID
)

/**
 * 钻石价格数据模型
 * 表示壁纸等内容的钻石价格
 */
data class DiamondPrice(
    val itemId: String, val price: Int, val originalPrice: Int? = null, // 原价，用于显示折扣
    val isDiscounted: Boolean = false
)
