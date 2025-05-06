package com.vistara.aestheticwalls.ui.screens.diamond

/**
 * 钻石购买结果
 */
sealed class DiamondPurchaseResult {
    /**
     * 购买成功
     */
    data class Success(val message: String) : DiamondPurchaseResult()
    
    /**
     * 购买失败
     */
    data class Error(val message: String) : DiamondPurchaseResult()
}
