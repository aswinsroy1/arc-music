import re

with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'text = if (selectedItems.size == 1) \n                            "Are you sure you want to delete "${itemName}"? This action cannot be undone." \n                        else \n                            "Are you sure you want to delete ${selectedItems.size} items? This action cannot be undone.",',
    'text = if (selectedItems.size == 1) \n                            "Are you sure you want to delete \\"${itemName}\\"? This action cannot be undone." \n                        else \n                            "Are you sure you want to delete ${selectedItems.size} items? This action cannot be undone.",'
)

# And check for missing imports
if "androidx.compose.ui.text.style.TextAlign" not in content:
    content = "import androidx.compose.ui.text.style.TextAlign\n" + content

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
