with open('app/src/main/java/com/example/CollectionHealthScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("            )\n        }\n}", "            )\n        }\n    }\n}")

with open('app/src/main/java/com/example/CollectionHealthScreen.kt', 'w') as f:
    f.write(content)
