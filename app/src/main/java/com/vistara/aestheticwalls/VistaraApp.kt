package com.vistara.aestheticwalls

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.vistara.aestheticwalls.manager.LocaleManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Vistara壁纸应用的Application类
 * 集成Hilt依赖注入框架
 */
@HiltAndroidApp
class VistaraApp : Application() {

    companion object {
        private const val AF_DEV_KEY = "pYBryM6RbSH6fTTYXAQ4xM"
        private const val TAG = "VistaraApp"
    }

    @Inject
    lateinit var localeManager: LocaleManager

    override fun onCreate() {
        super.onCreate()

        // 初始化AppsFlyer
        initAppsFlyer()

        // 启用应用内语言切换
        AppCompatDelegate.setApplicationLocales(AppCompatDelegate.getApplicationLocales())
    }

    private fun initAppsFlyer() {
        // 创建转化数据监听器
        val conversionDataListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
                data?.let { conversionData ->
                    Log.d(TAG, "转化数据获取成功: $conversionData")

                    // 获取媒体来源
                    val mediaSource = conversionData["media_source"]
                    val campaign = conversionData["campaign"]
                    val adSet = conversionData["adset"]
                    val adGroup = conversionData["adgroup"]
                    val af_status = conversionData["af_status"]

                    // 判断是否是首次安装
                    if (af_status == "Non-organic") {
                        // 非自然安装,来自广告
                        Log.d(TAG, "这是一个来自广告的安装")
                        // 可以在这里处理广告带来的安装,例如显示特定优惠等
                    } else if (af_status == "Organic") {
                        // 自然安装
                        Log.d(TAG, "这是一个自然安装")
                    }

                    // 处理campaign数据
                    campaign?.let {
                        // 例如: campaign可能包含特定优惠信息
                        handleCampaignData(it.toString())
                    }
                }
            }

            override fun onConversionDataFail(error: String?) {
                Log.e(TAG, "转化数据获取失败: $error")
            }

            override fun onAppOpenAttribution(data: MutableMap<String, String>?) {
                // 处理深度链接归因
                data?.let { attributionData ->
                    Log.d(TAG, "应用打开归因数据: $attributionData")

                    // 获取深度链接参数
                    val deepLinkValue = attributionData["deep_link_value"]
                    val mediaSource = attributionData["media_source"]
                    val campaign = attributionData["campaign"]

                    // 处理深度链接
                    deepLinkValue?.let {
                        handleDeepLink(it)
                    }
                }
            }

            override fun onAttributionFailure(error: String?) {
                Log.e(TAG, "归因失败: $error")
            }
        }

        AppsFlyerLib.getInstance().apply {
            // 初始化SDK时传入转化数据监听器
            init("pYBryM6RbSH6fTTYXAQ4xM", conversionDataListener, this@VistaraApp)

            setCollectAndroidID(true)

            // 设置Debug日志
            setDebugLog(BuildConfig.DEBUG)

            // 开始会话
            start(this@VistaraApp)
        }
    }

    private fun handleDeepLink(deepLinkValue: String?) {
        Log.d(TAG, "处理深度链接: $deepLinkValue")
        when {
            deepLinkValue?.startsWith("wallpaper/") == true -> {
                val wallpaperId = deepLinkValue.substringAfter("wallpaper/")
                // 跳转到壁纸详情
                // 可以使用EventBus或其他方式通知Activity处理导航
            }

            deepLinkValue?.startsWith("category/") == true -> {
                val category = deepLinkValue.substringAfter("category/")
                // 跳转到分类页面
            }

            deepLinkValue?.startsWith("premium/") == true -> {
                // 处理高级会员相关的深度链接
            }
        }
    }

    private fun handleCampaignData(campaign: String) {
        Log.d(TAG, "处理campaign数据: $campaign")
        // 例如: "summer_sale_50off"
        when {
            campaign.contains("summer_sale") -> {
                // 处理夏季促销活动
            }

            campaign.contains("new_user") -> {
                // 处理新用户活动
            }
        }
    }
}
