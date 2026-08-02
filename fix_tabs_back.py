import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace("ListeningStatsScreenContent(bottomPadding = bottomPadding)", "ListeningStatsScreenContent(bottomPadding = bottomPadding, onNavigateBack = { currentTab = 0 })")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
