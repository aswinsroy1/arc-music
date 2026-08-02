import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add the import if not there
if "import androidx.compose.material.icons.filled.KeyboardArrowDown" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.MoreVert", "import androidx.compose.material.icons.filled.MoreVert\nimport androidx.compose.material.icons.filled.KeyboardArrowDown")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

