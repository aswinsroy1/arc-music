import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Fix the call in MusicHomeScreen
old_call = """                        item {
                            ListeningStatsSection()
                        }"""
new_call = """                        item {
                            ListeningStatsSection(onClick = { currentTab = 3 })
                        }"""
content = content.replace(old_call, new_call)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
