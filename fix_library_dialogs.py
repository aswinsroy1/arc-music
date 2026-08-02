import re

with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

old_rename_dialog = """        androidx.compose.material3.AlertDialog(
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
        )"""

new_rename_dialog = """        androidx.compose.ui.window.Dialog(onDismissRequest = { showRenameDialog = false }) {
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
                                val originalName = selectedItems.firstOrNull()
                                if (originalName != null && renameText.isNotBlank()) {
                                    val index = playlists.indexOfFirst { it.title == originalName }
                                    if (index != -1) {
                                        playlists[index] = playlists[index].copy(title = renameText)
                                        onRenameComplete(renameText)
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
        }"""
        
content = content.replace(old_rename_dialog, new_rename_dialog)

old_delete_dialog_start = """        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false }"""

# Find full old delete dialog
start_idx = content.find(old_delete_dialog_start)
if start_idx != -1:
    end_idx = content.find("        )\n    }", start_idx) + 9
    old_delete_dialog = content[start_idx:end_idx]
    
    new_delete_dialog = """        androidx.compose.ui.window.Dialog(onDismissRequest = { showDeleteDialog = false }) {
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
                    
                    val itemName = selectedItems.firstOrNull() ?: ""
                    Text(
                        text = if (selectedItems.size == 1) 
                            "Are you sure you want to delete \"${itemName}\"? This action cannot be undone." 
                        else 
                            "Are you sure you want to delete ${selectedItems.size} items? This action cannot be undone.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
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
                                playlists.removeAll { selectedItems.contains(it.title) }
                                albums.removeAll { selectedItems.contains(it.title) }
                                artists.removeAll { selectedItems.contains(it.title) }
                                tracks.removeAll { selectedItems.contains(it.title) }
                                onClearSelection()
                                showDeleteDialog = false
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
        }"""
    content = content[:start_idx] + new_delete_dialog + content[end_idx:]

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)

print("Done")
