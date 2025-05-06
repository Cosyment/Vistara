package com.vistara.aestheticwalls.ui.screens.diamond

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.DiamondProduct
import com.vistara.aestheticwalls.data.model.DiamondTransaction
import com.vistara.aestheticwalls.data.model.DiamondTransactionType
import com.vistara.aestheticwalls.ui.icons.AppIcons
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
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.my_diamond_balance),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = AppIcons.Diamond,
                    contentDescription = null,
                    tint = Color(0xFF00BCD4),
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = diamondBalance.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * 钻石商品卡片
 */
@Composable
fun DiamondProductCard(
    product: DiamondProduct, isSelected: Boolean, price: String, onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = 2.dp, color = borderColor, shape = RoundedCornerShape(12.dp)
            ), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 钻石图标和数量
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF81D4FA), Color(0xFF29B6F6)
                                )
                            )
                        ), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AppIcons.Diamond,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "${product.diamondAmount}${stringResource(R.string.diamond)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (product.discount > 0) {
                        Text(
                            text = stringResource(R.string.discount_percent, product.discount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // 价格
            Text(
                text = price,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 交易历史内容
 */
@Composable
fun TransactionHistoryContent(
    transactions: List<DiamondTransaction>, diamondBalance: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.my_diamond_balance),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = AppIcons.Diamond,
                        contentDescription = null,
                        tint = Color(0xFF00BCD4),
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = diamondBalance.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            // 余额显示
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(MaterialTheme.colorScheme.primaryContainer)
//                    .padding(16.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text(
//                        text = stringResource(R.string.current_balance),
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.onPrimaryContainer
//                    )
//
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier.padding(top = 4.dp)
//                    ) {
//                        Icon(
//                            imageVector = AppIcons.Diamond,
//                            contentDescription = null,
//                            tint = Color(0xFF00BCD4),
//                            modifier = Modifier.size(24.dp)
//                        )
//
//                        Spacer(modifier = Modifier.width(4.dp))
//
//                        Text(
//                            text = diamondBalance.toString(),
//                            style = MaterialTheme.typography.headlineMedium,
//                            fontWeight = FontWeight.Bold,
//                            color = MaterialTheme.colorScheme.onPrimaryContainer
//                        )
//                    }
//                }
//            }
        }

        // 交易记录列表
        if (transactions.isEmpty()) {
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

    val isPositive = transaction.amount > 0
    val amountColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFE91E63)
    val amountPrefix = if (isPositive) "+" else ""

    val typeText = when (transaction.type) {
        DiamondTransactionType.RECHARGE -> stringResource(R.string.recharge)
        DiamondTransactionType.PURCHASE -> stringResource(R.string.purchase)
        DiamondTransactionType.REWARD -> stringResource(R.string.reward)
        DiamondTransactionType.REFUND -> stringResource(R.string.refund)
    }

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
                text = typeText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = transaction.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = formattedDate,
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
