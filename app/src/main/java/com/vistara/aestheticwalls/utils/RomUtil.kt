package com.vistara.aestheticwalls.utils

import android.os.Build
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * ROM 工具类
 * 用于判断手机厂商
 */
object RomUtil {
    private const val TAG = "RomUtils"
    
    // 华为
    val isHuaweiRom: Boolean by lazy {
        checkManufacturer("HUAWEI") || checkProperty("ro.build.display.id", "EMUI") ||
                checkProperty("ro.build.version.emui", "")
    }
    
    // 小米
    val isMiuiRom: Boolean by lazy {
        checkManufacturer("Xiaomi") || checkProperty("ro.miui.ui.version.name", "") ||
                checkProperty("ro.miui.ui.version.code", "")
    }
    
    // OPPO
    val isOppoRom: Boolean by lazy {
        checkManufacturer("OPPO") || checkProperty("ro.build.version.opporom", "")
    }
    
    // vivo
    val isVivoRom: Boolean by lazy {
        checkManufacturer("vivo") || checkProperty("ro.vivo.os.version", "")
    }
    
    // 一加
    val isOnePlusRom: Boolean by lazy {
        checkManufacturer("OnePlus") || checkProperty("ro.build.ota.versionname", "OnePlus")
    }
    
    // 三星
    val isSamsungRom: Boolean by lazy {
        checkManufacturer("samsung")
    }
    
    // 魅族
    val isMeizuRom: Boolean by lazy {
        checkManufacturer("Meizu") || checkProperty("ro.build.display.id", "Flyme")
    }
    
    // 联想
    val isLenovoRom: Boolean by lazy {
        checkManufacturer("LENOVO")
    }
    
    // 索尼
    val isSonyRom: Boolean by lazy {
        checkManufacturer("Sony")
    }
    
    // 谷歌
    val isGoogleRom: Boolean by lazy {
        checkManufacturer("Google")
    }
    
    /**
     * 检查制造商
     */
    private fun checkManufacturer(manufacturer: String): Boolean {
        return Build.MANUFACTURER.uppercase().contains(manufacturer.uppercase())
    }
    
    /**
     * 检查系统属性
     */
    private fun checkProperty(property: String, value: String): Boolean {
        return try {
            val propertyValue = getSystemProperty(property)
            if (value.isEmpty()) {
                !propertyValue.isNullOrEmpty()
            } else {
                propertyValue?.contains(value) == true
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取系统属性
     */
    private fun getSystemProperty(propName: String): String? {
        val line: String
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop $propName")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
            return line
        } catch (ex: IOException) {
            return null
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
