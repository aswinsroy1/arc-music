import re

with open('app/src/main/java/com/example/QueueScreen.kt', 'r') as f:
    content = f.read()

# Add state
if "var showAddToPlaylistSheet" not in content:
    state_decl = "    var showMoreOptions by remember { mutableStateOf(false) }"
    new_state_decl = state_decl + "\n    var showAddToPlaylistSheet by remember { mutableStateOf(false) }"
    content = content.replace(state_decl, new_state_decl)

# Update onClick
old_click = "onClick = { },"
new_click = "onClick = { showAddToPlaylistSheet = true },"
# Be careful not to replace the first `onClick = { }` blindly. We know it's right before `colors = ButtonDefaults.buttonColors`
content = content.replace("onClick = { },\n                                colors = ButtonDefaults.buttonColors(", 
                          "onClick = { showAddToPlaylistSheet = true },\n                                colors = ButtonDefaults.buttonColors(")

# Add component at the end
add_sheet = """
    if (showAddToPlaylistSheet) {
        AddToPlaylistSheet(onDismissRequest = { showAddToPlaylistSheet = false })
    }
"""

end_queue = content.find("}\n\n@Composable\nfun QueueItemRow")
if end_queue != -1:
    content = content[:end_queue] + add_sheet + content[end_queue:]

with open('app/src/main/java/com/example/QueueScreen.kt', 'w') as f:
    f.write(content)
print("Done Queue")
