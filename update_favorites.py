with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

old_favorites = """@Composable
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
            items(filters) { filter ->
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
}"""

new_favorites = """@Composable
fun FavoritesSection(
    modifier: Modifier = Modifier,
    onNavigateToAlbumDetails: () -> Unit = {},
    onNavigateToArtistDetails: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
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
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "No favorites",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "No favorites yet",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Songs and albums you favorite will\\nappear here.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        AppPrimaryButton(
            text = "Explore Music",
            onClick = { /* TODO: Navigate to Home or Search */ },
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
            modifier = Modifier.width(200.dp)
        )
    }
}"""

content = content.replace(old_favorites, new_favorites)

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
