package com.vistara.aestheticwalls.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.vistara.aestheticwalls.data.local.WallpaperDao
import com.vistara.aestheticwalls.data.mapper.PexelsMapper
import com.vistara.aestheticwalls.data.mapper.PixabayMapper
import com.vistara.aestheticwalls.data.mapper.UnsplashMapper
import com.vistara.aestheticwalls.data.mapper.WallhavenMapper
import com.vistara.aestheticwalls.data.remote.ApiLoadBalancer
import com.vistara.aestheticwalls.data.remote.ApiUsageTracker
import com.vistara.aestheticwalls.data.remote.api.PexelsApiService
import com.vistara.aestheticwalls.data.remote.api.PixabayApiService
import com.vistara.aestheticwalls.data.remote.api.UnsplashApiService
import com.vistara.aestheticwalls.data.remote.api.WallhavenApiService
import com.vistara.aestheticwalls.utils.NetworkMonitor
import com.vistara.aestheticwalls.data.repository.UserPrefsRepository
import com.vistara.aestheticwalls.data.repository.UserPrefsRepositoryImpl
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import com.vistara.aestheticwalls.data.repository.WallpaperRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideWallpaperRepository(
        unsplashApiService: UnsplashApiService,
        pexelsApiService: PexelsApiService,
        pixabayApiService: PixabayApiService,
        wallhavenApiService: WallhavenApiService,
        unsplashMapper: UnsplashMapper,
        pexelsMapper: PexelsMapper,
        pixabayMapper: PixabayMapper,
        wallhavenMapper: WallhavenMapper,
        wallpaperDao: WallpaperDao,
        apiLoadBalancer: ApiLoadBalancer,
        apiUsageTracker: ApiUsageTracker,
        networkMonitor: NetworkMonitor
    ): WallpaperRepository {
        return WallpaperRepositoryImpl(
            unsplashApiService = unsplashApiService,
            pexelsApiService = pexelsApiService,
            pixabayApiService = pixabayApiService,
            wallhavenApiService = wallhavenApiService,
            unsplashMapper = unsplashMapper,
            pexelsMapper = pexelsMapper,
            pixabayMapper = pixabayMapper,
            wallhavenMapper = wallhavenMapper,
            wallpaperDao = wallpaperDao,
            apiLoadBalancer = apiLoadBalancer,
            apiUsageTracker = apiUsageTracker,
            networkMonitor = networkMonitor
        )
    }

    @Provides
    @Singleton
    fun provideUserPrefsRepository(
        dataStore: DataStore<Preferences>
    ): UserPrefsRepository {
        return UserPrefsRepositoryImpl(dataStore)
    }
}