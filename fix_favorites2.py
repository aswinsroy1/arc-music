with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

old_tracks = """                    }
                }
            } else {
                // Grid of tracks
                val tracks = listOf("""

new_tracks = """                    }
                }
            } else if (tabName == "Favorites") {
                FavoritesSection(
                    onNavigateToAlbumDetails = onNavigateToAlbumDetails,
                    onNavigateToArtistDetails = onNavigateToArtistDetails
                )
            } else {
                // Grid of tracks
                val tracks = listOf("""

content = content.replace(old_tracks, new_tracks)

favorites_composable = """
@Composable
fun FavoritesSection(
    modifier: Modifier = Modifier,
    onNavigateToAlbumDetails: () -> Unit = {},
    onNavigateToArtistDetails: () -> Unit = {}
) {
    var selectedFilter by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("All") }
    val filters = listOf("All", "Songs", "Albums", "Artists")
    
    androidx.compose.foundation.layout.Column(modifier = modifier, verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(32.dp)) {
        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.foundation.lazy.items(filters) { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = filter,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        if (selectedFilter == "All" || selectedFilter == "Songs") {
            SongsResultSection(modifier = Modifier.fillMaxWidth())
        }
        if (selectedFilter == "All" || selectedFilter == "Albums") {
            AlbumsResultSection(modifier = Modifier.fillMaxWidth(), onNavigateToAlbumDetails = onNavigateToAlbumDetails)
        }
        if (selectedFilter == "All" || selectedFilter == "Artists") {
            ArtistsResultSection(modifier = Modifier.fillMaxWidth(), onNavigateToArtistDetails = onNavigateToArtistDetails)
        }
    }
}
"""

content = content + favorites_composable

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
