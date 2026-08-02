with open('app/src/main/java/com/example/ReusableComponents.kt', 'r') as f:
    content = f.read()

content = content.replace("import androidx.compose.material.icons.filled.LibraryMusic", "import androidx.compose.material.icons.filled.*\nimport androidx.compose.material.icons.filled.LibraryMusic")

with open('app/src/main/java/com/example/ReusableComponents.kt', 'w') as f:
    f.write(content)
