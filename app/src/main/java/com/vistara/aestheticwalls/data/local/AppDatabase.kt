package com.vistara.aestheticwalls.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vistara.aestheticwalls.data.model.AutoChangeHistory
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.local.DatabaseMigrations

/**
 * 应用数据库类
 */
@Database(
    entities = [
        Wallpaper::class,
        AutoChangeHistory::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wallpaperDao(): WallpaperDao

    companion object {
        private const val DATABASE_NAME = "vistara_db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
            .addMigrations(DatabaseMigrations.MIGRATION_1_2) // 添加1到2的迁移策略
            // 如果迁移失败，允许回退到破坏性迁移（会清除数据）
            .fallbackToDestructiveMigration()
            .build()
        }
    }
}