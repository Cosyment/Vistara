package com.vistara.aestheticwalls.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vistara.aestheticwalls.data.model.AutoChangeHistory
import com.vistara.aestheticwalls.data.model.DiamondAccount
import com.vistara.aestheticwalls.data.model.DiamondTransaction
import com.vistara.aestheticwalls.data.model.Wallpaper

/**
 * 应用数据库类
 */
@Database(
    entities = [Wallpaper::class, AutoChangeHistory::class, DiamondAccount::class, DiamondTransaction::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wallpaperDao(): WallpaperDao
    abstract fun diamondDao(): DiamondDao

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
                context.applicationContext, AppDatabase::class.java, DATABASE_NAME
            ).addMigrations(
                    DatabaseMigrations.MIGRATION_1_2, // 添加1到2的迁移策略
                    DatabaseMigrations.MIGRATION_2_3, // 添加2到3的迁移策略
                    DatabaseMigrations.MIGRATION_3_4, // 添加3到4的迁移策略
                    DatabaseMigrations.MIGRATION_4_5, // 添加4到5的迁移策略
                    DatabaseMigrations.MIGRATION_5_6  // 添加5到6的迁移策略
                )
                // 如果迁移失败，允许回退到破坏性迁移（会清除数据）
                // 注意：在生产环境中应该移除这一行，否则会导致用户数据丢失
                .fallbackToDestructiveMigration(true)
                .build()
        }
    }
}