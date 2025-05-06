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

    /**
     * 从版本2迁移到版本3
     * 添加钻石相关表
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 创建钻石账户表
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `diamond_accounts` (
                    `userId` TEXT NOT NULL,
                    `balance` INTEGER NOT NULL,
                    `lastUpdated` INTEGER NOT NULL,
                    PRIMARY KEY(`userId`)
                )
                """
            )

            // 创建钻石交易记录表
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `diamond_transactions` (
                    `id` TEXT NOT NULL,
                    `userId` TEXT NOT NULL,
                    `amount` INTEGER NOT NULL,
                    `type` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `timestamp` INTEGER NOT NULL,
                    `relatedItemId` TEXT,
                    PRIMARY KEY(`id`)
                )
                """
            )
        }
    }
}
