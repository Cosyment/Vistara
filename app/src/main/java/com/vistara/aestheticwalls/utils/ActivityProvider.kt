package com.vistara.aestheticwalls.utils

import android.app.Activity
import java.lang.ref.WeakReference

/**
 * Activity提供者
 * 用于在非Activity上下文中获取Activity实例
 * 主要用于需要Activity上下文的操作，如设置视频壁纸
 */
object ActivityProvider {
    private var mainActivityRef: WeakReference<Activity>? = null

    /**
     * 设置MainActivity实例
     * 使用WeakReference避免内存泄漏
     */
    fun setMainActivity(activity: Activity) {
        mainActivityRef = WeakReference(activity)
    }

    /**
     * 获取MainActivity实例
     * 如果Activity已被销毁，返回null
     */
    fun getMainActivity(): Activity? {
        return mainActivityRef?.get()
    }

    /**
     * 清除MainActivity引用
     * 在Activity销毁时调用
     */
    fun clearMainActivity() {
        mainActivityRef = null
    }
}
