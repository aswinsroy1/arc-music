import re

with open('app/src/main/java/com/example/AddToPlaylistSheet.kt', 'r') as f:
    content = f.read()

content = content.replace("containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,", "focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest")

with open('app/src/main/java/com/example/AddToPlaylistSheet.kt', 'w') as f:
    f.write(content)
