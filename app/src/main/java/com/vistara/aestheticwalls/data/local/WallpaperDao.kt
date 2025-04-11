package com.vistara.aestheticwalls.data.local

import androidx.room.*
import com.vistara.aestheticwalls.data.model.AutoChangeHistory
import com.vistara.aestheticwalls.data.model.Wallpaper
import kotlinx.coroutines.flow.Flow

/**
 * 壁纸数据访问对象接口
 * 处理所有与壁纸相关的本地数据库操作
 */
@Dao
interface WallpaperDao {
    // 收藏相关操作
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(wallpaper: Wallpaper)

    @Query("DELETE FROM wallpapers WHERE id = :wallpaperId")
    suspend fun deleteFavorite(wallpaperId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM wallpapers WHERE id = :wallpaperId)")
    suspend fun isFavorite(wallpaperId: String): Boolean

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1")
    fun getAllFavorites(): Flow<List<Wallpaper>>

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1")
    suspend fun getFavoritesList(): List<Wallpaper>

    // 下载相关操作
    @Query("SELECT * FROM wallpapers WHERE isDownloaded = 1")
    fun getAllDownloaded(): Flow<List<Wallpaper>>

    @Query("SELECT * FROM wallpapers WHERE isDownloaded = 1")
    suspend fun getDownloadedList(): List<Wallpaper>

    @Query("UPDATE wallpapers SET isDownloaded = 1 WHERE id = :wallpaperId")
    suspend fun insertDownload(wallpaperId: String)

    // 自动更换历史记录
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutoChangeHistory(history: AutoChangeHistory)

    @Query("SELECT * FROM auto_change_history ORDER BY timestamp DESC")
    fun getAutoChangeHistory(): Flow<List<AutoChangeHistory>>
} 