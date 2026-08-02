import re
with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

# Remove delete icon near select all
content = content.replace("""                    TextButton(onClick = { onSelectAll(emptyList()) }) {
                        Text("Select all", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }""", """                    TextButton(onClick = { onSelectAll(emptyList()) }) {
                        Text("Select all", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                }""")

# Add deleteTrigger parameter to LibraryMainSection
content = content.replace("""    selectedItems: List<String> = emptyList(),
    onToggleSelection: (String) -> Unit = {},
    onClearSelection: () -> Unit = {},
    onSelectAll: (List<String>) -> Unit = {}
) {""", """    selectedItems: List<String> = emptyList(),
    onToggleSelection: (String) -> Unit = {},
    onClearSelection: () -> Unit = {},
    onSelectAll: (List<String>) -> Unit = {},
    deleteTrigger: Int = 0
) {""")


# Add LaunchedEffect to LibraryMainSection
content = content.replace("""    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {""", """    var showDeleteDialog by remember { mutableStateOf(false) }
    
    androidx.compose.runtime.LaunchedEffect(deleteTrigger) {
        if (deleteTrigger > 0 && selectedItems.isNotEmpty()) {
            showDeleteDialog = true
        }
    }

    if (showDeleteDialog) {""")

# Pass deleteTrigger from LibraryScreenContent
content = content.replace("""    var isSelectionMode = selectedItems.isNotEmpty()
    var showRearrangeSheet by remember { mutableStateOf(false) }""", """    var isSelectionMode = selectedItems.isNotEmpty()
    var showRearrangeSheet by remember { mutableStateOf(false) }
    var deleteTrigger by remember { mutableStateOf(0) }""")

content = content.replace("""                        onClearSelection = { selectedItems.clear() },
                        onSelectAll = { items -> 
                            // TODO implement select all properly
                        }
                    )
                }""", """                        onClearSelection = { selectedItems.clear() },
                        onSelectAll = { items -> 
                            // TODO implement select all properly
                        },
                        deleteTrigger = if (currentTab == tabs.getOrNull(pagerState.currentPage)) deleteTrigger else 0
                    )
                }""")

# Trigger deleteTrigger from bottom bar
content = content.replace("""                onDelete = { selectedItems.clear() }
            )
        }
    }""", """                onDelete = { deleteTrigger++ }
            )
        }
    }""")

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
