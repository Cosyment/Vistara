package com.vistara.aestheticwalls.di

import android.content.Context
import com.vistara.aestheticwalls.billing.BillingManager
import com.vistara.aestheticwalls.data.repository.DiamondRepository
import com.vistara.aestheticwalls.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 计费模块
 * 提供计费相关的依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    /**
     * 提供BillingManager单例
     */
    @Provides
    @Singleton
    fun provideBillingManager(
        @ApplicationContext context: Context,
        userRepository: UserRepository,
        diamondRepository: DiamondRepository
    ): BillingManager {
        return BillingManager(context, userRepository, diamondRepository)
    }
}
