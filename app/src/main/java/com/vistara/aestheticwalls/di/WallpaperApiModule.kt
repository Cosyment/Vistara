package com.vistara.aestheticwalls.di

import com.vistara.aestheticwalls.data.mapper.PexelsMapper
import com.vistara.aestheticwalls.data.remote.api.PexelsApiAdapter
import com.vistara.aestheticwalls.data.remote.api.PexelsApiService
import com.vistara.aestheticwalls.data.remote.api.WallpaperApiAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * 壁纸API模块
 * 提供壁纸API适配器的依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
object WallpaperApiModule {

    /**
     * 提供Pexels API适配器
     */
    @Provides
    @Singleton
    @Named("pexelsApiAdapter")
    fun providePexelsApiAdapter(
        pexelsApiService: PexelsApiService,
        pexelsMapper: PexelsMapper
    ): WallpaperApiAdapter {
        return PexelsApiAdapter(pexelsApiService, pexelsMapper)
    }
    
    /**
     * 提供默认的壁纸API适配器
     * 当前使用Pexels作为默认API
     */
    @Provides
    @Singleton
    fun provideDefaultWallpaperApiAdapter(
        @Named("pexelsApiAdapter") pexelsApiAdapter: WallpaperApiAdapter
    ): WallpaperApiAdapter {
        return pexelsApiAdapter
    }
}
