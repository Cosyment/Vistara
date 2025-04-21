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
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.EditedImageCache
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import com.vistara.aestheticwalls.utils.StringProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val filter: ImageFilter = ImageFilter.NONE,
    val croppedBitmap: Bitmap? = null
)

/**
 * 壁纸编辑ViewModel
 */
@HiltViewModel
class WallpaperEditViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wallpaperRepository: WallpaperRepository,
    savedStateHandle: SavedStateHandle,
    val stringProvider: StringProvider
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
     * 优先从本地数据库加载，然后在后台更新
     */
    private fun loadWallpaper() {
        viewModelScope.launch {
            _wallpaperState.value = UiState.Loading

            try {
                // 先从本地数据库查询
                var wallpaper: Wallpaper? = wallpaperRepository.getWallpaperById(wallpaperId)

                // 如果本地数据库有这个壁纸，直接显示
                if (wallpaper != null) {
                    _wallpaperState.value = UiState.Success(wallpaper)

                    // 加载原始位图
                    loadOriginalBitmap(wallpaper.url)

                    // 同时在后台尝试从服务器获取最新数据更新本地缓存
                    try {
                        val updatedWallpaper = wallpaperRepository.getWallpaperById(wallpaperId)
                        if (updatedWallpaper != null && updatedWallpaper != wallpaper) {
                            _wallpaperState.value = UiState.Success(updatedWallpaper)
                            // 如果URL变了，重新加载原始位图
                            if (updatedWallpaper.url != wallpaper.url) {
                                loadOriginalBitmap(updatedWallpaper.url)
                            }
                        }
                    } catch (e: Exception) {
                        // 忽略后台更新错误，不影响用户体验
                    }
                    return@launch
                }

                // 如果本地数据库没有，尝试多次从服务器获取
                var retryCount = 0
                val maxRetries = 3
                var isRateLimitError = false

                while (wallpaper == null && retryCount < maxRetries) {
                    try {
                        wallpaper = wallpaperRepository.getWallpaperById(wallpaperId)
                        if (wallpaper != null) {
                            break
                        }
                    } catch (e: Exception) {
                        // 检查是否是速率限制错误
                        if (e.message?.contains("Rate Limit") == true || e.message?.contains("403") == true) {
                            isRateLimitError = true
                            // 如果是速率限制错误，等待时间更长
                            kotlinx.coroutines.delay(2000) // 等待2秒
                        }
                    }
                    retryCount++
                    if (!isRateLimitError) {
                        kotlinx.coroutines.delay(500) // 正常重试等待500毫秒
                    }
                }

                if (wallpaper != null) {
                    _wallpaperState.value = UiState.Success(wallpaper)
                    // 加载原始位图
                    loadOriginalBitmap(wallpaper.url)
                } else {
                    // 如果多次重试后仍然无法获取壁纸详情，显示错误信息
                    if (isRateLimitError) {
                        _wallpaperState.value =
                            UiState.Error(stringProvider.getString(R.string.api_rate_limit_exceeded))
                    } else {
                        _wallpaperState.value =
                            UiState.Error(stringProvider.getString(R.string.load_wallpaper_failed))
                    }
                }
            } catch (e: Exception) {
                _wallpaperState.value = UiState.Error(
                    e.message ?: stringProvider.getString(R.string.load_wallpaper_failed)
                )
            }
        }
    }

    /**
     * 加载原始位图
     * 使用缓存策略，避免重复下载
     */
    private fun loadOriginalBitmap(url: String?) {
        if (url == null) return

        // 检查是否已经有编辑后的图片
        val editedImage = EditedImageCache.getEditedImage(wallpaperId)
        if (editedImage != null) {
            // 如果有编辑后的图片，使用它作为原始位图
            originalBitmap = editedImage
            // 更新裁剪后的位图
            _editState.value = _editState.value.copy(croppedBitmap = editedImage)
            return
        }

        viewModelScope.launch {
            try {
                // 尝试使用缓存的方式加载原始位图
                withContext(Dispatchers.IO) {
                    try {
                        // 先尝试使用缓存文件
                        val cacheDir = File(context.cacheDir, "wallpapers")
                        if (!cacheDir.exists()) {
                            cacheDir.mkdirs()
                        }

                        val cacheFile = File(cacheDir, "original_${wallpaperId}.jpg")

                        if (cacheFile.exists() && cacheFile.length() > 0) {
                            // 如果缓存文件存在，直接从缓存加载
                            originalBitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)
                        } else {
                            // 如果缓存文件不存在，从网络加载并缓存
                            val imageUrl = URL(url)
                            val connection = imageUrl.openConnection()
                            connection.connectTimeout = 5000
                            connection.readTimeout = 5000

                            val inputStream = connection.getInputStream()
                            originalBitmap = BitmapFactory.decodeStream(inputStream)

                            // 将位图保存到缓存文件
                            if (originalBitmap != null) {
                                val outputStream = FileOutputStream(cacheFile)
                                originalBitmap?.compress(
                                    Bitmap.CompressFormat.JPEG, 100, outputStream
                                )
                                outputStream.close()
                            }

                            inputStream.close()
                        }
                    } catch (e: Exception) {
                        // 如果缓存加载失败，直接从网络加载
                        val imageUrl = URL(url)
                        originalBitmap = BitmapFactory.decodeStream(imageUrl.openStream())
                    }
                }
            } catch (e: Exception) {
                // 如果加载失败，显示错误信息，但不更新壁纸状态
                // 因为壁纸状态已经是成功状态，只是原始位图加载失败
                // 这样用户仍然可以看到壁纸信息，只是无法编辑
                // 在UI中显示错误提示
                withContext(Dispatchers.Main) {
                    // 使用Toast或其他方式显示错误提示
                    android.widget.Toast.makeText(
                        context,
                        "${stringProvider.getString(R.string.load_wallpaper_failed)}: ${e.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
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
     * 更新裁剪后的图片
     */
    fun updateCroppedBitmap(bitmap: Bitmap?) {
        _editState.value = _editState.value.copy(croppedBitmap = bitmap)
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
        // 使用裁剪后的图片或原始图片
        val sourceBitmap = _editState.value.croppedBitmap ?: originalBitmap ?: return

        viewModelScope.launch {
            _isSaving.value = true

            try {
                // 应用当前的编辑效果
                val finalBitmap = withContext(Dispatchers.Default) {
                    applyEffectsToBitmap(
                        sourceBitmap,
                        _editState.value.brightness,
                        _editState.value.contrast,
                        _editState.value.saturation,
                        _editState.value.filter
                    )
                }

                // 保存到相册
                saveToGallery(finalBitmap)

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
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
                )

                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                    }
                }
            } else {
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
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
        bitmap: Bitmap, brightness: Float, contrast: Float, saturation: Float, filter: ImageFilter
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
                scale,
                0f,
                0f,
                0f,
                0f,
                0f,
                scale,
                0f,
                0f,
                0f,
                0f,
                0f,
                scale,
                0f,
                0f,
                0f,
                0f,
                0f,
                1f,
                0f
            )
            set(matrix)
        }
        colorMatrix.postConcat(brightnessMatrix)

        // 应用对比度
        val contrastMatrix = ColorMatrix().apply {
            val scale = contrast
            val translate = (1.0f - scale) * 128f
            val matrix = floatArrayOf(
                scale,
                0f,
                0f,
                0f,
                translate,
                0f,
                scale,
                0f,
                0f,
                translate,
                0f,
                0f,
                scale,
                0f,
                translate,
                0f,
                0f,
                0f,
                1f,
                0f
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
                        0.393f,
                        0.769f,
                        0.189f,
                        0f,
                        0f,
                        0.349f,
                        0.686f,
                        0.168f,
                        0f,
                        0f,
                        0.272f,
                        0.534f,
                        0.131f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        1f,
                        0f
                    )
                    set(matrix)
                }
                colorMatrix.postConcat(sepiaMatrix)
            }

            ImageFilter.VINTAGE -> {
                val vintageMatrix = ColorMatrix().apply {
                    val matrix = floatArrayOf(
                        0.9f,
                        0.5f,
                        0.1f,
                        0f,
                        0f,
                        0.3f,
                        0.8f,
                        0.1f,
                        0f,
                        0f,
                        0.2f,
                        0.3f,
                        0.5f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        1f,
                        0f
                    )
                    set(matrix)
                }
                colorMatrix.postConcat(vintageMatrix)
            }

            ImageFilter.COLD -> {
                val coldMatrix = ColorMatrix().apply {
                    val matrix = floatArrayOf(
                        0.8f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0.9f,
                        0.1f,
                        0f,
                        0f,
                        0f,
                        0.1f,
                        1.1f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        1f,
                        0f
                    )
                    set(matrix)
                }
                colorMatrix.postConcat(coldMatrix)
            }

            ImageFilter.WARM -> {
                val warmMatrix = ColorMatrix().apply {
                    val matrix = floatArrayOf(
                        1.1f,
                        0f,
                        0f,
                        0f,
                        10f,
                        0f,
                        1.0f,
                        0f,
                        0f,
                        10f,
                        0f,
                        0f,
                        0.8f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        1f,
                        0f
                    )
                    set(matrix)
                }
                colorMatrix.postConcat(warmMatrix)
            }

            ImageFilter.PURPLE -> {
                val purpleMatrix = ColorMatrix().apply {
                    val matrix = floatArrayOf(
                        0.9f,
                        0.1f,
                        0.2f,
                        0f,
                        0f,
                        0.1f,
                        0.8f,
                        0.2f,
                        0f,
                        0f,
                        0.2f,
                        0.2f,
                        1.0f,
                        0f,
                        10f,
                        0f,
                        0f,
                        0f,
                        1f,
                        0f
                    )
                    set(matrix)
                }
                colorMatrix.postConcat(purpleMatrix)
            }

            ImageFilter.BLUE -> {
                val blueMatrix = ColorMatrix().apply {
                    val matrix = floatArrayOf(
                        0.8f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0.9f,
                        0f,
                        0f,
                        0f,
                        0.2f,
                        0.2f,
                        1.2f,
                        0f,
                        10f,
                        0f,
                        0f,
                        0f,
                        1f,
                        0f
                    )
                    set(matrix)
                }
                colorMatrix.postConcat(blueMatrix)
            }

            ImageFilter.GREEN -> {
                val greenMatrix = ColorMatrix().apply {
                    val matrix = floatArrayOf(
                        0.8f,
                        0.1f,
                        0f,
                        0f,
                        0f,
                        0.1f,
                        1.1f,
                        0.1f,
                        0f,
                        10f,
                        0f,
                        0.1f,
                        0.8f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        1f,
                        0f
                    )
                    set(matrix)
                }
                colorMatrix.postConcat(greenMatrix)
            }

            ImageFilter.PINK -> {
                val pinkMatrix = ColorMatrix().apply {
                    val matrix = floatArrayOf(
                        1.2f,
                        0.1f,
                        0.1f,
                        0f,
                        10f,
                        0.1f,
                        0.9f,
                        0.1f,
                        0f,
                        0f,
                        0.1f,
                        0.1f,
                        0.8f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        1f,
                        0f
                    )
                    set(matrix)
                }
                colorMatrix.postConcat(pinkMatrix)
            }

            ImageFilter.ORANGE -> {
                val orangeMatrix = ColorMatrix().apply {
                    val matrix = floatArrayOf(
                        1.3f,
                        0.1f,
                        0f,
                        0f,
                        10f,
                        0.1f,
                        0.9f,
                        0f,
                        0f,
                        5f,
                        0f,
                        0f,
                        0.7f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        1f,
                        0f
                    )
                    set(matrix)
                }
                colorMatrix.postConcat(orangeMatrix)
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
