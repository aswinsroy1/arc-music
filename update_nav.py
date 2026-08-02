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
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(AppCornerRadius))
            .glassEffect(hazeState, tintTransparency, noiseFactor)
            .padding(horizontal = 16.dp)
    ) {
        val tabs = listOf(
            Triple(0, "Home", Icons.Default.Home),
            Triple(1, "Library", Icons.Default.LibraryMusic),
            Triple(2, "Stats", Icons.Default.BarChart),
            Triple(3, "Profile", Icons.Default.Person)
        )

        tabs.forEach { (index, label, icon) ->
            val isSelected = currentTab == index
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = if (isSelected) 24.dp else 16.dp, vertical = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = icon, 
                        contentDescription = label, 
                        tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
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
