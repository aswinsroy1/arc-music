with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

content = content.replace("androidx.compose.foundation.lazy.items(filters)", "items(filters)")

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
