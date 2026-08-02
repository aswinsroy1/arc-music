import re
with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

content = content.replace("    var showDeleteDialog by remember { mutableStateOf(false) }", """    var showDeleteDialog by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(deleteTrigger) {
        if (deleteTrigger > 0 && selectedItems.isNotEmpty()) {
            showDeleteDialog = true
        }
    }""")

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
