with open('app/src/main/java/com/example/CollectionHealthScreen.kt', 'r') as f:
    content = f.read()

old_def = """fun CollectionHealthScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold("""

new_def = """fun CollectionHealthScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    glowIntensity: Float = 0.6f
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedGlowBackground(glowIntensity = glowIntensity)
        Scaffold("""

content = content.replace(old_def, new_def)
content = content.replace("            modifier = modifier", "            modifier = Modifier")

# the closing brace for scaffold needs to be adjusted?
# Let's check how CollectionHealthScreen is structured.
