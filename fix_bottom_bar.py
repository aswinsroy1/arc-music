import re
with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

# Update SelectionBottomBar signature
content = content.replace("""@Composable
fun SelectionBottomBar(
    onAddToPlaylist: () -> Unit,
    onAddToFavorites: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {""", """@Composable
fun SelectionBottomBar(
    currentTab: String,
    onAddToPlaylist: () -> Unit,
    onRename: () -> Unit,
    onAddToFavorites: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {""")

# Update SelectionBottomBar content
content = content.replace("""        BottomBarActionItem(icon = Icons.Default.PlaylistAdd, label = "PLAYLIST", onClick = onAddToPlaylist)
        BottomBarActionItem(icon = Icons.Default.FavoriteBorder, label = "FAVORITE", onClick = onAddToFavorites)
        BottomBarActionItem(icon = Icons.Default.Share, label = "SHARE", onClick = onShare)
        BottomBarActionItem(icon = Icons.Default.DeleteOutline, label = "DELETE", isDestructive = true, onClick = onDelete)""", """        if (currentTab == "Playlists") {
            BottomBarActionItem(icon = Icons.Default.Edit, label = "RENAME", onClick = onRename)
        } else {
            BottomBarActionItem(icon = Icons.AutoMirrored.Filled.PlaylistAdd, label = "PLAYLIST", onClick = onAddToPlaylist)
        }
        BottomBarActionItem(icon = Icons.Default.FavoriteBorder, label = "FAVORITE", onClick = onAddToFavorites)
        BottomBarActionItem(icon = Icons.Default.Share, label = "SHARE", onClick = onShare)
        BottomBarActionItem(icon = Icons.Default.DeleteOutline, label = "DELETE", isDestructive = true, onClick = onDelete)""")


# Update LibraryScreenContent call to SelectionBottomBar
content = content.replace("""        Box(modifier = Modifier.fillMaxSize().padding(bottom = 24.dp), contentAlignment = Alignment.BottomCenter) {
            SelectionBottomBar(
                onAddToPlaylist = { selectedItems.clear() },
                onAddToFavorites = { selectedItems.clear() },
                onShare = { selectedItems.clear() },
                onDelete = { deleteTrigger++ }
            )
        }""", """        Box(modifier = Modifier.fillMaxSize().padding(bottom = 24.dp), contentAlignment = Alignment.BottomCenter) {
            val currentTab = tabs.getOrNull(pagerState.currentPage) ?: ""
            SelectionBottomBar(
                currentTab = currentTab,
                onAddToPlaylist = { selectedItems.clear() },
                onRename = { selectedItems.clear() },
                onAddToFavorites = { selectedItems.clear() },
                onShare = { selectedItems.clear() },
                onDelete = { deleteTrigger++ }
            )
        }""")

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
