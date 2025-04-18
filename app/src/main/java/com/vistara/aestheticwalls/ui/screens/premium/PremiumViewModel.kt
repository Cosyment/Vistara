package com.vistara.aestheticwalls.ui.screens.premium

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.billing.BillingConnectionState
import com.vistara.aestheticwalls.billing.BillingManager
import com.vistara.aestheticwalls.billing.PurchaseState
import com.vistara.aestheticwalls.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.utils.StringProvider

/**
 * 升级页面的ViewModel
 */
@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val billingManager: BillingManager,
    private val stringProvider: StringProvider
) : ViewModel() {

    companion object {
        private const val TAG = "UpgradeViewModel"
    }

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
    val billingConnectionState: StateFlow<BillingConnectionState> = _billingConnectionState.asStateFlow()

    // 商品价格
    private val _productPrices = MutableStateFlow<Map<String, String>>(emptyMap())
    val productPrices: StateFlow<Map<String, String>> = _productPrices.asStateFlow()

    init {
        checkPremiumStatus()
        observeBillingState()
        observePurchaseState()
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
                        _upgradeResult.value = UpgradeResult.Success(stringProvider.getString(R.string.upgrade_success))
                    }
                    is PurchaseState.Failed -> {
                        _isUpgrading.value = false
                        _upgradeResult.value = UpgradeResult.Error(stringProvider.getString(R.string.upgrade_failed, state.message))
                    }
                    is PurchaseState.Cancelled -> {
                        _isUpgrading.value = false
                        _upgradeResult.value = UpgradeResult.Error(stringProvider.getString(R.string.upgrade_cancelled))
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
        prices[BillingManager.SUBSCRIPTION_MONTHLY] = billingManager.getProductPrice(BillingManager.SUBSCRIPTION_MONTHLY)

        // 年度套餐
        prices[BillingManager.SUBSCRIPTION_YEARLY] = billingManager.getProductPrice(BillingManager.SUBSCRIPTION_YEARLY)

        // 终身套餐
        prices[BillingManager.PREMIUM_LIFETIME] = billingManager.getProductPrice(BillingManager.PREMIUM_LIFETIME)

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
            _upgradeResult.value = UpgradeResult.Error(stringProvider.getString(R.string.already_premium_user))
            return
        }

        if (_billingConnectionState.value != BillingConnectionState.CONNECTED) {
            _upgradeResult.value = UpgradeResult.Error(stringProvider.getString(R.string.payment_service_not_connected))
            return
        }

        // 根据选择的套餐确定商品ID
        val productId = when (_selectedPlan.value) {
            PremiumPlan.MONTHLY -> BillingManager.SUBSCRIPTION_MONTHLY
            PremiumPlan.YEARLY -> BillingManager.SUBSCRIPTION_YEARLY
            PremiumPlan.LIFETIME -> BillingManager.PREMIUM_LIFETIME
        }

        // 启动购买流程
        billingManager.launchBillingFlow(activity, productId)
    }

    /**
     * 恢复购买
     */
    fun restorePurchases() {
        if (_billingConnectionState.value != BillingConnectionState.CONNECTED) {
            _upgradeResult.value = UpgradeResult.Error(stringProvider.getString(R.string.payment_service_not_connected))
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
     * 打开订阅管理页面
     * 跳转到 Google Play 商店的订阅管理页面
     * @param activity 当前 Activity
     */
    fun openSubscriptionManagementPage(activity: Activity?) {
        if (activity == null) {
            _upgradeResult.value = UpgradeResult.Error(stringProvider.getString(R.string.unknown_error))
            return
        }

        try {
            // 打开 Google Play 商店的订阅管理页面
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/account/subscriptions")
                setPackage("com.android.vending") // Google Play 商店包名
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening subscription management page: ${e.message}")

            // 如果无法打开特定页面，则打开 Google Play 商店主页
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/account")
                }
                activity.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "Error opening Google Play: ${e2.message}")
                _upgradeResult.value = UpgradeResult.Error(stringProvider.getString(R.string.unknown_error))
            }
        }
    }
}

/**
 * 升级套餐
 */
enum class PremiumPlan(val titleResId: Int, val descriptionResId: Int, val discountResId: Int? = null) {
    MONTHLY(R.string.monthly_plan, R.string.monthly_plan_description),
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
