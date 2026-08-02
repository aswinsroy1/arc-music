import re

with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

# Add showAddToPlaylistSheet
if "var showAddToPlaylistSheet by remember" not in content:
    state_decl = "    var selectedItems = remember { mutableStateListOf<String>() }"
    new_state_decl = state_decl + "\n    var showAddToPlaylistSheet by remember { mutableStateOf(false) }"
    content = content.replace(state_decl, new_state_decl)

old_onadd = "onAddToPlaylist = { selectedItems.clear() }"
new_onadd = "onAddToPlaylist = { showAddToPlaylistSheet = true }"
content = content.replace(old_onadd, new_onadd)

# Add sheet at the end of MusicHomeScreen composable
add_sheet = """
    if (showAddToPlaylistSheet) {
        AddToPlaylistSheet(onDismissRequest = { showAddToPlaylistSheet = false })
    }
"""

# Let's just insert it at the end of the top-level Box in MusicHomeScreen
# First, let's find the end of MusicHomeScreen.
end_home = content.find("}\n\n@Composable\nfun FilterChipRow")
if end_home != -1:
    content = content[:end_home] + add_sheet + content[end_home:]

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
print("Done Library")
