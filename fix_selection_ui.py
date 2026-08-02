import re

with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

# 1. Update LibraryMainSection signature
old_sig = """@Composable
fun LibraryMainSection(
    modifier: Modifier = Modifier, 
    tabName: String = "", 
    onNavigateToAlbumDetails: () -> Unit = {}, 
    onNavigateToPlaylistDetails: () -> Unit = {}, 
    onNavigateToArtistDetails: () -> Unit = {},
    selectedItems: List<String> = emptyList(),
    onToggleSelection: (String) -> Unit = {}
) {"""
new_sig = """@Composable
fun LibraryMainSection(
    modifier: Modifier = Modifier, 
    tabName: String = "", 
    onNavigateToAlbumDetails: () -> Unit = {}, 
    onNavigateToPlaylistDetails: () -> Unit = {}, 
    onNavigateToArtistDetails: () -> Unit = {},
    selectedItems: List<String> = emptyList(),
    onToggleSelection: (String) -> Unit = {},
    onClearSelection: () -> Unit = {},
    onSelectAll: (List<String>) -> Unit = {}
) {"""
content = content.replace(old_sig, new_sig)

# 2. Update the LibraryMainSection content row
old_row_block = """        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle View",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable { },
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
                            .clickable { },
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
            }"""
new_row_block = """        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
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
                        IconButton(onClick = { isGridView = !isGridView }) {
                            Icon(
                                imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                contentDescription = "Toggle View",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .clickable { },
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
                                .clickable { },
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
            }"""
content = content.replace(old_row_block, new_row_block)

# 3. Update LibraryScreenContent to pass the new callbacks
old_main_call = """                    LibraryMainSection(
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
                    )"""
new_main_call = """                    LibraryMainSection(
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
                        },
                        onClearSelection = { selectedItems.clear() },
                        onSelectAll = { items -> 
                            // TODO implement select all properly
                        }
                    )"""
content = content.replace(old_main_call, new_main_call)

# 4. Remove SelectionTopBar usage in LibraryScreenContent
old_topbar_usage = """        if (isSelectionMode) {
            SelectionTopBar(
                selectedCount = selectedItems.size,
                onClose = { selectedItems.clear() },
                onSelectAll = {
                    // Would ideally select all current items, for now just a placeholder
                }
            )
            
            Box(modifier = Modifier.fillMaxSize().padding(bottom = bottomPadding + 16.dp), contentAlignment = Alignment.BottomCenter) {"""
new_topbar_usage = """        if (isSelectionMode) {
            Box(modifier = Modifier.fillMaxSize().padding(bottom = bottomPadding + 16.dp), contentAlignment = Alignment.BottomCenter) {"""
content = content.replace(old_topbar_usage, new_topbar_usage)

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
