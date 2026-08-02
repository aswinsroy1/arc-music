with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

import_statement = "import androidx.compose.material.icons.automirrored.filled.*"
if import_statement not in content:
    content = content.replace("import androidx.compose.material.icons.filled.*", "import androidx.compose.material.icons.filled.*\n" + import_statement)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
