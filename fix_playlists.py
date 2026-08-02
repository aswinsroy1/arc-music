with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

old_code = """                for (p in playlists) {
                    PlaylistResultItem(title = p.title, subtitle = p.artist, imageUrl = p.imageUrl, onClick = onNavigateToPlaylistDetails)
                }"""

new_code = """                for (i in playlists.indices step 2) {
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
                }"""

if old_code in content:
    content = content.replace(old_code, new_code)
    with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
        f.write(content)
    print("Replaced!")
else:
    print("Not found!")
