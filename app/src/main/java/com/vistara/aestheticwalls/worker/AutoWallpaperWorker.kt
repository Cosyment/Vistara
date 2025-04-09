package com.vistara.aestheticwalls.worker

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.vistara.aestheticwalls.data.model.AutoChangeFrequency
import com.vistara.aestheticwalls.data.model.AutoChangeHistory
import com.vistara.aestheticwalls.data.model.AutoChangeSource
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.model.WallpaperTarget
import com.vistara.aestheticwalls.data.repository.UserPreferencesRepository
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import com.vistara.aestheticwalls.utils.NetworkUtil
import com.vistara.aestheticwalls.utils.NotificationUtil
import com.vistara.aestheticwalls.utils.WallpaperUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * 自动更换壁纸的Worker
 * 负责根据用户设置自动下载和设置壁纸
 */
class AutoWallpaperWorker @Inject constructor(
    @ApplicationContext private val context: Context,
    workerParameters: WorkerParameters,
    private val userPrefsRepository: UserPreferencesRepository,
    private val wallpaperRepository: WallpaperRepository,
    private val networkUtils: NetworkUtil,
    private val notificationUtils: NotificationUtil,
    private val wallpaperUtils: WallpaperUtil
) : CoroutineWorker(context, workerParameters) {

    companion object {
        private const val TAG = "AutoWallpaperWorker"
        const val WORK_NAME = "auto_wallpaper_work"

        // 输入数据键
        const val KEY_TARGET = "target_screen"

        /**
         * 安排自动更换壁纸任务
         */
        fun schedule(
            workManager: WorkManager, frequency: AutoChangeFrequency, wifiOnly: Boolean
        ) {
            // 取消之前的任务
            workManager.cancelUniqueWork(WORK_NAME)

            // 创建约束
            val constraints = Constraints.Builder().apply {
                if (wifiOnly) {
                    setRequiredNetworkType(NetworkType.UNMETERED)
                } else {
                    setRequiredNetworkType(NetworkType.CONNECTED)
                }
                setRequiresBatteryNotLow(true)
            }.build()

            // 如果频率是每次解锁，使用不同的工作管理器
            if (frequency == AutoChangeFrequency.EACH_UNLOCK) {
                // 使用其他方式处理，如广播接收器
                return
            }

            // 创建周期性工作请求
            val workRequest = PeriodicWorkRequestBuilder<AutoWallpaperWorker>(
                frequency.hours.toLong(), TimeUnit.HOURS
            ).setConstraints(constraints).build()

            // 将工作排入队列
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE, workRequest
            )

            Log.d(TAG, "Scheduled auto wallpaper change every ${frequency.hours} hours")
        }

        /**
         * 立即运行一次壁纸更换
         */
        fun runOnce(
            workManager: WorkManager, target: WallpaperTarget = WallpaperTarget.BOTH
        ) {
            val constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

            val inputData = Data.Builder().putString(KEY_TARGET, target.name).build()

            val workRequest =
                OneTimeWorkRequestBuilder<AutoWallpaperWorker>().setConstraints(constraints)
                    .setInputData(inputData).build()

            workManager.enqueue(workRequest)
            Log.d(TAG, "Scheduled one-time wallpaper change")
        }
    }

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) @androidx.annotation.RequiresPermission(
            android.Manifest.permission.POST_NOTIFICATIONS
        ) {
            try {
                Log.d(TAG, "Starting auto wallpaper change")

                // 获取用户设置
                val userSettings = userPrefsRepository.getUserSettings()

                // 检查是否启用了自动更换
                if (!userSettings.autoChangeEnabled) {
                    Log.d(TAG, "Auto wallpaper change is disabled")
                    return@withContext Result.success()
                }

                // 检查网络状态
                if (userSettings.autoChangeWifiOnly && !networkUtils.isWifiConnected()) {
                    Log.d(TAG, "WiFi required but not connected")
                    return@withContext Result.retry()
                }

                // 获取目标屏幕
                val targetScreen = inputData.getString(KEY_TARGET)?.let {
                    try {
                        WallpaperTarget.valueOf(it)
                    } catch (e: Exception) {
                        WallpaperTarget.BOTH
                    }
                } ?: WallpaperTarget.BOTH

                // 根据来源获取壁纸
                val wallpaper = when (userSettings.autoChangeSource) {
                    AutoChangeSource.FAVORITES -> {
                        // 从收藏中随机获取
                        wallpaperRepository.getRandomFavoriteWallpaper()
                    }

                    AutoChangeSource.CATEGORY -> {
                        // 从指定分类中随机获取
                        userSettings.autoChangeCategory?.let { categoryId ->
                            wallpaperRepository.getRandomWallpaperByCategory(categoryId)
                        } ?: wallpaperRepository.getRandomWallpaper()
                    }

                    AutoChangeSource.DOWNLOADED -> {
                        // 从下载中随机获取
                        wallpaperRepository.getRandomDownloadedWallpaper()
                    }

                    AutoChangeSource.TRENDING -> {
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
                    return@withContext Result.failure()
                }

                // 下载和设置壁纸
                val result = downloadAndSetWallpaper(wallpaper, targetScreen)
                if (!result) {
                    return@withContext Result.failure()
                }

                // 记录历史
                val history = AutoChangeHistory(
                    wallpaperId = wallpaper.id,
                    timestamp = System.currentTimeMillis(),
                    targetScreen = targetScreen
                )
                wallpaperRepository.recordAutoChangeHistory(history)

                // 发送通知（如果启用）
                if (userSettings.showDownloadNotification) {
                    if (ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return@withContext Result.failure()
                    }
                    notificationUtils.showWallpaperChangedNotification(wallpaper)
                }

                Log.d(TAG, "Auto wallpaper change completed successfully")
                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Auto wallpaper change failed", e)
                Result.failure()
            }
        }

    /**
     * 下载并设置壁纸
     */
    private suspend fun downloadAndSetWallpaper(
        wallpaper: Wallpaper, target: WallpaperTarget
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // 检查壁纸是否已下载
            val localFile = wallpaperRepository.getLocalFile(wallpaper.id)
            val bitmap = if (localFile != null && localFile.exists()) {
                // 使用本地文件
                BitmapFactory.decodeFile(localFile.absolutePath)
            } else {
                // 从URL下载
                val url = URL(wallpaper.url)
                val connection = url.openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 15000
                val inputStream = connection.getInputStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                // 保存到本地（可选）
                bitmap?.let {
                    wallpaperUtils.saveBitmapToFile(it, wallpaper.id)
                }

                bitmap
            }

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap")
                return@withContext false
            }

            // 设置壁纸
            val wallpaperManager = WallpaperManager.getInstance(context)

            when (target) {
                WallpaperTarget.HOME -> {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(
                            bitmap, null, true, WallpaperManager.FLAG_SYSTEM
                        )
                    } else {
                        wallpaperManager.setBitmap(bitmap)
                    }
                }

                WallpaperTarget.LOCK -> {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(
                            bitmap, null, true, WallpaperManager.FLAG_LOCK
                        )
                    }
                }

                WallpaperTarget.BOTH -> {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
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

            // 如果需要，触发API下载跟踪
            wallpaperRepository.trackWallpaperDownload(wallpaper.id)

            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting wallpaper", e)
            return@withContext false
        }
    }
} 