with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

old_screen_content = """
fun LibraryScreenContent(modifier: Modifier = Modifier, bottomPadding: androidx.compose.ui.unit.Dp, onNavigateToAlbumDetails: () -> Unit = {}, onNavigateToPlaylistDetails: () -> Unit = {}, onNavigateToArtistDetails: () -> Unit = {}) {
    val allSections = remember { mutableStateListOf(LibrarySection("Playlists", true), LibrarySection("Tracks", true), LibrarySection("Albums", true), LibrarySection("Artists", true), LibrarySection("Favorites", false), LibrarySection("Folders", false)) }
    val tabs = allSections.filter { it.isVisible }.map { it.name }
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(initialPage = 1, pageCount = { tabs.size })
    var showRearrangeSheet by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
"""

new_screen_content = """
fun LibraryScreenContent(modifier: Modifier = Modifier, bottomPadding: androidx.compose.ui.unit.Dp, onNavigateToAlbumDetails: () -> Unit = {}, onNavigateToPlaylistDetails: () -> Unit = {}, onNavigateToArtistDetails: () -> Unit = {}) {
    val allSections = remember { mutableStateListOf(LibrarySection("Playlists", true), LibrarySection("Tracks", true), LibrarySection("Albums", true), LibrarySection("Artists", true), LibrarySection("Favorites", false), LibrarySection("Folders", false)) }
    val tabs = allSections.filter { it.isVisible }.map { it.name }
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(initialPage = 1, pageCount = { tabs.size })
    var showRearrangeSheet by remember { mutableStateOf(false) }
    
    val selectedItems = remember { mutableStateListOf<String>() }
    val isSelectionMode = selectedItems.isNotEmpty()

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
"""

content = content.replace(old_screen_content.strip(), new_screen_content.strip())

# Need to close the Box at the end of LibraryScreenContent
old_close = """
        }
    }

    if (showRearrangeSheet) {
"""
new_close = """
        }
        
        if (isSelectionMode) {
            SelectionTopBar(
                selectedCount = selectedItems.size,
                onClose = { selectedItems.clear() },
                onSelectAll = {
                    // Would ideally select all current items, for now just a placeholder
                }
            )
            
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = bottomPadding + 16.dp)) {
                SelectionBottomBar(
                    onAddToPlaylist = { selectedItems.clear() },
                    onAddToFavorites = { selectedItems.clear() },
                    onShare = { selectedItems.clear() },
                    onDelete = { selectedItems.clear() }
                )
            }
        }
    }

    if (showRearrangeSheet) {
"""
content = content.replace(old_close.strip(), new_close.strip())

# Replace LibraryMainSection call
old_main_call = """
                    val currentTab = tabs.getOrNull(page) ?: ""
                    LibraryMainSection(tabName = currentTab, onNavigateToAlbumDetails = onNavigateToAlbumDetails, onNavigateToPlaylistDetails = onNavigateToPlaylistDetails, onNavigateToArtistDetails = onNavigateToArtistDetails)
                }
"""
new_main_call = """
                    val currentTab = tabs.getOrNull(page) ?: ""
                    LibraryMainSection(
                        tabName = currentTab, 
                        onNavigateToAlbumDetails = onNavigateToAlbumDetails, 
                        onNavigateToPlaylistDetails = onNavigateToPlaylistDetails, 
                        onNavigateToArtistDetails = onNavigateToArtistDetails,
                        selectedItems = selectedItems,
                        onToggleSelection = { title ->
                            if (selectedItems.contains(title)) {
                                selectedItems.remove(title)
                            } else {
                                selectedItems.add(title)
                            }
                        }
                    )
                }
"""
content = content.replace(old_main_call.strip(), new_main_call.strip())

# Modify LibraryMainSection definition
old_main_def = """
@Composable
fun LibraryMainSection(modifier: Modifier = Modifier, tabName: String = "", onNavigateToAlbumDetails: () -> Unit = {}, onNavigateToPlaylistDetails: () -> Unit = {}, onNavigateToArtistDetails: () -> Unit = {}) {
    var sortExpanded by remember { mutableStateOf(false) }
"""
new_main_def = """
@Composable
fun LibraryMainSection(
    modifier: Modifier = Modifier, 
    tabName: String = "", 
    onNavigateToAlbumDetails: () -> Unit = {}, 
    onNavigateToPlaylistDetails: () -> Unit = {}, 
    onNavigateToArtistDetails: () -> Unit = {},
    selectedItems: List<String> = emptyList(),
    onToggleSelection: (String) -> Unit = {}
) {
    var sortExpanded by remember { mutableStateOf(false) }
    val isSelectionMode = selectedItems.isNotEmpty()
"""
content = content.replace(old_main_def.strip(), new_main_def.strip())


with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
