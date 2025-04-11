package com.vistara.aestheticwalls.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vistara.aestheticwalls.data.model.Resolution

/**
 * Room数据库类型转换器
 */
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromResolution(resolution: Resolution?): String? {
        return resolution?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toResolution(value: String?): Resolution? {
        return value?.let { gson.fromJson(it, Resolution::class.java) }
    }
} 