with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

old_code = """
            if (tabName == "Playlists") {
                val playlists = listOf(
                    Track("This Is Conan Gray", "50 songs", "https://lh3.googleusercontent.com/aida-public/AB6AXuAN2IRvdhR_bkbDMJ8TqVqG2y4rQ3L_f1eRHAi_gAsteuGmZIEL0FgQOJ7eagpNOd8RyZJEar704LgA2EaC_eTYUgCmOiMzxQpaVHQJ8M6flPFUIJ71YUtKiIKyo6UAJW1DAoztixz1AbUm17RLgT5jOGGPPEHgGrkOwf0WxcPSFfkL_vvYISYxu70oxAu8dCHUOY7vYG371MR9sBrRRiEMLJ30KgLLrV0hg9_zUe8qs294_IXoKmQpLvb3X8pgvDIEYaT6kFzuQnqG"),
                    Track("Conan Gray Complete", "92 songs", "https://lh3.googleusercontent.com/aida-public/AB6AXuBoqgZeT99UHwbjmcDIfQ71twNukb3Tzd6G0dsOGEVmLDnD0HXbeipJGkmNY9O9jEZmz2oFg0vvlWWcin0dGwUVfoPwskoe8c4oYobJajOAkkxzb37yRJyhMINPt8VjjLF_6LNVXoE0lMjq4rRHPngOSRTiUsB4XfaYdshllx1R9y55Lfv6Texk356Nj71-a8U5r8eU33_lhmgL7cxyPCDo13d9oHJOiqTN2K8-aKMogfnmKcgzCNBO_-ctALvKFyota4_U73THmnSM")
                )
                for (i in playlists.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AlbumResultItem(title = playlists[i].title, year = playlists[i].artist, imageUrl = playlists[i].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToPlaylistDetails)
                        if (i + 1 < playlists.size) {
                            AlbumResultItem(title = playlists[i + 1].title, year = playlists[i + 1].artist, imageUrl = playlists[i + 1].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToPlaylistDetails)
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else if (tabName == "Albums") {"""

new_code = """
            if (tabName == "Playlists") {
                val playlists = listOf(
                    Track("This Is Conan Gray", "50 songs", "https://lh3.googleusercontent.com/aida-public/AB6AXuAN2IRvdhR_bkbDMJ8TqVqG2y4rQ3L_f1eRHAi_gAsteuGmZIEL0FgQOJ7eagpNOd8RyZJEar704LgA2EaC_eTYUgCmOiMzxQpaVHQJ8M6flPFUIJ71YUtKiIKyo6UAJW1DAoztixz1AbUm17RLgT5jOGGPPEHgGrkOwf0WxcPSFfkL_vvYISYxu70oxAu8dCHUOY7vYG371MR9sBrRRiEMLJ30KgLLrV0hg9_zUe8qs294_IXoKmQpLvb3X8pgvDIEYaT6kFzuQnqG"),
                    Track("Conan Gray Complete", "92 songs", "https://lh3.googleusercontent.com/aida-public/AB6AXuBoqgZeT99UHwbjmcDIfQ71twNukb3Tzd6G0dsOGEVmLDnD0HXbeipJGkmNY9O9jEZmz2oFg0vvlWWcin0dGwUVfoPwskoe8c4oYobJajOAkkxzb37yRJyhMINPt8VjjLF_6LNVXoE0lMjq4rRHPngOSRTiUsB4XfaYdshllx1R9y55Lfv6Texk356Nj71-a8U5r8eU33_lhmgL7cxyPCDo13d9oHJOiqTN2K8-aKMogfnmKcgzCNBO_-ctALvKFyota4_U73THmnSM")
                )
                if (isGridView) {
                    for (i in playlists.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AlbumResultItem(title = playlists[i].title, year = playlists[i].artist, imageUrl = playlists[i].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToPlaylistDetails)
                            if (i + 1 < playlists.size) {
                                AlbumResultItem(title = playlists[i + 1].title, year = playlists[i + 1].artist, imageUrl = playlists[i + 1].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToPlaylistDetails)
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    for (p in playlists) {
                        PlaylistResultItem(
                            title = p.title, 
                            subtitle = p.artist, 
                            imageUrl = p.imageUrl, 
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onNavigateToPlaylistDetails
                        )
                    }
                }
            } else if (tabName == "Albums") {"""

content = content.replace(old_code.strip(), new_code.strip())

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
