package com.vistara.aestheticwalls.ui.components

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.ui.theme.stringResource
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

// Define GRID_COLUMNS if not defined elsewhere accessible
private const val GRID_COLUMNS = 2
private const val TAG = "LiveVideoGrid"

// Threshold for triggering load more (e.g., when 2 rows from the end are visible)
private const val LOAD_MORE_THRESHOLD = GRID_COLUMNS * 2

/**
 * Displays live wallpapers in a grid. Playback control is handled by the parent.
 *
 * @param wallpapers List of wallpapers to display.
 * @param onWallpaperClick Callback when a wallpaper item is clicked.
 * @param gridState The LazyGridState controlled by the parent screen.
 * @param exoPlayer The single shared ExoPlayer instance from the parent screen.
 * @param playingIndex The index of the currently playing wallpaper in the `wallpapers` list (-1 if none).
 * @param isLoadingMore Flag indicating if more items are being loaded.
 * @param canLoadMore Flag indicating if more items can be loaded.
 * @param onLoadMore Callback to trigger loading more items.
 * @param modifier Modifier for the grid.
 * @param contentPadding Padding around the grid content.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiveVideoGrid(
    wallpapers: List<Wallpaper>,
    onWallpaperClick: (Wallpaper) -> Unit,
    gridState: LazyGridState,           // Received from parent
    exoPlayer: ExoPlayer,               // Received from parent
    playingIndex: Int,                // Received from parent
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp) // Default padding
) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        state = gridState, // Use the state passed from the parent
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp), // Spacing between columns
        verticalArrangement = Arrangement.spacedBy(8.dp),   // Spacing between rows
        modifier = modifier // Apply modifier from parameter (e.g., .weight(1f))
    ) {
        itemsIndexed(
            items = wallpapers,
            // Ensure wallpaper.id is unique and stable
            key = { _, wallpaper -> wallpaper.id }
        ) { index, wallpaper ->

            // Determine if this specific item should be playing
            val isCurrentlyPlaying = index == playingIndex

            // Effect to trigger 'onLoadMore' when scrolling near the end
            // This runs when the item enters composition and deps change
            LaunchedEffect(gridState, wallpapers.size, canLoadMore, isLoadingMore) {
                // Flow observing the index of the last visible item
                snapshotFlow { gridState.layoutInfo.visibleItemsInfo }
                    .map { visibleItems -> visibleItems.lastOrNull()?.index ?: -1 }
                    .distinctUntilChanged() // Only react when the last visible index actually changes
                    .filter { lastVisibleIndex ->
                        // Conditions to trigger load more:
                        lastVisibleIndex != -1 && // Need a valid index
                                lastVisibleIndex >= wallpapers.size - 1 - LOAD_MORE_THRESHOLD && // Is it near the end?
                                canLoadMore && // Can we actually load more?
                                !isLoadingMore // Are we not already loading?
                    }
                    .collect { // If all conditions met
                        Log.d(TAG, "Load More Threshold Reached (last visible: $it). Triggering onLoadMore.")
                        onLoadMore() // Call the lambda
                    }
            }

            // Render the individual video item component
            // Make sure VideoItem is defined/imported correctly
            VideoPreviewItem(
                wallpaper = wallpaper,
                exoPlayer = exoPlayer, // Pass down the shared player
                isCurrentlyPlaying = isCurrentlyPlaying, // Tell the item if it should play
                onClick = { onWallpaperClick(wallpaper) }, // Handle item clicks
                modifier = Modifier
                    .aspectRatio(8f / 12f) // Maintain aspect ratio (adjust if needed)
                    .animateItemPlacement() // Animate item position changes
            )
        }

        // Footer item: Shows loading indicator or "End of list" message
        if (isLoadingMore || (!canLoadMore && wallpapers.isNotEmpty())) {
            item(span = { GridItemSpan(GRID_COLUMNS) }) { // Span the footer across all columns
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp), // Add padding for visual spacing
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingMore) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    } else { // Only show "end" message if not loading AND list has items
                        Text(
                            text = stringResource(R.string.end_of_list),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}