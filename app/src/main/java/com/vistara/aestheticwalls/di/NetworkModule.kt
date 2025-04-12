package com.vistara.aestheticwalls.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.vistara.aestheticwalls.BuildConfig
import com.vistara.aestheticwalls.data.remote.ApiKeyManager
import com.vistara.aestheticwalls.data.remote.ApiUsageTracker
import com.vistara.aestheticwalls.data.remote.ApiSource
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
        val cacheSize = 50L * 1024L * 1024L // 50 MB
        return Cache(context.cacheDir, cacheSize)
    }

    /**
     * 提供日志拦截器
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    /**
     * 提供缓存拦截器
     */
    @Provides
    @Singleton
    fun provideCacheInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val url = request.url.toString()

            // 根据请求类型设置不同的缓存策略
            val cacheControl = when {
                // 搜索结果缓存较短时间
                url.contains("search") -> "public, max-age=300"
                // 详情页可以缓存更长时间
                url.contains("photos/") || url.contains("w/") -> "public, max-age=3600"
                // 默认缓存策略
                else -> "public, max-age=600"
            }

            val response = chain.proceed(request)
            response.newBuilder()
                .header("Cache-Control", cacheControl)
                .build()
        }
    }

    /**
     * 提供离线模式拦截器
     */
    @Provides
    @Singleton
    fun provideOfflineInterceptor(@ApplicationContext context: Context): Interceptor {
        return Interceptor { chain ->
            var request = chain.request()

            // 检查网络连接
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            val isConnected = networkInfo != null && networkInfo.isConnected

            if (!isConnected) {
                // 如果无网络连接，使用缓存
                request = request.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=2419200") // 4周
                    .build()
            }

            chain.proceed(request)
        }
    }

    /**
     * 提供基础OkHttpClient实例
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor,
        cacheInterceptor: Interceptor,
        offlineInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(offlineInterceptor) // 离线拦截器先执行
            .addNetworkInterceptor(cacheInterceptor) // 网络拦截器后执行
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
    fun provideUnsplashAuthInterceptor(
        apiKeyManager: ApiKeyManager,
        apiUsageTracker: ApiUsageTracker
    ): Interceptor {
        return Interceptor { chain ->
            // 跟踪API调用
            apiUsageTracker.trackApiCall(ApiSource.UNSPLASH)

            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Client-ID ${apiKeyManager.getUnsplashApiKey()}")
                .build()

            val response = chain.proceed(request)

            // 跟踪API响应
            if (response.isSuccessful) {
                apiUsageTracker.trackApiSuccess(ApiSource.UNSPLASH)
            } else {
                apiUsageTracker.trackApiError(ApiSource.UNSPLASH, "HTTP ${response.code}")
            }

            response
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
    fun providePexelsAuthInterceptor(
        apiKeyManager: ApiKeyManager,
        apiUsageTracker: ApiUsageTracker
    ): Interceptor {
        return Interceptor { chain ->
            // 跟踪API调用
            apiUsageTracker.trackApiCall(ApiSource.PEXELS)

            val request = chain.request().newBuilder()
                .addHeader("Authorization", apiKeyManager.getPexelsApiKey())
                .build()

            val response = chain.proceed(request)

            // 跟踪API响应
            if (response.isSuccessful) {
                apiUsageTracker.trackApiSuccess(ApiSource.PEXELS)
            } else {
                apiUsageTracker.trackApiError(ApiSource.PEXELS, "HTTP ${response.code}")
            }

            response
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
    fun providePixabayAuthInterceptor(
        apiKeyManager: ApiKeyManager,
        apiUsageTracker: ApiUsageTracker
    ): Interceptor {
        return Interceptor { chain ->
            // 跟踪API调用
            apiUsageTracker.trackApiCall(ApiSource.PIXABAY)

            val originalRequest = chain.request()
            val originalUrl = originalRequest.url

            // 添加API密钥作为查询参数
            val url = originalUrl.newBuilder()
                .addQueryParameter("key", apiKeyManager.getPixabayApiKey())
                .build()

            val request = originalRequest.newBuilder()
                .url(url)
                .build()

            val response = chain.proceed(request)

            // 跟踪API响应
            if (response.isSuccessful) {
                apiUsageTracker.trackApiSuccess(ApiSource.PIXABAY)
            } else {
                apiUsageTracker.trackApiError(ApiSource.PIXABAY, "HTTP ${response.code}")
            }

            response
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
    fun provideWallhavenAuthInterceptor(
        apiKeyManager: ApiKeyManager,
        apiUsageTracker: ApiUsageTracker
    ): Interceptor {
        return Interceptor { chain ->
            // 跟踪API调用
            apiUsageTracker.trackApiCall(ApiSource.WALLHAVEN)

            val originalRequest = chain.request()
            val originalUrl = originalRequest.url

            // 添加API密钥作为查询参数
            val url = originalUrl.newBuilder()
                .addQueryParameter("apikey", apiKeyManager.getWallhavenApiKey())
                .build()

            val request = originalRequest.newBuilder()
                .url(url)
                .build()

            val response = chain.proceed(request)

            // 跟踪API响应
            if (response.isSuccessful) {
                apiUsageTracker.trackApiSuccess(ApiSource.WALLHAVEN)
            } else {
                apiUsageTracker.trackApiError(ApiSource.WALLHAVEN, "HTTP ${response.code}")
            }

            response
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
