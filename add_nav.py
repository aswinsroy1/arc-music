with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

nav_route = """                        composable("collection_health") {
                            Box(modifier = Modifier.padding(innerPadding)) {
                                CollectionHealthScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }"""
content = content.replace('composable("artist_details") {', nav_route + '\n                        composable("artist_details") {')

# Add onNavigateToCollectionHealth to MusicHomeScreen
music_home_screen_sig = "fun MusicHomeScreen("
new_music_home_screen_sig = "fun MusicHomeScreen(onNavigateToCollectionHealth: () -> Unit = {}, "
content = content.replace(music_home_screen_sig, new_music_home_screen_sig)

# Add it to the Home screen nav route
old_home_route = """                                MusicHomeScreen(
                                    tintTransparency = tintTransparency,"""
new_home_route = """                                MusicHomeScreen(
                                    onNavigateToCollectionHealth = { navController.navigate("collection_health") },
                                    tintTransparency = tintTransparency,"""
content = content.replace(old_home_route, new_home_route)

# Now update CollectionHealthSection to take onClick
old_collection_health_section = """@Composable
fun CollectionHealthSection(modifier: Modifier = Modifier) {
    Column("""
new_collection_health_section = """@Composable
fun CollectionHealthSection(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column("""
content = content.replace(old_collection_health_section, new_collection_health_section)

# And add the clickable modifier to GlassCard in CollectionHealthSection
old_glass_card = """        GlassCard(
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {"""
new_glass_card = """        GlassCard(
            modifier = Modifier.padding(horizontal = 24.dp).clickable { onClick() }
        ) {"""
content = content.replace(old_glass_card, new_glass_card)

# Update MusicHomeScreen call of CollectionHealthSection
old_call = """                            CollectionHealthSection()"""
new_call = """                            CollectionHealthSection(onClick = onNavigateToCollectionHealth)"""
content = content.replace(old_call, new_call)


with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

