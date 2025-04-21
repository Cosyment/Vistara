package com.vistara.aestheticwalls.ui.screens.lives

// Make sure this points to the definition that includes cachedData
// Import VideoItem if it's in components, otherwise define it below or import from correct path
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridLayoutInfo
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.UiState
import com.vistara.aestheticwalls.data.model.Wallpaper
import com.vistara.aestheticwalls.ui.components.CategorySelector
import com.vistara.aestheticwalls.ui.components.LiveVideoGrid
import com.vistara.aestheticwalls.ui.components.LoadingState
import com.vistara.aestheticwalls.ui.theme.VistaraTheme
import com.vistara.aestheticwalls.ui.theme.stringResource
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlin.math.abs

private const val TAG = "LiveLibraryScreen"
private const val GRID_COLUMNS = 2

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun LiveLibraryScreen(
    onWallpaperClick: (Wallpaper) -> Unit,
    onSearchClick: () -> Unit = {},
    viewModel: LiveLibraryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ViewModel States
    val wallpapersUiState by viewModel.wallpapersState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categories = viewModel.categories
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val canLoadMore by viewModel.canLoadMore.collectAsState()

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Video Playback State ---
    val exoPlayer = rememberExoPlayerInstance(context) // Create and manage ExoPlayer instance
    var playingIndex by remember { mutableIntStateOf(-1) } // Index in the *current* list data, -1 means nothing is playing
    var wasPausedForLifecycle by remember { mutableStateOf(false) } // Track if pause was due to lifecycle event
    val gridState = rememberLazyGridState() // State for the LazyVerticalGrid

    // Extract the current list of wallpapers, handling different UI states
    val currentWallpapers = remember(wallpapersUiState) {
        when (wallpapersUiState) {
            is UiState.Success -> (wallpapersUiState as UiState.Success<List<Wallpaper>>).data
            is UiState.Error -> emptyList() // Use cached data on error if available
            is UiState.Loading -> {
                // If loading, try to show previous success data or cached error data to avoid blank screen
                (wallpapersUiState as? UiState.Success<List<Wallpaper>>)?.data ?:
//                (wallpapersUiState as? UiState.Error)?.cachedData ?:
                emptyList()
            }
            // Remove else branch or ensure all sealed class subtypes are handled
        }
    }


    // --- Lifecycle Management for Player ---
    DisposableEffect(exoPlayer, lifecycleOwner) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (exoPlayer.isPlaying) {
                        Log.d(TAG, "Lifecycle ON_PAUSE, pausing player.")
                        wasPausedForLifecycle = true // Mark that pause was due to lifecycle
                        exoPlayer.pause()
                    }
                }

                Lifecycle.Event.ON_RESUME -> {
                    // Only resume if it was paused by lifecycle and conditions still allow playback
                    if (wasPausedForLifecycle && playingIndex != -1 && !gridState.isScrollInProgress) {
                        Log.d(TAG, "Lifecycle ON_RESUME, resuming player for index $playingIndex.")
                        exoPlayer.playWhenReady = true // Resume playback
                    }
                    wasPausedForLifecycle = false // Reset flag regardless
                }
                // Player release is handled in rememberExoPlayerInstance's onDispose
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    // --- Player Listener for Auto-Play Next and Errors ---
    DisposableEffect(
        exoPlayer, currentWallpapers.size, gridState
    ) { // Re-evaluate if these deps change
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                // Check if playback ended, not scrolling, and not paused by lifecycle
                if (playbackState == Player.STATE_ENDED && !gridState.isScrollInProgress && !wasPausedForLifecycle) {
                    Log.d(
                        TAG,
                        "Playback ended for index $playingIndex. Finding next visible item to play."
                    )
                    val visibleItemsInfo = gridState.layoutInfo.visibleItemsInfo
                    // Ensure we have visible items and something was actually playing
                    if (visibleItemsInfo.isEmpty() || playingIndex == -1) {
                        Log.d(TAG, "No visible items or nothing was playing. Stopping.")
                        playingIndex = -1 // Reset playing index
                        return
                    }

                    // Find the list index of the item that just finished within the visible items
                    val currentVisibleItem = visibleItemsInfo.find { it.index == playingIndex }
                    val currentVisibleListIndex = visibleItemsInfo.indexOf(currentVisibleItem)

                    if (currentVisibleListIndex != -1) {
                        // Calculate the next index within the *visible* items list, looping back to the start
                        val nextVisibleListIndex =
                            (currentVisibleListIndex + 1) % visibleItemsInfo.size
                        val nextVisibleItemInfo = visibleItemsInfo[nextVisibleListIndex]
                        val nextPlayingIndex =
                            nextVisibleItemInfo.index // Get the index in the full data list

                        Log.d(TAG, "Next visible item to play is at index: $nextPlayingIndex")

                        // Check if the calculated index is valid within the data list
                        if (nextPlayingIndex < currentWallpapers.size) {
                            val nextVideo = currentWallpapers[nextPlayingIndex]
                            // Ensure the next item has a valid video URL
                            if (nextVideo.url != null) {
                                Log.d(
                                    TAG,
                                    "Playing next video at index $nextPlayingIndex: ${nextVideo.url}"
                                )
                                playingIndex =
                                    nextPlayingIndex // Update state to the new playing index
                                // Set the media item, prepare, and start playback
                                exoPlayer.setMediaItem(
                                    MediaItem.Builder().setUri(Uri.parse(nextVideo.url))
                                        .setMediaId(nextVideo.url).build()
                                )
                                exoPlayer.prepare()
                                exoPlayer.playWhenReady = true
                            } else {
                                Log.w(
                                    TAG,
                                    "Next video item at index $nextPlayingIndex has null URL. Stopping playback."
                                )
                                playingIndex = -1 // Stop playback if the next item is invalid
                            }
                        } else {
                            Log.w(
                                TAG,
                                "Calculated next index $nextPlayingIndex is out of bounds for currentWallpapers size (${currentWallpapers.size}). Stopping playback."
                            )
                            playingIndex = -1 // Stop playback if index is invalid
                        }
                    } else {
                        Log.d(
                            TAG,
                            "The item that finished playback (index $playingIndex) is no longer visible. Stopping playback."
                        )
                        // If the item scrolled out of view just as it finished, stop.
                        playingIndex = -1
                    }
                } else if (playbackState == Player.STATE_BUFFERING) {
                    Log.d(TAG, "Player is BUFFERING for index $playingIndex")
                    // This state can be used in VideoItem to show a loading indicator
                } else if (playbackState == Player.STATE_READY) {
                    Log.d(
                        TAG,
                        "Player is READY for index $playingIndex. PlayWhenReady=${exoPlayer.playWhenReady}"
                    )
                    // Player is ready, will play if playWhenReady is true
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "Player Error occurred for index $playingIndex: ${error.message}", error)
                // Simple error handling: stop playback and reset index
                playingIndex = -1
                exoPlayer.stop()
                exoPlayer.clearMediaItems() // Clear the failing item
                // Consider showing a snackbar message about the error
                // scope.launch { snackbarHostState.showSnackbar("Video playback error: ${error.errorCodeName}") }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // --- Scroll Listener for Pause/Resume Playback ---
    LaunchedEffect(gridState, currentWallpapers.size) { // Re-run if gridState or list size changes
        snapshotFlow { gridState.isScrollInProgress }.distinctUntilChanged() // Only react when the scrolling state changes
            .collect { isScrolling ->
                if (isScrolling) {
                    // If scrolling starts and a video is playing, pause it
                    if (exoPlayer.isPlaying) {
                        Log.d(
                            TAG, "Scroll detected, pausing player currently at index $playingIndex."
                        )
                        exoPlayer.pause()
                    }
                } else {
                    // If scrolling stops:
                    // Add a small delay to ensure scrolling has fully stopped and avoid jittery playback start/stop
                    // kotlinx.coroutines.delay(150) // Optional delay

                    // Do not resume playback if the player was paused due to a lifecycle event (e.g., app went to background)
                    if (wasPausedForLifecycle) {
                        Log.d(
                            TAG,
                            "Scroll stopped, but player was paused for lifecycle. Resume will be handled by ON_RESUME."
                        )
                        return@collect // Let the lifecycle observer handle resume
                    }

                    Log.d(TAG, "Scroll stopped. Determining best item to play.")
                    val visibleItemsInfo = gridState.layoutInfo.visibleItemsInfo
                    if (visibleItemsInfo.isNotEmpty()) {
                        // Find the index of the item that should be played based on visibility
                        val bestVisibleIndex =
                            findBestVisibleItemToPlay(visibleItemsInfo, gridState.layoutInfo)

                        // Check if a valid item was found and is within bounds
                        if (bestVisibleIndex != -1 && bestVisibleIndex < currentWallpapers.size) {
                            // Play only if the target index is different from the currently playing one,
                            // OR if nothing is currently playing/prepared.
                            if (bestVisibleIndex != playingIndex || (!exoPlayer.isPlaying && exoPlayer.playbackState == Player.STATE_IDLE)) {
                                val videoToPlay = currentWallpapers[bestVisibleIndex]
                                if (videoToPlay.url != null) {
                                    Log.d(
                                        TAG,
                                        "Scroll stopped. Playing item at target index $bestVisibleIndex: ${videoToPlay.url}"
                                    )
                                    playingIndex =
                                        bestVisibleIndex // Update the playing index state
                                    exoPlayer.setMediaItem(
                                        MediaItem.Builder().setUri(Uri.parse(videoToPlay.url))
                                            .setMediaId(videoToPlay.url).build()
                                    )
                                    exoPlayer.prepare()
                                    exoPlayer.playWhenReady = true
                                } else {
                                    Log.w(
                                        TAG,
                                        "Scroll stopped. Target video at index $bestVisibleIndex has null URL."
                                    )
                                    // If this invalid item was somehow marked as playing, reset it
                                    if (playingIndex == bestVisibleIndex) playingIndex = -1
                                }
                            } else if (bestVisibleIndex == playingIndex && !exoPlayer.isPlaying && exoPlayer.playbackState != Player.STATE_ENDED) {
                                // If the target is the same as the current index, but it's paused (and not ended), resume it.
                                Log.d(
                                    TAG,
                                    "Scroll stopped. Resuming playback for already targeted index $playingIndex."
                                )
                                exoPlayer.playWhenReady = true
                            } else {
                                Log.d(
                                    TAG,
                                    "Scroll stopped. Target $bestVisibleIndex is already playing or preparing."
                                )
                            }
                        } else if (bestVisibleIndex != -1) {
                            Log.w(
                                TAG,
                                "Scroll stopped. Best visible index $bestVisibleIndex is out of bounds (${currentWallpapers.size})."
                            )
                            playingIndex = -1 // Reset playing index if target is invalid
                            exoPlayer.stop()
                        } else {
                            Log.d(
                                TAG,
                                "Scroll stopped. No suitable visible item found to play. Stopping playback."
                            )
                            playingIndex = -1
                            exoPlayer.stop() // Stop if no item is deemed suitable
                        }
                    } else {
                        // No items visible after scroll stops (e.g., scrolled way too fast)
                        Log.d(TAG, "Scroll stopped, but no items are visible. Stopping playback.")
                        playingIndex = -1
                        exoPlayer.stop()
                    }
                }
            }
    }

    // Effect to handle initial playback when data first appears and screen is not scrolling
    LaunchedEffect(currentWallpapers, gridState) {
        // Only run when the list is not empty
        if (currentWallpapers.isNotEmpty()) {
            snapshotFlow { gridState.layoutInfo.visibleItemsInfo }
                // Trigger only when:
                // 1. Visible items exist
                // 2. Not currently scrolling
                // 3. Nothing is currently marked as playing
                // 4. Not paused due to lifecycle event
                .filter { it.isNotEmpty() && !gridState.isScrollInProgress && playingIndex == -1 && !wasPausedForLifecycle }
                .map {
                    findBestVisibleItemToPlay(
                        it, gridState.layoutInfo
                    )
                } // Find the best item based on current visibility
                .distinctUntilChanged() // Only proceed if the best item index changes
                .collect { bestVisibleIndex ->
                    if (bestVisibleIndex != -1 && bestVisibleIndex < currentWallpapers.size) {
                        val videoToPlay = currentWallpapers[bestVisibleIndex]
                        if (videoToPlay.url != null) {
                            Log.d(
                                TAG,
                                "Initial Play Triggered. Playing item at index $bestVisibleIndex: ${videoToPlay.url}"
                            )
                            playingIndex = bestVisibleIndex // Update state
                            exoPlayer.setMediaItem(
                                MediaItem.Builder().setUri(Uri.parse(videoToPlay.url))
                                    .setMediaId(videoToPlay.url).build()
                            )
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                        } else {
                            Log.w(
                                TAG,
                                "Initial Play. Target video URL is null at index $bestVisibleIndex"
                            )
                        }
                    } else {
                        Log.d(
                            TAG,
                            "Initial Play. No suitable item found or index $bestVisibleIndex invalid."
                        )
                    }
                }
        }
    }


    // --- UI Structure ---
    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, topBar = {
        TopAppBar(
            title = {
            Text(
                stringResource(R.string.category_live),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }, actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(R.string.search_hint)
                )
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
        )
    }) { paddingValues ->
        val pullRefreshState = rememberPullRefreshState(
            refreshing = isRefreshing, onRefresh = {
                Log.d(TAG, "Pull to refresh initiated.")
                // Stop playback before refreshing data
                playingIndex = -1
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
                viewModel.refresh()
            })

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) { // Wrap content in Box for PullRefreshIndicator positioning
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply padding from Scaffold
            ) {
                // Category Selector
                CategorySelector(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        Log.d(
                            TAG,
                            "Category selected: ${category.name}. Stopping player and filtering."
                        )
                        // Stop player immediately when changing category
                        playingIndex = -1
                        exoPlayer.stop()
                        exoPlayer.clearMediaItems()
                        // Trigger filtering in ViewModel
                        viewModel.filterByCategory(category)
                    })

                Spacer(Modifier.height(8.dp)) // Add some space below categories

                // --- Grid Content Area ---
                when (val state = wallpapersUiState) { // Use 'state' for easier access
                    is UiState.Loading -> {
                        // Show loading indicator only if there's no previous data to display
                        if (currentWallpapers.isEmpty()) {
                            LoadingState(
                                message = stringResource(R.string.no_wallpapers_found),
                                modifier = Modifier.fillMaxSize()
                            ) // Your loading composable
                        } else {
                            // Show the existing grid data while loading new data in the background
                            Log.d(
                                TAG,
                                "Loading state, but showing existing ${currentWallpapers.size} items."
                            )
                            LiveVideoGrid(
                                wallpapers = currentWallpapers,
                                onWallpaperClick = onWallpaperClick,
                                gridState = gridState,
                                exoPlayer = exoPlayer,
                                playingIndex = playingIndex,
                                isLoadingMore = true, // Indicate loading at the bottom potentially
                                canLoadMore = canLoadMore,
                                onLoadMore = { viewModel.loadMore() },
                                modifier = Modifier.weight(1f) // Ensure grid takes available space
                            )
                        }
                    }

                    is UiState.Success -> {
                        // val wallpapers = state.data // data is already in currentWallpapers
                        if (currentWallpapers.isEmpty()) {
                            // Show empty state message if the list is empty after success
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    stringResource(R.string.no_wallpapers_found),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            // Display the grid with the successful data
                            LiveVideoGrid(
                                wallpapers = currentWallpapers,
                                onWallpaperClick = onWallpaperClick,
                                gridState = gridState,
                                exoPlayer = exoPlayer,
                                playingIndex = playingIndex,
                                isLoadingMore = isLoadingMore, // Pass loading more state
                                canLoadMore = canLoadMore,     // Pass can load more state
                                onLoadMore = { viewModel.loadMore() }, // Pass load more callback
                                modifier = Modifier.weight(1f) // Ensure grid takes available space
                            )
                        }
                    }

                    is UiState.Error -> {
                        // Show error message, potentially overlaid or with cached data
                        Log.e(TAG, "Error state: ${state.message}")
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                        ) {
                            // If there's no cached data, show the error message prominently
                            if (currentWallpapers.isEmpty()) {
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else {
                                // If there is cached data, show it in the grid and display error via Snackbar
                                LaunchedEffect(state.message) { // Show snackbar when error message changes
                                    snackbarHostState.showSnackbar(
                                        message = state.message, duration = SnackbarDuration.Short
                                    )
                                }
                                LiveVideoGrid(
                                    wallpapers = currentWallpapers, // Display cached data
                                    onWallpaperClick = onWallpaperClick,
                                    gridState = gridState,
                                    exoPlayer = exoPlayer,
                                    playingIndex = playingIndex, // Keep player state consistent
                                    isLoadingMore = false, // Not loading more on error
                                    canLoadMore = false,   // Cannot load more after error
                                    onLoadMore = {}, // No-op load more
//                                    modifier = Modifier.weight(1F)
                                )
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 30.dp)
            )
        }
    }
}

