package com.vistara.aestheticwalls.ui.screens.search

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.data.repository.UserPrefsRepository
import com.vistara.aestheticwalls.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 搜索ViewModel
 * 管理搜索相关的状态和数据
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
    private val userPrefsRepository: UserPrefsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "SearchViewModel"
        private const val MAX_HISTORY_SIZE = 10
    }

    // 搜索查询
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // 搜索结果
    private val _searchResults = MutableStateFlow<List<Wallpaper>>(emptyList())
    val searchResults: StateFlow<List<Wallpaper>> = _searchResults.asStateFlow()

    // 搜索历史
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    // 搜索建议
    private val _searchSuggestions = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestions: StateFlow<List<String>> = _searchSuggestions.asStateFlow()

    // 热门搜索关键词
    private val _hotSearches = MutableStateFlow(
        listOf(
            context.getString(R.string.category_nature),
            context.getString(R.string.category_abstract),
            context.getString(R.string.category_animals),
            context.getString(R.string.category_city),
            context.getString(R.string.category_minimal),
            context.getString(R.string.category_space),
            context.getString(R.string.category_flowers),
            context.getString(R.string.category_dark)
        )
    )
    val hotSearches: StateFlow<List<String>> = _hotSearches.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 错误状态
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadSearchHistory()
    }

    /**
     * 加载搜索历史
     */
    private fun loadSearchHistory() {
        viewModelScope.launch {
            try {
                val history = userPrefsRepository.getSearchHistory()
                _searchHistory.value = history
                Log.d(TAG, "搜索历史加载成功: ${history.size}条记录")
            } catch (e: Exception) {
                Log.e(TAG, "搜索历史加载失败", e)
            }
        }
    }

    /**
     * 更新搜索查询
     */
    fun updateQuery(newQuery: String) {
        _query.value = newQuery
        if (newQuery.isNotEmpty()) {
            generateSuggestions(newQuery)
        } else {
            _searchSuggestions.value = emptyList()
        }
    }

    /**
     * 生成搜索建议
     */
    private fun generateSuggestions(query: String) {
        if (query.length < 2) {
            _searchSuggestions.value = emptyList()
            return
        }

        // 从搜索历史中筛选匹配的建议
        val historyMatches = _searchHistory.value.filter {
            it.contains(query, ignoreCase = true)
        }

        // 从热门搜索中筛选匹配的建议
        val hotMatches = _hotSearches.value.filter {
            it.contains(query, ignoreCase = true)
        }

        // 合并建议并去重
        val suggestions = (historyMatches + hotMatches).distinct().take(5)
        _searchSuggestions.value = suggestions
    }

    /**
     * 执行搜索
     */
    fun search(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                Log.d(TAG, "开始搜索: $query")
                val results = wallpaperRepository.searchWallpapers(query, 1, 20)
                _searchResults.value = results
                Log.d(TAG, "搜索完成: 找到${results.size}个结果")

                // 添加到搜索历史
                addToSearchHistory(query)
            } catch (e: Exception) {
                Log.e(TAG, "搜索失败", e)
                _error.value = e.message ?: "搜索失败，请重试"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 添加到搜索历史
     */
    private fun addToSearchHistory(query: String) {
        viewModelScope.launch {
            try {
                // 获取当前历史
                val currentHistory = _searchHistory.value.toMutableList()

                // 如果已存在，先移除
                currentHistory.remove(query)

                // 添加到开头
                currentHistory.add(0, query)

                // 限制历史记录数量
                val newHistory = currentHistory.take(MAX_HISTORY_SIZE)
                _searchHistory.value = newHistory

                // 保存到持久化存储
                userPrefsRepository.saveSearchHistory(newHistory)
                Log.d(TAG, "搜索历史已更新")
            } catch (e: Exception) {
                Log.e(TAG, "保存搜索历史失败", e)
            }
        }
    }

    /**
     * 清除搜索历史
     */
    fun clearSearchHistory() {
        viewModelScope.launch {
            try {
                userPrefsRepository.clearSearchHistory()
                _searchHistory.value = emptyList()
                Log.d(TAG, "搜索历史已清除")
            } catch (e: Exception) {
                Log.e(TAG, "清除搜索历史失败", e)
            }
        }
    }

    /**
     * 从搜索历史中选择
     */
    fun selectFromHistory(historyItem: String) {
        _query.value = historyItem
        search(historyItem)
    }

    /**
     * 从搜索建议中选择
     */
    fun selectSuggestion(suggestion: String) {
        _query.value = suggestion
        search(suggestion)
    }
}
