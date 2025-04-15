package com.vistara.aestheticwalls.data

import android.graphics.Bitmap

/**
 * 编辑后的图片缓存
 * 用于在编辑页面和详情页面之间传递编辑后的图片
 */
object EditedImageCache {
    private val cache = mutableMapOf<String, Bitmap>()
    
    /**
     * 保存编辑后的图片
     */
    fun saveEditedImage(wallpaperId: String, bitmap: Bitmap) {
        cache[wallpaperId] = bitmap
    }
    
    /**
     * 获取编辑后的图片
     */
    fun getEditedImage(wallpaperId: String): Bitmap? {
        return cache[wallpaperId]
    }
    
    /**
     * 清除编辑后的图片
     */
    fun clearEditedImage(wallpaperId: String) {
        cache.remove(wallpaperId)
    }
    
    /**
     * 清除所有编辑后的图片
     */
    fun clearAll() {
        cache.clear()
    }
}
