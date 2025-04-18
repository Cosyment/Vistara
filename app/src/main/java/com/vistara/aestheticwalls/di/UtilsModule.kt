package com.vistara.aestheticwalls.di

import android.content.Context
import com.vistara.aestheticwalls.utils.NetworkMonitor
import com.vistara.aestheticwalls.utils.NetworkUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 工具模块
 * 提供各种工具类的依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {

    /**
     * 提供网络状态监听器
     */
    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitor(context)
    }

    /**
     * 提供网络工具类
     */
    @Provides
    @Singleton
    fun provideNetworkUtil(@ApplicationContext context: Context): NetworkUtil {
        return NetworkUtil(context)
    }
}
