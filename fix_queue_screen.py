import re
with open('app/src/main/java/com/example/QueueScreen.kt', 'r') as f:
    content = f.read()

# Add statusBarsPadding()
content = content.replace("""        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),""", """        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp),""")

# Remove Shuffle / Repeat block
content = re.sub(r'            // Controls \(Shuffle / Repeat\).*?// Main Queue Container', '            // Main Queue Container', content, flags=re.DOTALL)

# Update Bottom Actions
content = content.replace("""                    item {
                        // Bottom Actions
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .clickable { },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Tune, contentDescription = "Tune", tint = MaterialTheme.colorScheme.onSurface)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { }) {
                                    Text("Clear queue", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(
                                    onClick = { },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text("Save as playlist")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(imageVector = Icons.Default.PlaylistAdd, contentDescription = null)
                                }
                            }
                        }
                    }""", """                    item {
                        // Bottom Actions
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { }) {
                                Text("Clear queue", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = { },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text("Save as playlist")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(imageVector = Icons.Default.PlaylistAdd, contentDescription = null)
                            }
                        }
                    }""")

with open('app/src/main/java/com/example/QueueScreen.kt', 'w') as f:
    f.write(content)
