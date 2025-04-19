package com.vistara.aestheticwalls.utils

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * 壁纸预览工具类
 * 用于调用系统壁纸预览功能
 */
object WallpaperPreviewUtil {
    private const val TAG = "WallpaperPreviewUtils"
    
    /**
     * 预览壁纸
     * 根据不同手机厂商调用不同的壁纸预览界面
     * 
     * @param context 上下文
     * @param bitmap 壁纸位图
     */
    suspend fun previewWallpaper(context: Context, bitmap: Bitmap): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 将位图保存到缓存文件
                val cachePath = File(context.cacheDir, "wallpapers")
                cachePath.mkdirs()
                
                val wallpaperFile = File(cachePath, "temp_wallpaper_${System.currentTimeMillis()}.jpg")
                FileOutputStream(wallpaperFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                
                // 使用FileProvider获取URI
                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    wallpaperFile
                )
                
                // 调用系统壁纸预览
                previewWallpaper(context, contentUri)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error previewing wallpaper", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "预览壁纸失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                false
            }
        }
    }
    
    /**
     * 预览壁纸
     * 根据不同手机厂商调用不同的壁纸预览界面
     * 
     * @param context 上下文
     * @param uri 壁纸URI
     */
    fun previewWallpaper(context: Context, uri: Uri): Boolean {
        val intent: Intent
        
        // 根据不同手机厂商调用不同的壁纸预览界面
        when {
            RomUtil.isHuaweiRom -> {
                try {
                    val componentName = ComponentName(
                        "com.android.gallery3d", 
                        "com.android.gallery3d.app.Wallpaper"
                    )
                    intent = Intent(Intent.ACTION_VIEW)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.setDataAndType(uri, "image/*")
                    intent.putExtra("mimeType", "image/*")
                    intent.component = componentName
                    context.startActivity(intent)
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "Error launching Huawei wallpaper preview", e)
                    return defaultWallpaperPreview(context, uri)
                }
            }
            
            RomUtil.isMiuiRom -> {
                try {
                    val componentName = ComponentName(
                        "com.android.thememanager",
                        "com.android.thememanager.activity.WallpaperDetailActivity"
                    )
                    intent = Intent("miui.intent.action.START_WALLPAPER_DETAIL")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.setDataAndType(uri, "image/*")
                    intent.putExtra("mimeType", "image/*")
                    intent.component = componentName
                    context.startActivity(intent)
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "Error launching Xiaomi wallpaper preview", e)
                    return defaultWallpaperPreview(context, uri)
                }
            }
            
            RomUtil.isOppoRom -> {
                try {
                    val componentName = ComponentName(
                        "com.oplus.wallpapers",
                        "com.oplus.wallpapers.wallpaperpreview.PreviewStatementActivity"
                    )
                    intent = Intent(Intent.ACTION_VIEW)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.setDataAndType(uri, "image/*")
                    intent.putExtra("mimeType", "image/*")
                    intent.component = componentName
                    context.startActivity(intent)
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "Error launching OPPO wallpaper preview", e)
                    return defaultWallpaperPreview(context, uri)
                }
            }
            
            RomUtil.isVivoRom -> {
                try {
                    val componentName = ComponentName(
                        "com.vivo.gallery", 
                        "com.android.gallery3d.app.Wallpaper"
                    )
                    intent = Intent(Intent.ACTION_VIEW)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.setDataAndType(uri, "image/*")
                    intent.putExtra("mimeType", "image/*")
                    intent.component = componentName
                    context.startActivity(intent)
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "Error launching vivo wallpaper preview", e)
                    return defaultWallpaperPreview(context, uri)
                }
            }
            
            RomUtil.isOnePlusRom -> {
                try {
                    val componentName = ComponentName(
                        "com.oplus.wallpapers",
                        "com.oplus.wallpapers.wallpaperpreview.WallpaperPreviewActivity"
                    )
                    intent = Intent(Intent.ACTION_VIEW)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.setDataAndType(uri, "image/*")
                    intent.putExtra("mimeType", "image/*")
                    intent.component = componentName
                    context.startActivity(intent)
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "Error launching OnePlus wallpaper preview", e)
                    return defaultWallpaperPreview(context, uri)
                }
            }
            
            else -> {
                return defaultWallpaperPreview(context, uri)
            }
        }
    }
    
    /**
     * 默认壁纸预览方式
     * 使用系统壁纸管理器的裁剪和设置壁纸功能
     */
    private fun defaultWallpaperPreview(context: Context, uri: Uri): Boolean {
        return try {
            val intent = WallpaperManager.getInstance(context).getCropAndSetWallpaperIntent(uri)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error launching default wallpaper preview", e)
            
            // 如果系统壁纸管理器的裁剪和设置壁纸功能不可用，尝试使用图片查看器
            try {
                val viewIntent = Intent(Intent.ACTION_VIEW)
                viewIntent.setDataAndType(uri, "image/*")
                viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(viewIntent)
                true
            } catch (e2: ActivityNotFoundException) {
                Log.e(TAG, "No app can handle viewing images", e2)
                false
            }
        }
    }
}
