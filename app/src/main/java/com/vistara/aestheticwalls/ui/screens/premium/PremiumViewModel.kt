package com.vistara.aestheticwalls.ui.screens.premium

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.billing.BillingConnectionState
import com.vistara.aestheticwalls.billing.BillingManager
import com.vistara.aestheticwalls.billing.PurchaseState
import com.vistara.aestheticwalls.data.repository.UserRepository
import com.vistara.aestheticwalls.manager.ThemeManager
import com.vistara.aestheticwalls.utils.Constants.PRIVACY_POLICY_URL
import com.vistara.aestheticwalls.utils.Constants.TERMS_OF_SERVICE_URL
import com.vistara.aestheticwalls.utils.StringProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.URLEncoder
import javax.inject.Inject

/**
 * 升级页面的ViewModel
 */
@HiltViewModel
class PremiumViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val billingManager: BillingManager,
    private val stringProvider: StringProvider,
    private val themeManager: ThemeManager
) : ViewModel() {

    companion object {
        private const val TAG = "PremiumViewModel"
    }

    // 导航控制器
    private var navController: NavController? = null

    private val _darkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()


    // 高级用户状态
    private val _isPremiumUser = MutableStateFlow(false)
    val isPremiumUser: StateFlow<Boolean> = _isPremiumUser.asStateFlow()

    // 升级中状态
    private val _isUpgrading = MutableStateFlow(false)
    val isUpgrading: StateFlow<Boolean> = _isUpgrading.asStateFlow()

    // 升级结果
    private val _upgradeResult = MutableStateFlow<UpgradeResult?>(null)
    val upgradeResult: StateFlow<UpgradeResult?> = _upgradeResult.asStateFlow()

    // 选中的套餐
    private val _selectedPlan = MutableStateFlow(PremiumPlan.MONTHLY)
    val selectedPlan: StateFlow<PremiumPlan> = _selectedPlan.asStateFlow()

    // 计费连接状态
    private val _billingConnectionState = MutableStateFlow(BillingConnectionState.DISCONNECTED)
    val billingConnectionState: StateFlow<BillingConnectionState> =
        _billingConnectionState.asStateFlow()

    // 商品价格
    private val _productPrices = MutableStateFlow<Map<String, String>>(emptyMap())
    val productPrices: StateFlow<Map<String, String>> = _productPrices.asStateFlow()

    init {
        checkPremiumStatus()
        observeBillingState()
        observePurchaseState()
        observeDarkTheme()
    }

    private fun observeDarkTheme() {
        viewModelScope.launch {
            _darkTheme.value = themeManager.isDarkTheme().first()
        }
    }

    /**
     * 检查高级用户状态
     */
    private fun checkPremiumStatus() {
        viewModelScope.launch {
            try {
                val isPremium = userRepository.isPremiumUser.first()
                _isPremiumUser.value = isPremium
                Log.d(TAG, "Premium status: $isPremium")
            } catch (e: Exception) {
                Log.e(TAG, "Error checking premium status: ${e.message}")
            }
        }
    }

    /**
     * 观察计费状态
     */
    private fun observeBillingState() {
        viewModelScope.launch {
            billingManager.connectionState.collectLatest { state ->
                _billingConnectionState.value = state
                Log.d(TAG, "Billing connection state: $state")

                if (state == BillingConnectionState.CONNECTED) {
                    updateProductPrices()
                }
            }
        }
    }

    /**
     * 观察购买状态
     */
    private fun observePurchaseState() {
        viewModelScope.launch {
            billingManager.purchaseState.collectLatest { state ->
                when (state) {
                    is PurchaseState.Pending -> {
                        _isUpgrading.value = true
                    }

                    is PurchaseState.Completed -> {
                        _isUpgrading.value = false
                        _isPremiumUser.value = true
                        _upgradeResult.value =
                            UpgradeResult.Success(stringProvider.getString(R.string.upgrade_success))
                    }

                    is PurchaseState.Failed -> {
                        _isUpgrading.value = false
                        _upgradeResult.value = UpgradeResult.Error(
                            stringProvider.getString(
                                R.string.upgrade_failed,
                                state.message
                            )
                        )
                    }

                    is PurchaseState.Cancelled -> {
                        _isUpgrading.value = false
                        _upgradeResult.value =
                            UpgradeResult.Error(stringProvider.getString(R.string.upgrade_cancelled))
                    }

                    is PurchaseState.Restoring -> {
                        _isUpgrading.value = true
                    }

                    else -> {
                        _isUpgrading.value = false
                    }
                }
            }
        }
    }

    /**
     * 更新商品价格
     */
    private fun updateProductPrices() {
        val prices = mutableMapOf<String, String>()

        // 月度套餐
        prices[BillingManager.SUBSCRIPTION_WEEKLY] =
            billingManager.getProductPrice(BillingManager.SUBSCRIPTION_WEEKLY)
        // 周套餐
        prices[BillingManager.SUBSCRIPTION_MONTHLY] =
            billingManager.getProductPrice(BillingManager.SUBSCRIPTION_MONTHLY)
        // 季度套餐
        prices[BillingManager.SUBSCRIPTION_QUARTERLY] =
            billingManager.getProductPrice(BillingManager.SUBSCRIPTION_QUARTERLY)
        // 年度套餐
//        prices[BillingManager.SUBSCRIPTION_YEARLY] = billingManager.getProductPrice(BillingManager.SUBSCRIPTION_YEARLY)

        // 终身套餐
//        prices[BillingManager.PREMIUM_LIFETIME] = billingManager.getProductPrice(BillingManager.PREMIUM_LIFETIME)

        _productPrices.value = prices
    }

    /**
     * 选择套餐
     */
    fun selectPlan(plan: PremiumPlan) {
        _selectedPlan.value = plan
    }

    /**
     * 升级到高级版
     */
    fun upgrade(activity: Activity?) {
        if (_isPremiumUser.value) {
            _upgradeResult.value =
                UpgradeResult.Error(stringProvider.getString(R.string.already_premium_user))
            return
        }

        if (_billingConnectionState.value != BillingConnectionState.CONNECTED) {
            _upgradeResult.value =
                UpgradeResult.Error(stringProvider.getString(R.string.payment_service_not_connected))
            return
        }

        // 根据选择的套餐确定商品ID
        val productId = when (_selectedPlan.value) {
            PremiumPlan.WEEKLY -> BillingManager.SUBSCRIPTION_WEEKLY
            PremiumPlan.MONTHLY -> BillingManager.SUBSCRIPTION_MONTHLY
            PremiumPlan.QUARTERLY -> BillingManager.SUBSCRIPTION_QUARTERLY
            PremiumPlan.YEARLY -> TODO()
            PremiumPlan.LIFETIME -> TODO()
        }

        // 启动购买流程
        billingManager.launchBillingFlow(activity, productId)
    }

    /**
     * 恢复购买
     */
    fun restorePurchases() {
        if (_billingConnectionState.value != BillingConnectionState.CONNECTED) {
            _upgradeResult.value =
                UpgradeResult.Error(stringProvider.getString(R.string.payment_service_not_connected))
            return
        }

        billingManager.restorePurchases()
    }

    /**
     * 清除升级结果
     */
    fun clearUpgradeResult() {
        _upgradeResult.value = null
    }

    /**
     * 设置导航控制器
     */
    fun setNavController(controller: NavController) {
        navController = controller
    }

    /**
     * 打开隐私政策
     */
    fun openPrivacyPolicy() {
        openInWebView(PRIVACY_POLICY_URL, context.getString(R.string.privacy_policy))
    }

    /**
     * 打开服务条款
     */
    fun openTermsOfService() {
        openInWebView(TERMS_OF_SERVICE_URL, context.getString(R.string.terms_of_use))
    }

    /**
     * 在WebView中打开URL
     */
    private fun openInWebView(url: String, title: String) {
        try {
            navController?.let { nav ->
                // 使用 URLEncoder 对 URL 和标题进行编码
                val encodedUrl = URLEncoder.encode(url, "UTF-8")
                val encodedTitle = URLEncoder.encode(title, "UTF-8")
                val route = "webview?url=$encodedUrl"
                Log.d(TAG, "Navigating to WebView with route: $route")
                nav.navigate(route)
            } ?: run {
                Log.d(TAG, "NavController is null, opening URL in external browser: $url")
                openExternalUrl(url) // 如果没有导航控制器，则使用外部浏览器
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to WebView: $url, ${e.message}")
            openExternalUrl(url) // 如果导航失败，则使用外部浏览器
        }
    }

    /**
     * 在外部浏览器中打开URL
     */
    private fun openExternalUrl(url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening URL in external browser: $url, ${e.message}")
            false
        }
    }

    /**
     * 打开订阅管理页面
     * 跳转到 Google Play 商店的订阅管理页面
     * @param activity 当前 Activity
     */
    fun openSubscriptionManagementPage(activity: Activity?) {
        if (activity == null) {
            _upgradeResult.value =
                UpgradeResult.Error(stringProvider.getString(R.string.unknown_error))
            return
        }

        try {
            // 打开 Google Play 商店的订阅管理页面
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = "https://play.google.com/store/account/subscriptions".toUri()
                setPackage("com.android.vending") // Google Play 商店包名
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening subscription management page: ${e.message}")

            // 如果无法打开特定页面，则打开 Google Play 商店主页
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = "https://play.google.com/store/account".toUri()
                }
                activity.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "Error opening Google Play: ${e2.message}")
                _upgradeResult.value =
                    UpgradeResult.Error(stringProvider.getString(R.string.unknown_error))
            }
        }
    }
}

/**
 * 升级套餐
 */
enum class PremiumPlan(
    val titleResId: Int,
    val descriptionResId: Int,
    val discountResId: Int? = null
) {
    WEEKLY(R.string.weekly_plan, R.string.weekly_plan_description),
    MONTHLY(R.string.monthly_plan, R.string.monthly_plan_description),

    QUARTERLY(R.string.quarterly_plan, R.string.quarterly_plan_description),
    YEARLY(R.string.yearly_plan, R.string.yearly_plan_description, R.string.save_about),
    LIFETIME(R.string.lifetime_plan, R.string.lifetime_plan_description)
}

/**
 * 升级结果
 */
sealed class UpgradeResult {
    data class Success(val message: String) : UpgradeResult()
    data class Error(val message: String) : UpgradeResult()
}
