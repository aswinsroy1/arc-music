import re
with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

old_track = """        if (isSelectionMode) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), horizontalArrangement = Arrangement.Start) {
                Checkbox(checked = isSelected, onCheckedChange = null, modifier = Modifier.size(24.dp))
            }
        }
        AsyncImage(
            model = track.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(28.dp))
        )"""

new_track = """        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            AsyncImage(
                model = track.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
            )
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f))
                        .border(1.5.dp, if (isSelected) androidx.compose.ui.graphics.Color.Transparent else androidx.compose.ui.graphics.Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }"""
content = content.replace(old_track, new_track)

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
