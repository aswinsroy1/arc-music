import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Update MusicHomeScreen
content = content.replace("fun MusicHomeScreen(", "fun MusicHomeScreen(\n    onNavigateToPlaylistDetails: () -> Unit = {},")
content = content.replace(
    "onNavigateToAlbumDetails = { navController.navigate(\"album_details\") }",
    "onNavigateToAlbumDetails = { navController.navigate(\"album_details\") },\n                                    onNavigateToPlaylistDetails = { navController.navigate(\"playlist_details\") }"
)

content = content.replace(
    "PlaylistsResultSection(modifier = Modifier.padding(horizontal = 24.dp))",
    "PlaylistsResultSection(modifier = Modifier.padding(horizontal = 24.dp), onNavigateToPlaylistDetails = onNavigateToPlaylistDetails)"
)

content = content.replace(
    "fun PlaylistsResultSection(modifier: Modifier = Modifier)",
    "fun PlaylistsResultSection(modifier: Modifier = Modifier, onNavigateToPlaylistDetails: () -> Unit = {})"
)

content = content.replace(
    "PlaylistResultItem(\"This Is Conan Gray\"",
    "PlaylistResultItem(onClick = onNavigateToPlaylistDetails, title = \"This Is Conan Gray\""
)
content = content.replace(
    "PlaylistResultItem(\"Conan Gray Complete\"",
    "PlaylistResultItem(onClick = onNavigateToPlaylistDetails, title = \"Conan Gray Complete\""
)

content = content.replace(
    "fun PlaylistResultItem(title: String, subtitle: String, imageUrl: String)",
    "fun PlaylistResultItem(title: String, subtitle: String, imageUrl: String, onClick: () -> Unit = {})"
)

content = content.replace(
    "modifier = Modifier\n            .fillMaxWidth()\n            .clickable { }",
    "modifier = Modifier\n            .fillMaxWidth()\n            .clickable { onClick() }"
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)


with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    lib_content = f.read()

lib_content = lib_content.replace(
    "fun LibraryScreenContent(modifier: Modifier = Modifier, bottomPadding: androidx.compose.ui.unit.Dp, onNavigateToAlbumDetails: () -> Unit = {})",
    "fun LibraryScreenContent(modifier: Modifier = Modifier, bottomPadding: androidx.compose.ui.unit.Dp, onNavigateToAlbumDetails: () -> Unit = {}, onNavigateToPlaylistDetails: () -> Unit = {})"
)

lib_content = lib_content.replace(
    "LibraryMainSection(tabName = currentTab, onNavigateToAlbumDetails = onNavigateToAlbumDetails)",
    "LibraryMainSection(tabName = currentTab, onNavigateToAlbumDetails = onNavigateToAlbumDetails, onNavigateToPlaylistDetails = onNavigateToPlaylistDetails)"
)

lib_content = lib_content.replace(
    "fun LibraryMainSection(tabName: String, onNavigateToAlbumDetails: () -> Unit = {})",
    "fun LibraryMainSection(tabName: String, onNavigateToAlbumDetails: () -> Unit = {}, onNavigateToPlaylistDetails: () -> Unit = {})"
)

# wait I need to pass onNavigateToPlaylistDetails into PlaylistsResultSection? No, library uses LibraryPlaylistsSection if it existed...
# Let's check LibraryMainSection

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(lib_content)

