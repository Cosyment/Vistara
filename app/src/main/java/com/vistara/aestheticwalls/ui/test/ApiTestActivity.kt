package com.vistara.aestheticwalls.ui.test

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.mapper.PexelsMapper
import com.vistara.aestheticwalls.data.mapper.UnsplashMapper
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.remote.api.PexelsApiAdapter
import com.vistara.aestheticwalls.data.remote.api.PexelsApiService
import com.vistara.aestheticwalls.data.remote.api.UnsplashApiAdapter
import com.vistara.aestheticwalls.data.remote.api.UnsplashApiService
import com.vistara.aestheticwalls.ui.theme.VistaraTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * API测试Activity
 * 用于测试API接口的联通情况
 */
@AndroidEntryPoint
class ApiTestActivity : ComponentActivity() {

    @Inject
    lateinit var pexelsApiService: PexelsApiService

    @Inject
    lateinit var pexelsMapper: PexelsMapper

    @Inject
    lateinit var unsplashApiService: UnsplashApiService

    @Inject
    lateinit var unsplashMapper: UnsplashMapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VistaraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    ApiTestScreen(
                        onTestPexelsApi = { testPexelsApi() },
                        onTestUnsplashApi = { testUnsplashApi() })
                }
            }
        }
    }

    /**
     * 测试Pexels API
     */
    private fun testPexelsApi() {
        lifecycleScope.launch {
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
                        Log.d("ApiTest", "测试集合ID: $collectionId")
                        val collectionWallpapersResult =
                            pexelsApiAdapter.getWallpapersByCollection(collectionId, 1, 10)
                        logApiResult("getWallpapersByCollection", collectionWallpapersResult)
                    } catch (e: Exception) {
                        Log.e("ApiTest", "获取集合壁纸失败", e)
                        Toast.makeText(
                            this@ApiTestActivity,
                            "获取集合壁纸失败: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                Toast.makeText(
                    this@ApiTestActivity,
                    "Pexels API测试完成，请查看日志",
                    Toast.LENGTH_LONG
                )
                    .show()
            } catch (e: Exception) {
                Log.e("ApiTest", "测试过程中发生错误", e)
                Toast.makeText(this@ApiTestActivity, getString(R.string.test_failed, e.message), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    /**
     * 测试Unsplash API
     */
    private fun testUnsplashApi() {
        lifecycleScope.launch {
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
                        Log.d("ApiTest", "测试集合ID: $collectionId")
                        val collectionWallpapersResult =
                            unsplashApiAdapter.getWallpapersByCollection(collectionId, 1, 10)
                        logApiResult("getWallpapersByCollection", collectionWallpapersResult)
                    } catch (e: Exception) {
                        Log.e("ApiTest", "获取集合壁纸失败", e)
                        Toast.makeText(
                            this@ApiTestActivity,
                            "获取集合壁纸失败: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                // 测试跟踪下载
                if (featuredResult is ApiResult.Success && featuredResult.data.isNotEmpty()) {
                    val wallpaperId = featuredResult.data.first().id.split("_")[1]
                    val trackResult = unsplashApiAdapter.trackDownload(wallpaperId)
                    logApiResult("trackDownload", trackResult)
                }

                Toast.makeText(
                    this@ApiTestActivity,
                    "Unsplash API测试完成，请查看日志",
                    Toast.LENGTH_LONG
                )
                    .show()
            } catch (e: Exception) {
                Log.e("ApiTest", "测试过程中发生错误", e)
                Toast.makeText(this@ApiTestActivity, getString(R.string.test_failed, e.message), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    /**
     * 记录API结果
     */
    private fun <T> logApiResult(methodName: String, result: ApiResult<T>) {
        when (result) {
            is ApiResult.Success -> {
                Log.d("ApiTest", "✅ $methodName 成功")
                when (val data = result.data) {
                    is List<*> -> Log.d("ApiTest", "   返回 ${data.size} 条数据")
                    else -> Log.d("ApiTest", "   返回数据: $data")
                }
            }

            is ApiResult.Error -> {
                Log.e("ApiTest", "❌ $methodName 失败: ${result.message}")
            }

            is ApiResult.Loading -> {
                Log.d("ApiTest", "⏳ $methodName 加载中")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiTestScreen(
    onTestPexelsApi: () -> Unit,
    onTestUnsplashApi: () -> Unit
) {
    var testResults by remember { mutableStateOf<List<String>>(emptyList()) }

    val context = LocalActivity.current
    val onBackPressed: () -> Unit = { context?.finish() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API测试工具") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                })
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "API测试工具",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Button(
                onClick = onTestPexelsApi, modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("测试Pexels API")
            }

            Button(
                onClick = onTestUnsplashApi, modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("测试Unsplash API")
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            Text(
                text = "测试结果将显示在Logcat中",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "过滤标签: ApiTest",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
