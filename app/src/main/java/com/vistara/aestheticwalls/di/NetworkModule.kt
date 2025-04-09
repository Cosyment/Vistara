package com.vistara.aestheticwalls.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.vistara.aestheticwalls.data.remote.ApiKeyManager
import com.vistara.aestheticwalls.data.remote.api.PexelsApiService
import com.vistara.aestheticwalls.data.remote.api.PixabayApiService
import com.vistara.aestheticwalls.data.remote.api.UnsplashApiService
import com.vistara.aestheticwalls.data.remote.api.WallhavenApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * 网络模块
 * 提供Retrofit和API服务的依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * 提供Gson实例，用于JSON序列化/反序列化
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    /**
     * 提供OkHttp缓存
     */
    @Provides
    @Singleton
    fun provideOkHttpCache(@ApplicationContext context: Context): Cache {
        val cacheSize = 10L * 1024L * 1024L // 10 MB
        return Cache(context.cacheDir, cacheSize)
    }

    /**
     * 提供日志拦截器
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(apiKeyManager: ApiKeyManager): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (apiKeyManager.isDebugMode()) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    /**
     * 提供基础OkHttpClient实例
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(cache: Cache, loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)
                
                // 缓存控制
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=600")
                    .build()
            }
            .build()
    }

    /**
     * 提供基础Retrofit实例，后续会为各个API服务创建特定的Service实例
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/") // 将在具体的API Module中替换
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }

    /**
     * 以下是各API服务特定的配置
     */

    // Unsplash API
    @Provides
    @Singleton
    @Named("unsplashAuthInterceptor")
    fun provideUnsplashAuthInterceptor(apiKeyManager: ApiKeyManager): Interceptor {
        return Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Client-ID ${apiKeyManager.getUnsplashApiKey()}")
                .build()
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    @Named("unsplashHttpClient")
    fun provideUnsplashHttpClient(
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor,
        @Named("unsplashAuthInterceptor") authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("unsplashRetrofit")
    fun provideUnsplashRetrofit(
        gson: Gson,
        @Named("unsplashHttpClient") client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(UnsplashApiService.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideUnsplashApiService(
        @Named("unsplashRetrofit") retrofit: Retrofit
    ): UnsplashApiService {
        return retrofit.create(UnsplashApiService::class.java)
    }

    // Pexels API
    @Provides
    @Singleton
    @Named("pexelsAuthInterceptor")
    fun providePexelsAuthInterceptor(apiKeyManager: ApiKeyManager): Interceptor {
        return Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", apiKeyManager.getPexelsApiKey())
                .build()
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    @Named("pexelsHttpClient")
    fun providePexelsHttpClient(
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor,
        @Named("pexelsAuthInterceptor") authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("pexelsRetrofit")
    fun providePexelsRetrofit(
        gson: Gson,
        @Named("pexelsHttpClient") client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(PexelsApiService.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun providePexelsApiService(
        @Named("pexelsRetrofit") retrofit: Retrofit
    ): PexelsApiService {
        return retrofit.create(PexelsApiService::class.java)
    }

    // Pixabay API
    @Provides
    @Singleton
    @Named("pixabayAuthInterceptor")
    fun providePixabayAuthInterceptor(apiKeyManager: ApiKeyManager): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url
            
            // 添加API密钥作为查询参数
            val url = originalUrl.newBuilder()
                .addQueryParameter("key", apiKeyManager.getPixabayApiKey())
                .build()
            
            val request = originalRequest.newBuilder()
                .url(url)
                .build()
            
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    @Named("pixabayHttpClient")
    fun providePixabayHttpClient(
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor,
        @Named("pixabayAuthInterceptor") authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("pixabayRetrofit")
    fun providePixabayRetrofit(
        gson: Gson,
        @Named("pixabayHttpClient") client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(PixabayApiService.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun providePixabayApiService(
        @Named("pixabayRetrofit") retrofit: Retrofit
    ): PixabayApiService {
        return retrofit.create(PixabayApiService::class.java)
    }

    // Wallhaven API
    @Provides
    @Singleton
    @Named("wallhavenAuthInterceptor")
    fun provideWallhavenAuthInterceptor(apiKeyManager: ApiKeyManager): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url
            
            // 添加API密钥作为查询参数
            val url = originalUrl.newBuilder()
                .addQueryParameter("apikey", apiKeyManager.getWallhavenApiKey())
                .build()
            
            val request = originalRequest.newBuilder()
                .url(url)
                .build()
            
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    @Named("wallhavenHttpClient")
    fun provideWallhavenHttpClient(
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor,
        @Named("wallhavenAuthInterceptor") authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("wallhavenRetrofit")
    fun provideWallhavenRetrofit(
        gson: Gson,
        @Named("wallhavenHttpClient") client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(WallhavenApiService.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideWallhavenApiService(
        @Named("wallhavenRetrofit") retrofit: Retrofit
    ): WallhavenApiService {
        return retrofit.create(WallhavenApiService::class.java)
    }
}
