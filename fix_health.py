with open('app/src/main/java/com/example/CollectionHealthScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("    }\n}\n\n@Composable\nfun CollectionHealthScoreSection", "    }\n    }\n}\n\n@Composable\nfun CollectionHealthScoreSection")

with open('app/src/main/java/com/example/CollectionHealthScreen.kt', 'w') as f:
    f.write(content)
