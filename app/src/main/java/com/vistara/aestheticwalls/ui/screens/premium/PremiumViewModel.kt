package com.vistara.aestheticwalls.ui.screens.premium

import android.app.Activity
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

/**
 * 升级页面的ViewModel
 */
@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val billingManager: BillingManager
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
    private val _selectedPlan = MutableStateFlow(UpgradePlan.MONTHLY)
    val selectedPlan: StateFlow<UpgradePlan> = _selectedPlan.asStateFlow()

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
                        _upgradeResult.value = UpgradeResult.Success("升级成功！感谢您的支持")
                    }
                    is PurchaseState.Failed -> {
                        _isUpgrading.value = false
                        _upgradeResult.value = UpgradeResult.Error("升级失败: ${state.message}")
                    }
                    is PurchaseState.Cancelled -> {
                        _isUpgrading.value = false
                        _upgradeResult.value = UpgradeResult.Error("升级已取消")
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
    fun selectPlan(plan: UpgradePlan) {
        _selectedPlan.value = plan
    }

    /**
     * 升级到高级版
     */
    fun upgrade(activity: Activity?) {
        if (_isPremiumUser.value) {
            _upgradeResult.value = UpgradeResult.Error("您已经是高级用户")
            return
        }

        if (_billingConnectionState.value != BillingConnectionState.CONNECTED) {
            _upgradeResult.value = UpgradeResult.Error("支付服务未连接，请稍后再试")
            return
        }

        // 根据选择的套餐确定商品ID
        val productId = when (_selectedPlan.value) {
            UpgradePlan.MONTHLY -> BillingManager.SUBSCRIPTION_MONTHLY
            UpgradePlan.YEARLY -> BillingManager.SUBSCRIPTION_YEARLY
            UpgradePlan.LIFETIME -> BillingManager.PREMIUM_LIFETIME
        }

        // 启动购买流程
        billingManager.launchBillingFlow(activity, productId)
    }

    /**
     * 恢复购买
     */
    fun restorePurchases() {
        if (_billingConnectionState.value != BillingConnectionState.CONNECTED) {
            _upgradeResult.value = UpgradeResult.Error("支付服务未连接，请稍后再试")
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
}

/**
 * 升级套餐
 */
enum class UpgradePlan(val title: String, val description: String, val discount: String? = null) {
    MONTHLY("月度套餐", "每月自动续费，随时可取消"),
    YEARLY("年度套餐", "每年自动续费，随时可取消", "节省约27%"),
    LIFETIME("终身套餐", "一次性付费，永久使用")
}

/**
 * 升级结果
 */
sealed class UpgradeResult {
    data class Success(val message: String) : UpgradeResult()
    data class Error(val message: String) : UpgradeResult()
}
