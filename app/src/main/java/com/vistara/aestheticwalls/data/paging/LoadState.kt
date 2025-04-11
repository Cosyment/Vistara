package com.vistara.aestheticwalls.data.paging

sealed class LoadState {
    object Loading : LoadState()
    object NotLoading : LoadState()
    data class Error(val error: Throwable) : LoadState()
} 