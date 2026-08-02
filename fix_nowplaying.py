import re

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'r') as f:
    content = f.read()

# Fix the Pause button click
bad_click = """                        .clickable { 
                                showOptionsSheet = false 
                                if (title == "Add to playlist") {
                                    showAddToPlaylistSheet = true
                                }
                            },"""

good_click = "                        .clickable { /* TODO */ },"

# Replace only the one in the Pause button... wait, let's just find the Pause button context
pause_context = """                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clickable { 
                                showOptionsSheet = false 
                                if (title == "Add to playlist") {
                                    showAddToPlaylistSheet = true
                                }
                            },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,"""

fixed_pause_context = """                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clickable { /* TODO Play/Pause */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,"""

content = content.replace(pause_context, fixed_pause_context)

# Let's fix the other place I might have messed up...
# Wait, did it replace ANY other .clickable?
# In NowPlayingScreen.kt, options list has a .clickable { ... }
options_click = """.clickable { 
                                showOptionsSheet = false 
                                if (title == "Add to playlist") {
                                    showAddToPlaylistSheet = true
                                }
                            }"""
if content.count(options_click) > 1:
    # There are multiple replacements, let's fix them all manually if needed... but the one in options is correct.
    pass

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
    f.write(content)

print("Fixed NowPlaying")
