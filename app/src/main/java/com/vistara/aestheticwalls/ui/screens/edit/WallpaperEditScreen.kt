package com.vistara.aestheticwalls.ui.screens.edit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Done
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.Wallpaper
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

    // 当前选中的编辑工具
    var selectedTool by remember { mutableStateOf(EditTool.BRIGHTNESS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑壁纸") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!isSaving) onBackPressed()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
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
                            contentDescription = "保存"
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
                            text = "加载失败",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (wallpaperState as UiState.Error).message ?: "未知错误",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackPressed) {
                            Text("返回")
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
                                        label = "亮度",
                                        value = editState.brightness,
                                        onValueChange = { viewModel.updateBrightness(it) },
                                        valueRange = 0.5f..1.5f
                                    )
                                }
                                EditTool.CONTRAST -> {
                                    SliderControl(
                                        label = "对比度",
                                        value = editState.contrast,
                                        onValueChange = { viewModel.updateContrast(it) },
                                        valueRange = 0.5f..1.5f
                                    )
                                }
                                EditTool.SATURATION -> {
                                    SliderControl(
                                        label = "饱和度",
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
                                onClick = { viewModel.resetEdits() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "重置",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("重置编辑")
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                contentDescription = tool.title,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = tool.title,
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
            text = "滤镜",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ImageFilter.values().forEach { filter ->
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
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(
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
            text = filter.title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 编辑工具枚举
 */
enum class EditTool(val title: String) {
    BRIGHTNESS("亮度"),
    CONTRAST("对比度"),
    SATURATION("饱和度"),
    FILTER("滤镜")
}

/**
 * 图片滤镜枚举
 */
enum class ImageFilter(val title: String, val previewColor: Color) {
    NONE("原图", Color.White),
    GRAYSCALE("黑白", Color.Gray),
    SEPIA("复古", Color(0xFFD2B48C)),
    VINTAGE("怀旧", Color(0xFFCDC9A5)),
    COLD("冷色", Color(0xFF87CEFA)),
    WARM("暖色", Color(0xFFFFB347))
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
