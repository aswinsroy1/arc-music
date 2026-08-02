import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace("Icons.Default.KeyboardArrowRight", "Icons.AutoMirrored.Filled.KeyboardArrowRight")
content = content.replace("import androidx.compose.material.icons.filled.*", "import androidx.compose.material.icons.filled.*\nimport androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
