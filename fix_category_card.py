import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_decl = """fun CategoryCard(category: Category, modifier: Modifier = Modifier) {"""
new_decl = """fun CategoryCard(category: Category, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {"""
content = content.replace(old_decl, new_decl)

old_box = """    Box(
        modifier = modifier
            .height(112.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(category.bgColor)
            .clickable { }
            .padding(24.dp)
    ) {"""
new_box = """    Box(
        modifier = modifier
            .height(112.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(category.bgColor)
            .clickable { onClick() }
            .padding(24.dp)
    ) {"""
content = content.replace(old_box, new_box)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
