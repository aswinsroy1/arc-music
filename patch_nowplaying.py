import re

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'r') as f:
    content = f.read()

# Add showAddToPlaylistSheet
if "var showAddToPlaylistSheet" not in content:
    state_decl = "    var showOptionsSheet by remember { mutableStateOf(false) }"
    new_state_decl = state_decl + "\n    var showAddToPlaylistSheet by remember { mutableStateOf(false) }"
    content = content.replace(state_decl, new_state_decl)

# Update clickable in options list
old_click = ".clickable { showOptionsSheet = false }"
new_click = """.clickable { 
                                showOptionsSheet = false 
                                if (title == "Add to playlist") {
                                    showAddToPlaylistSheet = true
                                }
                            }"""
content = content.replace(old_click, new_click)

# Insert AddToPlaylistSheet at the end
if "AddToPlaylistSheet(" not in content:
    add_to_playlist_call = """
    if (showAddToPlaylistSheet) {
        AddToPlaylistSheet(onDismissRequest = { showAddToPlaylistSheet = false })
    }
"""
    last_brace = content.rfind("}")
    if last_brace != -1:
        # Find the last brace of the composable, maybe not the very end. Let's insert just before the end of NowPlayingScreen
        # NowPlayingScreen ends where?
        now_playing_end = content.rfind("}\n\n@Composable\nfun WavySlider")
        if now_playing_end != -1:
            content = content[:now_playing_end] + add_to_playlist_call + content[now_playing_end:]
        else:
            now_playing_end = content.rfind("}\n\n@Composable")
            if now_playing_end != -1:
                content = content[:now_playing_end] + add_to_playlist_call + content[now_playing_end:]
            else:
                content = content[:last_brace] + add_to_playlist_call + content[last_brace:]

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
    f.write(content)
print("Done NowPlaying")
