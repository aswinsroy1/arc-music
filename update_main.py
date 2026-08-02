import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Update MusicHomeScreen definition
old_home_def = "fun MusicHomeScreen(onNavigateToCollectionHealth: () -> Unit = {}, onNavigateToArtistDetails: () -> Unit = {},"
new_home_def = "fun MusicHomeScreen(onNavigateToCollectionGrowth: () -> Unit = {}, onNavigateToCollectionHealth: () -> Unit = {}, onNavigateToArtistDetails: () -> Unit = {},"
content = content.replace(old_home_def, new_home_def)

# Update NavHost in MainActivity to pass onNavigateToCollectionGrowth
old_home_call = """                                MusicHomeScreen(
                                    onNavigateToCollectionHealth = { navController.navigate("collection_health") },"""
new_home_call = """                                MusicHomeScreen(
                                    onNavigateToCollectionGrowth = { navController.navigate("collection_growth") },
                                    onNavigateToCollectionHealth = { navController.navigate("collection_health") },"""
content = content.replace(old_home_call, new_home_call)

# Add collection_growth to NavHost
old_health_nav = """                        composable("collection_health") {"""
new_growth_nav = """                        composable("collection_growth") {
                            Box(modifier = Modifier.padding(innerPadding)) {
                                CollectionGrowthScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable("collection_health") {"""
content = content.replace(old_health_nav, new_growth_nav)

# Update RecommendedDownloadsSection call in MusicHomeScreen
old_rec_call = """                        item {
                            RecommendedDownloadsSection(onSongClick = onSongClick)
                        }"""
new_rec_call = """                        item {
                            RecommendedDownloadsSection(onSongClick = onSongClick, onNavigateToCollectionGrowth = onNavigateToCollectionGrowth)
                        }"""
content = content.replace(old_rec_call, new_rec_call)

# Update RecommendedDownloadsSection definition
old_rec_def = "fun RecommendedDownloadsSection(onSongClick: (Song) -> Unit, modifier: Modifier = Modifier) {"
new_rec_def = "fun RecommendedDownloadsSection(onSongClick: (Song) -> Unit, onNavigateToCollectionGrowth: () -> Unit = {}, modifier: Modifier = Modifier) {"
content = content.replace(old_rec_def, new_rec_def)

# Update the header in RecommendedDownloadsSection to have the > and be clickable
old_rec_header = """        Text(
            text = "Recommended Downloads", 
            style = MaterialTheme.typography.headlineMedium, 
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp)
        )"""
new_rec_header = """        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToCollectionGrowth() }
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Recommended Downloads", 
                style = MaterialTheme.typography.headlineMedium, 
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Go to Collection Growth",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }"""
content = content.replace(old_rec_header, new_rec_header)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
