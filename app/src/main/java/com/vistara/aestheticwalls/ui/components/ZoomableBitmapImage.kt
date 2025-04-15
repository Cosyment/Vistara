package com.vistara.aestheticwalls.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale

/**
 * 可缩放位图图片组件
 * 支持双指缩放和平移操作
 *
 * @param bitmap 位图对象
 * @param contentDescription 图片描述
 * @param modifier 修饰符
 * @param onTap 点击回调
 */
@Composable
fun ZoomableBitmapImage(
    bitmap: Bitmap,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {}
) {
    // 缩放和平移状态
    var targetScale by remember { mutableStateOf(1f) }
    var targetOffsetX by remember { mutableStateOf(0f) }
    var targetOffsetY by remember { mutableStateOf(0f) }

    // 定义动画规格 - 使用弹簧动画效果
    val springSpec: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    // 使用动画来平滑过渡缩放和平移值
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = springSpec,
        label = "scale"
    )

    val animatedOffsetX by animateFloatAsState(
        targetValue = targetOffsetX,
        animationSpec = springSpec,
        label = "offsetX"
    )

    val animatedOffsetY by animateFloatAsState(
        targetValue = targetOffsetY,
        animationSpec = springSpec,
        label = "offsetY"
    )

    // 重置缩放和平移
    fun resetZoom() {
        targetScale = 1f
        targetOffsetX = 0f
        targetOffsetY = 0f
    }

    // 放大到指定位置
    fun zoomIn(position: Offset? = null, targetZoom: Float = 2f) {
        targetScale = targetZoom

        // 如果提供了位置，则将该位置作为缩放中心
        // 这里的计算是为了使点击位置成为缩放中心
        position?.let { pos ->
            // 这里的计算需要根据实际情况调整
            // 简化版本不考虑位置
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = animatedScale,
                    scaleY = animatedScale,
                    translationX = animatedOffsetX,
                    translationY = animatedOffsetY
                )
                // 第一个pointerInput处理双指缩放和平移
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, gestureZoom, _ ->
                        // 处理缩放
                        targetScale = (targetScale * gestureZoom).coerceIn(1f, 3f)

                        // 处理平移 - 只有在放大状态下才允许平移
                        if (targetScale > 1f) {
                            val maxX = (targetScale - 1) * size.width / 2
                            val maxY = (targetScale - 1) * size.height / 2

                            targetOffsetX = (targetOffsetX + pan.x).coerceIn(-maxX, maxX)
                            targetOffsetY = (targetOffsetY + pan.y).coerceIn(-maxY, maxY)
                        } else {
                            // 如果缩放回到原始大小，重置偏移
                            targetOffsetX = 0f
                            targetOffsetY = 0f
                        }
                    }
                }
                // 第二个pointerInput处理单击和双击
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { tapOffset ->
                            // 双击时切换缩放状态
                            if (targetScale > 1f) {
                                // 如果已经放大，双击重置
                                resetZoom()
                            } else {
                                // 如果未放大，双击放大到指定值
                                zoomIn(tapOffset, 2.5f)  // 设置为中等缩放值
                            }
                        },
                        onTap = {
                            // 只有在非缩放状态下才触发单击事件
                            if (animatedScale <= 1.01f) { // 添加小的容差处理浮点数精度问题
                                onTap()
                            }
                        }
                    )
                }
        )
    }
}
