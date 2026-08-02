import re

with open('app/src/main/java/com/example/QueueScreen.kt', 'r') as f:
    content = f.read()

# I need to add var showAddToPlaylistSheet by remember { mutableStateOf(false) } to QueueScreen
# Let's insert it after `val laterInQueue = remember { mutableStateListOf(`
if "var showAddToPlaylistSheet" not in content:
    old_code = "val laterInQueue = remember { mutableStateListOf("
    new_code = "var showAddToPlaylistSheet by remember { mutableStateOf(false) }\n    val laterInQueue = remember { mutableStateListOf("
    content = content.replace(old_code, new_code)
    with open('app/src/main/java/com/example/QueueScreen.kt', 'w') as f:
        f.write(content)
print("Done")
