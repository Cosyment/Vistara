package com.vistara.aestheticwalls.di

import com.vistara.aestheticwalls.data.repository.UserPrefsRepository
import com.vistara.aestheticwalls.manager.LocaleManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 语言管理模块
 * 提供LocaleManager的依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
object LocaleModule {
    
    /**
     * 提供LocaleManager单例
     */
    @Provides
    @Singleton
    fun provideLocaleManager(userPrefsRepository: UserPrefsRepository): LocaleManager {
        return LocaleManager(userPrefsRepository)
    }
}