// --- Helper Composables and Functions ---

/**
 * Creates and remembers an ExoPlayer instance, ensuring it's released on dispose.
 */
@Composable
fun rememberExoPlayerInstance(context: Context): ExoPlayer {
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF // We handle list looping manually
            volume = 0f
            Log.d("ExoPlayerLifecycle", "ExoPlayer instance created.")
        }
    }

    // Release the player when the composable leaves the composition
    DisposableEffect(Unit) {
        onDispose {
            Log.d("ExoPlayerLifecycle", "Releasing ExoPlayer instance.")
            exoPlayer.release()
        }
    }
    return exoPlayer
}

/**
 * Finds the best item index (in the full data list) to play based on visibility within the grid.
 * Strategy: Prioritizes the item closest to the top-left of the viewport.
 */
fun findBestVisibleItemToPlay(
    visibleItems: List<LazyGridItemInfo>, layoutInfo: LazyGridLayoutInfo
): Int {
    if (visibleItems.isEmpty()) return -1

    // Define the viewport boundaries (relative to the LazyGrid's content area)
    val viewportStartY = 0 // Top edge
    val viewportEndY = layoutInfo.viewportSize.height // Bottom edge
    val viewportStartX = 0 // Left edge
    val viewportEndX = layoutInfo.viewportSize.width // Right edge

    val candidate = visibleItems.filter { item ->
        // Filter for items that are at least partially visible vertically and horizontally
        val itemStartY = item.offset.y
        val itemEndY = itemStartY + item.size.height
        val itemStartX = item.offset.x
        val itemEndX = itemStartX + item.size.width

        val vertVisible = itemEndY > viewportStartY && itemStartY < viewportEndY
        val horizVisible = itemEndX > viewportStartX && itemStartX < viewportEndX

        vertVisible && horizVisible
    }.minByOrNull { item ->
        // Calculate the distance from the top-left corner of the viewport
        // Prioritize items closer to the top, then closer to the left
        val yDist = abs(item.offset.y - viewportStartY)
        val xDist = abs(item.offset.x - viewportStartX)
        // Weight vertical distance more heavily if desired, or use simple Euclidean distance
        // sqrt((yDist * yDist).toDouble() + (xDist * xDist).toDouble())
        yDist * 1000 + xDist // Simple heuristic: prioritize top-ness strongly
    }

    Log.d(
        "findBestItem",
        "Candidate for playback: index=${candidate?.index}, offset=${candidate?.offset}"
    )
    return candidate?.index ?: -1 // Return the index of the best candidate, or -1 if none found
}


// --- Preview ---
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun LiveLibraryScreenPreview() {
    VistaraTheme {
        // Preview uses mock data, doesn't actually run ViewModel or Player
        Scaffold(
            topBar = { TopAppBar(title = { Text("Live Wallpapers Preview") }) }) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Text("Preview Content Area")
                // You could add mock CategorySelector and a simple Grid layout here for visual preview
            }
        }
    }
}