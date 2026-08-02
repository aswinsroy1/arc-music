with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

imports_to_add = """
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Check
"""

content = content.replace("import androidx.compose.material.icons.filled.Close", "import androidx.compose.material.icons.filled.Close" + imports_to_add)

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
