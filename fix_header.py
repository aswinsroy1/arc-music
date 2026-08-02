import re

content = open("app/src/main/java/com/example/MainActivity.kt").read()

old_header = """@Composable
fun Header(modifier: Modifier = Modifier, title: String? = "Serene", onSettingsClick: () -> Unit = {}, onBackClick: () -> Unit = { /*TODO*/ }) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Down",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(32.dp)
            )
        }
        if (title != null) {
            Text(
                text = title, 
                style = MaterialTheme.typography.displayLarge, 
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        IconButton(onClick = onSettingsClick) {"""

new_header = """@Composable
fun Header(modifier: Modifier = Modifier, title: String? = "Serene", onSettingsClick: () -> Unit = {}, onBackClick: () -> Unit = { /*TODO*/ }) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (title != null) {
            Text(
                text = title, 
                style = MaterialTheme.typography.displayLarge, 
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
        } else {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Down",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
        IconButton(onClick = onSettingsClick) {"""

content = content.replace(old_header, new_header)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

