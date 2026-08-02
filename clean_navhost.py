import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Remove the listening_stats composable route
old_nav_entries = """                        composable("listening_stats") {
                            Box(modifier = Modifier.padding(innerPadding)) {
                                ListeningStatsScreenContent(
                                    bottomPadding = 0.dp,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable("collection_health") {"""
new_nav_entries = """                        composable("collection_health") {"""
content = content.replace(old_nav_entries, new_nav_entries)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
