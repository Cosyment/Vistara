package com.vistara.aestheticwalls.ui.screens.lives

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.remote.ApiResult
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 静态壁纸库ViewModel
 * 管理静态壁纸数据和状态
 */
@HiltViewModel
class LiveLibraryViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository
) : ViewModel() {

    // 壁纸数据状态
    private val _wallpapersState = MutableStateFlow<UiState<List<Wallpaper>>>(UiState.Loading)
    val wallpapersState: StateFlow<UiState<List<Wallpaper>>> = _wallpapersState.asStateFlow()

    // 分类数据
    val categories = listOf(
        "全部", "自然", "城市", "抽象", "插画", "科技", "简约", "动物", "食物"
    )

    // 当前选中的分类
    private val _selectedCategory = MutableStateFlow("全部")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    init {
        loadWallpapers()
    }

    /**
     * 加载壁纸数据
     */
    fun loadWallpapers() {
        viewModelScope.launch {
            _wallpapersState.value = UiState.Loading

            try {
                val result = wallpaperRepository.getWallpapers("static", 1, 20)

                when (result) {
                    is ApiResult.Success -> {
                        if (result.data.isEmpty()) {
                            _wallpapersState.value = UiState.Success(emptyList())
                        } else {
                            _wallpapersState.value = UiState.Success(result.data)
                        }
                    }
                    is ApiResult.Error -> {
                        _wallpapersState.value = UiState.Error(result.message)
                    }
                    is ApiResult.Loading -> {
                        // 已经设置了Loading状态，不需要额外处理
                    }
                }
            } catch (e: Exception) {
                _wallpapersState.value = UiState.Error(e.message ?: "加载壁纸失败")
            }
        }
    }

    /**
     * 根据分类筛选壁纸
     * @param category 分类名称
     */
    fun filterByCategory(category: String) {
        _selectedCategory.value = category

        // 如果选择"全部"，则重新加载所有壁纸
        if (category == "全部") {
            loadWallpapers()
            return
        }

        // 否则，根据分类筛选
        viewModelScope.launch {
            _wallpapersState.value = UiState.Loading

            try {
                // 使用现有数据进行筛选，避免重复网络请求
                val currentWallpapers = (_wallpapersState.value as? UiState.Success)?.data

                if (currentWallpapers != null) {
                    // 如果已有数据，直接筛选
                    val filtered = currentWallpapers.filter { wallpaper ->
                        wallpaper.tags.any { it.contains(category, ignoreCase = true) }
                    }
                    _wallpapersState.value = UiState.Success(filtered)
                } else {
                    // 如果没有数据，重新加载
                    loadWallpapers()
                }
            } catch (e: Exception) {
                _wallpapersState.value = UiState.Error(e.message ?: "筛选壁纸失败")
            }
        }
    }

    /**
     * 刷新壁纸数据
     */
    fun refresh() {
        loadWallpapers()
    }
}
