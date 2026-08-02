with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

old_box = """        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {"""

new_box = """        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {"""

content = content.replace(old_box, new_box)

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)

