import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    main_content = f.read()

old_header = """@Composable
fun Header(modifier: Modifier = Modifier, onSettingsClick: () -> Unit = {}) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Music","""

new_header = """@Composable
fun Header(modifier: Modifier = Modifier, onSettingsClick: () -> Unit = {}, onBackClick: () -> Unit = { /*TODO*/ }) {

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
        Text(
            text = "Serene","""

main_content = main_content.replace(old_header, new_header)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(main_content)
