package com.aeswox.arcmusic

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aeswox.arcmusic.db.entities.getQualityBadgeResId

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import com.aeswox.arcmusic.ui.animations.jellyClick
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.gestures.detectVerticalDragGestures

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.togetherWith
import androidx.compose.ui.draw.scale
import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreenContent(modifier: Modifier = Modifier, bottomPadding: androidx.compose.ui.unit.Dp, onNavigateToAlbumDetails: (String) -> Unit = {}, onNavigateToPlaylistDetails: (String) -> Unit = {}, onNavigateToArtistDetails: (String) -> Unit = {}, onNavigateToShare: (String, String) -> Unit = { _, _ -> }, onSelectionModeChange: (Boolean) -> Unit = {}, onCreatePlaylistClick: () -> Unit = {}) {
    val viewModel: MusicViewModel = hiltViewModel()
    val libraryTracks by viewModel.libraryTracks.collectAsState()
    val libraryAlbums by viewModel.libraryAlbums.collectAsState()
    val libraryArtists by viewModel.libraryArtists.collectAsState()
    val favoriteTracks by viewModel.favoriteTracks.collectAsState()
    val favoriteAlbums by viewModel.favoriteAlbums.collectAsState()
    val favoriteArtists by viewModel.favoriteArtists.collectAsState()
    val allSections = remember { mutableStateListOf(LibrarySection("Playlists", true), LibrarySection("Tracks", true), LibrarySection("Albums", true), LibrarySection("Artists", true), LibrarySection("Favorites", true), LibrarySection("Folders", false)) }
    val tabs = allSections.filter { it.isVisible }.map { it.name }
    val scope = rememberCoroutineScope()
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(initialPage = 1, pageCount = { tabs.size })
    var showRearrangeSheet by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<String>() }
    val isSelectionMode = selectedItems.isNotEmpty()
    
    androidx.activity.compose.BackHandler(enabled = isSelectionMode) {
        selectedItems.clear()
    }
    
    var deleteTrigger by remember { mutableStateOf(0) }
    var renameTrigger by remember { mutableStateOf(0) }
    androidx.compose.runtime.LaunchedEffect(isSelectionMode) {
        onSelectionModeChange(isSelectionMode)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
        LibraryHeader(
            title = tabs.getOrNull(pagerState.currentPage) ?: "",
            onTitleLongPress = { showRearrangeSheet = true },
            onOptionsClick = { showOptionsMenu = true },
            optionsMenuExpanded = showOptionsMenu,
            onOptionsDismiss = { showOptionsMenu = false },
            optionsMenuContent = {
                val currentTab = tabs.getOrNull(pagerState.currentPage)
                if (currentTab == "Artists") {
                    ArcDropdownMenuItem(
                        text = "Refresh All Artists",
                        icon = Icons.Outlined.Refresh,
                        onClick = {
                            showOptionsMenu = false
                            viewModel.refetchAllArtistsDetails()
                        }
                    )
                }
            },
            modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(tabs.size) { iteration ->
                val isCurrent = pagerState.currentPage == iteration
                
                val color by animateColorAsState(
                    targetValue = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "dotColor"
                )
                
                val size by animateDpAsState(
                    targetValue = if (isCurrent) 6.dp else 4.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "dotSize"
                )
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(size)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
        
        LibraryPagerContent(
            modifier = Modifier.weight(1f),
            pagerState = pagerState,
            tabs = tabs,
            onNavigateToAlbumDetails = onNavigateToAlbumDetails,
            onNavigateToPlaylistDetails = onNavigateToPlaylistDetails,
            onNavigateToArtistDetails = onNavigateToArtistDetails,
            onCreatePlaylistClick = onCreatePlaylistClick,
            selectedItems = selectedItems,
            onToggleSelection = { title ->
                if (selectedItems.contains(title)) {
                    selectedItems.remove(title)
                } else {
                    selectedItems.add(title)
                }
            },
            onClearSelection = { selectedItems.clear() },
            onSelectAll = { items -> 
                // TODO implement select all properly
            },
            deleteTrigger = deleteTrigger,
            renameTrigger = renameTrigger,
            onRenameComplete = { newName ->
                val oldName = selectedItems.firstOrNull()
                if (oldName != null) {
                    selectedItems.remove(oldName)
                    selectedItems.add(newName)
                }
            },
            onExploreClick = {
                scope.launch {
                    val tracksIndex = tabs.indexOf("Tracks")
                    if (tracksIndex != -1) {
                        pagerState.animateScrollToPage(tracksIndex)
                    }
                }
            }
        )
    }

    if (isSelectionMode) {
        Box(modifier = Modifier.fillMaxSize().padding(bottom = 24.dp), contentAlignment = Alignment.BottomCenter) {
            val currentTab = tabs.getOrNull(pagerState.currentPage) ?: ""
            SelectionBottomBar(
                currentTab = currentTab,
                onAddToPlaylist = { showAddToPlaylistSheet = true },
                onRename = { renameTrigger++ },
                onPlayNext = { selectedItems.clear() },
                onPlayLater = { selectedItems.clear() },
                onAddToFavorites = { 
                    when (currentTab) {
                        "Albums" -> {
                            val albumIds = selectedItems.mapNotNull { if (it.startsWith("album_")) it.removePrefix("album_") else null }
                            if (albumIds.isNotEmpty()) {
                                val isAllFavorited = libraryAlbums.filter { albumIds.contains(it.id) }.all { it.isFavorite }
                                viewModel.toggleAlbumFavorite(albumIds, !isAllFavorited)
                            }
                        }
                        "Artists" -> {
                            val artistIds = selectedItems.mapNotNull { if (it.startsWith("artist_")) it.removePrefix("artist_") else null }
                            if (artistIds.isNotEmpty()) {
                                val isAllFavorited = libraryArtists.filter { artistIds.contains(it.id) }.all { it.isFavorite }
                                viewModel.toggleArtistFavorite(artistIds, !isAllFavorited)
                            }
                        }
                        "Favorites" -> {
                            // If in favorites tab, pressing "remove" should just turn off favorite for selected things.
                            val trackIds = favoriteTracks.filter { selectedItems.contains("track_${it.id}") }.map { it.id }
                            if (trackIds.isNotEmpty()) viewModel.toggleFavorite(trackIds, false)
                            
                            val albumIds = favoriteAlbums.filter { selectedItems.contains("album_${it.id}") }.map { it.id }
                            if (albumIds.isNotEmpty()) viewModel.toggleAlbumFavorite(albumIds, false)
                            
                            val artistIds = favoriteArtists.filter { selectedItems.contains("artist_${it.id}") }.map { it.id }
                            if (artistIds.isNotEmpty()) viewModel.toggleArtistFavorite(artistIds, false)
                        }
                        else -> {
                            val trackIds = selectedItems.mapNotNull { if (it.startsWith("track_")) it.removePrefix("track_") else null }
                            if (trackIds.isNotEmpty()) {
                                val isAllFavorited = libraryTracks.filter { trackIds.contains(it.id) }.all { it.isFavorite }
                                viewModel.toggleFavorite(trackIds, !isAllFavorited)
                            }
                        }
                    }
                    selectedItems.clear() 
                },
                onShare = { 
                    val trackIds = selectedItems.mapNotNull { if (it.startsWith("track_")) it.removePrefix("track_") else null }.joinToString(",")
                    if (trackIds.isNotEmpty()) {
                        onNavigateToShare("tracks", trackIds)
                    }
                    selectedItems.clear()
                    onSelectionModeChange(false)
                },
                onDelete = { deleteTrigger++ }
            )
        }
    }

    if (showRearrangeSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showRearrangeSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manage Sections",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "DONE",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showRearrangeSheet = false }
                            .padding(8.dp)
                    )
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                var draggedIndex by remember { mutableStateOf<Int?>(null) }
                var dragOffset by remember { mutableStateOf(0f) }
                val density = LocalDensity.current
                val itemHeightPx = with(density) { 56.dp.toPx() }

                allSections.forEachIndexed { index, section ->
                    val isDragged = index == draggedIndex
                    val yOffset = if (isDragged) dragOffset else 0f

                    Row(
                        modifier = Modifier
                            .zIndex(if (isDragged) 1f else 0f)
                            .offset { androidx.compose.ui.unit.IntOffset(0, yOffset.roundToInt()) }
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Switch(
                                checked = section.isVisible,
                                onCheckedChange = { checked ->
                                    allSections[index] = section.copy(isVisible = checked)
                                },
                                modifier = Modifier.scale(0.8f)
                            )
                            Text(
                                text = section.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = if (section.isVisible) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.DragHandle, 
                            contentDescription = "Reorder",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragStart = { 
                                        draggedIndex = index
                                        dragOffset = 0f
                                    },
                                    onDragEnd = { draggedIndex = null },
                                    onDragCancel = { draggedIndex = null },
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount
                                        
                                        val currentIndex = draggedIndex ?: return@detectVerticalDragGestures
                                        
                                        if (dragOffset > itemHeightPx / 1.5 && currentIndex < allSections.size - 1) {
                                            val temp = allSections[currentIndex]
                                            allSections[currentIndex] = allSections[currentIndex + 1]
                                            allSections[currentIndex + 1] = temp
                                            draggedIndex = currentIndex + 1
                                            dragOffset -= itemHeightPx
                                        } else if (dragOffset < -itemHeightPx / 1.5 && currentIndex > 0) {
                                            val temp = allSections[currentIndex]
                                            allSections[currentIndex] = allSections[currentIndex - 1]
                                            allSections[currentIndex - 1] = temp
                                            draggedIndex = currentIndex - 1
                                            dragOffset += itemHeightPx
                                        }
                                    }
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

    if (showAddToPlaylistSheet) {
        val trackIds = selectedItems.mapNotNull { if (it.startsWith("track_")) it.removePrefix("track_") else null }
        AddToPlaylistSheet(trackIds = trackIds, onDismissRequest = { showAddToPlaylistSheet = false })
    }
}

@Composable
fun LibraryPagerContent(
    modifier: Modifier = Modifier,
    pagerState: androidx.compose.foundation.pager.PagerState,
    tabs: List<String>,
    onNavigateToAlbumDetails: (String) -> Unit = {},
    onNavigateToPlaylistDetails: (String) -> Unit = {},
    onNavigateToArtistDetails: (String) -> Unit = {},
    onCreatePlaylistClick: () -> Unit = {},
    selectedItems: List<String> = emptyList(),
    onToggleSelection: (String) -> Unit = {},
    onClearSelection: () -> Unit = {},
    onSelectAll: (List<String>) -> Unit = {},
    onExploreClick: () -> Unit = {},
    deleteTrigger: Int = 0,
    renameTrigger: Int = 0,
    onRenameComplete: (String) -> Unit = {}
) {
    androidx.compose.foundation.pager.HorizontalPager(
        state = pagerState,
        modifier = modifier
    ) { page ->
        val currentTab = tabs.getOrNull(page) ?: ""
        LibraryMainSection(
            modifier = Modifier.fillMaxSize(),
            tabName = currentTab, 
            onNavigateToAlbumDetails = onNavigateToAlbumDetails, 
            onNavigateToPlaylistDetails = onNavigateToPlaylistDetails, 
            onNavigateToArtistDetails = onNavigateToArtistDetails,
            onCreatePlaylistClick = onCreatePlaylistClick,
            selectedItems = selectedItems,
            onToggleSelection = onToggleSelection,
            onClearSelection = onClearSelection,
            onSelectAll = onSelectAll,
            onExploreClick = onExploreClick,
            deleteTrigger = if (currentTab == tabs.getOrNull(pagerState.currentPage)) deleteTrigger else 0,
            renameTrigger = if (currentTab == tabs.getOrNull(pagerState.currentPage)) renameTrigger else 0,
            onRenameComplete = onRenameComplete
        )
    }
}

@Composable
fun LibraryHeader(
    title: String, 
    onTitleLongPress: () -> Unit = {}, 
    onOptionsClick: () -> Unit = {}, 
    optionsMenuExpanded: Boolean = false,
    onOptionsDismiss: () -> Unit = {},
    optionsMenuContent: @Composable ColumnScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onTitleLongPress() }
                )
            }
        ) {
            androidx.compose.animation.AnimatedContent(
                targetState = title,
                transitionSpec = {
                    val enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(220)) +
                        androidx.compose.animation.slideInVertically(
                            initialOffsetY = { -it / 2 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy, 
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    val exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(200)) +
                        androidx.compose.animation.slideOutVertically(
                            targetOffsetY = { it / 2 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy, 
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    enter.togetherWith(exit)
                },
                label = "LibraryTitleAnimation"
            ) { targetTitle ->
                Text(
                    text = targetTitle,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box {
                IconButton(onClick = onOptionsClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert, 
                        contentDescription = "More", 
                        tint = MaterialTheme.colorScheme.onSurface, 
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                ArcDropdownMenu(
                    expanded = optionsMenuExpanded,
                    onDismissRequest = onOptionsDismiss
                ) {
                    optionsMenuContent()
                }
            }
        }
    }
}



@Composable
fun LibraryMainSection(
    modifier: Modifier = Modifier, 
    tabName: String = "", 
    onNavigateToAlbumDetails: (String) -> Unit = {}, 
    onNavigateToPlaylistDetails: (String) -> Unit = {}, 
    onNavigateToArtistDetails: (String) -> Unit = {},
    onCreatePlaylistClick: () -> Unit = {},
    selectedItems: List<String> = emptyList(),
    onToggleSelection: (String) -> Unit = {},
    onClearSelection: () -> Unit = {},
    onSelectAll: (List<String>) -> Unit = {},
    onExploreClick: () -> Unit = {},
    deleteTrigger: Int = 0,
    renameTrigger: Int = 0,
    onRenameComplete: (String) -> Unit = {},
    bottomPadding: androidx.compose.ui.unit.Dp = 100.dp
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("LibraryPrefs", android.content.Context.MODE_PRIVATE) }

    var sortExpanded by remember { mutableStateOf(false) }
    val isSelectionMode = selectedItems.isNotEmpty()
    var sortOption by remember { mutableStateOf(prefs.getString("${tabName}_sortOption", "Date added") ?: "Date added") }
    var sortOrder by remember { mutableStateOf(prefs.getString("${tabName}_sortOrder", "Descending") ?: "Descending") }
    val isGridView = tabName != "Tracks"
    
    androidx.compose.runtime.LaunchedEffect(sortOption) { prefs.edit().putString("${tabName}_sortOption", sortOption).apply() }
    androidx.compose.runtime.LaunchedEffect(sortOrder) { prefs.edit().putString("${tabName}_sortOrder", sortOrder).apply() }
    val viewModel: MusicViewModel = hiltViewModel()
    
    val playlistEntities by viewModel.libraryPlaylists.collectAsState()
    val playlists = remember(playlistEntities) { playlistEntities.map { LibraryUiItem(it.id, it.name, "Playlist", it.coverArtUri) }.toMutableStateList() }
    val albumEntities by viewModel.libraryAlbums.collectAsState()
    val albums = remember(albumEntities) { albumEntities.map { LibraryUiItem(it.id, it.title, it.artist ?: "Unknown Artist", it.artworkUri) }.toMutableStateList() }
    val artistEntities by viewModel.libraryArtists.collectAsState()
    val artists = remember(artistEntities) { artistEntities.map { LibraryUiItem(it.id, it.name, "Artist", it.photoUri) }.toMutableStateList() }
    val trackEntities by viewModel.libraryTracks.collectAsState()
    val tracks = remember(trackEntities) { trackEntities.map { LibraryUiItem(it.id, it.title, it.artist ?: "Unknown", it.artworkUri, it) }.toMutableStateList() }
    val favoriteTrackEntities by viewModel.favoriteTracks.collectAsState()
    val favoriteTracks = remember(favoriteTrackEntities) { favoriteTrackEntities.map { LibraryUiItem(it.id, it.title, it.artist ?: "Unknown", it.artworkUri, it) }.toMutableStateList() }
    val favoriteAlbumEntities by viewModel.favoriteAlbums.collectAsState()
    val favoriteAlbums = remember(favoriteAlbumEntities) { favoriteAlbumEntities.map { LibraryUiItem(it.id, it.title, it.artist ?: "Unknown Artist", it.artworkUri) }.toMutableStateList() }
    val favoriteArtistEntities by viewModel.favoriteArtists.collectAsState()
    val favoriteArtists = remember(favoriteArtistEntities) { favoriteArtistEntities.map { LibraryUiItem(it.id, it.name, "Artist", it.photoUri) }.toMutableStateList() }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    
    androidx.compose.runtime.LaunchedEffect(renameTrigger) {
        if (renameTrigger > 0 && selectedItems.size == 1) {
            renameText = playlists.find { selectedItems.contains("playlist_${it.id}") }?.title ?: ""
            showRenameDialog = true
        }
    }

    if (showRenameDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showRenameDialog = false }) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Rename Playlist",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showRenameDialog = false },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = {
                                val selectedId = selectedItems.firstOrNull()?.removePrefix("playlist_")
                                if (selectedId != null && renameText.isNotBlank()) {
                                    val index = playlists.indexOfFirst { it.id == selectedId }
                                    if (index != -1) {
                                        playlists[index] = playlists[index].copy(title = renameText)
                                        onRenameComplete(renameText) // Keeping this as renameText is fine since it's just the new name
                                    }
                                }
                                showRenameDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Text("Rename", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    val deleteLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val tracksToDelete = tracks.filter { selectedItems.contains("track_${it.id}") }.mapNotNull { it.track?.id }
            viewModel.deleteTracks(tracksToDelete)
            tracks.removeAll { selectedItems.contains("track_${it.id}") }
            onClearSelection()
        }
        showDeleteDialog = false
    }

    androidx.compose.runtime.LaunchedEffect(deleteTrigger) {
        if (deleteTrigger > 0 && selectedItems.isNotEmpty()) {
            showDeleteDialog = true
        }
    }



    if (showDeleteDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showDeleteDialog = false }) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Delete ${if (selectedItems.size == 1) "item" else "${selectedItems.size} items"}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    val itemName = when(tabName) {
                        "Playlists" -> playlists.find { selectedItems.contains("playlist_${it.id}") }?.title
                        "Albums" -> albums.find { selectedItems.contains("album_${it.id}") }?.title
                        "Artists" -> artists.find { selectedItems.contains("artist_${it.id}") }?.title
                        else -> tracks.find { selectedItems.contains("track_${it.id}") }?.title
                    } ?: ""
                    Text(
                        text = if (selectedItems.size == 1) 
                            "Are you sure you want to delete \"${itemName}\"? This action cannot be undone." 
                        else 
                            "Are you sure you want to delete ${selectedItems.size} items? This action cannot be undone.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showDeleteDialog = false },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = {
                                if (tabName == "Tracks") {
                                    val tracksToDelete = selectedItems.mapNotNull { if (it.startsWith("track_")) it.removePrefix("track_") else null }
                                    if (tracksToDelete.isNotEmpty() && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                        try {
                                            val uris = tracksToDelete.map { android.content.ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, it.toLong()) }
                                            val pendingIntent = android.provider.MediaStore.createDeleteRequest(context.contentResolver, uris)
                                            deleteLauncher.launch(androidx.activity.result.IntentSenderRequest.Builder(pendingIntent.intentSender).build())
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            showDeleteDialog = false
                                        }
                                    } else {
                                        if (tracksToDelete.isNotEmpty()) {
                                            try {
                                                tracksToDelete.forEach { id ->
                                                    context.contentResolver.delete(android.content.ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toLong()), null, null)
                                                }
                                                viewModel.deleteTracks(tracksToDelete)
                                            } catch(e: Exception) { e.printStackTrace() }
                                        }
                                        tracks.removeAll { selectedItems.contains("track_${it.id}") }
                                        onClearSelection()
                                        showDeleteDialog = false
                                    }
                                } else {
                                    if (tabName == "Playlists") {
                                        val itemsList = selectedItems.mapNotNull { if (it.startsWith("playlist_")) it.removePrefix("playlist_") else null }
                                        val titles = playlists.filter { itemsList.contains(it.id) }.map { it.title }
                                        viewModel.deletePlaylists(titles)
                                        playlists.removeAll { itemsList.contains(it.id) }
                                    } else if (tabName == "Albums") {
                                        val itemsList = selectedItems.mapNotNull { if (it.startsWith("album_")) it.removePrefix("album_") else null }
                                        val titles = albums.filter { itemsList.contains(it.id) }.map { it.title }
                                        viewModel.deleteAlbums(titles)
                                        albums.removeAll { itemsList.contains(it.id) }
                                    } else if (tabName == "Artists") {
                                        val itemsList = selectedItems.mapNotNull { if (it.startsWith("artist_")) it.removePrefix("artist_") else null }
                                        val titles = artists.filter { itemsList.contains(it.id) }.map { it.title }
                                        viewModel.deleteArtists(titles)
                                        artists.removeAll { itemsList.contains(it.id) }
                                    }
                                    onClearSelection()
                                    showDeleteDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Text("Delete", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    if (sortExpanded) {
        SortBottomSheet(
            onDismissRequest = { sortExpanded = false },
            currentSortOption = sortOption,
            currentSortOrder = sortOrder,
            onApply = { option, order ->
                sortOption = option
                sortOrder = order
                sortExpanded = false
            }
        )
    }

    val sortedPlaylists: List<LibraryUiItem> = androidx.compose.runtime.remember(playlists.toList(), sortOption, sortOrder) {
        if (sortOption == "Name") {
            if (sortOrder == "Ascending") playlists.sortedBy { it.title } else playlists.sortedByDescending { it.title }
        } else {
            if (sortOrder == "Ascending") playlists else playlists.reversed()
        }
    }

    val sortedAlbums: List<LibraryUiItem> = androidx.compose.runtime.remember(albums.toList(), sortOption, sortOrder) {
        if (sortOption == "Name") {
            if (sortOrder == "Ascending") albums.sortedBy { it.title } else albums.sortedByDescending { it.title }
        } else {
            if (sortOrder == "Ascending") albums else albums.reversed()
        }
    }

    val sortedArtists: List<LibraryUiItem> = androidx.compose.runtime.remember(artists.toList(), sortOption, sortOrder) {
        if (sortOption == "Name") {
            if (sortOrder == "Ascending") artists.sortedBy { it.title } else artists.sortedByDescending { it.title }
        } else {
            if (sortOrder == "Ascending") artists else artists.reversed()
        }
    }

    val sortedTracks: List<LibraryUiItem> = androidx.compose.runtime.remember(tracks.toList(), sortOption, sortOrder) {
        if (sortOption == "Name") {
            if (sortOrder == "Ascending") tracks.sortedBy { it.title } else tracks.sortedByDescending { it.title }
        } else {
            if (sortOrder == "Ascending") tracks else tracks.reversed()
        }
    }

    val sortedFavoriteTracks: List<LibraryUiItem> = androidx.compose.runtime.remember(favoriteTracks.toList(), sortOption, sortOrder) {
        if (sortOption == "Name") {
            if (sortOrder == "Ascending") favoriteTracks.sortedBy { it.title } else favoriteTracks.sortedByDescending { it.title }
        } else {
            if (sortOrder == "Ascending") favoriteTracks else favoriteTracks.reversed()
        }
    }

    val sortedFavoriteAlbums: List<LibraryUiItem> = androidx.compose.runtime.remember(favoriteAlbums.toList(), sortOption, sortOrder) {
        if (sortOption == "Name") {
            if (sortOrder == "Ascending") favoriteAlbums.sortedBy { it.title } else favoriteAlbums.sortedByDescending { it.title }
        } else {
            if (sortOrder == "Ascending") favoriteAlbums else favoriteAlbums.reversed()
        }
    }

    val sortedFavoriteArtists: List<LibraryUiItem> = androidx.compose.runtime.remember(favoriteArtists.toList(), sortOption, sortOrder) {
        if (sortOption == "Name") {
            if (sortOrder == "Ascending") favoriteArtists.sortedBy { it.title } else favoriteArtists.sortedByDescending { it.title }
        } else {
            if (sortOrder == "Ascending") favoriteArtists else favoriteArtists.reversed()
        }
    }

    val handlePlayAll = {
        val tracksToPlay = when (tabName) {
            "Tracks" -> sortedTracks.mapNotNull { it.track }
            "Albums" -> sortedAlbums.flatMap { a -> trackEntities.filter { it.album == a.title }.sortedBy { it.trackNumber ?: 0 } }
            "Artists" -> sortedArtists.flatMap { a -> trackEntities.filter { it.artist == a.title || it.albumArtist == a.title }.sortedWith(compareBy({ it.album }, { it.trackNumber ?: 0 })) }
            else -> trackEntities
        }
        if (tracksToPlay.isNotEmpty()) {
            viewModel.setCurrentlyPlaying(tracksToPlay.first(), tracksToPlay)
        }
    }

    val handleShuffleAll = {
        val tracksToPlay = when (tabName) {
            "Tracks" -> sortedTracks.mapNotNull { it.track }.shuffled()
            "Albums" -> sortedAlbums.shuffled().flatMap { a -> trackEntities.filter { it.album == a.title }.sortedBy { it.trackNumber ?: 0 } }
            "Artists" -> sortedArtists.shuffled().flatMap { a -> trackEntities.filter { it.artist == a.title || it.albumArtist == a.title }.sortedWith(compareBy({ it.album }, { it.trackNumber ?: 0 })) }
            else -> trackEntities.shuffled()
        }
        if (tracksToPlay.isNotEmpty()) {
            viewModel.setCurrentlyPlaying(tracksToPlay.first(), tracksToPlay)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f))
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = bottomPadding + 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.physicsBounceOverscroll().fillMaxSize()
        ) {
            item {
                if (isSelectionMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onClearSelection) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close selection", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "${selectedItems.size} selected",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { onSelectAll(emptyList()) }) {
                            Text("Select all", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { sortExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort by",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        if (tabName == "Playlists") {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { onCreatePlaylistClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create Playlist",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .clickable { handleShuffleAll() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = "Shuffle",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .clickable { handlePlayAll() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            if (tabName == "Playlists") {
                if (playlists.isEmpty()) {
                    item { PlaylistsEmptyState(modifier = Modifier.fillMaxWidth(), onCreatePlaylistClick = onCreatePlaylistClick) }
                } else {
                    val fallbackImage = "https://lh3.googleusercontent.com/aida-public/AB6AXuDK2gSPmhFiKqcqPLlCJlIp7lxpTt2scS9SuOmzxmZKXa1UQIjSKITZh8tGxaLLsMWtK_rqugpIF6kWjdqifIFpbIHQ51KFkHHGCwprGn7T1jWwAFiUiOgft22mJtHc311emev_Y9qChhO44k-VwJC7dvX80Zs-JHFurqrp7BRfflgHO2uz-vspGyR9BoWhQUaXuELDgddlmK__JFlAjdrkjKUgyxH0SVRHhhE0iqWq7lQMTieDIl6s1Oh1frE5nhxruwt9dXwi3SRK"
                    if (isGridView) {
                        val chunked = sortedPlaylists.chunked(2)
                        items(chunked.size) { i ->
                            val rowItems = chunked[i]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AlbumResultItem(title = rowItems[0].title, year = rowItems[0].subtitle, imageUrl = rowItems[0].imageUrl ?: fallbackImage, modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains("playlist_${rowItems[0].id}"), onLongClick = { onToggleSelection("playlist_${rowItems[0].id}") }, onClick = { if (isSelectionMode) onToggleSelection("playlist_${rowItems[0].id}") else onNavigateToPlaylistDetails(rowItems[0].title) })
                                if (rowItems.size > 1) {
                                    AlbumResultItem(title = rowItems[1].title, year = rowItems[1].subtitle, imageUrl = rowItems[1].imageUrl ?: fallbackImage, modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains("playlist_${rowItems[1].id}"), onLongClick = { onToggleSelection("playlist_${rowItems[1].id}") }, onClick = { if (isSelectionMode) onToggleSelection("playlist_${rowItems[1].id}") else onNavigateToPlaylistDetails(rowItems[1].title) })
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    } else {
                        items(sortedPlaylists.size) { i ->
                            val p = sortedPlaylists[i]
                            PlaylistResultItem(
                                title = p.title, 
                                subtitle = p.subtitle, 
                                imageUrl = p.imageUrl ?: fallbackImage, 
                                isSelectionMode = isSelectionMode,
                                isSelected = selectedItems.contains("playlist_${p.id}"),
                                onLongClick = { onToggleSelection("playlist_${p.id}") },
                                onClick = { if (isSelectionMode) onToggleSelection("playlist_${p.id}") else onNavigateToPlaylistDetails(p.title) }
                            )
                        }
                    }
                }
            } else if (tabName == "Albums") {
                if (isGridView) {
                    val chunked = sortedAlbums.chunked(2)
                    items(chunked.size) { i ->
                        val rowItems = chunked[i]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AlbumResultItem(title = rowItems[0].title, year = rowItems[0].subtitle, imageUrl = rowItems[0].imageUrl ?: "", modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains("album_${rowItems[0].id}"), onLongClick = { onToggleSelection("album_${rowItems[0].id}") }, onClick = { if (isSelectionMode) onToggleSelection("album_${rowItems[0].id}") else onNavigateToAlbumDetails(rowItems[0].title) })
                            if (rowItems.size > 1) {
                                AlbumResultItem(title = rowItems[1].title, year = rowItems[1].subtitle, imageUrl = rowItems[1].imageUrl ?: "", modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains("album_${rowItems[1].id}"), onLongClick = { onToggleSelection("album_${rowItems[1].id}") }, onClick = { if (isSelectionMode) onToggleSelection("album_${rowItems[1].id}") else onNavigateToAlbumDetails(rowItems[1].title) })
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    items(sortedAlbums.size) { i ->
                        val a = sortedAlbums[i]
                        PlaylistResultItem(
                            title = a.title, 
                            subtitle = a.subtitle, 
                            imageUrl = a.imageUrl ?: "", 
                            modifier = Modifier.fillMaxWidth(),
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedItems.contains("album_${a.id}"),
                            onLongClick = { onToggleSelection("album_${a.id}") },
                            onClick = { if (isSelectionMode) onToggleSelection("album_${a.id}") else onNavigateToAlbumDetails(a.title) }
                        )
                    }
                }
            } else if (tabName == "Artists") {
                if (isGridView) {
                    val chunked = sortedArtists.chunked(2)
                    items(chunked.size) { i ->
                        val rowItems = chunked[i]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ArtistResultItem(name = rowItems[0].title, imageUrl = rowItems[0].imageUrl ?: "", modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains("artist_${rowItems[0].id}"), onLongClick = { onToggleSelection("artist_${rowItems[0].id}") }, onClick = { if (isSelectionMode) onToggleSelection("artist_${rowItems[0].id}") else onNavigateToArtistDetails(rowItems[0].title) })
                            if (rowItems.size > 1) {
                                ArtistResultItem(name = rowItems[1].title, imageUrl = rowItems[1].imageUrl ?: "", modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains("artist_${rowItems[1].id}"), onLongClick = { onToggleSelection("artist_${rowItems[1].id}") }, onClick = { if (isSelectionMode) onToggleSelection("artist_${rowItems[1].id}") else onNavigateToArtistDetails(rowItems[1].title) })
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    items(sortedArtists.size) { i ->
                        val a = sortedArtists[i]
                        PlaylistResultItem(
                            title = a.title, 
                            subtitle = a.subtitle, 
                            imageUrl = a.imageUrl ?: "", 
                            modifier = Modifier.fillMaxWidth(),
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedItems.contains("artist_${a.id}"),
                            onLongClick = { onToggleSelection("artist_${a.id}") },
                            onClick = { if (isSelectionMode) onToggleSelection("artist_${a.id}") else onNavigateToArtistDetails(a.title) }
                        )
                    }
                }
            } else if (tabName == "Favorites") {
                if (favoriteTracks.isEmpty() && favoriteAlbums.isEmpty() && favoriteArtists.isEmpty()) {
                    item {
                        FavoritesEmptyState(
                            onExploreClick = onExploreClick
                        )
                    }
                } else {
                    if (sortedFavoriteArtists.isNotEmpty()) {
                        item {
                            Text(
                                text = "Artists",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                            )
                        }
                        if (isGridView) {
                            val chunked = sortedFavoriteArtists.chunked(2)
                            items(chunked.size) { i ->
                                val rowItems = chunked[i]
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    ArtistResultItem(name = rowItems[0].title, imageUrl = rowItems[0].imageUrl ?: "", modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains("artist_${rowItems[0].id}"), onLongClick = { onToggleSelection("artist_${rowItems[0].id}") }, onClick = { if (isSelectionMode) onToggleSelection("artist_${rowItems[0].id}") else onNavigateToArtistDetails(rowItems[0].title) })
                                    if (rowItems.size > 1) {
                                        ArtistResultItem(name = rowItems[1].title, imageUrl = rowItems[1].imageUrl ?: "", modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains("artist_${rowItems[1].id}"), onLongClick = { onToggleSelection("artist_${rowItems[1].id}") }, onClick = { if (isSelectionMode) onToggleSelection("artist_${rowItems[1].id}") else onNavigateToArtistDetails(rowItems[1].title) })
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        } else {
                            items(sortedFavoriteArtists.size) { i ->
                                val a = sortedFavoriteArtists[i]
                                PlaylistResultItem(
                                    title = a.title, 
                                    subtitle = a.subtitle, 
                                    imageUrl = a.imageUrl ?: "", 
                                    modifier = Modifier.fillMaxWidth(),
                                    isSelectionMode = isSelectionMode,
                                    isSelected = selectedItems.contains("artist_${a.id}"),
                                    onLongClick = { onToggleSelection("artist_${a.id}") },
                                    onClick = { if (isSelectionMode) onToggleSelection("artist_${a.id}") else onNavigateToArtistDetails(a.title) }
                                )
                            }
                        }
                    }
                    
                    if (sortedFavoriteTracks.isNotEmpty()) {
                        item {
                            Text(
                                text = "Tracks",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp, top = if (sortedFavoriteArtists.isNotEmpty()) 16.dp else 8.dp)
                            )
                        }
                        items(sortedFavoriteTracks.size) { i ->
                            val t = sortedFavoriteTracks[i]
                            SongResultItem(
                                title = t.title, 
                                artist = t.subtitle, 
                                duration = t.track?.let { formatDuration(it.durationMs) } ?: "0:00", 
                                imageUrl = t.imageUrl ?: "",
                                isSelectionMode = isSelectionMode,
                                isSelected = selectedItems.contains("track_${t.id}"),
                                qualityBadgeResId = t.track?.getQualityBadgeResId(),
                                onLongClick = { onToggleSelection("track_${t.id}") },
                                onClick = { 
                                    if (isSelectionMode) {
                                        onToggleSelection("track_${t.id}")
                                    } else {
                                        t.track?.let { viewModel.setCurrentlyPlaying(it, favoriteTrackEntities) }
                                    }
                                }
                            )
                        }
                    }

                    if (sortedFavoriteAlbums.isNotEmpty()) {
                        item {
                            Text(
                                text = "Albums",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp, top = if (sortedFavoriteArtists.isNotEmpty() || sortedFavoriteTracks.isNotEmpty()) 16.dp else 8.dp)
                            )
                        }
                        if (isGridView) {
                            val chunked = sortedFavoriteAlbums.chunked(2)
                            items(chunked.size) { i ->
                                val rowItems = chunked[i]
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    AlbumResultItem(title = rowItems[0].title, year = rowItems[0].subtitle, imageUrl = rowItems[0].imageUrl ?: "", modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains("album_${rowItems[0].id}"), onLongClick = { onToggleSelection("album_${rowItems[0].id}") }, onClick = { if (isSelectionMode) onToggleSelection("album_${rowItems[0].id}") else onNavigateToAlbumDetails(rowItems[0].title) })
                                    if (rowItems.size > 1) {
                                        AlbumResultItem(title = rowItems[1].title, year = rowItems[1].subtitle, imageUrl = rowItems[1].imageUrl ?: "", modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains("album_${rowItems[1].id}"), onLongClick = { onToggleSelection("album_${rowItems[1].id}") }, onClick = { if (isSelectionMode) onToggleSelection("album_${rowItems[1].id}") else onNavigateToAlbumDetails(rowItems[1].title) })
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        } else {
                            items(sortedFavoriteAlbums.size) { i ->
                                val a = sortedFavoriteAlbums[i]
                                PlaylistResultItem(
                                    title = a.title, 
                                    subtitle = a.subtitle, 
                                    imageUrl = a.imageUrl ?: "", 
                                    modifier = Modifier.fillMaxWidth(),
                                    isSelectionMode = isSelectionMode,
                                    isSelected = selectedItems.contains("album_${a.id}"),
                                    onLongClick = { onToggleSelection("album_${a.id}") },
                                    onClick = { if (isSelectionMode) onToggleSelection("album_${a.id}") else onNavigateToAlbumDetails(a.title) }
                                )
                            }
                        }
                    }
                }
            } else {
                if (isGridView) {
                    val chunked = sortedTracks.chunked(2)
                    items(chunked.size) { i ->
                        val rowItems = chunked[i]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LibraryGridItem(item = rowItems[0], modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains("track_${rowItems[0].id}"), onLongClick = { onToggleSelection("track_${rowItems[0].id}") }, onClick = { if (isSelectionMode) onToggleSelection("track_${rowItems[0].id}") else rowItems[0].track?.let { viewModel.setCurrentlyPlaying(it, trackEntities) } })
                            if (rowItems.size > 1) {
                                LibraryGridItem(item = rowItems[1], modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains("track_${rowItems[1].id}"), onLongClick = { onToggleSelection("track_${rowItems[1].id}") }, onClick = { if (isSelectionMode) onToggleSelection("track_${rowItems[1].id}") else rowItems[1].track?.let { viewModel.setCurrentlyPlaying(it, trackEntities) } })
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    items(sortedTracks.size) { i ->
                        val t = sortedTracks[i]
                        SongResultItem(
                            title = t.title, 
                            artist = t.subtitle, 
                            duration = t.track?.let { formatDuration(it.durationMs) } ?: "0:00", 
                            imageUrl = t.imageUrl ?: "",
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedItems.contains("track_${t.id}"),
                            qualityBadgeResId = t.track?.getQualityBadgeResId(),
                            onLongClick = { onToggleSelection("track_${t.id}") },
                            onClick = { 
                                if (isSelectionMode) {
                                    onToggleSelection("track_${t.id}")
                                } else {
                                    t.track?.let { viewModel.setCurrentlyPlaying(it, trackEntities) }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

data class LibraryUiItem(val id: String, val title: String, val subtitle: String, val imageUrl: String?, val track: com.aeswox.arcmusic.db.entities.Track? = null)

@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun LibraryGridItem(item: LibraryUiItem, modifier: Modifier = Modifier, isSelectionMode: Boolean = false, isSelected: Boolean = false, onLongClick: (() -> Unit)? = null, onClick: () -> Unit = {}) {
    Column(
        modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
            )
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f))
                        .border(1.5.dp, if (isSelected) androidx.compose.ui.graphics.Color.Transparent else androidx.compose.ui.graphics.Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = item.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
data class LibrarySection(val name: String, val isVisible: Boolean)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    onDismissRequest: () -> Unit,
    currentSortOption: String,
    currentSortOrder: String,
    onApply: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var sortOption by remember { mutableStateOf(currentSortOption) }
    var sortOrder by remember { mutableStateOf(currentSortOrder) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort by",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            SortOptionRow(
                title = "Name",
                icon = Icons.Default.SortByAlpha,
                isSelected = sortOption == "Name",
                onClick = { sortOption = "Name" }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SortOptionRow(
                title = "Date added",
                icon = Icons.Default.DateRange,
                isSelected = sortOption == "Date added",
                onClick = { sortOption = "Date added" }
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "ORDER",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SortOrderButton(
                    title = "Ascending",
                    icon = Icons.Default.ArrowUpward,
                    isSelected = sortOrder == "Ascending",
                    onClick = { sortOrder = "Ascending" },
                    modifier = Modifier.weight(1f)
                )
                SortOrderButton(
                    title = "Descending",
                    icon = Icons.Default.ArrowDownward,
                    isSelected = sortOrder == "Descending",
                    onClick = { sortOrder = "Descending" },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { onApply(sortOption, sortOrder) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Apply Sorting",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun SortOptionRow(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else androidx.compose.ui.graphics.Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
                .border(
                    width = 2.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun SortOrderButton(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SelectionTopBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close selection")
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "$selectedCount selected",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onSelectAll) {
            Text("Select all", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun SelectionBottomBar(
    currentTab: String,
    onAddToPlaylist: () -> Unit,
    onRename: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayLater: () -> Unit,
    onAddToFavorites: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val favoriteIcon = if (currentTab == "Favorites") Icons.Default.Favorite else Icons.Default.FavoriteBorder
        val favoriteLabel = if (currentTab == "Favorites") "REMOVE" else "FAVORITE"
        
        if (currentTab == "Playlists") {
            BottomBarActionItem(icon = Icons.Default.Edit, label = "RENAME", onClick = onRename)
            BottomBarActionItem(icon = Icons.Default.Share, label = "SHARE", onClick = onShare)
            BottomBarActionItem(icon = Icons.Default.DeleteOutline, label = "DELETE", isDestructive = true, onClick = onDelete)
        } else if (currentTab == "Artists" || currentTab == "Albums") {
            BottomBarActionItem(icon = Icons.Default.PlayArrow, label = "PLAY NEXT", onClick = onPlayNext)
            BottomBarActionItem(icon = Icons.Default.Add, label = "PLAY LATER", onClick = onPlayLater)
            BottomBarActionItem(icon = favoriteIcon, label = favoriteLabel, onClick = onAddToFavorites)
            BottomBarActionItem(icon = Icons.Default.Share, label = "SHARE", onClick = onShare)
        } else {
            BottomBarActionItem(icon = Icons.Default.PlaylistAdd, label = "PLAYLIST", onClick = onAddToPlaylist)
            if (currentTab != "Folders") {
                BottomBarActionItem(icon = favoriteIcon, label = favoriteLabel, onClick = onAddToFavorites)
            }
            BottomBarActionItem(icon = Icons.Default.Share, label = "SHARE", onClick = onShare)
            if (currentTab != "Favorites") {
                BottomBarActionItem(icon = Icons.Default.DeleteOutline, label = "DELETE", isDestructive = true, onClick = onDelete)
            }
        }
    }
}

@Composable
fun BottomBarActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    val color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.jellyClick { onClick() }
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            color = color,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun FavoritesEmptyState(
    modifier: Modifier = Modifier,
    onExploreClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "No favorites",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "No favorites yet",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Songs and albums you favorite will\nappear here.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        AppPrimaryButton(
            text = "Explore Music",
            onClick = onExploreClick,
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
            modifier = Modifier.width(200.dp)
        )
    }
}

@Composable
fun PlaylistsEmptyState(modifier: Modifier = Modifier, onCreatePlaylistClick: () -> Unit = {}) {
    Column(
        modifier = modifier
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = "No playlists",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "No playlists yet",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Create your first playlist to get started\norganizing your favorite tracks.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        AppPrimaryButton(
            text = "Create Playlist",
            icon = Icons.Default.Add,
            onClick = onCreatePlaylistClick,
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
            modifier = Modifier.width(220.dp)
        )
    }
}

@Composable
fun ExplicitBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                shape = RoundedCornerShape(3.dp)
            )
            .padding(horizontal = 4.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "E",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            ),
            color = MaterialTheme.colorScheme.surface
        )
    }
}
