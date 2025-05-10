package com.vistara.aestheticwalls.ui.screens.recharge

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.DiamondProduct
import com.vistara.aestheticwalls.data.model.DiamondTransaction
import com.vistara.aestheticwalls.data.model.DiamondTransactionType
import com.vistara.aestheticwalls.ui.icons.AppIcons
import com.vistara.aestheticwalls.ui.theme.VistaraTheme
import com.vistara.aestheticwalls.ui.theme.stringResource
import com.vistara.aestheticwalls.utils.ImageUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 钻石余额卡片
 */
@Composable
fun DiamondBalanceCard(diamondBalance: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF9F2BEE), // 紫色渐变起始色
                            Color(0xFF8545FF)  // 紫色渐变结束色
                        )
                    )
                )
        ) {
            // 背景圆形装饰
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .offset(x = (-20).dp, y = 50.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f), Color.Transparent
                            )
                        ), shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = 150.dp, y = (-30).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f), Color.Transparent
                            )
                        ), shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = 250.dp, y = 30.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f), Color.Transparent
                            )
                        ), shape = CircleShape
                    )
            )

            // 内容
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.available_diamonds),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = diamondBalance.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 钻石图标
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp, top = 20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier.size(90.dp)
                ) {
                    Image(
                        painter = painterResource(R.mipmap.ic_diamond3),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
        }
    }
}

/**
 * 首充奖励卡片
 */
@Composable
fun FirstRechargeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF569dfe), // 蓝色渐变起始色
                            Color(0xFFb286fe), // 蓝色渐变起始色
                            Color(0xFFc1c7fe)  // 紫色渐变结束色
                        )
                    )
                )
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
            ) {
                // 礼物图标
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.mipmap.ic_gift_package),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 文本内容
                Text(
                    text = stringResource(R.string.first_recharge_bonus),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.weight(1f))

                // 箭头图标
//                Icon(
//                    imageVector = Icons.Default.ArrowForward,
//                    contentDescription = null,
//                    tint = Color.White,
//                    modifier = Modifier.size(16.dp)
//                )
            }
        }
    }
}

/**
 * 钻石商品卡片
 */
@Composable
fun DiamondProductCard(
    index: Int, product: DiamondProduct, isSelected: Boolean, price: String, onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        Color(0xFF9F2BEE) // 紫色
    } else {
        Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = 2.dp, color = borderColor, shape = RoundedCornerShape(16.dp)
            ), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ), elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 钻石图标
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        ImageUtil.getDrawableByName("ic_diamond${if (index < 6) (index + 1) else 6}")
                            ?: R.mipmap.ic_diamond6
                    ), modifier = Modifier.size(40.dp), contentDescription = null
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 钻石数量和折扣信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(
                        R.string.diamond_purchase_description, product.diamondAmount
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                if (product.discount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFFF5252), shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.discount_percent, product.discount),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }


            // 价格
            Text(
                text = price,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .background(
                        color = Color(0xFF9F2AF8), shape = RoundedCornerShape(size = 16.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 7.dp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1
            )
        }
    }
}

/**
 * 交易历史内容
 */
@Composable
fun TransactionHistoryContent(
    transactions: List<DiamondTransaction>,
    diamondBalance: Int,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DiamondBalanceCard(diamondBalance)

        // 加载状态
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "加载交易记录中...", style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        // 错误提示
        else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "加载交易记录失败: $errorMessage",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        // 交易记录列表
        else if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_transaction_history),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)
            ) {
                items(transactions) { transaction ->
                    TransactionItem(transaction)

                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

/**
 * 交易记录项
 */
@Composable
fun TransactionItem(transaction: DiamondTransaction) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(transaction.timestamp))

    val isPositive = transaction.amount.toFloat() > 0
    val amountColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFE91E63)
    val amountPrefix = if (isPositive) "+" else ""

//    val typeText = when (transaction.type) {
//        DiamondTransactionType.RECHARGE -> stringResource(R.string.recharge)
//        DiamondTransactionType.PURCHASE -> stringResource(R.string.purchase)
//        DiamondTransactionType.REWARD -> stringResource(R.string.reward)
//        DiamondTransactionType.REFUND -> stringResource(R.string.refund)
//    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 类型和描述
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.recharge),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = transaction.remark ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = transaction.createTime ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        // 金额
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = AppIcons.Diamond,
                contentDescription = null,
                tint = amountColor,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "$amountPrefix${transaction.amount}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DiamondScreenComponentsPreview() {
    VistaraTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // 注意：预览中不会显示真实数据，因为没有提供真实的ViewModel
            // 这里只是UI预览
            TransactionHistoryContent(
                transactions = listOf(
                    DiamondTransaction(
                        id = "1",
                        userId = "1",
                        amount = "100",
                        type = DiamondTransactionType.RECHARGE,
                        description = "Recharge",
                        timestamp = System.currentTimeMillis()
                    ), DiamondTransaction(
                        id = "2",
                        userId = "1",
                        amount = "-50",
                        type = DiamondTransactionType.PURCHASE,
                        description = "Purchase Wallpaper",
                        timestamp = System.currentTimeMillis() - 1000000
                    )
                ), diamondBalance = 150
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DiamondBalanceCardPreview() {
    VistaraTheme {
        DiamondBalanceCard(100)
    }
}

