with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

old_sig = "fun LibraryMainSection(modifier: Modifier = Modifier, tabName: String = \"\", onNavigateToAlbumDetails: () -> Unit = {})"
new_sig = "fun LibraryMainSection(modifier: Modifier = Modifier, tabName: String = \"\", onNavigateToAlbumDetails: () -> Unit = {}, onNavigateToPlaylistDetails: () -> Unit = {})"
content = content.replace(old_sig, new_sig)

old_call = "LibraryMainSection(tabName = currentTab, onNavigateToAlbumDetails = onNavigateToAlbumDetails)"
new_call = "LibraryMainSection(tabName = currentTab, onNavigateToAlbumDetails = onNavigateToAlbumDetails, onNavigateToPlaylistDetails = onNavigateToPlaylistDetails)"
content = content.replace(old_call, new_call)

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
