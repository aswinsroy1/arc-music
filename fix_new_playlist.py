import re

with open('app/src/main/java/com/example/AddToPlaylistSheet.kt', 'r') as f:
    content = f.read()

# Add dialog state and make list mutable
if "var showNewPlaylistDialog" not in content:
    old_list = """    val samplePlaylists = listOf("""
    new_list = """    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    val samplePlaylists = remember { mutableStateListOf(
"""
    content = content.replace(old_list, new_list)
    # Also we need to replace the closing parens of the list if we change listOf to mutableStateListOf
    # Wait, mutableStateListOf takes varargs, same as listOf, so replacing `listOf(` with `remember { mutableStateListOf(` and adding `) }` at the end
    
    # Let's do it more safely
    content = content.replace("    val samplePlaylists = listOf(", "    var showNewPlaylistDialog by remember { mutableStateOf(false) }\n    val samplePlaylists = remember { mutableStateListOf(")
    
    # Replace the matching closing parenthesis of listOf
    content = content.replace("""    )

    ModalBottomSheet(""", """    ) }

    ModalBottomSheet(""")

    # Update new playlist click
    old_new_playlist_click = "modifier = Modifier\n                    .fillMaxWidth()\n                    .clickable { onDismissRequest() }\n                    .padding(horizontal = 24.dp, vertical = 12.dp)"
    new_new_playlist_click = "modifier = Modifier\n                    .fillMaxWidth()\n                    .clickable { showNewPlaylistDialog = true }\n                    .padding(horizontal = 24.dp, vertical = 12.dp)"
    content = content.replace(old_new_playlist_click, new_new_playlist_click)
    
    # Add dialog at the end
    dialog_code = """
    if (showNewPlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewPlaylistDialog = false },
            title = { Text("New playlist") },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Playlist name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (playlistName.isNotBlank()) {
                            samplePlaylists.add(
                                0,
                                PlaylistSimple(
                                    title = playlistName,
                                    songCount = 0,
                                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCZvDOXXQCEagIawLuXpQo8ALONnM_oGDtzdbsFk7qzwhXh_vaFl3y3qtoPRBdPPapo7KiajAeeyhqy7M2t501KLT12aFp0EvwtgVI8Kd_Cv00WllNY3y7kDS8jZ0uu0GFm8XJsxxl_s3dj7MUJ85qWCcHVYpkU4JHuDBj-AdEWgGbojbOP-vwFc9l2qF7VR3Q1DwGs3s7vdO0ppEZ8ldzJsIvGUXFq59VS4ijH8O9na5-lC2l0EYckjztTNqT5B6X6ucT5r8T5h-if",
                                    isSelected = true
                                )
                            )
                        }
                        showNewPlaylistDialog = false
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewPlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
"""
    # Insert before the last brace
    last_brace_idx = content.rfind("}")
    content = content[:last_brace_idx] + dialog_code + content[last_brace_idx:]
    
    with open('app/src/main/java/com/example/AddToPlaylistSheet.kt', 'w') as f:
        f.write(content)
print("Done")
