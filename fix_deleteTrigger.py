import re
with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

content = content.replace("""    val isSelectionMode = selectedItems.isNotEmpty()
    androidx.compose.runtime.LaunchedEffect""", """    val isSelectionMode = selectedItems.isNotEmpty()
    var deleteTrigger by remember { mutableStateOf(0) }
    androidx.compose.runtime.LaunchedEffect""")

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
