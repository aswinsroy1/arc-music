import re

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'r') as f:
    content = f.read()

# 1. Add state variable
state_decl = "    var showLyrics by remember { mutableStateOf(false) }"
new_state_decl = state_decl + "\n    var showOptionsSheet by remember { mutableStateOf(false) }\n    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)"

if "var showOptionsSheet" not in content:
    content = content.replace(state_decl, new_state_decl)

# 2. Change 3-dot menu click
old_menu_click = ".clickable { onNavigateToQueue() },"
new_menu_click = ".clickable { showOptionsSheet = true },"
content = content.replace(old_menu_click, new_menu_click)

# 3. Add ModalBottomSheet
sheet_composable = """
    if (showOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOptionsSheet = false },
            sheetState = sheetState,
            containerColor = if (isDarkTheme) Color(0xFF181C22) else Color.White,
            shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
            scrimColor = Color.Black.copy(alpha = 0.4f),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 8.dp)
                        .size(width = 32.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(if (isDarkTheme) Color.DarkGray else Color.LightGray)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "When I Saw You",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                        Text(
                            text = "Mariah Carey",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Divider(
                    color = textColor.copy(alpha = 0.1f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Options
                val options = listOf(
                    Triple(Icons.Outlined.PlaylistAdd, "Add to playlist", false),
                    Triple(Icons.Outlined.FavoriteBorder, "Add to favorites", false),
                    Triple(Icons.Outlined.Download, "Download", true),
                    Triple(Icons.Outlined.Album, "Go to album", false),
                    Triple(Icons.Outlined.Person, "Go to artist", false),
                    Triple(Icons.Outlined.Share, "Share", false),
                    Triple(Icons.Outlined.Info, "Details", false)
                )
                
                options.forEach { (icon, title, hasToggle) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor
                            )
                        }
                        if (hasToggle) {
                            var isDownloaded by remember { mutableStateOf(true) }
                            Switch(
                                checked = isDownloaded,
                                onCheckedChange = { isDownloaded = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = textColor,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.DarkGray
                                ),
                                modifier = Modifier.scale(0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
"""

# Insert before the last brace of the composable function
# Find the end of the NowPlayingScreen composable
# We'll just find "val playPauseIcon = " to see where the Box ends, actually we can just insert it inside the outermost Box? No, it should be outside the Box to render properly over everything, but since it's a ModalBottomSheet it's fine anywhere in the hierarchy, but usually best at the end of the top-level Box.
last_box_end = content.rfind("}\n\n@Composable")
if last_box_end == -1:
    last_box_end = content.rfind("}")

content = content[:last_box_end] + sheet_composable + content[last_box_end:]

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
    f.write(content)

print("Done")
