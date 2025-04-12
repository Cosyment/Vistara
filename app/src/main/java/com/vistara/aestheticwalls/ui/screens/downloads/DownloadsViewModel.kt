package com.vistara.aestheticwalls.ui.screens.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 下载页面的ViewModel
 * 管理下载壁纸的数据和状态
 */
@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository
) : ViewModel() {

    // 下载壁纸状态
    private val _downloadsState = MutableStateFlow<UiState<List<Wallpaper>>>(UiState.Loading)
    val downloadsState: StateFlow<UiState<List<Wallpaper>>> = _downloadsState.asStateFlow()

    init {
        loadDownloads()
    }

    /**
     * 加载下载壁纸
     */
    private fun loadDownloads() {
        viewModelScope.launch {
            _downloadsState.value = UiState.Loading
            
            wallpaperRepository.getDownloadedWallpapers()
                .catch { e ->
                    _downloadsState.value = UiState.Error(e.message ?: "加载下载壁纸失败")
                }
                .collectLatest { wallpapers ->
                    if (wallpapers.isEmpty()) {
                        _downloadsState.value = UiState.Success(emptyList())
                    } else {
                        _downloadsState.value = UiState.Success(wallpapers)
                    }
                }
        }
    }

    /**
     * 删除下载壁纸
     * 注意：此功能需要在WallpaperRepository中实现
     */
    fun deleteDownloadedWallpaper(wallpaperId: String) {
        // TODO: 实现删除下载壁纸的功能
        // 由于当前Repository中没有此方法，需要先在Repository中实现
    }

    /**
     * 刷新下载列表
     */
    fun refresh() {
        loadDownloads()
    }
}
