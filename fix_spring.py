import re
with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("dampingRatio = Spring.DampingRatioNoBouncy", "dampingRatio = 0.7f")

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
    f.write(content)
