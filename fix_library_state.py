import re

with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

if "var showAddToPlaylistSheet" not in content:
    content = content.replace(
        "    val selectedItems = remember { mutableStateListOf<String>() }",
        "    var showAddToPlaylistSheet by remember { mutableStateOf(false) }\n    val selectedItems = remember { mutableStateListOf<String>() }"
    )

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
