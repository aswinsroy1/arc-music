import re

def fix_file(path, class_name):
    with open(path, 'r') as f:
        content = f.read()

    # Revert all global brace messes
    content = content.replace("    }\n    }\n}\n\n@Composable", "    }\n}\n\n@Composable")
    content = content.replace("    }\n    }\n}\n\n", "    }\n}\n\n")
    content = content.replace("    }\n    }\n}", "    }\n}")
    
    with open(path, 'w') as f:
        f.write(content)

fix_file('app/src/main/java/com/example/CollectionGrowthScreen.kt', 'CollectionGrowthScreen')
fix_file('app/src/main/java/com/example/CollectionHealthScreen.kt', 'CollectionHealthScreen')
