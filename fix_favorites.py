with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

# fix line 104
content = content.replace("} else if (tabName == \"Tracks\") {", "} else {", 1)

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
