import re
with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

# Update SelectionBottomBar signature
content = content.replace("""@Composable
fun SelectionBottomBar(
    currentTab: String,
    onAddToPlaylist: () -> Unit,
    onRename: () -> Unit,
    onAddToFavorites: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {""", """@Composable
fun SelectionBottomBar(
    currentTab: String,
    onAddToPlaylist: () -> Unit,
    onRename: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayLater: () -> Unit,
    onAddToFavorites: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {""")

# Update SelectionBottomBar content
content = content.replace("""        if (currentTab == "Playlists") {
            BottomBarActionItem(icon = Icons.Default.Edit, label = "RENAME", onClick = onRename)
        } else {
            BottomBarActionItem(icon = Icons.Default.PlaylistAdd, label = "PLAYLIST", onClick = onAddToPlaylist)
        }
        BottomBarActionItem(icon = Icons.Default.FavoriteBorder, label = "FAVORITE", onClick = onAddToFavorites)
        BottomBarActionItem(icon = Icons.Default.Share, label = "SHARE", onClick = onShare)
        BottomBarActionItem(icon = Icons.Default.DeleteOutline, label = "DELETE", isDestructive = true, onClick = onDelete)""", """        if (currentTab == "Playlists") {
            BottomBarActionItem(icon = Icons.Default.Edit, label = "RENAME", onClick = onRename)
            BottomBarActionItem(icon = Icons.Default.FavoriteBorder, label = "FAVORITE", onClick = onAddToFavorites)
            BottomBarActionItem(icon = Icons.Default.Share, label = "SHARE", onClick = onShare)
            BottomBarActionItem(icon = Icons.Default.DeleteOutline, label = "DELETE", isDestructive = true, onClick = onDelete)
        } else if (currentTab == "Artists" || currentTab == "Albums") {
            BottomBarActionItem(icon = Icons.Default.PlayArrow, label = "PLAY NEXT", onClick = onPlayNext)
            BottomBarActionItem(icon = Icons.Default.Add, label = "PLAY LATER", onClick = onPlayLater)
            BottomBarActionItem(icon = Icons.Default.FavoriteBorder, label = "FAVORITE", onClick = onAddToFavorites)
            BottomBarActionItem(icon = Icons.Default.Share, label = "SHARE", onClick = onShare)
        } else {
            BottomBarActionItem(icon = Icons.Default.PlaylistAdd, label = "PLAYLIST", onClick = onAddToPlaylist)
            BottomBarActionItem(icon = Icons.Default.FavoriteBorder, label = "FAVORITE", onClick = onAddToFavorites)
            BottomBarActionItem(icon = Icons.Default.Share, label = "SHARE", onClick = onShare)
            BottomBarActionItem(icon = Icons.Default.DeleteOutline, label = "DELETE", isDestructive = true, onClick = onDelete)
        }""")

# Update LibraryScreenContent call to SelectionBottomBar
content = content.replace("""            SelectionBottomBar(
                currentTab = currentTab,
                onAddToPlaylist = { selectedItems.clear() },
                onRename = { renameTrigger++ },
                onAddToFavorites = { selectedItems.clear() },
                onShare = { selectedItems.clear() },
                onDelete = { deleteTrigger++ }
            )""", """            SelectionBottomBar(
                currentTab = currentTab,
                onAddToPlaylist = { selectedItems.clear() },
                onRename = { renameTrigger++ },
                onPlayNext = { selectedItems.clear() },
                onPlayLater = { selectedItems.clear() },
                onAddToFavorites = { selectedItems.clear() },
                onShare = { selectedItems.clear() },
                onDelete = { deleteTrigger++ }
            )""")

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
