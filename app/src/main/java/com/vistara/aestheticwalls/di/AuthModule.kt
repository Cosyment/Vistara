package com.vistara.aestheticwalls.di

import com.vistara.aestheticwalls.data.repository.AuthRepository
import com.vistara.aestheticwalls.data.repository.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 认证模块
 * 提供认证相关的依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    /**
     * 提供认证仓库
     */
    @Binds
    @Singleton
    abstract fun provideAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository
}
