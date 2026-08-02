import re
with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

old_bar = """@Composable
fun SelectionBottomBar(
    onAddToPlaylist: () -> Unit,
    onAddToFavorites: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomBarActionItem(icon = Icons.Default.PlaylistAdd, label = "Add to\\nplaylist", onClick = onAddToPlaylist)
        BottomBarActionItem(icon = Icons.Default.FavoriteBorder, label = "Add to\\nfavorites", onClick = onAddToFavorites)
        BottomBarActionItem(icon = Icons.Default.Share, label = "Share", onClick = onShare)
        BottomBarActionItem(icon = Icons.Default.DeleteOutline, label = "Delete", onClick = onDelete)
    }
}

@Composable
fun BottomBarActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2
        )
    }
}"""

new_bar = """@Composable
fun SelectionBottomBar(
    onAddToPlaylist: () -> Unit,
    onAddToFavorites: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomBarActionItem(icon = Icons.AutoMirrored.Filled.PlaylistAdd, label = "PLAYLIST", onClick = onAddToPlaylist)
        BottomBarActionItem(icon = Icons.Default.FavoriteBorder, label = "FAVORITE", onClick = onAddToFavorites)
        BottomBarActionItem(icon = Icons.Default.Share, label = "SHARE", onClick = onShare)
        BottomBarActionItem(icon = Icons.Default.DeleteOutline, label = "DELETE", isDestructive = true, onClick = onDelete)
    }
}

@Composable
fun BottomBarActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    val color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            color = color,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1
        )
    }
}"""
content = content.replace(old_bar, new_bar)

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
