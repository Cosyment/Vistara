package com.vistara.aestheticwalls.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.CachePolicy
import coil.request.ImageRequest.Builder
import coil.request.ImageRequest
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.ui.theme.stringResource

/**
 * 可缩放图片组件
 * 支持双指缩放和平移操作
 *
 * @param imageUrl 图片URL
 * @param contentDescription 图片描述
 * @param modifier 修饰符
 * @param onTap 点击回调
 */
@Composable
fun ZoomableImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {}
) {
    val context = LocalContext.current

    // 缩放和平移状态
    var targetScale by remember { mutableStateOf(1f) }
    var targetOffsetX by remember { mutableStateOf(0f) }
    var targetOffsetY by remember { mutableStateOf(0f) }

    // 定义动画规格 - 使用弹簧动画效果
    val springSpec: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
    )

    // 使用动画来平滑过渡缩放和平移值
    val animatedScale by animateFloatAsState(
        targetValue = targetScale, animationSpec = springSpec, label = "scale"
    )

    val animatedOffsetX by animateFloatAsState(
        targetValue = targetOffsetX, animationSpec = springSpec, label = "offsetX"
    )

    val animatedOffsetY by animateFloatAsState(
        targetValue = targetOffsetY, animationSpec = springSpec, label = "offsetY"
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
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .placeholder(R.drawable.placeholder_image)
                .allowHardware(true)  // 启用硬件加速
                .allowRgb565(true)   // 允许使用RGB565格式，减少内存使用
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
            loading = {
                // 显示加载动画
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // 简单的加载动画，不显示具体进度
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color(0xFF3F51B5)
                    )
                }
            },
            error = {
                // 加载失败时显示的占位内容
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.load_wallpaper_failed), color = Color.Red)
                }
            },
            success = {
                // 图片加载成功，显示可缩放的图片
                Box(
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
                        detectTapGestures(onDoubleTap = { tapOffset ->
                            // 双击时切换缩放状态
                            if (targetScale > 1f) {
                                // 如果已经放大，双击重置
                                resetZoom()
                            } else {
                                // 如果未放大，双击放大到指定值
                                zoomIn(tapOffset, 2.5f)  // 设置为中等缩放值
                            }
                        }, onTap = {
                            // 只有在非缩放状态下才触发单击事件
                            if (animatedScale <= 1.01f) { // 添加小的容差处理浮点数精度问题
                                onTap()
                            }
                        })
                    }, contentAlignment = Alignment.Center
                ) {
                    // 使用this作为明确的接收者
                    this@SubcomposeAsyncImage.SubcomposeAsyncImageContent()
                }
            })
    }
}
