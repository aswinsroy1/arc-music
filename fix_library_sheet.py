import re

with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

# Add sheet at the end of LibraryScreenContent composable
add_sheet = """
    if (showAddToPlaylistSheet) {
        AddToPlaylistSheet(onDismissRequest = { showAddToPlaylistSheet = false })
    }
"""

if "AddToPlaylistSheet(" not in content:
    end_home = content.find("}\n\n@Composable\nfun LibraryHeader")
    if end_home != -1:
        content = content[:end_home] + add_sheet + content[end_home:]
    with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
        f.write(content)
print("Done")
