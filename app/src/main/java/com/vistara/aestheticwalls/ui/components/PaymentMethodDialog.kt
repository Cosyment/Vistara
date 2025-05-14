package com.vistara.aestheticwalls.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.remote.api.PaymentMethod
import com.vistara.aestheticwalls.ui.theme.VistaraTheme

/**
 * 支付方式选择弹框
 */
@Composable
fun PaymentMethodDialog(
    amount: String = "5600",
    paymentMethods: List<PaymentMethod> = emptyList(),
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onPaymentSelected: (paymentMethodId: String) -> Unit
) {
    // 控制动画状态
    var isVisible by remember { mutableStateOf(false) }

    // 用于控制关闭动画
    var isClosing by remember { mutableStateOf(false) }

    // 动画持续时间
    val animationDuration = 300

    // 计算偏移量，从底部滑入或滑出
    val offsetYPercentage by animateFloatAsState(
        targetValue = if (isVisible && !isClosing) 0f else 1f,
        animationSpec = tween(durationMillis = animationDuration),
        finishedListener = {
            // 当关闭动画完成后，真正关闭对话框
            if (isClosing) {
                onDismiss()
            }
        })

    // 启动时触发显示动画
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // 处理关闭逻辑
    val handleDismiss = {
        isClosing = true
        // 不立即关闭，等待动画完成
    }

    // 处理支付方式选择
    val handlePaymentSelected = { paymentMethod: PaymentMethod ->
        isClosing = true
        // 等待动画完成后再调用回调
        onPaymentSelected(paymentMethod.id)
    }

    Dialog(
        onDismissRequest = handleDismiss, properties = DialogProperties(
            usePlatformDefaultWidth = false, // 使对话框可以全宽
            dismissOnClickOutside = true, dismissOnBackPress = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .navigationBarsPadding()
                .wrapContentSize(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f) // 占据屏幕底部60%的高度
                    .offset(y = (offsetYPercentage * 1000).dp) // 根据动画状态计算偏移量
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF10082A), // 深紫色
                                        Color(0xFF100521)  // 更深的紫色
                                    )
                                )
                            )
                    ) {
                        // 顶部光效 - 使用遮罩组实现更好的光晕效果
                        Image(
                            painter = painterResource(R.mipmap.bg_popup),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.3f),
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                        ) {
                            // 顶部拖动条
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(3.dp)
                                        .background(
                                            color = Color.White, shape = RoundedCornerShape(1.5.dp)
                                        )
                                )
                            }

                            // 金额显示 - 使用紫色背景的胶囊形状
                            Box(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier.padding(
                                        horizontal = 24.dp, vertical = 12.dp
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.mipmap.ic_diamond1),
                                            contentDescription = stringResource(R.string.diamond_label),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = amount.replace("\n", " "),
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            // 国家选择
                            CountrySelector(
                                modifier = Modifier.padding(top = 20.dp)
                            )

                            // 优惠券选择
//                            CouponSelector(
//                                modifier = Modifier.padding(top = 20.dp)
//                            )

                            // 支付方式
                            Text(
                                text = stringResource(R.string.payment_methods),
                                color = Color(0xFF9F9CA6),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 20.dp)
                            )

                            // 支付方式列表
                            if (isLoading) {
                                // 显示加载指示器
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .padding(top = 5.dp), contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color.White
                                    )
                                }
                            } else if (paymentMethods.isEmpty()) {
                                // 显示空状态
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .padding(top = 5.dp), contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.no_payment_methods),
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                // 显示支付方式列表
                                PaymentMethodList(
                                    modifier = Modifier.padding(top = 5.dp),
                                    paymentMethods = paymentMethods,
                                    onPaymentSelected = handlePaymentSelected
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 国家选择器
 */
@Composable
private fun CountrySelector(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.country), color = Color(0xFF9F9CA6), fontSize = 12.sp
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF201730)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 国旗图标
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Transparent)
                ) {
                    // 印尼国旗 - 上红下白
                    Box(
                        modifier = Modifier
                            .size(width = 24.dp, height = 10.dp)
                            .background(Color(0xFFDC1F26))
                            .align(Alignment.TopCenter)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 24.dp, height = 10.dp)
                            .background(Color.White)
                            .align(Alignment.BottomCenter)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = stringResource(R.string.indonesia),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.weight(1f))

//                Icon(
//                    imageVector = Icons.Default.KeyboardArrowDown,
//                    contentDescription = "Select Country",
//                    tint = Color.White.copy(alpha = 0.4f),
//                    modifier = Modifier.size(24.dp)
//                )
            }
        }
    }
}

/**
 * 优惠券选择器
 */
@Composable
private fun CouponSelector(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.coupon), color = Color(0xFF9F9CA6), fontSize = 12.sp
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF201730)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.none),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 支付方式列表
 */
@Composable
private fun PaymentMethodList(
    modifier: Modifier = Modifier,
    paymentMethods: List<PaymentMethod> = emptyList(),
    onPaymentSelected: (PaymentMethod) -> Unit
) {
    var selectedPaymentMethod by remember {
        mutableStateOf<PaymentMethod?>(
            null
        )
    }

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        // 显示支付方式列表
        paymentMethods.forEachIndexed { index, paymentMethod ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            PaymentMethodItem(
                paymentMethod = paymentMethod,
                isSelected = selectedPaymentMethod?.id == paymentMethod.id,
                onClick = {
                    selectedPaymentMethod = paymentMethod
                    onPaymentSelected(paymentMethod)
                })
        }
    }
}

/**
 * 支付方式项
 */
@Composable
private fun PaymentMethodItem(
    paymentMethod: PaymentMethod, isSelected: Boolean, onClick: () -> Unit
) {
    // 根据支付方式ID获取对应的图标资源
    val iconRes = when (paymentMethod.payMethodId) {
        1 -> R.mipmap.ic_google
        else -> R.mipmap.ic_payermax // 默认图标
    }

    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = 1.dp, color = Color(0xFF746A88), shape = RoundedCornerShape(16.dp) // 更大的圆角
            ), shape = RoundedCornerShape(16.dp), // 更大的圆角
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 银行图标
//            Image(
//                painter = painterResource(id = iconRes),
//                contentDescription = paymentMethod.name,
//                modifier = Modifier.size(24.dp),
//                contentScale = ContentScale.Fit
//            )

            AsyncImage(
                model = ImageRequest.Builder(context).data(paymentMethod.imageUrl).crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = paymentMethod.payTypeMessage,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))

            // 支付按钮
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFF9F29F8), shape = RoundedCornerShape(20.dp) // 更大的圆角
                    )
                    .padding(horizontal = 20.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${paymentMethod.currency} ${paymentMethod.price}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentMethodDialogPreview() {
    VistaraTheme {
        PaymentMethodDialog(
            amount = "1 \n VIP Week",
            paymentMethods = listOf(
                PaymentMethod(
                    "maybank",
                    "Maybank",
                    "ic_maybank",
                    "123",
                    productId = "111",
                    payMethodId = 1,
                    currency = "$",
                    payTypeMessage = "TODO()"
                ), PaymentMethod(
                    "bsn",
                    "BSN",
                    "ic_bsn",
                    "123",
                    productId = "123",
                    payMethodId = 1,
                    currency = "$",
                    payTypeMessage = "TODO()"
                )
            ),
            isLoading = false,
            onDismiss = { /* Handle dismiss */ },
            onPaymentSelected = { /* Handle payment selection */ })
    }
}