with open('app/src/main/java/com/example/ListeningStatsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("Icons.Default.TrendingUp", "Icons.AutoMirrored.Filled.TrendingUp")

with open('app/src/main/java/com/example/ListeningStatsScreen.kt', 'w') as f:
    f.write(content)
