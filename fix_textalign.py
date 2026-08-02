import re

with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

content = content.replace("textAlign = TextAlign.Center", "textAlign = androidx.compose.ui.text.style.TextAlign.Center")

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
