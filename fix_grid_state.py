with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

import re

old_grid_state = "var isGridView by remember { mutableStateOf(true) }"
new_grid_state = "var isGridView by androidx.compose.runtime.saveable.rememberSaveable(tabName) { mutableStateOf(true) }"
content = content.replace(old_grid_state, new_grid_state)

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
