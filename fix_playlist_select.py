import re
with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

old_playlist_1 = """                    for (p in sortedPlaylists) {
                        PlaylistResultItem(
                            title = p.title, 
                            subtitle = p.artist, 
                            imageUrl = p.imageUrl, 
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onNavigateToPlaylistDetails
                        )
                    }"""

new_playlist_1 = """                    for (p in sortedPlaylists) {
                        PlaylistResultItem(
                            title = p.title, 
                            subtitle = p.artist, 
                            imageUrl = p.imageUrl, 
                            modifier = Modifier.fillMaxWidth(),
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedItems.contains(p.title),
                            onLongClick = { onToggleSelection(p.title) },
                            onClick = { if (isSelectionMode) onToggleSelection(p.title) else onNavigateToPlaylistDetails() }
                        )
                    }"""
content = content.replace(old_playlist_1, new_playlist_1)

old_playlist_2 = """                    for (a in sortedAlbums) {
                        PlaylistResultItem(
                            title = a.title, 
                            subtitle = a.artist, 
                            imageUrl = a.imageUrl, 
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onNavigateToAlbumDetails
                        )
                    }"""

new_playlist_2 = """                    for (a in sortedAlbums) {
                        PlaylistResultItem(
                            title = a.title, 
                            subtitle = a.artist, 
                            imageUrl = a.imageUrl, 
                            modifier = Modifier.fillMaxWidth(),
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedItems.contains(a.title),
                            onLongClick = { onToggleSelection(a.title) },
                            onClick = { if (isSelectionMode) onToggleSelection(a.title) else onNavigateToAlbumDetails() }
                        )
                    }"""
content = content.replace(old_playlist_2, new_playlist_2)

old_playlist_3 = """                    for (a in sortedArtists) {
                        PlaylistResultItem(
                            title = a.title, 
                            subtitle = "Artist", 
                            imageUrl = a.imageUrl, 
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onNavigateToArtistDetails
                        )
                    }"""

new_playlist_3 = """                    for (a in sortedArtists) {
                        PlaylistResultItem(
                            title = a.title, 
                            subtitle = "Artist", 
                            imageUrl = a.imageUrl, 
                            modifier = Modifier.fillMaxWidth(),
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedItems.contains(a.title),
                            onLongClick = { onToggleSelection(a.title) },
                            onClick = { if (isSelectionMode) onToggleSelection(a.title) else onNavigateToArtistDetails() }
                        )
                    }"""
content = content.replace(old_playlist_3, new_playlist_3)

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
