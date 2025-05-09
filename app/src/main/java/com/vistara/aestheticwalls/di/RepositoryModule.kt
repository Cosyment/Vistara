package com.vistara.aestheticwalls.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.vistara.aestheticwalls.data.local.DiamondDao
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
import com.vistara.aestheticwalls.data.remote.api.WallpaperApiAdapter
import com.vistara.aestheticwalls.data.repository.AuthRepository
import com.vistara.aestheticwalls.data.repository.BannerRepository
import com.vistara.aestheticwalls.data.repository.BannerRepositoryImpl
import com.vistara.aestheticwalls.data.repository.DiamondRepository
import com.vistara.aestheticwalls.data.repository.DiamondRepositoryImpl
import com.vistara.aestheticwalls.data.repository.UserPrefsRepository
import com.vistara.aestheticwalls.data.repository.UserPrefsRepositoryImpl
import com.vistara.aestheticwalls.data.repository.UserRepository
import com.vistara.aestheticwalls.data.repository.UserRepositoryImpl
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import com.vistara.aestheticwalls.data.repository.WallpaperRepositoryImpl
import com.vistara.aestheticwalls.manager.ThemeManager
import com.vistara.aestheticwalls.utils.NetworkMonitor
import com.vistara.aestheticwalls.utils.StringProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideWallpaperRepository(
        @ApplicationContext context: Context,
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
        networkMonitor: NetworkMonitor,
        wallpaperApiAdapter: WallpaperApiAdapter,
        @Named("pexelsApiAdapter") pexelsApiAdapter: WallpaperApiAdapter,
        stringProvider: StringProvider
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
            networkMonitor = networkMonitor,
            wallpaperApiAdapter = wallpaperApiAdapter,
            pexelsApiAdapter = pexelsApiAdapter,
            stringProvider = stringProvider,
            context = context
        )
    }

    @Provides
    @Singleton
    fun provideUserPrefsRepository(
        dataStore: DataStore<Preferences>
    ): UserPrefsRepository {
        return UserPrefsRepositoryImpl(dataStore)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        dataStore: DataStore<Preferences>
    ): UserRepository {
        return UserRepositoryImpl(dataStore)
    }

    @Provides
    @Singleton
    fun provideBannerRepository(
        networkMonitor: NetworkMonitor, stringProvider: StringProvider
    ): BannerRepository {
        return BannerRepositoryImpl(networkMonitor, stringProvider)
    }

    @Provides
    @Singleton
    fun provideThemeManager(
        userPrefsRepository: UserPrefsRepository
    ): ThemeManager {
        return ThemeManager(userPrefsRepository)
    }

    @Provides
    @Singleton
    fun provideDiamondRepository(
        diamondDao: DiamondDao,
        authRepository: AuthRepository,
        billingManagerProvider: javax.inject.Provider<com.vistara.aestheticwalls.billing.BillingManager>,
        stringProvider: com.vistara.aestheticwalls.utils.StringProvider,
        apiService: com.vistara.aestheticwalls.data.remote.api.ApiService
    ): DiamondRepository {
        return DiamondRepositoryImpl(
            diamondDao, authRepository, billingManagerProvider, stringProvider, apiService
        )
    }
}