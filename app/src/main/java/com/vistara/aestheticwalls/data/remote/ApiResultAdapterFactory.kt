package com.vistara.aestheticwalls.data.remote

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.reflect.ParameterizedType

/**
 * Gson TypeAdapterFactory，用于处理ApiResult类型的序列化和反序列化
 */
class ApiResultAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        // 检查类型是否是ApiResult
        val rawType = type.rawType
        if (rawType != ApiResult::class.java) {
            return null
        }

        // 获取ApiResult的泛型参数类型
        val parameterizedType = type.type as ParameterizedType
        val dataType = parameterizedType.actualTypeArguments[0]
        val dataTypeAdapter = gson.getAdapter(TypeToken.get(dataType))

        // 创建ApiResult的TypeAdapter
        @Suppress("UNCHECKED_CAST")
        return ApiResultTypeAdapter(gson, dataTypeAdapter) as TypeAdapter<T>
    }
}

/**
 * ApiResult的TypeAdapter，处理序列化和反序列化
 */
private class ApiResultTypeAdapter<T>(
    private val gson: Gson,
    private val dataTypeAdapter: TypeAdapter<T>
) : TypeAdapter<ApiResult<T>>() {

    override fun write(out: JsonWriter, value: ApiResult<T>?) {
        if (value == null) {
            out.nullValue()
            return
        }

        when (value) {
            is ApiResult.Success -> {
                out.beginObject()
                out.name("status").value("success")
                out.name("data")
                dataTypeAdapter.write(out, value.data)
                out.endObject()
            }
            is ApiResult.Error -> {
                out.beginObject()
                out.name("status").value("error")
                out.name("code").value(value.code ?: -1)
                out.name("message").value(value.message)
                out.name("source").value(value.source.name)
                out.endObject()
            }
            is ApiResult.Loading -> {
                out.beginObject()
                out.name("status").value("loading")
                out.endObject()
            }
        }
    }

    override fun read(reader: JsonReader): ApiResult<T> {
        // 默认假设API返回的是成功结果
        // 实际应用中可能需要根据API的响应格式进行调整
        return try {
            val jsonObject = gson.fromJson<Map<String, Any>>(reader, Map::class.java)
            
            // 检查是否有错误信息
            if (jsonObject.containsKey("error") || jsonObject.containsKey("message") && !jsonObject.containsKey("data")) {
                val code = (jsonObject["code"] as? Number)?.toInt()
                val message = jsonObject["message"] as? String ?: "Unknown error"
                ApiResult.Error(code, message, ApiSource.BACKEND)
            } else {
                // 假设成功响应包含data字段
                val data = gson.fromJson<T>(gson.toJson(jsonObject), TypeToken.get(dataTypeAdapter.javaClass).type)
                ApiResult.Success(data)
            }
        } catch (e: Exception) {
            ApiResult.Error(null, e.message ?: "Parsing error", ApiSource.BACKEND)
        }
    }
}
