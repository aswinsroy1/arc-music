with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_search = "fun SearchScreenContent(modifier: Modifier = Modifier, bottomPadding: androidx.compose.ui.unit.Dp, onNavigateToAlbumDetails: () -> Unit = {})"
new_search = "fun SearchScreenContent(modifier: Modifier = Modifier, bottomPadding: androidx.compose.ui.unit.Dp, onNavigateToAlbumDetails: () -> Unit = {}, onNavigateToPlaylistDetails: () -> Unit = {})"

content = content.replace(old_search, new_search)

old_call = "SearchScreenContent(modifier = Modifier.fillMaxSize(), bottomPadding = bottomPadding, onNavigateToAlbumDetails = onNavigateToAlbumDetails)"
new_call = "SearchScreenContent(modifier = Modifier.fillMaxSize(), bottomPadding = bottomPadding, onNavigateToAlbumDetails = onNavigateToAlbumDetails, onNavigateToPlaylistDetails = onNavigateToPlaylistDetails)"

content = content.replace(old_call, new_call)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
