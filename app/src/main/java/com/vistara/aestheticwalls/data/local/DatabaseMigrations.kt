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

    /**
     * 从版本3迁移到版本4
     * 修改diamond_transactions表的amount字段类型从INTEGER改为TEXT
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // SQLite不支持直接修改列类型，需要创建新表并复制数据

            // 1. 创建临时表
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `diamond_transactions_temp` (
                    `id` TEXT NOT NULL,
                    `userId` TEXT NOT NULL,
                    `amount` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `timestamp` INTEGER NOT NULL,
                    `relatedItemId` TEXT,
                    PRIMARY KEY(`id`)
                )
                """
            )

            // 2. 复制数据（将INTEGER类型的amount转换为TEXT类型）
            database.execSQL(
                """
                INSERT INTO diamond_transactions_temp
                SELECT id, userId, CAST(amount AS TEXT), type, description, timestamp, relatedItemId
                FROM diamond_transactions
                """
            )

            // 3. 删除旧表
            database.execSQL("DROP TABLE diamond_transactions")

            // 4. 重命名新表
            database.execSQL("ALTER TABLE diamond_transactions_temp RENAME TO diamond_transactions")
        }
    }

    /**
     * 从版本4迁移到版本5
     * 添加DiamondTransaction表的新列：diamondNum和createTime，以及remark
     */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 创建临时表，包含所有需要的列
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `diamond_transactions_temp` (
                    `id` TEXT NOT NULL,
                    `userId` TEXT NOT NULL,
                    `amount` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `remark` TEXT,
                    `diamondNum` INTEGER NOT NULL DEFAULT 0,
                    `createTime` TEXT,
                    `timestamp` INTEGER NOT NULL,
                    `relatedItemId` TEXT,
                    PRIMARY KEY(`id`)
                )
                """
            )

            // 复制数据，为新列设置默认值
            db.execSQL(
                """
                INSERT INTO diamond_transactions_temp
                SELECT id, userId, amount, type, description, NULL, 0, NULL, timestamp, relatedItemId
                FROM diamond_transactions
                """
            )

            // 删除旧表
            db.execSQL("DROP TABLE diamond_transactions")

            // 重命名新表
            db.execSQL("ALTER TABLE diamond_transactions_temp RENAME TO diamond_transactions")
        }
    }

    /**
     * 从版本5迁移到版本6
     * 确保DiamondTransaction表的结构与预期一致
     */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 检查表是否存在
            val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='diamond_transactions'")
            val tableExists = cursor.moveToFirst()
            cursor.close()

            if (tableExists) {
                // 创建临时表，确保结构与预期一致
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `diamond_transactions_temp` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `amount` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `remark` TEXT,
                        `diamondNum` INTEGER NOT NULL DEFAULT 0,
                        `createTime` TEXT,
                        `timestamp` INTEGER NOT NULL,
                        `relatedItemId` TEXT,
                        PRIMARY KEY(`id`)
                    )
                    """
                )

                // 复制数据
                try {
                    db.execSQL(
                        """
                        INSERT INTO diamond_transactions_temp
                        SELECT id, userId, amount, type, description, remark, diamondNum, createTime, timestamp, relatedItemId
                        FROM diamond_transactions
                        """
                    )
                } catch (e: Exception) {
                    // 如果复制失败（可能是因为列不匹配），尝试更灵活的复制方式
                    db.execSQL("DELETE FROM diamond_transactions_temp")

                    // 获取现有表的列信息
                    val columnsCursor = db.query("PRAGMA table_info(diamond_transactions)")
                    val columns = mutableListOf<String>()
                    while (columnsCursor.moveToNext()) {
                        val columnName = columnsCursor.getString(columnsCursor.getColumnIndex("name"))
                        columns.add(columnName)
                    }
                    columnsCursor.close()

                    // 根据现有列构建INSERT语句
                    if ("id" in columns) {
                        val selectColumns = columns.joinToString(", ")
                        db.execSQL(
                            """
                            INSERT INTO diamond_transactions_temp (${selectColumns})
                            SELECT ${selectColumns} FROM diamond_transactions
                            """
                        )
                    }
                }

                // 删除旧表
                db.execSQL("DROP TABLE diamond_transactions")

                // 重命名新表
                db.execSQL("ALTER TABLE diamond_transactions_temp RENAME TO diamond_transactions")
            } else {
                // 如果表不存在，创建新表
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `diamond_transactions` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `amount` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `remark` TEXT,
                        `diamondNum` INTEGER NOT NULL DEFAULT 0,
                        `createTime` TEXT,
                        `timestamp` INTEGER NOT NULL,
                        `relatedItemId` TEXT,
                        PRIMARY KEY(`id`)
                    )
                    """
                )
            }
        }
    }
}
