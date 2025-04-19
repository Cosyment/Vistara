package com.vistara.aestheticwalls.worker

import android.content.Context
import android.util.Log
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
import com.vistara.aestheticwalls.data.model.WallpaperTarget
import com.vistara.aestheticwalls.data.repository.UserPrefsRepository
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import com.vistara.aestheticwalls.manager.AppWallpaperManager
import com.vistara.aestheticwalls.utils.NetworkUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * 自动更换壁纸的Worker
 * 负责根据用户设置自动下载和设置壁纸
 */
class AutoWallpaperWorker @Inject constructor(
    @ApplicationContext private val context: Context,
    workerParameters: WorkerParameters,
    private val userPrefsRepository: UserPrefsRepository,
    private val wallpaperRepository: WallpaperRepository,
    private val networkUtil: NetworkUtil,
    private val appWallpaperManager: AppWallpaperManager,
) : CoroutineWorker(context, workerParameters) {

    companion object {
        private const val TAG = "AutoWallpaperWorker"
        const val WORK_NAME = "auto_wallpaper_work"
        private const val NOTIFICATION_ID = 123
        private const val CHANNEL_ID = "auto_wallpaper_channel"

        // 输入数据键
        const val KEY_TARGET = "target_screen"

        // 间隔类型
        const val INTERVAL_DAILY = "daily"
        const val INTERVAL_12_HOURS = "12_hours"
        const val INTERVAL_6_HOURS = "6_hours"
        const val INTERVAL_1_HOUR = "1_hour"

        // 壁纸类型
        const val WALLPAPER_TYPE_HOME = "home"
        const val WALLPAPER_TYPE_LOCK = "lock"
        const val WALLPAPER_TYPE_BOTH = "both"

        // 壁纸来源
        const val SOURCE_FAVORITES = "favorites"
        const val SOURCE_CATEGORY = "category"

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

            // 如果频率是每次解锁，使用广播接收器
            if (frequency == AutoChangeFrequency.EACH_UNLOCK) {
                // 使用广播接收器处理
                // 不需要额外的操作，因为广播接收器已经在AndroidManifest.xml中注册
                Log.d(TAG, "Using broadcast receiver for EACH_UNLOCK frequency")
                return
            }

            // 创建周期性工作请求
            val hours = when (frequency) {
                AutoChangeFrequency.DAILY -> 24L
                AutoChangeFrequency.TWELVE_HOURS -> 12L
                AutoChangeFrequency.SIX_HOURS -> 6L
                AutoChangeFrequency.HOURLY -> 1L
                AutoChangeFrequency.EACH_UNLOCK -> 24L // 默认值，实际不会使用
            }
            val workRequest = PeriodicWorkRequestBuilder<AutoWallpaperWorker>(
                hours, TimeUnit.HOURS
            ).setConstraints(constraints).build()

            // 将工作排入队列
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE, workRequest
            )

            Log.d(TAG, "Scheduled auto wallpaper change every $hours hours")
        }

        /**
         * 立即运行一次壁纸更换
         */
        fun runOnce(
            workManager: WorkManager, target: WallpaperTarget = WallpaperTarget.BOTH
        ) {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

            val inputData = Data.Builder().putString(KEY_TARGET, target.name).build()

            val workRequest = OneTimeWorkRequestBuilder<AutoWallpaperWorker>().setConstraints(constraints).setInputData(inputData).build()

            workManager.enqueue(workRequest)
            Log.d(TAG, "Scheduled one-time wallpaper change")
        }

        /**
         * 设置自动壁纸更换任务
         * @param context 上下文
         * @param interval 更换间隔
         * @param source 壁纸来源
         * @param category 分类名称(当source为SOURCE_CATEGORY时有效)
         * @param wallpaperType 壁纸类型(主屏/锁屏/两者)
         * @param wifiOnly 是否仅在WiFi连接下更换
         */
        fun scheduleAutoWallpaperChange(
            context: Context,
            interval: String,
            source: String,
            category: String? = null,
            wallpaperType: String = WALLPAPER_TYPE_BOTH,
            wifiOnly: Boolean = true
        ) {
            val intervalHours = when (interval) {
                INTERVAL_DAILY -> 24
                INTERVAL_12_HOURS -> 12
                INTERVAL_6_HOURS -> 6
                INTERVAL_1_HOUR -> 1
                else -> 24
            }

            val inputData =
                Data.Builder().putString("source", source).putString("category", category).putString("wallpaperType", wallpaperType).build()

            val constraints = Constraints.Builder().setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true).build()

            val workRequest = PeriodicWorkRequestBuilder<AutoWallpaperWorker>(
                intervalHours.toLong(), TimeUnit.HOURS
            ).setConstraints(constraints).setInputData(inputData).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, workRequest
            )

            Log.d(TAG, "Scheduled auto wallpaper change - Interval: $interval, Source: $source")
        }

        /**
         * 取消自动壁纸更换任务
         */
        fun cancelAutoWallpaperChange(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Cancelled auto wallpaper change")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) @androidx.annotation.RequiresPermission(
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
            if (userSettings.autoChangeWifiOnly && !networkUtil.isWifiConnected()) {
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

            // 记录历史
            val history = AutoChangeHistory(
                wallpaperId = wallpaper.id,
                wallpaperUrl = wallpaper.url ?: "",
                timestamp = System.currentTimeMillis(),
                targetScreen = when (targetScreen) {
                    WallpaperTarget.HOME -> "home"
                    WallpaperTarget.LOCK -> "lock"
                    WallpaperTarget.BOTH -> "both"
                }
            )
            wallpaperRepository.recordAutoChangeHistory(history)

            appWallpaperManager.setWallpaper(wallpaper, target = userSettings.autoChangeTarget, onComplete = { success ->
                if (success) {
                    Log.d(TAG, "壁纸设置成功")
                } else {
                    Log.e(TAG, "壁纸设置失败")
                }
            })
            Log.d(TAG, "Auto wallpaper change completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Auto wallpaper change failed", e)
            Result.failure()
        }
    }
}