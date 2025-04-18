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
                    Log.d(TAG, "用户设置获取成功: autoChangeEnabled=${userSettings.autoChangeEnabled}, "
                        + "frequency=${userSettings.autoChangeFrequency}, "
                        + "source=${userSettings.autoChangeSource}, "
                        + "target=${userSettings.autoChangeTarget}, "
                        + "wifiOnly=${userSettings.autoChangeWifiOnly}")

                    // 检查是否启用了自动更换壁纸
                    if (!userSettings.autoChangeEnabled) {
                        Log.d(TAG, "自动更换壁纸功能未启用，不执行更换")
                        return@launch
                    }

                    // 检查是否设置为每次解锁更换
                    if (userSettings.autoChangeFrequency != AutoChangeFrequency.EACH_UNLOCK) {
                        Log.d(TAG, "自动更换频率不是每次解锁，当前设置为: ${userSettings.autoChangeFrequency}")
                        return@launch
                    }

                    // 检查网络状态
                    val wifiConnected = networkUtil.isWifiConnected()
                    Log.d(TAG, "WiFi连接状态: $wifiConnected, 是否需要WiFi: ${userSettings.autoChangeWifiOnly}")
                    if (userSettings.autoChangeWifiOnly && !wifiConnected) {
                        Log.d(TAG, "需要WiFi连接但当前未连接，不执行更换")
                        return@launch
                    }

                    Log.d(TAG, "所有检查通过，开始获取壁纸")

                    // 根据来源获取壁纸
                    Log.d(TAG, "根据来源获取壁纸: ${userSettings.autoChangeSource}")
                    val wallpaper = when (userSettings.autoChangeSource) {
                        com.vistara.aestheticwalls.data.model.AutoChangeSource.FAVORITES -> {
                            // 从收藏中随机获取
                            Log.d(TAG, "从收藏中随机获取壁纸")
                            wallpaperRepository.getRandomFavoriteWallpaper()
                        }

                        com.vistara.aestheticwalls.data.model.AutoChangeSource.CATEGORY -> {
                            // 从指定分类中随机获取
                            val categoryId = userSettings.autoChangeCategory
                            Log.d(TAG, "从分类中随机获取壁纸，分类ID: $categoryId")
                            categoryId?.let { id ->
                                wallpaperRepository.getRandomWallpaperByCategory(id)
                            } ?: run {
                                Log.d(TAG, "分类ID为空，使用随机壁纸")
                                wallpaperRepository.getRandomWallpaper()
                            }
                        }

                        com.vistara.aestheticwalls.data.model.AutoChangeSource.DOWNLOADED -> {
                            // 从下载中随机获取
                            Log.d(TAG, "从下载中随机获取壁纸")
                            wallpaperRepository.getRandomDownloadedWallpaper()
                        }

                        com.vistara.aestheticwalls.data.model.AutoChangeSource.TRENDING -> {
                            // 从热门壁纸中随机获取
                            Log.d(TAG, "从热门壁纸中随机获取")
                            wallpaperRepository.getRandomTrendingWallpaper()
                        }

                        else -> {
                            // 默认情况下从随机壁纸中获取
                            Log.d(TAG, "使用默认来源，从随机壁纸中获取")
                            wallpaperRepository.getRandomWallpaper()
                        }
                    }

                    if (wallpaper == null) {
                        Log.e(TAG, "未找到可设置的壁纸，取消操作")
                        return@launch
                    }

                    Log.d(TAG, "成功获取壁纸: id=${wallpaper.id}, title=${wallpaper.title}")

                    // 记录自动更换历史
                    Log.d(TAG, "记录自动更换历史")
                    val history = AutoChangeHistory(
                        wallpaperId = wallpaper.id,
                        wallpaperUrl = wallpaper.url ?: "",
                        success = true,
                        targetScreen = userSettings.autoChangeTarget.name
                    )
                    wallpaperRepository.recordAutoChangeHistory(history)

                    // 设置壁纸
                    Log.d(TAG, "开始设置壁纸，目标屏幕: ${userSettings.autoChangeTarget}")
                    val success = setWallpaper(context, wallpaper, userSettings.autoChangeTarget)

                    if (success) {
                        Log.d(TAG, "自动更换壁纸设置成功！")
                    } else {
                        Log.e(TAG, "自动更换壁纸设置失败！")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "自动更换壁纸过程中出错: ${e.message}")
                    e.printStackTrace()
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
            Log.d(TAG, "检查本地文件: ${localFile?.absolutePath}, 存在: ${localFile?.exists()}")

            val bitmap = if (localFile != null && localFile.exists()) {
                // 使用本地文件
                Log.d(TAG, "使用本地文件加载壁纸")
                android.graphics.BitmapFactory.decodeFile(localFile.absolutePath)
            } else {
                // 从URL下载
                Log.d(TAG, "从网络加载壁纸: ${wallpaper.url}")
                val url = java.net.URL(wallpaper.url)
                android.graphics.BitmapFactory.decodeStream(url.openStream())
            }

            if (bitmap == null) {
                Log.e(TAG, "解码位图失败，无法设置壁纸")
                return false
            }

            Log.d(TAG, "位图加载成功，尺寸: ${bitmap.width}x${bitmap.height}")

            // 设置壁纸
            val wallpaperManager = android.app.WallpaperManager.getInstance(context)
            Log.d(TAG, "开始设置壁纸，Android版本: ${android.os.Build.VERSION.SDK_INT}")

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

            Log.d(TAG, "壁纸设置成功")
            true
        } catch (e: Exception) {
            Log.e(TAG, "设置壁纸时出错: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}
