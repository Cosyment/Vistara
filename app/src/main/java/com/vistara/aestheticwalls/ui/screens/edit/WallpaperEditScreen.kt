package com.vistara.aestheticwalls.ui.screens.edit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * 壁纸编辑屏幕
 * 提供基本的图片编辑功能，如亮度、对比度、饱和度调整和滤镜应用
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperEditScreen(
    onBackPressed: () -> Unit,
    onSaveComplete: () -> Unit,
    viewModel: WallpaperEditViewModel = hiltViewModel()
) {
    val wallpaperState by viewModel.wallpaperState.collectAsState()
    val editState by viewModel.editState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val context = LocalContext.current

    // 获取原始位图用于裁剪
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // 当壁纸加载成功时，异步加载原始位图
    LaunchedEffect(wallpaperState) {
        if (wallpaperState is UiState.Success) {
            val wallpaper = (wallpaperState as UiState.Success).data
            if (wallpaper.url != null) {
                withContext(Dispatchers.IO) {
                    try {
                        val url = URL(wallpaper.url)
                        originalBitmap = BitmapFactory.decodeStream(url.openStream())
                    } catch (e: Exception) {
                        // 处理加载错误
                    }
                }
            }
        }
    }

    // 当前选中的编辑工具
    var selectedTool by remember { mutableStateOf(EditTool.BRIGHTNESS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_wallpaper)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!isSaving) onBackPressed()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    // 保存按钮
                    IconButton(
                        onClick = {
                            viewModel.saveEditedWallpaper(onComplete = {
                                onSaveComplete()
                            })
                        },
                        enabled = !isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = stringResource(R.string.save)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (wallpaperState) {
                is UiState.Loading -> {
                    // 显示加载中
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is UiState.Error -> {
                    // 显示错误信息
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.loading_failed),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (wallpaperState as UiState.Error).message ?: stringResource(R.string.unknown_error),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackPressed) {
                            Text(stringResource(R.string.back))
                        }
                    }
                }

                is UiState.Success -> {
                    val wallpaper = (wallpaperState as UiState.Success).data

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // 图片预览区域
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black)
                        ) {
                            // 显示带有实时效果的图片
                            if (editState.croppedBitmap != null) {
                                // 如果有裁剪后的图片，显示裁剪后的图片
                                Image(
                                    bitmap = editState.croppedBitmap!!.asImageBitmap(),
                                    contentDescription = wallpaper.title,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize(),
                                    colorFilter = ColorFilter.colorMatrix(
                                        createPreviewColorMatrix(
                                            editState.brightness,
                                            editState.contrast,
                                            editState.saturation,
                                            editState.filter
                                        )
                                    )
                                )
                            } else {
                                // 否则显示原始图片
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(wallpaper.url)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = wallpaper.title,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize(),
                                    colorFilter = ColorFilter.colorMatrix(
                                        createPreviewColorMatrix(
                                            editState.brightness,
                                            editState.contrast,
                                            editState.saturation,
                                            editState.filter
                                        )
                                    )
                                )
                            }

                            // 保存中指示器
                            if (isSaving) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color.White)
                                }
                            }
                        }

                        // 编辑工具选择栏
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            EditToolButton(
                                tool = EditTool.BRIGHTNESS,
                                icon = Icons.Default.Star,
                                isSelected = selectedTool == EditTool.BRIGHTNESS,
                                onClick = { selectedTool = EditTool.BRIGHTNESS }
                            )
                            EditToolButton(
                                tool = EditTool.CONTRAST,
                                icon = Icons.Default.Star,
                                isSelected = selectedTool == EditTool.CONTRAST,
                                onClick = { selectedTool = EditTool.CONTRAST }
                            )
                            EditToolButton(
                                tool = EditTool.SATURATION,
                                icon = Icons.Default.Star,
                                isSelected = selectedTool == EditTool.SATURATION,
                                onClick = { selectedTool = EditTool.SATURATION }
                            )
                            EditToolButton(
                                tool = EditTool.FILTER,
                                icon = Icons.Default.Settings,
                                isSelected = selectedTool == EditTool.FILTER,
                                onClick = { selectedTool = EditTool.FILTER }
                            )
                            // 裁剪功能暂时隐藏
                            // EditToolButton(
                            //     tool = EditTool.CROP,
                            //     icon = Icons.Default.Edit,
                            //     isSelected = selectedTool == EditTool.CROP,
                            //     onClick = { selectedTool = EditTool.CROP }
                            // )
                        }

                        // 编辑控制区域
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            when (selectedTool) {
                                EditTool.BRIGHTNESS -> {
                                    SliderControl(
                                        label = stringResource(R.string.brightness),
                                        value = editState.brightness,
                                        onValueChange = { viewModel.updateBrightness(it) },
                                        valueRange = 0.5f..1.5f
                                    )
                                }

                                EditTool.CONTRAST -> {
                                    SliderControl(
                                        label = stringResource(R.string.contrast),
                                        value = editState.contrast,
                                        onValueChange = { viewModel.updateContrast(it) },
                                        valueRange = 0.5f..1.5f
                                    )
                                }

                                EditTool.SATURATION -> {
                                    SliderControl(
                                        label = stringResource(R.string.saturation),
                                        value = editState.saturation,
                                        onValueChange = { viewModel.updateSaturation(it) },
                                        valueRange = 0f..2f
                                    )
                                }

                                EditTool.FILTER -> {
                                    FilterOptions(
                                        selectedFilter = editState.filter,
                                        onFilterSelected = { viewModel.applyFilter(it) }
                                    )
                                }

                                EditTool.CROP -> {
                                    CropOptions(
                                        originalBitmap = originalBitmap,
                                        onCropComplete = { croppedBitmap ->
                                            viewModel.updateCroppedBitmap(croppedBitmap)
                                        }
                                    )
                                }
                            }
                        }

                        // 只保留重置按钮
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = {
                                    viewModel.resetEdits()
                                    // 如果当前在裁剪工具，切换到亮度工具
                                    if (selectedTool == EditTool.CROP) {
                                        selectedTool = EditTool.BRIGHTNESS
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.reset),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(stringResource(R.string.reset_edits))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 编辑工具按钮
 */
@Composable
fun EditToolButton(
    tool: EditTool,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                )
                .border(
                    width = 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(tool.titleRes),
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = stringResource(tool.titleRes),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 滑块控制组件
 */
@Composable
fun SliderControl(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = String.format("%.1f", value),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 滤镜选项组件
 */
@Composable
fun FilterOptions(
    selectedFilter: ImageFilter,
    onFilterSelected: (ImageFilter) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.filter),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 使用 LazyRow 来支持水平滚动
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ImageFilter.values()) { filter ->
                FilterOption(
                    filter = filter,
                    isSelected = filter == selectedFilter,
                    onClick = { onFilterSelected(filter) }
                )
            }
        }
    }
}

