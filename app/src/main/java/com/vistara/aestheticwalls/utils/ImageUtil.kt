package com.vistara.aestheticwalls.utils

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import com.vistara.aestheticwalls.R

/**
 * 图像处理工具类
 */
object ImageUtil {

    /**
     * 对图像应用高斯模糊
     *
     * @param context 上下文
     * @param bitmap 原始位图
     * @param radius 模糊半径 (0-25)
     * @param scale 缩放因子，用于提高性能 (值越小，性能越好，但质量越低)
     * @return 模糊后的位图
     */
    fun applyGaussianBlur(
        context: Context,
        bitmap: Bitmap,
        radius: Float = 25f,
        scale: Float = 0.2f
    ): Bitmap {
        // 缩小图像以提高性能
        val width = (bitmap.width * scale).toInt()
        val height = (bitmap.height * scale).toInt()
        
        // 创建缩小的位图
        val inputBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
        
        // 创建输出位图
        val outputBitmap = Bitmap.createBitmap(inputBitmap)
        
        // 使用RenderScript进行高斯模糊
        val rs = RenderScript.create(context)
        val theInAllocation = Allocation.createFromBitmap(rs, inputBitmap)
        val theOutAllocation = Allocation.createFromBitmap(rs, outputBitmap)
        val theScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        
        // 设置模糊半径 (范围: 0-25)
        theScript.setRadius(radius.coerceIn(0f, 25f))
        
        // 应用模糊
        theScript.setInput(theInAllocation)
        theScript.forEach(theOutAllocation)
        theOutAllocation.copyTo(outputBitmap)
        
        // 释放资源
        rs.destroy()
        
        return outputBitmap
    }


    fun getDrawableByName(resourceName: String): Int? {
        return try {
            val res = R.mipmap::class.java.getField(resourceName).getInt(null)
            res
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
