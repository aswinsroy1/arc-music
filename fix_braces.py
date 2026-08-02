with open('app/src/main/java/com/example/CollectionGrowthScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("        }\n    }\n}\n\n@Composable", "        }\n    }\n    }\n}\n\n@Composable")

with open('app/src/main/java/com/example/CollectionGrowthScreen.kt', 'w') as f:
    f.write(content)
