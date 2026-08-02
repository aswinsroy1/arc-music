import re

with open('app/src/main/java/com/example/AddToPlaylistSheet.kt', 'r') as f:
    content = f.read()

new_dialog = """    if (showNewPlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        androidx.compose.ui.window.Dialog(onDismissRequest = { showNewPlaylistDialog = false }) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "New playlist",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        placeholder = { Text("Playlist name") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showNewPlaylistDialog = false },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
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
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Text("Create", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }"""

old_dialog_start = "    if (showNewPlaylistDialog) {"
old_dialog_end = "    }\n}"

start_idx = content.find(old_dialog_start)
end_idx = content.find(old_dialog_end, start_idx) + 5

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + new_dialog + "\n}"
    
    with open('app/src/main/java/com/example/AddToPlaylistSheet.kt', 'w') as f:
        f.write(content)
    print("Replaced dialog")
else:
    print("Could not find dialog to replace")
