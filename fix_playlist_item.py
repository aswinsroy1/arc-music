with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_fun = """fun PlaylistResultItem(title: String, subtitle: String, imageUrl: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .width(280.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(12.dp),"""

new_fun = """fun PlaylistResultItem(title: String, subtitle: String, imageUrl: String, modifier: Modifier = Modifier.width(280.dp), onClick: () -> Unit = {}) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(12.dp),"""

content = content.replace(old_fun, new_fun)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
