import re
with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_playlist = """        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
        )"""

new_playlist = """        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
                    .border(1.5.dp, if (isSelected) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.onSurfaceVariant, CircleShape),
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
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
        )"""

content = content.replace(old_playlist, new_playlist)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
