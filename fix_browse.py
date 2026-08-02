import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_call1 = """                    CategoryCard(
                        category = categories[i],
                        modifier = Modifier.weight(1f).clickable { onGenreClick(categories[i].name) }
                    )"""
new_call1 = """                    CategoryCard(
                        category = categories[i],
                        modifier = Modifier.weight(1f),
                        onClick = { onGenreClick(categories[i].name) }
                    )"""
content = content.replace(old_call1, new_call1)

old_call2 = """                        CategoryCard(
                            category = categories[i + 1],
                            modifier = Modifier.weight(1f).clickable { onGenreClick(categories[i + 1].name) }
                        )"""
new_call2 = """                        CategoryCard(
                            category = categories[i + 1],
                            modifier = Modifier.weight(1f),
                            onClick = { onGenreClick(categories[i + 1].name) }
                        )"""
content = content.replace(old_call2, new_call2)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
