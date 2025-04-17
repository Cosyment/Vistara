package com.vistara.aestheticwalls.utils

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 字符串提供者，用于在非 UI 层获取字符串资源
 */
@Singleton
class StringProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * 获取字符串资源
     *
     * @param resId 字符串资源 ID
     * @return 字符串资源的值
     */
    fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    /**
     * 获取带格式化参数的字符串资源
     *
     * @param resId 字符串资源 ID
     * @param formatArgs 格式化参数
     * @return 格式化后的字符串资源的值
     */
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
}
