package com.vistara.aestheticwalls.data.repository

import android.content.Context
import com.vistara.aestheticwalls.data.model.Banner
import com.vistara.aestheticwalls.data.model.BannerActionType
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.remote.ApiSource
import com.vistara.aestheticwalls.utils.NetworkMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Banner 仓库实现类
 * 负责获取首页轮播 Banner 数据
 */
@Singleton
class BannerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkMonitor: NetworkMonitor
) : BannerRepository {

    /**
     * 获取首页轮播 Banner 数据
     * 目前从本地资源获取，后续可以改为从远程 API 获取
     */
    override suspend fun getHomeBanners(): ApiResult<List<Banner>> = withContext(Dispatchers.IO) {
        try {
            // 检查网络连接
            if (!networkMonitor.isNetworkAvailable()) {
                return@withContext ApiResult.Error(
                    message = "无网络连接，无法获取轮播图数据",
                    source = ApiSource.UNSPLASH // 使用一个默认的API来源
                )
            }

            // 从本地资源获取 Banner 数据
            // 实际应用中，这里应该从远程 API 获取
            val banners = listOf(
                Banner(
                    id = "1",
                    imageUrl = "https://picsum.photos/id/237/800/400",
                    title = "精选壁纸",
                    subtitle = "发现最新最美壁纸",
                    actionType = BannerActionType.WALLPAPER,
                    actionTarget = "unsplash_Dwu85P9SOIk"
                ),
                Banner(
                    id = "2",
                    imageUrl = "https://picsum.photos/id/1015/800/400",
                    title = "高级会员",
                    subtitle = "解锁所有高清壁纸",
                    actionType = BannerActionType.PREMIUM,
                    actionTarget = "premium"
                ),
                Banner(
                    id = "3",
                    imageUrl = "https://picsum.photos/id/1018/800/400",
                    title = "动态壁纸",
                    subtitle = "让你的屏幕动起来",
                    actionType = BannerActionType.WALLPAPER,
                    actionTarget = "pexels_photo_2014422"
                )
            )

            ApiResult.Success(banners)
        } catch (e: Exception) {
            ApiResult.Error(
                message = e.message ?: "获取轮播图数据失败",
                source = ApiSource.UNSPLASH // 使用一个默认的API来源
            )
        }
    }
}
