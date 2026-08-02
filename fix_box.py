with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

old_code = """        }
        
        if (isSelectionMode) {
            Box(modifier = Modifier.fillMaxSize().padding(bottom = bottomPadding + 16.dp), contentAlignment = Alignment.BottomCenter) {
                SelectionBottomBar(
                    onAddToPlaylist = { selectedItems.clear() },
                    onAddToFavorites = { selectedItems.clear() },
                    onShare = { selectedItems.clear() },
                    onDelete = { selectedItems.clear() }
                )
            }
        }
    }
    }"""
new_code = """        }
    }

    if (isSelectionMode) {
        Box(modifier = Modifier.fillMaxSize().padding(bottom = bottomPadding + 16.dp), contentAlignment = Alignment.BottomCenter) {
            SelectionBottomBar(
                onAddToPlaylist = { selectedItems.clear() },
                onAddToFavorites = { selectedItems.clear() },
                onShare = { selectedItems.clear() },
                onDelete = { selectedItems.clear() }
            )
        }
    }"""
content = content.replace(old_code, new_code)
if old_code not in content and new_code in content:
    print("Already applied?")
elif old_code in content:
    print("Found exact match.")

# actually let's just use re
import re
content = re.sub(r'\s+if \(isSelectionMode\) \{\s+Box\(modifier = Modifier.fillMaxSize\(\).padding\(bottom = bottomPadding \+ 16.dp\), contentAlignment = Alignment.BottomCenter\) \{\s+SelectionBottomBar\(\s+onAddToPlaylist = \{ selectedItems.clear\(\) \},\s+onAddToFavorites = \{ selectedItems.clear\(\) \},\s+onShare = \{ selectedItems.clear\(\) \},\s+onDelete = \{ selectedItems.clear\(\) \}\s+\)\s+\}\s+\}\s+\}\s+\}', 
r'''
        }
    }

    if (isSelectionMode) {
        Box(modifier = Modifier.fillMaxSize().padding(bottom = bottomPadding + 16.dp), contentAlignment = Alignment.BottomCenter) {
            SelectionBottomBar(
                onAddToPlaylist = { selectedItems.clear() },
                onAddToFavorites = { selectedItems.clear() },
                onShare = { selectedItems.clear() },
                onDelete = { selectedItems.clear() }
            )
        }
    }''', content)

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
