import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace("Icons.AutoMirrored.Filled.KeyboardArrowRight", "Icons.Default.KeyboardArrowRight")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
