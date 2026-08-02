import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Update PlaylistResultItem
old_playlist = """fun PlaylistResultItem(title: String, subtitle: String, imageUrl: String, modifier: Modifier = Modifier.width(280.dp), onClick: () -> Unit = {}) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(12.dp),
"""
new_playlist = """@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun PlaylistResultItem(title: String, subtitle: String, imageUrl: String, modifier: Modifier = Modifier.width(280.dp), isSelectionMode: Boolean = false, isSelected: Boolean = false, onLongClick: (() -> Unit)? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(12.dp),
"""
content = content.replace(old_playlist, new_playlist)

# Inside PlaylistResultItem row
old_playlist_row = """
        AsyncImage(
            model = imageUrl,
"""
new_playlist_row = """
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
        AsyncImage(
            model = imageUrl,
"""
content = content.replace(old_playlist_row, new_playlist_row)

# Update SongResultItem
old_song = """fun SongResultItem(title: String, artist: String, duration: String, imageUrl: String, isActive: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (isActive) MaterialTheme.colorScheme.surfaceContainerLow else Color.Transparent)
            .clickable { }
            .padding(12.dp),
"""
new_song = """@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun SongResultItem(title: String, artist: String, duration: String, imageUrl: String, isActive: Boolean = false, isSelectionMode: Boolean = false, isSelected: Boolean = false, onLongClick: (() -> Unit)? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (isActive) MaterialTheme.colorScheme.surfaceContainerLow else Color.Transparent)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(12.dp),
"""
content = content.replace(old_song, new_song)

old_song_row = """
        Box(modifier = Modifier.width(24.dp), contentAlignment = Alignment.Center) {
"""
new_song_row = """
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Box(modifier = Modifier.width(24.dp), contentAlignment = Alignment.Center) {
"""
content = content.replace(old_song_row, new_song_row)


# Update AlbumResultItem
old_album = """fun AlbumResultItem(title: String, year: String, imageUrl: String, modifier: Modifier = Modifier.width(140.dp), onClick: () -> Unit = {}) {
    Column(
        modifier = modifier.clickable { onClick() }
    ) {
"""
new_album = """@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun AlbumResultItem(title: String, year: String, imageUrl: String, modifier: Modifier = Modifier.width(140.dp), isSelectionMode: Boolean = false, isSelected: Boolean = false, onLongClick: (() -> Unit)? = null, onClick: () -> Unit = {}) {
    Column(
        modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        if (isSelectionMode) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), horizontalArrangement = Arrangement.Start) {
                Checkbox(checked = isSelected, onCheckedChange = null, modifier = Modifier.size(24.dp))
            }
        }
"""
content = content.replace(old_album, new_album)


# Update ArtistResultItem
old_artist = """fun ArtistResultItem(name: String, imageUrl: String, isVerified: Boolean = false, modifier: Modifier = Modifier.width(100.dp), onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable { onClick() }
    ) {
"""
new_artist = """@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun ArtistResultItem(name: String, imageUrl: String, isVerified: Boolean = false, modifier: Modifier = Modifier.width(100.dp), isSelectionMode: Boolean = false, isSelected: Boolean = false, onLongClick: (() -> Unit)? = null, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        if (isSelectionMode) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), horizontalArrangement = Arrangement.Start) {
                Checkbox(checked = isSelected, onCheckedChange = null, modifier = Modifier.size(24.dp))
            }
        }
"""
content = content.replace(old_artist, new_artist)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

