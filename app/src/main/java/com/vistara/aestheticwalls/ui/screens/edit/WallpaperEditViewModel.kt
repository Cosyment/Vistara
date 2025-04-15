package com.vistara.aestheticwalls.ui.screens.edit

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.data.EditedImageCache
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 壁纸编辑状态数据类
 */
data class WallpaperEditState(
    val brightness: Float = 1.0f,
    val contrast: Float = 1.0f,
    val saturation: Float = 1.0f,
    val filter: ImageFilter = ImageFilter.NONE
)

/**
 * 壁纸编辑ViewModel
 */
@HiltViewModel
class WallpaperEditViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 壁纸ID
    private val wallpaperId: String = checkNotNull(savedStateHandle["wallpaperId"])

    // 壁纸详情状态
    private val _wallpaperState = MutableStateFlow<UiState<Wallpaper>>(UiState.Loading)
    val wallpaperState: StateFlow<UiState<Wallpaper>> = _wallpaperState.asStateFlow()

    // 编辑状态
    private val _editState = MutableStateFlow(WallpaperEditState())
    val editState: StateFlow<WallpaperEditState> = _editState.asStateFlow()

    // 原始位图
    private var originalBitmap: Bitmap? = null

    // 保存状态
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        loadWallpaper()
    }

    /**
     * 加载壁纸详情
     */
    private fun loadWallpaper() {
        viewModelScope.launch {
            _wallpaperState.value = UiState.Loading

            try {
                val wallpaper = wallpaperRepository.getWallpaperById(wallpaperId)
                if (wallpaper != null) {
                    _wallpaperState.value = UiState.Success(wallpaper)

                    // 加载原始位图
                    loadOriginalBitmap(wallpaper.url)
                } else {
                    _wallpaperState.value = UiState.Error("壁纸不存在")
                }
            } catch (e: Exception) {
                _wallpaperState.value = UiState.Error(e.message ?: "加载壁纸失败")
            }
        }
    }

    /**
     * 加载原始位图
     */
    private fun loadOriginalBitmap(url: String?) {
        if (url == null) return

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val imageUrl = URL(url)
                    originalBitmap = BitmapFactory.decodeStream(imageUrl.openStream())
                }
            } catch (e: Exception) {
                _wallpaperState.value = UiState.Error("加载图片失败: ${e.message}")
            }
        }
    }

    /**
     * 更新亮度
     */
    fun updateBrightness(value: Float) {
        _editState.value = _editState.value.copy(brightness = value)
    }

    /**
     * 更新对比度
     */
    fun updateContrast(value: Float) {
        _editState.value = _editState.value.copy(contrast = value)
    }

    /**
     * 更新饱和度
     */
    fun updateSaturation(value: Float) {
        _editState.value = _editState.value.copy(saturation = value)
    }

    /**
     * 应用滤镜
     */
    fun applyFilter(filter: ImageFilter) {
        _editState.value = _editState.value.copy(filter = filter)
    }

    /**
     * 重置编辑
     */
    fun resetEdits() {
        _editState.value = WallpaperEditState()
    }

    /**
     * 应用编辑 - 现在不需要这个方法了，因为我们实时显示效果
     */
    fun applyEdits() {
        // 不再需要单独应用编辑，因为效果已经实时显示
    }

    /**
     * 保存编辑后的壁纸
     */
    fun saveEditedWallpaper(onComplete: () -> Unit) {
        val bitmap = originalBitmap ?: return

        viewModelScope.launch {
            _isSaving.value = true

            try {
                // 应用当前的编辑效果
                val finalBitmap = withContext(Dispatchers.Default) {
                    applyEffectsToBitmap(
                        bitmap,
                        _editState.value.brightness,
                        _editState.value.contrast,
                        _editState.value.saturation,
                        _editState.value.filter
                    )
                }

                // 保存到相册
                saveToGallery(finalBitmap!!)

                // 将编辑后的图片保存到缓存
                EditedImageCache.saveEditedImage(wallpaperId, finalBitmap)

                // 完成回调
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                // 处理错误
            } finally {
                _isSaving.value = false
            }
        }
    }

    /**
     * 将位图保存到相册
     */
    private suspend fun saveToGallery(bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Vistara_Edited_$timestamp.jpg"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                    }
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, fileName)
                FileOutputStream(image).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                }
            }
        }
    }

    /**
     * 应用效果到位图
     */
    private fun applyEffectsToBitmap(
        bitmap: Bitmap,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        filter: ImageFilter
    ): Bitmap {
        // 创建一个新的位图，避免修改原始位图
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()

        // 创建颜色矩阵
        val colorMatrix = ColorMatrix()

        // 应用亮度
        val brightnessMatrix = ColorMatrix().apply {
            val scale = brightness
            val matrix = floatArrayOf(
                scale, 0f, 0f, 0f, 0f,
                0f, scale, 0f, 0f, 0f,
                0f, 0f, scale, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            set(matrix)
        }
        colorMatrix.postConcat(brightnessMatrix)

        // 应用对比度
        val contrastMatrix = ColorMatrix().apply {
            val scale = contrast
            val translate = (1.0f - scale) * 128f
            val matrix = floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
            set(matrix)
        }
        colorMatrix.postConcat(contrastMatrix)

        // 应用饱和度
        val saturationMatrix = ColorMatrix().apply {
            setSaturation(saturation)
        }
        colorMatrix.postConcat(saturationMatrix)

        // 应用滤镜
        when (filter) {
            ImageFilter.GRAYSCALE -> {
                val grayscaleMatrix = ColorMatrix().apply {
                    setSaturation(0f)
                }
                colorMatrix.postConcat(grayscaleMatrix)
            }
            ImageFilter.SEPIA -> {
                val sepiaMatrix = ColorMatrix().apply {
                    val matrix = floatArrayOf(
                        0.393f, 0.769f, 0.189f, 0f, 0f,
                        0.349f, 0.686f, 0.168f, 0f, 0f,
                        0.272f, 0.534f, 0.131f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                    set(matrix)
                }
                colorMatrix.postConcat(sepiaMatrix)
            }
            ImageFilter.VINTAGE -> {
                val vintageMatrix = ColorMatrix().apply {
                    val matrix = floatArrayOf(
                        0.9f, 0.5f, 0.1f, 0f, 0f,
                        0.3f, 0.8f, 0.1f, 0f, 0f,
                        0.2f, 0.3f, 0.5f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                    set(matrix)
                }
                colorMatrix.postConcat(vintageMatrix)
            }
            ImageFilter.COLD -> {
                val coldMatrix = ColorMatrix().apply {
                    val matrix = floatArrayOf(
                        0.8f, 0f, 0f, 0f, 0f,
                        0f, 0.9f, 0.1f, 0f, 0f,
                        0f, 0.1f, 1.1f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                    set(matrix)
                }
                colorMatrix.postConcat(coldMatrix)
            }
            ImageFilter.WARM -> {
                val warmMatrix = ColorMatrix().apply {
                    val matrix = floatArrayOf(
                        1.1f, 0f, 0f, 0f, 10f,
                        0f, 1.0f, 0f, 0f, 10f,
                        0f, 0f, 0.8f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                    set(matrix)
                }
                colorMatrix.postConcat(warmMatrix)
            }
            ImageFilter.NONE -> {
                // 不应用滤镜
            }
        }

        // 设置颜色滤镜
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)

        // 绘制位图
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return result
    }
}
