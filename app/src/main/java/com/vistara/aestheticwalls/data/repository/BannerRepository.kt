package com.vistara.aestheticwalls.data.repository

import com.vistara.aestheticwalls.data.model.Banner
import com.vistara.aestheticwalls.data.remote.ApiResult

/**
 * Banner 仓库接口
 * 负责获取首页轮播 Banner 数据
 */
interface BannerRepository {
    /**
     * 获取首页轮播 Banner 数据
     * @return Banner 列表
     */
    suspend fun getHomeBanners(): ApiResult<List<Banner>>
}
