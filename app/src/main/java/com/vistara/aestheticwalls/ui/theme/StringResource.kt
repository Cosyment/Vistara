package com.vistara.aestheticwalls.ui.theme

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource as androidStringResource

/**
 * 获取字符串资源，优先使用 LocalAppResources 中的资源
 */
@Composable
@ReadOnlyComposable
fun stringResource(@StringRes id: Int): String {
    val localResources = LocalAppResources.current
    val context = LocalContext.current
    
    return try {
        localResources.getString(id)
    } catch (e: Exception) {
        // 如果 LocalAppResources 中没有该资源，则使用默认的 stringResource
        androidStringResource(id)
    }
}

/**
 * 获取带格式化参数的字符串资源，优先使用 LocalAppResources 中的资源
 */
@Composable
@ReadOnlyComposable
fun stringResource(@StringRes id: Int, vararg formatArgs: Any): String {
    val localResources = LocalAppResources.current
    val context = LocalContext.current
    
    return try {
        localResources.getString(id, *formatArgs)
    } catch (e: Exception) {
        // 如果 LocalAppResources 中没有该资源，则使用默认的 stringResource
        androidStringResource(id, *formatArgs)
    }
}

/**
 * 获取复数字符串资源，优先使用 LocalAppResources 中的资源
 */
@Composable
@ReadOnlyComposable
fun pluralStringResource(
    @PluralsRes id: Int,
    quantity: Int,
    vararg formatArgs: Any
): String {
    val localResources = LocalAppResources.current
    val context = LocalContext.current
    
    return try {
        localResources.getQuantityString(id, quantity, *formatArgs)
    } catch (e: Exception) {
        // 如果 LocalAppResources 中没有该资源，则使用默认的方式
        context.resources.getQuantityString(id, quantity, *formatArgs)
    }
}
