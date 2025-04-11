package com.vistara.aestheticwalls.di

import com.vistara.aestheticwalls.data.mapper.PexelsMapper
import com.vistara.aestheticwalls.data.mapper.PixabayMapper
import com.vistara.aestheticwalls.data.mapper.UnsplashMapper
import com.vistara.aestheticwalls.data.mapper.WallhavenMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapperModule {

    @Provides
    @Singleton
    fun provideUnsplashMapper(): UnsplashMapper {
        return UnsplashMapper()
    }

    @Provides
    @Singleton
    fun providePexelsMapper(): PexelsMapper {
        return PexelsMapper()
    }

    @Provides
    @Singleton
    fun providePixabayMapper(): PixabayMapper {
        return PixabayMapper()
    }

    @Provides
    @Singleton
    fun provideWallhavenMapper(): WallhavenMapper {
        return WallhavenMapper()
    }
} 