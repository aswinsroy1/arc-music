with open('app/src/main/java/com/example/CollectionGrowthScreen.kt', 'r') as f:
    content = f.read()

old_def = """fun CollectionGrowthScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold("""

new_def = """fun CollectionGrowthScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    glowIntensity: Float = 0.6f
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedGlowBackground(glowIntensity = glowIntensity)
        Scaffold("""

content = content.replace(old_def, new_def)
content = content.replace("containerColor = Color.Transparent\n    ) { innerPadding ->\n        LazyColumn(\n            modifier = modifier", "containerColor = Color.Transparent\n    ) { innerPadding ->\n        LazyColumn(\n            modifier = Modifier")
content = content.replace("        }\n    }\n}\n\n@Composable\nfun CompleteCollectionCard", "        }\n    }\n    }\n}\n\n@Composable\nfun CompleteCollectionCard")

with open('app/src/main/java/com/example/CollectionGrowthScreen.kt', 'w') as f:
    f.write(content)