/**
 * 裁剪组件
 */
@Composable
fun CropOptions(
    originalBitmap: Bitmap?,
    onCropComplete: (Bitmap?) -> Unit
) {
    if (originalBitmap == null) {
        Text(
            text = stringResource(R.string.loading_image),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    var showCropView by remember { mutableStateOf(true) }
    var cropType by remember { mutableStateOf(CropType.FREE) }
    var cropRect by remember { mutableStateOf<Rect?>(null) }

    // 裁剪区域的初始值
    LaunchedEffect(originalBitmap, cropType) {
        val width = originalBitmap.width
        val height = originalBitmap.height

        cropRect = when (cropType) {
            CropType.FREE -> {
                // 默认裁剪区域为图片的中心80%
                val cropWidth = width * 0.8f
                val cropHeight = height * 0.8f
                val left = (width - cropWidth) / 2
                val top = (height - cropHeight) / 2
                Rect(left, top, left + cropWidth, top + cropHeight)
            }

            CropType.SQUARE -> {
                // 正方形裁剪区域
                val size = minOf(width, height) * 0.8f
                val left = (width - size) / 2
                val top = (height - size) / 2
                Rect(left, top, left + size, top + size)
            }

            CropType.CIRCLE -> {
                // 圆形裁剪区域（实际上也是正方形，只是显示为圆形）
                val size = minOf(width, height) * 0.8f
                val left = (width - size) / 2
                val top = (height - size) / 2
                Rect(left, top, left + size, top + size)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (showCropView) {
            // 裁剪视图
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(vertical = 8.dp)
                    .background(Color.Black)
            ) {
                // 显示原始图片
                Image(
                    bitmap = originalBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.original),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                // 裁剪框
                cropRect?.let { rect ->
                    // 计算裁剪框在UI上的位置
                    val imageWidth = originalBitmap.width.toFloat()
                    val imageHeight = originalBitmap.height.toFloat()
                    val boxWidth = 300.dp.toPx() // 这里需要转换，实际应用中应该使用布局信息
                    val boxHeight = 300.dp.toPx()

                    // 简化版：假设图片完全填充Box
                    val scaleX = boxWidth / imageWidth
                    val scaleY = boxHeight / imageHeight
                    val scale = minOf(scaleX, scaleY)

                    val uiRect = Rect(
                        rect.left * scale,
                        rect.top * scale,
                        rect.right * scale,
                        rect.bottom * scale
                    )

                    // 绘制裁剪框
                    Box(
                        modifier = Modifier
                            .absoluteOffset(uiRect.left.dp, uiRect.top.dp)
                            .size(
                                width = (uiRect.right - uiRect.left).dp,
                                height = (uiRect.bottom - uiRect.top).dp
                            )
                            .border(
                                width = 2.dp,
                                color = Color.White,
                                shape = if (cropType == CropType.CIRCLE) CircleShape else RoundedCornerShape(
                                    4.dp
                                )
                            )
                    )
                }
            }

            // 裁剪类型选择
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CropTypeButton(
                    title = stringResource(R.string.free_crop),
                    isSelected = cropType == CropType.FREE,
                    onClick = { cropType = CropType.FREE }
                )
                CropTypeButton(
                    title = stringResource(R.string.square),
                    isSelected = cropType == CropType.SQUARE,
                    onClick = { cropType = CropType.SQUARE }
                )
                CropTypeButton(
                    title = stringResource(R.string.circle),
                    isSelected = cropType == CropType.CIRCLE,
                    onClick = { cropType = CropType.CIRCLE }
                )
            }

            // 操作按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        // 取消裁剪
                        onCropComplete(null)
                        showCropView = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cancel),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(stringResource(R.string.cancel))
                }

                Button(
                    onClick = {
                        // 应用裁剪
                        cropRect?.let { rect ->
                            val x = rect.left.toInt().coerceAtLeast(0)
                            val y = rect.top.toInt().coerceAtLeast(0)
                            val width = (rect.right - rect.left).toInt()
                                .coerceAtMost(originalBitmap.width - x)
                            val height = (rect.bottom - rect.top).toInt()
                                .coerceAtMost(originalBitmap.height - y)

                            if (width > 0 && height > 0) {
                                // 创建裁剪后的位图
                                val cropped = Bitmap.createBitmap(
                                    originalBitmap,
                                    x, y, width, height
                                )

                                // 如果是圆形裁剪，创建圆形位图
                                val finalBitmap = if (cropType == CropType.CIRCLE) {
                                    createCircularBitmap(cropped)
                                } else {
                                    cropped
                                }

                                onCropComplete(finalBitmap)
                                showCropView = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.apply),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(stringResource(R.string.apply))
                }
            }
        } else {
            // 显示裁剪完成的提示
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.crop_applied),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // 重新裁剪按钮
            Button(
                onClick = { showCropView = true },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.recrop),
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(stringResource(R.string.recrop))
            }
        }
    }
}

