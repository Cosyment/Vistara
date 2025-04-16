package com.vistara.aestheticwalls.manager

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.vistara.aestheticwalls.data.repository.UserPrefsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 主题管理器
 * 负责管理应用的主题设置
 */
@Singleton
class ThemeManager @Inject constructor(
    private val userPrefsRepository: UserPrefsRepository
) {
    /**
     * 获取深色主题设置流
     */
    val darkThemeFlow: Flow<Boolean> = userPrefsRepository.getUserSettingsFlow()
        .map { it.darkTheme }

    /**
     * 获取动态颜色设置流
     */
    val dynamicColorsFlow: Flow<Boolean> = userPrefsRepository.getUserSettingsFlow()
        .map { it.dynamicColors }

    /**
     * 获取当前深色主题设置
     */
    @Composable
    fun darkTheme(): State<Boolean> {
        return darkThemeFlow.collectAsState(initial = false)
    }

    /**
     * 获取当前动态颜色设置
     */
    @Composable
    fun dynamicColors(): State<Boolean> {
        return dynamicColorsFlow.collectAsState(initial = true)
    }

    /**
     * 更新深色主题设置
     */
    suspend fun updateDarkTheme(enabled: Boolean) {
        val currentSettings = userPrefsRepository.getUserSettings()
        val updatedSettings = currentSettings.copy(darkTheme = enabled)
        userPrefsRepository.saveUserSettings(updatedSettings)
    }

    /**
     * 更新动态颜色设置
     */
    suspend fun updateDynamicColors(enabled: Boolean) {
        val currentSettings = userPrefsRepository.getUserSettings()
        val updatedSettings = currentSettings.copy(dynamicColors = enabled)
        userPrefsRepository.saveUserSettings(updatedSettings)
    }
}
