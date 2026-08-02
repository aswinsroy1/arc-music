import re
with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

old_fun = "fun LibraryScreenContent(modifier: Modifier = Modifier, bottomPadding: androidx.compose.ui.unit.Dp, onNavigateToAlbumDetails: () -> Unit = {}, onNavigateToPlaylistDetails: () -> Unit = {}, onNavigateToArtistDetails: () -> Unit = {}) {"
new_fun = "fun LibraryScreenContent(modifier: Modifier = Modifier, bottomPadding: androidx.compose.ui.unit.Dp, onNavigateToAlbumDetails: () -> Unit = {}, onNavigateToPlaylistDetails: () -> Unit = {}, onNavigateToArtistDetails: () -> Unit = {}, onSelectionModeChange: (Boolean) -> Unit = {}) {"
content = content.replace(old_fun, new_fun)

old_state = """    val selectedItems = remember { mutableStateListOf<String>() }
    val isSelectionMode = selectedItems.isNotEmpty()"""
new_state = """    val selectedItems = remember { mutableStateListOf<String>() }
    val isSelectionMode = selectedItems.isNotEmpty()
    androidx.compose.runtime.LaunchedEffect(isSelectionMode) {
        onSelectionModeChange(isSelectionMode)
    }"""
content = content.replace(old_state, new_state)

old_selection_bar = """    if (isSelectionMode) {
        Box(modifier = Modifier.fillMaxSize().padding(bottom = bottomPadding + 16.dp), contentAlignment = Alignment.BottomCenter) {
            SelectionBottomBar(
                onAddToPlaylist = { selectedItems.clear() },
                onAddToFavorites = { selectedItems.clear() },
                onShare = { selectedItems.clear() },
                onDelete = { selectedItems.clear() }
            )
        }
    }"""
new_selection_bar = """    if (isSelectionMode) {
        Box(modifier = Modifier.fillMaxSize().padding(bottom = 24.dp), contentAlignment = Alignment.BottomCenter) {
            SelectionBottomBar(
                onAddToPlaylist = { selectedItems.clear() },
                onAddToFavorites = { selectedItems.clear() },
                onShare = { selectedItems.clear() },
                onDelete = { selectedItems.clear() }
            )
        }
    }"""
content = content.replace(old_selection_bar, new_selection_bar)

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
