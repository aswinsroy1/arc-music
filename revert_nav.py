import re

with open('app/src/main/java/com/example/ReusableComponents.kt', 'r') as f:
    content = f.read()

new_nav = """@Composable
fun BottomNavigation(
    currentTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier, 
    hazeState: HazeState? = null, 
    tintTransparency: Float = 0.4f, 
    noiseFactor: Float = 0.06f
) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(AppCornerRadius))
            .glassEffect(hazeState, tintTransparency, noiseFactor)
            .padding(horizontal = 24.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (currentTab == 0) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .clickable { onTabSelected(0) }
        ) {
            Icon(
                imageVector = Icons.Default.Home, 
                contentDescription = "Home", 
                tint = if (currentTab == 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (currentTab == 1) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .clickable { onTabSelected(1) }
        ) {
            Icon(
                imageVector = Icons.Default.Search, 
                contentDescription = "Search", 
                tint = if (currentTab == 1) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (currentTab == 2) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .clickable { onTabSelected(2) }
        ) {
            Icon(
                imageVector = Icons.Default.LibraryMusic, 
                contentDescription = "Library", 
                tint = if (currentTab == 2) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}"""

# Find the start of fun BottomNavigation
start_idx = content.find("@Composable\nfun BottomNavigation(")
end_idx = content.find("fun AppPrimaryButton(", start_idx)

# Find the @Composable before AppPrimaryButton
end_idx = content.rfind("@Composable", start_idx, end_idx)

content = content[:start_idx] + new_nav + "\n\n" + content[end_idx:]

with open('app/src/main/java/com/example/ReusableComponents.kt', 'w') as f:
    f.write(content)
