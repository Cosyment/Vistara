package com.vistara.aestheticwalls.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.repository.UserPrefsRepository
import com.vistara.aestheticwalls.ui.MainActivity
import kotlinx.coroutines.runBlocking
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通知工具类
 * 处理应用内所有通知的创建和显示
 */
@Singleton
class NotificationUtil @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPrefsRepository: UserPrefsRepository
) {
    companion object {
        private const val CHANNEL_ID_WALLPAPER = "wallpaper_channel"
        private const val CHANNEL_ID_DOWNLOAD = "download_channel"
        private const val CHANNEL_ID_LIVE_WALLPAPER = "live_wallpaper_channel"

        private const val NOTIFICATION_ID_WALLPAPER_CHANGED = 1001
        private const val NOTIFICATION_ID_DOWNLOAD_PROGRESS = 1002
        private const val NOTIFICATION_ID_DOWNLOAD_COMPLETE = 1003
        private const val NOTIFICATION_ID_LIVE_WALLPAPER_PENDING = 1004
    }

    init {
        createNotificationChannels()
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 壁纸更换通知渠道
            val wallpaperChannel = NotificationChannel(
                CHANNEL_ID_WALLPAPER,
                context.getString(R.string.notification_channel_wallpaper),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_wallpaper_desc)
                setShowBadge(false)
            }

            // 下载通知渠道
            val downloadChannel = NotificationChannel(
                CHANNEL_ID_DOWNLOAD,
                context.getString(R.string.notification_channel_download),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_download_desc)
                setShowBadge(true)
            }

            // 视频壁纸待处理通知渠道
            val liveWallpaperChannel = NotificationChannel(
                CHANNEL_ID_LIVE_WALLPAPER,
                context.getString(R.string.notification_channel_live_wallpaper),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_live_wallpaper_desc)
                setShowBadge(true)
            }

            // 注册通知渠道
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannels(listOf(wallpaperChannel, downloadChannel, liveWallpaperChannel))
        }
    }

    /**
     * 检查是否应该显示壁纸更换通知
     * 根据用户设置决定
     *
     * @return 是否应该显示通知
     */
    suspend fun shouldShowWallpaperChangedNotification(): Boolean {
        return userPrefsRepository.getUserSettings().showWallpaperChangeNotification
    }

    /**
     * 显示壁纸已更换通知
     * 注意：调用前应先检查用户设置和权限
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showWallpaperChangedNotification(wallpaper: Wallpaper) {
        // 创建点击意图
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        // 构建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_WALLPAPER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_wallpaper_changed_title))
            .setContentText(wallpaper.title ?: context.getString(R.string.notification_wallpaper_changed_text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // 显示通知
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_WALLPAPER_CHANGED, notification)
    }

    /**
     * 显示下载进度通知
     * 根据用户设置决定是否显示通知
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showDownloadProgressNotification(wallpaper: Wallpaper, progress: Int) {
        // 检查用户设置
        val showNotification = runBlocking { userPrefsRepository.getUserSettings().showDownloadNotification }
        if (!showNotification) {
            return
        }
        // 构建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_DOWNLOAD)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle(context.getString(R.string.notification_download_progress_title))
            .setContentText(wallpaper.title ?: context.getString(R.string.notification_download_progress_text))
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // 显示通知
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DOWNLOAD_PROGRESS, notification)
    }

    /**
     * 显示下载完成通知
     * 根据用户设置决定是否显示通知
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showDownloadCompleteNotification(wallpaper: Wallpaper, filePath: String) {
        // 检查用户设置
        val showNotification = runBlocking { userPrefsRepository.getUserSettings().showDownloadNotification }
        if (!showNotification) {
            return
        }
        // 创建点击意图
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("file_path", filePath)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        // 构建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_DOWNLOAD)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle(context.getString(R.string.notification_download_complete_title))
            .setContentText(wallpaper.title ?: context.getString(R.string.notification_download_complete_text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // 关闭进度通知并显示完成通知
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NOTIFICATION_ID_DOWNLOAD_PROGRESS)
        notificationManager.notify(NOTIFICATION_ID_DOWNLOAD_COMPLETE, notification)
    }

    /**
     * 显示视频壁纸待处理通知
     * 当用户解锁屏幕时，如果有待设置的视频壁纸但当前上下文不是MainActivity，
     * 则显示此通知引导用户打开应用设置视频壁纸
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showLiveWallpaperPendingNotification(wallpaper: Wallpaper, pendingIntent: PendingIntent) {
        // 构建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_LIVE_WALLPAPER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_live_wallpaper_pending_title))
            .setContentText(context.getString(R.string.notification_live_wallpaper_pending_text, wallpaper.title ?: ""))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // 显示通知
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_LIVE_WALLPAPER_PENDING, notification)
    }

    /**
     * 取消所有通知
     */
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }
}