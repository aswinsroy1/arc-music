import re

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'r') as f:
    content = f.read()

# Replace containerColor
content = content.replace(
    "containerColor = if (isDarkTheme) Color(0xFF181C22) else Color.White,",
    "containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),"
)

# Replace dragHandle background
content = content.replace(
    "background(if (isDarkTheme) Color.DarkGray else Color.LightGray)",
    "background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))"
)

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
    f.write(content)
print("Done")