/**
 * 创建圆形位图
 */
fun createCircularBitmap(bitmap: Bitmap): Bitmap {
    val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(output)
    val paint = android.graphics.Paint()
    val rect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)

    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = android.graphics.Color.WHITE

    canvas.drawCircle(
        bitmap.width / 2f,
        bitmap.height / 2f,
        minOf(bitmap.width, bitmap.height) / 2f,
        paint
    )

    paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, rect, rect, paint)

    return output
}

/**
 * 裁剪类型
 */
enum class CropType {
    FREE, SQUARE, CIRCLE
}

/**
 * 将 Dp 转换为像素
 */
@Composable
fun Dp.toPx(): Float {
    return with(LocalDensity.current) { this@toPx.toPx() }
}

/**
 * 裁剪类型按钮
 */
@Composable
fun CropTypeButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(
                    width = 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title.take(1),
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 单个滤镜选项
 */
@Composable
fun FilterOption(
    filter: ImageFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .width(80.dp) // 增加宽度，确保所有滤镜选项都能完全显示
            .clickable(onClick = onClick)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else Color.Transparent
            ),
            modifier = Modifier.size(64.dp)
        ) {
            // 这里应该显示应用了滤镜的缩略图
            // 简化处理，只显示颜色块
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(filter.previewColor)
            )
        }
        Text(
            text = stringResource(filter.titleRes),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * 编辑工具枚举
 */
enum class EditTool(val titleRes: Int) {
    BRIGHTNESS(R.string.brightness),
    CONTRAST(R.string.contrast),
    SATURATION(R.string.saturation),
    FILTER(R.string.filter),
    CROP(R.string.crop)
}

/**
 * 图片滤镜枚举
 */
enum class ImageFilter(val titleRes: Int, val previewColor: Color) {
    NONE(R.string.original, Color.White),
    GRAYSCALE(R.string.grayscale, Color.Gray),
    SEPIA(R.string.sepia, Color(0xFFD2B48C)),
    VINTAGE(R.string.vintage, Color(0xFFCDC9A5)),
    COLD(R.string.cold, Color(0xFF87CEFA)),
    WARM(R.string.warm, Color(0xFFFFB347))
}

/**
 * 创建预览用的颜色矩阵
 */
fun createPreviewColorMatrix(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    filter: ImageFilter
): ColorMatrix {
    // 在Compose中，我们需要使用不同的方法来应用效果

    // 创建一个基本的矩阵
    val matrix = ColorMatrix()

    // 应用亮度 - 使用缩放方法
    val brightnessScale = brightness
    val brightnessMatrix = ColorMatrix()
    brightnessMatrix.setToScale(brightnessScale, brightnessScale, brightnessScale, 1f)

    // 应用对比度 - 使用缩放方法
    val contrastMatrix = ColorMatrix()
    contrastMatrix.setToScale(contrast, contrast, contrast, 1f)

    // 应用饱和度
    val saturationMatrix = ColorMatrix()
    saturationMatrix.setToSaturation(saturation)

    // 应用滤镜
    val filterMatrix = when (filter) {
        ImageFilter.GRAYSCALE -> {
            val m = ColorMatrix()
            m.setToSaturation(0f)
            m
        }

        ImageFilter.SEPIA -> ColorMatrix().apply { setToScale(0.8f, 0.6f, 0.4f, 1f) }
        ImageFilter.VINTAGE -> ColorMatrix().apply { setToScale(0.7f, 0.7f, 0.5f, 1f) }
        ImageFilter.COLD -> ColorMatrix().apply { setToScale(0.6f, 0.8f, 1.2f, 1f) }
        ImageFilter.WARM -> ColorMatrix().apply { setToScale(1.2f, 0.8f, 0.6f, 1f) }
        ImageFilter.NONE -> null
    }

    // 将所有矩阵组合起来
    // 先应用亮度
    matrix.timesAssign(brightnessMatrix)
    // 然后应用对比度
    matrix.timesAssign(contrastMatrix)
    // 然后应用饱和度
    matrix.timesAssign(saturationMatrix)
    // 最后应用滤镜
    if (filterMatrix != null) {
        matrix.timesAssign(filterMatrix)
    }

    return matrix
}
