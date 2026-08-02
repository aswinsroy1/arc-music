import re
with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'r') as f:
    content = f.read()

# Fix waveHeight animations
content = re.sub(r'if \(isDragging\) 14\.dp\.value else 8\.dp\.value', 'if (isDragging) 18.dp.value else 12.dp.value', content)
content = re.sub(r'if \(isDragging\) 1\.1f else 1f', 'if (isDragging) 1.25f else 1f', content)

# Fix dimensions
content = re.sub(r'val waveW = 80\.dp\.toPx\(\)', 'val waveW = 180.dp.toPx()', content)
content = re.sub(r'val trackHeight = 2\.dp\.toPx\(\)', 'val trackHeight = 4.dp.toPx()', content)

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
    f.write(content)
print("Updated parameters")
