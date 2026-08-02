with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

# Replace Playlist items
content = content.replace(
    'AlbumResultItem(title = sortedPlaylists[i].title, year = sortedPlaylists[i].artist, imageUrl = sortedPlaylists[i].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToPlaylistDetails)',
    'AlbumResultItem(title = sortedPlaylists[i].title, year = sortedPlaylists[i].artist, imageUrl = sortedPlaylists[i].imageUrl, modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains(sortedPlaylists[i].title), onLongClick = { onToggleSelection(sortedPlaylists[i].title) }, onClick = { if (isSelectionMode) onToggleSelection(sortedPlaylists[i].title) else onNavigateToPlaylistDetails() })'
)
content = content.replace(
    'AlbumResultItem(title = sortedPlaylists[i + 1].title, year = sortedPlaylists[i + 1].artist, imageUrl = sortedPlaylists[i + 1].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToPlaylistDetails)',
    'AlbumResultItem(title = sortedPlaylists[i + 1].title, year = sortedPlaylists[i + 1].artist, imageUrl = sortedPlaylists[i + 1].imageUrl, modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains(sortedPlaylists[i+1].title), onLongClick = { onToggleSelection(sortedPlaylists[i+1].title) }, onClick = { if (isSelectionMode) onToggleSelection(sortedPlaylists[i+1].title) else onNavigateToPlaylistDetails() })'
)
content = content.replace(
    'PlaylistResultItem(title = p.title, subtitle = p.artist, imageUrl = p.imageUrl, modifier = Modifier.fillMaxWidth(), onClick = onNavigateToPlaylistDetails)',
    'PlaylistResultItem(title = p.title, subtitle = p.artist, imageUrl = p.imageUrl, modifier = Modifier.fillMaxWidth(), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains(p.title), onLongClick = { onToggleSelection(p.title) }, onClick = { if (isSelectionMode) onToggleSelection(p.title) else onNavigateToPlaylistDetails() })'
)

# Replace Album items
content = content.replace(
    'AlbumResultItem(title = sortedAlbums[i].title, year = sortedAlbums[i].artist, imageUrl = sortedAlbums[i].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToAlbumDetails)',
    'AlbumResultItem(title = sortedAlbums[i].title, year = sortedAlbums[i].artist, imageUrl = sortedAlbums[i].imageUrl, modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains(sortedAlbums[i].title), onLongClick = { onToggleSelection(sortedAlbums[i].title) }, onClick = { if (isSelectionMode) onToggleSelection(sortedAlbums[i].title) else onNavigateToAlbumDetails() })'
)
content = content.replace(
    'AlbumResultItem(title = sortedAlbums[i + 1].title, year = sortedAlbums[i + 1].artist, imageUrl = sortedAlbums[i + 1].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToAlbumDetails)',
    'AlbumResultItem(title = sortedAlbums[i + 1].title, year = sortedAlbums[i + 1].artist, imageUrl = sortedAlbums[i + 1].imageUrl, modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains(sortedAlbums[i+1].title), onLongClick = { onToggleSelection(sortedAlbums[i+1].title) }, onClick = { if (isSelectionMode) onToggleSelection(sortedAlbums[i+1].title) else onNavigateToAlbumDetails() })'
)
content = content.replace(
    'PlaylistResultItem(title = a.title, subtitle = a.artist, imageUrl = a.imageUrl, modifier = Modifier.fillMaxWidth(), onClick = onNavigateToAlbumDetails)',
    'PlaylistResultItem(title = a.title, subtitle = a.artist, imageUrl = a.imageUrl, modifier = Modifier.fillMaxWidth(), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains(a.title), onLongClick = { onToggleSelection(a.title) }, onClick = { if (isSelectionMode) onToggleSelection(a.title) else onNavigateToAlbumDetails() })'
)

# Replace Artist items
content = content.replace(
    'ArtistResultItem(name = sortedArtists[i].title, imageUrl = sortedArtists[i].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToArtistDetails)',
    'ArtistResultItem(name = sortedArtists[i].title, imageUrl = sortedArtists[i].imageUrl, modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains(sortedArtists[i].title), onLongClick = { onToggleSelection(sortedArtists[i].title) }, onClick = { if (isSelectionMode) onToggleSelection(sortedArtists[i].title) else onNavigateToArtistDetails() })'
)
content = content.replace(
    'ArtistResultItem(name = sortedArtists[i + 1].title, imageUrl = sortedArtists[i + 1].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToArtistDetails)',
    'ArtistResultItem(name = sortedArtists[i + 1].title, imageUrl = sortedArtists[i + 1].imageUrl, modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains(sortedArtists[i+1].title), onLongClick = { onToggleSelection(sortedArtists[i+1].title) }, onClick = { if (isSelectionMode) onToggleSelection(sortedArtists[i+1].title) else onNavigateToArtistDetails() })'
)
content = content.replace(
    'ArtistResultItem(name = sortedArtists[i + 2].title, imageUrl = sortedArtists[i + 2].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToArtistDetails)',
    'ArtistResultItem(name = sortedArtists[i + 2].title, imageUrl = sortedArtists[i + 2].imageUrl, modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains(sortedArtists[i+2].title), onLongClick = { onToggleSelection(sortedArtists[i+2].title) }, onClick = { if (isSelectionMode) onToggleSelection(sortedArtists[i+2].title) else onNavigateToArtistDetails() })'
)
content = content.replace(
    'PlaylistResultItem(title = a.title, subtitle = a.artist, imageUrl = a.imageUrl, modifier = Modifier.fillMaxWidth(), onClick = onNavigateToArtistDetails)',
    'PlaylistResultItem(title = a.title, subtitle = a.artist, imageUrl = a.imageUrl, modifier = Modifier.fillMaxWidth(), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains(a.title), onLongClick = { onToggleSelection(a.title) }, onClick = { if (isSelectionMode) onToggleSelection(a.title) else onNavigateToArtistDetails() })'
)

# Replace Track items
content = content.replace(
    'TrackGridItem(track = sortedTracks[i], modifier = Modifier.weight(1f))',
    'TrackGridItem(track = sortedTracks[i], modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains(sortedTracks[i].title), onLongClick = { onToggleSelection(sortedTracks[i].title) }, onClick = { if (isSelectionMode) onToggleSelection(sortedTracks[i].title) })'
)
content = content.replace(
    'TrackGridItem(track = sortedTracks[i + 1], modifier = Modifier.weight(1f))',
    'TrackGridItem(track = sortedTracks[i + 1], modifier = Modifier.weight(1f), isSelectionMode = isSelectionMode, isSelected = selectedItems.contains(sortedTracks[i+1].title), onLongClick = { onToggleSelection(sortedTracks[i+1].title) }, onClick = { if (isSelectionMode) onToggleSelection(sortedTracks[i+1].title) })'
)

old_song_result = """
                        SongResultItem(
                            title = t.title, 
                            artist = t.artist, 
                            duration = "3:30", 
                            imageUrl = t.imageUrl
                        )
"""
new_song_result = """
                        SongResultItem(
                            title = t.title, 
                            artist = t.artist, 
                            duration = "3:30", 
                            imageUrl = t.imageUrl,
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedItems.contains(t.title),
                            onLongClick = { onToggleSelection(t.title) },
                            onClick = { if (isSelectionMode) onToggleSelection(t.title) }
                        )
"""
content = content.replace(old_song_result, new_song_result)


with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
