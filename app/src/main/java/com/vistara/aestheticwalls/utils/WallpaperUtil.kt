package com.vistara.aestheticwalls.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import com.vistara.aestheticwalls.data.model.WallpaperTarget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 壁纸工具类
 * 处理壁纸的设置与保存
 */
@Singleton
class WallpaperUtil @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "WallpaperUtil"
        private const val WALLPAPERS_DIR = "wallpapers"
    }
    
    /**
     * 设置壁纸
     *
     * @param bitmap 要设置的壁纸图像
     * @param target 设置目标 (主屏幕, 锁屏或两者)
     * @return 设置是否成功
     */
    fun setWallpaper(bitmap: Bitmap, target: WallpaperTarget): Boolean {
        return try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            
            when (target) {
                WallpaperTarget.HOME -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(
                            bitmap,
                            null,
                            true,
                            WallpaperManager.FLAG_SYSTEM
                        )
                    } else {
                        wallpaperManager.setBitmap(bitmap)
                    }
                }
                WallpaperTarget.LOCK -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(
                            bitmap,
                            null,
                            true,
                            WallpaperManager.FLAG_LOCK
                        )
                    } else {
                        // 旧版本Android不支持设置锁屏壁纸
                        return false
                    }
                }
                WallpaperTarget.BOTH -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(
                            bitmap,
                            null,
                            true,
                            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                        )
                    } else {
                        wallpaperManager.setBitmap(bitmap)
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "设置壁纸失败", e)
            false
        }
    }
    
    /**
     * 获取本地壁纸目录
     */
    fun getWallpapersDir(): File {
        val dir = File(context.filesDir, WALLPAPERS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    /**
     * 保存Bitmap到本地文件
     *
     * @param bitmap 要保存的位图
     * @param fileName 文件名
     * @return 保存的文件或null（如果保存失败）
     */
    fun saveBitmapToFile(bitmap: Bitmap, fileName: String): File? {
        return try {
            val wallpapersDir = getWallpapersDir()
            val file = File(wallpapersDir, "$fileName.jpg")
            
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
            }
            
            file
        } catch (e: Exception) {
            Log.e(TAG, "保存壁纸到文件失败", e)
            null
        }
    }
    
    /**
     * 从本地文件加载Bitmap
     *
     * @param fileName 文件名
     * @return 位图或null（如果加载失败）
     */
    fun loadBitmapFromFile(fileName: String): Bitmap? {
        return try {
            val wallpapersDir = getWallpapersDir()
            val file = File(wallpapersDir, "$fileName.jpg")
            
            if (!file.exists()) {
                return null
            }
            
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "从文件加载壁纸失败", e)
            null
        }
    }
    
    /**
     * 删除本地壁纸文件
     *
     * @param fileName 文件名
     * @return 是否删除成功
     */
    fun deleteWallpaperFile(fileName: String): Boolean {
        return try {
            val wallpapersDir = getWallpapersDir()
            val file = File(wallpapersDir, "$fileName.jpg")
            
            if (!file.exists()) {
                return false
            }
            
            file.delete()
        } catch (e: Exception) {
            Log.e(TAG, "删除壁纸文件失败", e)
            false
        }
    }

    /**
     * 获取壁纸本地文件
     * @param wallpaperId 壁纸ID
     * @return 本地文件，如果不存在则返回null
     */
    fun getLocalFile(wallpaperId: String): File? {
        val wallpapersDir = File(context.filesDir, WALLPAPERS_DIR)
        val file = File(wallpapersDir, "$wallpaperId.jpg")
        return if (file.exists()) file else null
    }

    /**
     * 清理壁纸缓存
     * @return 成功清理的文件数量
     */
    suspend fun clearWallpaperCache(): Int = withContext(Dispatchers.IO) {
        try {
            val wallpapersDir = File(context.filesDir, WALLPAPERS_DIR)
            if (!wallpapersDir.exists()) return@withContext 0

            var count = 0
            wallpapersDir.listFiles()?.forEach { file ->
                if (file.delete()) count++
            }
            return@withContext count
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing wallpaper cache", e)
            return@withContext 0
        }
    }
} 