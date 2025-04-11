package com.vistara.aestheticwalls.di

import android.content.Context
import androidx.room.Room
import com.vistara.aestheticwalls.data.local.AppDatabase
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
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideWallpaperDao(
        database: AppDatabase
    ): WallpaperDao {
        return database.wallpaperDao()
    }
} 