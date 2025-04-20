package com.vistara.aestheticwalls.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room数据库迁移策略
 */
object DatabaseMigrations {
    /**
     * 从版本1迁移到版本2
     * 添加了downloadSdUrl和downloadHdUrl字段
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 为wallpapers表添加新的列
            database.execSQL("ALTER TABLE wallpapers ADD COLUMN downloadSdUrl TEXT")
            database.execSQL("ALTER TABLE wallpapers ADD COLUMN downloadHdUrl TEXT")
        }
    }
}
