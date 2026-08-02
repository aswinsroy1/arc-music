with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

# Make playlists empty by default
old_playlists = """    val playlists = remember { mutableStateListOf(
        Track("This Is Conan Gray", "50 songs", "https://lh3.googleusercontent.com/aida-public/AB6AXuAN2IRvdhR_bkbDMJ8TqVqG2y4rQ3L_f1eRHAi_gAsteuGmZIEL0FgQOJ7eagpNOd8RyZJEar704LgA2EaC_eTYUgCmOiMzxQpaVHQJ8M6flPFUIJ71YUtKiIKyo6UAJW1DAoztixz1AbUm17RLgT5jOGGPPEHgGrkOwf0WxcPSFfkL_vvYISYxu70oxAu8dCHUOY7vYG371MR9sBrRRiEMLJ30KgLLrV0hg9_zUe8qs294_IXoKmQpLvb3X8pgvDIEYaT6kFzuQnqG"),
        Track("Conan Gray Complete", "92 songs", "https://lh3.googleusercontent.com/aida-public/AB6AXuBoqgZeT99UHwbjmcDIfQ71twNukb3Tzd6G0dsOGEVmLDnD0HXbeipJGkmNY9O9jEZmz2oFg0vvlWWcin0dGwUVfoPwskoe8c4oYobJajOAkkxzb37yRJyhMINPt8VjjLF_6LNVXoE0lMjq4rRHPngOSRTiUsB4XfaYdshllx1R9y55Lfv6Texk356Nj71-a8U5r8eU33_lhmgL7cxyPCDo13d9oHJOiqTN2K8-aKMogfnmKcgzCNBO_-ctALvKFyota4_U73THmnSM")
    ) }"""
new_playlists = """    val playlists = remember { mutableStateListOf<Track>() }"""
content = content.replace(old_playlists, new_playlists)

old_playlists_render = """            if (tabName == "Playlists") {

                val sortedPlaylists = if (sortOption == "Name") {"""

new_playlists_render = """            if (tabName == "Playlists") {
                if (playlists.isEmpty()) {
                    PlaylistsEmptyState(modifier = Modifier.fillMaxWidth())
                } else {
                val sortedPlaylists = if (sortOption == "Name") {"""
content = content.replace(old_playlists_render, new_playlists_render)

old_playlists_end = """                        }
                    }
                }
            } else if (tabName == "Albums") {"""

new_playlists_end = """                        }
                    }
                }
                }
            } else if (tabName == "Albums") {"""
content = content.replace(old_playlists_end, new_playlists_end)


playlists_empty_state = """
@Composable
fun PlaylistsEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = "No playlists",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "No playlists yet",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Create your first playlist to get started\\norganizing your favorite tracks.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        AppPrimaryButton(
            text = "Create Playlist",
            icon = Icons.Default.Add,
            onClick = { /* TODO: Create Playlist */ },
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
            modifier = Modifier.width(220.dp)
        )
    }
}
"""

content = content + playlists_empty_state

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
