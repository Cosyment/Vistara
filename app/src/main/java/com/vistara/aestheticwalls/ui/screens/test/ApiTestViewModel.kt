package com.vistara.aestheticwalls.ui.screens.test

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.mapper.PexelsMapper
import com.vistara.aestheticwalls.data.mapper.UnsplashMapper
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.remote.api.PexelsApiAdapter
import com.vistara.aestheticwalls.data.remote.api.PexelsApiService
import com.vistara.aestheticwalls.data.remote.api.UnsplashApiAdapter
import com.vistara.aestheticwalls.data.remote.api.UnsplashApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * API测试ViewModel
 * 用于测试API接口的联通情况
 */
@HiltViewModel
class ApiTestViewModel @Inject constructor(
    private val pexelsApiService: PexelsApiService,
    private val pexelsMapper: PexelsMapper,
    private val unsplashApiService: UnsplashApiService,
    private val unsplashMapper: UnsplashMapper,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "ApiTest"
    }

    // 测试结果
    private val _testResults = MutableStateFlow<List<String>>(emptyList())
    val testResults: StateFlow<List<String>> = _testResults.asStateFlow()

    // 测试状态
    private val _isTestingPexels = MutableStateFlow(false)
    val isTestingPexels: StateFlow<Boolean> = _isTestingPexels.asStateFlow()

    private val _isTestingUnsplash = MutableStateFlow(false)
    val isTestingUnsplash: StateFlow<Boolean> = _isTestingUnsplash.asStateFlow()

    // 测试结果消息
    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage: StateFlow<String?> = _resultMessage.asStateFlow()

    /**
     * 测试Pexels API
     */
    fun testPexelsApi() {
        viewModelScope.launch {
            _isTestingPexels.value = true
            try {
                // 创建PexelsApiAdapter
                val pexelsApiAdapter = PexelsApiAdapter(pexelsApiService, pexelsMapper)

                // 测试获取精选壁纸
                val featuredResult = pexelsApiAdapter.getFeaturedWallpapers(1, 10)
                logApiResult("getFeaturedWallpapers", featuredResult)

                // 测试搜索壁纸
                val searchResult = pexelsApiAdapter.searchWallpapers("nature", 1, 10, emptyMap())
                logApiResult("searchWallpapers", searchResult)

                // 测试获取随机壁纸
                val randomResult = pexelsApiAdapter.getRandomWallpapers(5)
                logApiResult("getRandomWallpapers", randomResult)

                // 测试获取集合
                val collectionsResult = pexelsApiAdapter.getCollections(1, 10)
                logApiResult("getCollections", collectionsResult)

                // 如果有集合，测试获取集合中的壁纸
                if (collectionsResult is ApiResult.Success && collectionsResult.data.isNotEmpty()) {
                    try {
                        val collectionId = collectionsResult.data.first().id.split("_")[1]
                        Log.d(TAG, "测试集合ID: $collectionId")
                        val collectionWallpapersResult =
                            pexelsApiAdapter.getWallpapersByCollection(collectionId, 1, 10)
                        logApiResult("getWallpapersByCollection", collectionWallpapersResult)
                    } catch (e: Exception) {
                        Log.e(TAG, "获取集合壁纸失败", e)
                        addTestResult("❌ 获取集合壁纸失败: ${e.message}")
                    }
                }

                _resultMessage.value = context.getString(R.string.pexels_api_test_complete)
            } catch (e: Exception) {
                Log.e(TAG, "测试过程中发生错误", e)
                _resultMessage.value = context.getString(R.string.test_failed, e.message)
            } finally {
                _isTestingPexels.value = false
            }
        }
    }

    /**
     * 测试Unsplash API
     */
    fun testUnsplashApi() {
        viewModelScope.launch {
            _isTestingUnsplash.value = true
            try {
                // 创建UnsplashApiAdapter
                val unsplashApiAdapter = UnsplashApiAdapter(unsplashApiService, unsplashMapper)

                // 测试获取精选壁纸
                val featuredResult = unsplashApiAdapter.getFeaturedWallpapers(1, 10)
                logApiResult("getFeaturedWallpapers", featuredResult)

                // 测试搜索壁纸
                val searchResult = unsplashApiAdapter.searchWallpapers("nature", 1, 10, emptyMap())
                logApiResult("searchWallpapers", searchResult)

                // 测试获取随机壁纸
                val randomResult = unsplashApiAdapter.getRandomWallpapers(5)
                logApiResult("getRandomWallpapers", randomResult)

                // 测试获取集合
                val collectionsResult = unsplashApiAdapter.getCollections(1, 10)
                logApiResult("getCollections", collectionsResult)

                // 如果有集合，测试获取集合中的壁纸
                if (collectionsResult is ApiResult.Success && collectionsResult.data.isNotEmpty()) {
                    try {
                        val collectionId = collectionsResult.data.first().id.split("_")[1]
                        Log.d(TAG, "测试集合ID: $collectionId")
                        val collectionWallpapersResult =
                            unsplashApiAdapter.getWallpapersByCollection(collectionId, 1, 10)
                        logApiResult("getWallpapersByCollection", collectionWallpapersResult)
                    } catch (e: Exception) {
                        Log.e(TAG, "获取集合壁纸失败", e)
                        addTestResult("❌ 获取集合壁纸失败: ${e.message}")
                    }
                }

                // 测试跟踪下载
                if (featuredResult is ApiResult.Success && featuredResult.data.isNotEmpty()) {
                    val wallpaperId = featuredResult.data.first().id.split("_")[1]
                    val trackResult = unsplashApiAdapter.trackDownload(wallpaperId)
                    logApiResult("trackDownload", trackResult)
                }

                _resultMessage.value = context.getString(R.string.unsplash_api_test_complete)
            } catch (e: Exception) {
                Log.e(TAG, "测试过程中发生错误", e)
                _resultMessage.value = context.getString(R.string.test_failed, e.message)
            } finally {
                _isTestingUnsplash.value = false
            }
        }
    }

    /**
     * 记录API结果
     */
    private fun <T> logApiResult(methodName: String, result: ApiResult<T>) {
        when (result) {
            is ApiResult.Success -> {
                val message = "✅ $methodName 成功"
                Log.d(TAG, message)
                when (val data = result.data) {
                    is List<*> -> {
                        val dataMessage = "   返回 ${data.size} 条数据"
                        Log.d(TAG, dataMessage)
                        addTestResult("$message\n$dataMessage")
                    }
                    else -> {
                        val dataMessage = "   返回数据: $data"
                        Log.d(TAG, dataMessage)
                        addTestResult("$message\n$dataMessage")
                    }
                }
            }

            is ApiResult.Error -> {
                val message = "❌ $methodName 失败: ${result.message}"
                Log.e(TAG, message)
                addTestResult(message)
            }

            is ApiResult.Loading -> {
                val message = "⏳ $methodName 加载中"
                Log.d(TAG, message)
                addTestResult(message)
            }
        }
    }

    /**
     * 添加测试结果
     */
    private fun addTestResult(result: String) {
        val currentResults = _testResults.value.toMutableList()
        currentResults.add(result)
        _testResults.value = currentResults
    }

    /**
     * 清除测试结果
     */
    fun clearTestResults() {
        _testResults.value = emptyList()
    }

    /**
     * 清除结果消息
     */
    fun clearResultMessage() {
        _resultMessage.value = null
    }
}
