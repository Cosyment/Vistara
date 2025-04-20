package com.vistara.aestheticwalls.di

import android.content.Context
import androidx.room.Room
import com.vistara.aestheticwalls.data.local.AppDatabase
import com.vistara.aestheticwalls.data.local.DatabaseMigrations
import com.vistara.aestheticwalls.data.local.WallpaperDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "vistara_db"
        )
        .addMigrations(DatabaseMigrations.MIGRATION_1_2) // 添加1到2的迁移策略
        // 如果迁移失败，允许回退到破坏性迁移（会清除数据）
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideWallpaperDao(
        database: AppDatabase
    ): WallpaperDao {
        return database.wallpaperDao()
    }
}