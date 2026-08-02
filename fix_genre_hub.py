import re

with open('app/src/main/java/com/example/GenreHubScreen.kt', 'r') as f:
    content = f.read()

old_header_call = """        item {
            Header(
                modifier = Modifier.padding(horizontal = 24.dp),
                onSettingsClick = { },
                onBackClick = onNavigateBack
            )
        }"""
new_header_call = """        item {
            Header(
                modifier = Modifier.padding(horizontal = 24.dp),
                title = null,
                onSettingsClick = { },
                onBackClick = onNavigateBack
            )
        }"""
content = content.replace(old_header_call, new_header_call)

with open('app/src/main/java/com/example/GenreHubScreen.kt', 'w') as f:
    f.write(content)
