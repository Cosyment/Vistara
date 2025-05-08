package com.vistara.aestheticwalls.ui.screens.recharge

/**
 * 钻石购买结果
 */
sealed class RechargePurchaseResult {
    /**
     * 购买成功
     */
    data class Success(val message: String) : RechargePurchaseResult()
    
    /**
     * 购买失败
     */
    data class Error(val message: String) : RechargePurchaseResult()
}
