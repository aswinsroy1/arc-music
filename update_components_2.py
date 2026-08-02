with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

old_track = """fun TrackGridItem(track: Track, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clickable { },
"""
new_track = """@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun TrackGridItem(track: Track, modifier: Modifier = Modifier, isSelectionMode: Boolean = false, isSelected: Boolean = false, onLongClick: (() -> Unit)? = null, onClick: () -> Unit = {}) {
    Column(
        modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
"""
content = content.replace(old_track, new_track)

old_track_row = """
    ) {
        AsyncImage(
"""
new_track_row = """
    ) {
        if (isSelectionMode) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), horizontalArrangement = Arrangement.Start) {
                Checkbox(checked = isSelected, onCheckedChange = null, modifier = Modifier.size(24.dp))
            }
        }
        AsyncImage(
"""
content = content.replace(old_track_row, new_track_row)


with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
