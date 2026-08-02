import re
with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

# Add renameTrigger to LibraryMainSection parameters
content = content.replace("""    onSelectAll: (List<String>) -> Unit = {},
    deleteTrigger: Int = 0
) {""", """    onSelectAll: (List<String>) -> Unit = {},
    deleteTrigger: Int = 0,
    renameTrigger: Int = 0,
    onRenameComplete: (String) -> Unit = {}
) {""")

# Add rename state and dialog to LibraryMainSection
rename_dialog = """    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    
    androidx.compose.runtime.LaunchedEffect(renameTrigger) {
        if (renameTrigger > 0 && selectedItems.size == 1) {
            renameText = selectedItems.first()
            showRenameDialog = true
        }
    }

    if (showRenameDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = {
                Text("Rename Playlist", style = MaterialTheme.typography.headlineMedium)
            },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val originalName = selectedItems.firstOrNull()
                    if (originalName != null && renameText.isNotBlank()) {
                        val index = playlists.indexOfFirst { it.title == originalName }
                        if (index != -1) {
                            playlists[index] = playlists[index].copy(title = renameText)
                            onRenameComplete(renameText)
                        }
                    }
                    showRenameDialog = false
                }) {
                    Text("Rename", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }"""

content = content.replace("    var showDeleteDialog by remember { mutableStateOf(false) }", rename_dialog + "\n\n    var showDeleteDialog by remember { mutableStateOf(false) }")

# Add renameTrigger to LibraryScreenContent state
content = content.replace("""    var deleteTrigger by remember { mutableStateOf(0) }
    androidx.compose.runtime.LaunchedEffect(isSelectionMode) {""", """    var deleteTrigger by remember { mutableStateOf(0) }
    var renameTrigger by remember { mutableStateOf(0) }
    androidx.compose.runtime.LaunchedEffect(isSelectionMode) {""")

# Pass renameTrigger to LibraryMainSection
content = content.replace("""                        deleteTrigger = if (currentTab == tabs.getOrNull(pagerState.currentPage)) deleteTrigger else 0
                    )""", """                        deleteTrigger = if (currentTab == tabs.getOrNull(pagerState.currentPage)) deleteTrigger else 0,
                        renameTrigger = if (currentTab == tabs.getOrNull(pagerState.currentPage)) renameTrigger else 0,
                        onRenameComplete = { newName ->
                            val oldName = selectedItems.firstOrNull()
                            if (oldName != null) {
                                selectedItems.remove(oldName)
                                selectedItems.add(newName)
                            }
                        }
                    )""")

# Update SelectionBottomBar onRename action
content = content.replace("""                onAddToPlaylist = { selectedItems.clear() },
                onRename = { selectedItems.clear() },
                onAddToFavorites = { selectedItems.clear() },""", """                onAddToPlaylist = { selectedItems.clear() },
                onRename = { renameTrigger++ },
                onAddToFavorites = { selectedItems.clear() },""")

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
