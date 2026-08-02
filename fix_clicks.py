import re

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'r') as f:
    content = f.read()

# Make the options close the bottom sheet
content = content.replace(".clickable { }", ".clickable { showOptionsSheet = false }")

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
    f.write(content)
print("Done")
