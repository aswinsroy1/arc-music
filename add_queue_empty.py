with open('app/src/main/java/com/example/QueueScreen.kt', 'r') as f:
    content = f.read()

empty_state = """
@Composable
fun QueueEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = "Queue is empty",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Queue is empty",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Add some songs to the queue to keep the\\nmusic playing.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        AppPrimaryButton(
            text = "Go to Library",
            onClick = { /* TODO: Navigate to Library */ },
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
            modifier = Modifier.width(220.dp)
        )
    }
}
"""

# Let's insert the empty_state at the end of the file.
content += empty_state

# Now update the QueueScreen to show QueueEmptyState if both are empty.
# We will wrap the LazyColumn in an if/else.
old_lazy = """                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {"""
new_lazy = """                if (upNext.isEmpty() && laterInQueue.isEmpty()) {
                    QueueEmptyState()
                } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {"""
content = content.replace(old_lazy, new_lazy)

# We also need to add a closing brace for the else branch.
old_end_box = """                        }
                    }
                }
            }
        }
    }"""
new_end_box = """                        }
                    }
                }
                }
            }
        }
    }"""
content = content.replace(old_end_box, new_end_box)

with open('app/src/main/java/com/example/QueueScreen.kt', 'w') as f:
    f.write(content)

