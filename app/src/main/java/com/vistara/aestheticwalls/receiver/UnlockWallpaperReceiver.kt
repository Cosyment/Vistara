package com.vistara.aestheticwalls.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.vistara.aestheticwalls.data.model.AutoChangeFrequency
import com.vistara.aestheticwalls.data.model.AutoChangeHistory
import com.vistara.aestheticwalls.data.model.WallpaperTarget
import com.vistara.aestheticwalls.data.repository.UserPrefsRepository
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import com.vistara.aestheticwalls.utils.NetworkUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 解锁屏幕壁纸更换广播接收器
 * 监听屏幕解锁事件，根据用户设置自动更换壁纸
 */
@AndroidEntryPoint
class UnlockWallpaperReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userPrefsRepository: UserPrefsRepository

    @Inject
    lateinit var wallpaperRepository: WallpaperRepository

    @Inject
    lateinit var networkUtil: NetworkUtil

    companion object {
        private const val TAG = "UnlockWallpaperReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            Log.d(TAG, "Screen unlocked, checking auto change settings")
            
            // 使用协程处理异步操作
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 获取用户设置
                    val userSettings = userPrefsRepository.getUserSettings()
                    
                    // 检查是否启用了自动更换壁纸
                    if (!userSettings.autoChangeEnabled) {
                        Log.d(TAG, "Auto wallpaper change is disabled")
                        return@launch
                    }
                    
                    // 检查是否设置为每次解锁更换
                    if (userSettings.autoChangeFrequency != AutoChangeFrequency.EACH_UNLOCK) {
                        Log.d(TAG, "Auto change frequency is not set to EACH_UNLOCK")
                        return@launch
                    }
                    
                    // 检查网络状态
                    if (userSettings.autoChangeWifiOnly && !networkUtil.isWifiConnected()) {
                        Log.d(TAG, "WiFi required but not connected")
                        return@launch
                    }
                    
                    // 根据来源获取壁纸
                    val wallpaper = when (userSettings.autoChangeSource) {
                        com.vistara.aestheticwalls.data.model.AutoChangeSource.FAVORITES -> {
                            // 从收藏中随机获取
                            wallpaperRepository.getRandomFavoriteWallpaper()
                        }
                        
                        com.vistara.aestheticwalls.data.model.AutoChangeSource.CATEGORY -> {
                            // 从指定分类中随机获取
                            userSettings.autoChangeCategory?.let { categoryId ->
                                wallpaperRepository.getRandomWallpaperByCategory(categoryId)
                            } ?: wallpaperRepository.getRandomWallpaper()
                        }
                        
                        com.vistara.aestheticwalls.data.model.AutoChangeSource.DOWNLOADED -> {
                            // 从下载中随机获取
                            wallpaperRepository.getRandomDownloadedWallpaper()
                        }
                        
                        com.vistara.aestheticwalls.data.model.AutoChangeSource.TRENDING -> {
                            // 从热门壁纸中随机获取
                            wallpaperRepository.getRandomTrendingWallpaper()
                        }
                        
                        else -> {
                            // 默认情况下从随机壁纸中获取
                            wallpaperRepository.getRandomWallpaper()
                        }
                    }
                    
                    if (wallpaper == null) {
                        Log.e(TAG, "No wallpaper found to set")
                        return@launch
                    }
                    
                    // 记录自动更换历史
                    val history = AutoChangeHistory(
                        wallpaperId = wallpaper.id,
                        wallpaperUrl = wallpaper.url ?: "",
                        success = true,
                        targetScreen = userSettings.autoChangeTarget.name
                    )
                    wallpaperRepository.recordAutoChangeHistory(history)
                    
                    // 设置壁纸
                    val success = setWallpaper(context, wallpaper, userSettings.autoChangeTarget)
                    
                    if (success) {
                        Log.d(TAG, "Auto change wallpaper set successfully")
                    } else {
                        Log.e(TAG, "Failed to set auto change wallpaper")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during auto wallpaper change: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 设置壁纸
     */
    private suspend fun setWallpaper(
        context: Context,
        wallpaper: com.vistara.aestheticwalls.data.model.Wallpaper,
        target: WallpaperTarget
    ): Boolean {
        return try {
            // 检查壁纸是否已下载
            val localFile = wallpaperRepository.getLocalFile(wallpaper.id)
            val bitmap = if (localFile != null && localFile.exists()) {
                // 使用本地文件
                android.graphics.BitmapFactory.decodeFile(localFile.absolutePath)
            } else {
                // 从URL下载
                val url = java.net.URL(wallpaper.url)
                android.graphics.BitmapFactory.decodeStream(url.openStream())
            }
            
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap")
                return false
            }
            
            // 设置壁纸
            val wallpaperManager = android.app.WallpaperManager.getInstance(context)
            
            when (target) {
                WallpaperTarget.HOME -> {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(
                            bitmap, null, true, android.app.WallpaperManager.FLAG_SYSTEM
                        )
                    } else {
                        wallpaperManager.setBitmap(bitmap)
                    }
                }
                
                WallpaperTarget.LOCK -> {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(
                            bitmap, null, true, android.app.WallpaperManager.FLAG_LOCK
                        )
                    }
                }
                
                WallpaperTarget.BOTH -> {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(
                            bitmap,
                            null,
                            true,
                            android.app.WallpaperManager.FLAG_SYSTEM or android.app.WallpaperManager.FLAG_LOCK
                        )
                    } else {
                        wallpaperManager.setBitmap(bitmap)
                    }
                }
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting wallpaper: ${e.message}")
            false
        }
    }
}
