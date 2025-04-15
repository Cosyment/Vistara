package com.vistara.aestheticwalls.ui.screens.upgrade

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 升级页面的ViewModel
 */
@HiltViewModel
class UpgradeViewModel @Inject constructor(
    private val userRepository: UserRepository
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

    init {
        checkPremiumStatus()
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
     * 选择套餐
     */
    fun selectPlan(plan: UpgradePlan) {
        _selectedPlan.value = plan
    }

    /**
     * 升级到高级版
     */
    fun upgrade() {
        if (_isPremiumUser.value) {
            _upgradeResult.value = UpgradeResult.Error("您已经是高级用户")
            return
        }

        viewModelScope.launch {
            try {
                _isUpgrading.value = true
                
                // 模拟支付过程
                Log.d(TAG, "Upgrading to premium with plan: ${_selectedPlan.value}")
                
                // 延迟2秒模拟网络请求
                kotlinx.coroutines.delay(2000)
                
                // 更新用户状态
                userRepository.updatePremiumStatus(true)
                _isPremiumUser.value = true
                
                _upgradeResult.value = UpgradeResult.Success("升级成功！感谢您的支持")
            } catch (e: Exception) {
                Log.e(TAG, "Error upgrading: ${e.message}")
                _upgradeResult.value = UpgradeResult.Error("升级失败，请稍后再试")
            } finally {
                _isUpgrading.value = false
            }
        }
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
enum class UpgradePlan(val title: String, val price: String, val description: String, val discount: String? = null) {
    MONTHLY("月度套餐", "¥18.00/月", "每月自动续费，随时可取消"),
    YEARLY("年度套餐", "¥158.00/年", "每年自动续费，随时可取消", "节省27%"),
    LIFETIME("终身套餐", "¥298.00", "一次性付费，永久使用")
}

/**
 * 升级结果
 */
sealed class UpgradeResult {
    data class Success(val message: String) : UpgradeResult()
    data class Error(val message: String) : UpgradeResult()
}
