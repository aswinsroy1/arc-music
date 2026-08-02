import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# 1. Update ListeningStatsSection call to set tab to 3
old_stats_call = """                        item {
                            ListeningStatsSection(onClick = onNavigateToListeningStats)
                        }"""
new_stats_call = """                        item {
                            ListeningStatsSection(onClick = { currentTab = 3 })
                        }"""
content = content.replace(old_stats_call, new_stats_call)

# 2. Add tab 3 to `when (currentTab)`
old_when = """                2 -> {
                    LibraryScreenContent(bottomPadding = if (isLibrarySelectionMode) 100.dp else bottomPadding, onNavigateToAlbumDetails = onNavigateToAlbumDetails, onNavigateToPlaylistDetails = onNavigateToPlaylistDetails, onNavigateToArtistDetails = onNavigateToArtistDetails, onSelectionModeChange = { isLibrarySelectionMode = it })
                }
            }"""
new_when = """                2 -> {
                    LibraryScreenContent(bottomPadding = if (isLibrarySelectionMode) 100.dp else bottomPadding, onNavigateToAlbumDetails = onNavigateToAlbumDetails, onNavigateToPlaylistDetails = onNavigateToPlaylistDetails, onNavigateToArtistDetails = onNavigateToArtistDetails, onSelectionModeChange = { isLibrarySelectionMode = it })
                }
                3 -> {
                    ListeningStatsScreenContent(bottomPadding = bottomPadding, onNavigateBack = { currentTab = 0 })
                }
            }"""
content = content.replace(old_when, new_when)

# 3. Remove `onNavigateToListeningStats` from `MusicHomeScreen` args so it doesn't break
old_music_home_decl = """fun MusicHomeScreen(onNavigateToCollectionGrowth: () -> Unit = {}, onNavigateToCollectionHealth: () -> Unit = {}, onNavigateToArtistDetails: () -> Unit = {}, onNavigateToListeningStats: () -> Unit = {},
    onNavigateToPlaylistDetails: () -> Unit = {},modifier: Modifier = Modifier, tintTransparency: Float = 0.4f, noiseFactor: Float = 0.06f, glowIntensity: Float = 0.6f, onNavigateToSettings: () -> Unit = {}, onNavigateToNowPlaying: () -> Unit = {}, onNavigateToAlbumDetails: () -> Unit = {}) {"""

new_music_home_decl = """fun MusicHomeScreen(onNavigateToCollectionGrowth: () -> Unit = {}, onNavigateToCollectionHealth: () -> Unit = {}, onNavigateToArtistDetails: () -> Unit = {}, 
    onNavigateToPlaylistDetails: () -> Unit = {},modifier: Modifier = Modifier, tintTransparency: Float = 0.4f, noiseFactor: Float = 0.06f, glowIntensity: Float = 0.6f, onNavigateToSettings: () -> Unit = {}, onNavigateToNowPlaying: () -> Unit = {}, onNavigateToAlbumDetails: () -> Unit = {}) {"""
content = content.replace(old_music_home_decl, new_music_home_decl)

# 4. Remove `onNavigateToListeningStats` from NavHost
old_home_call = """                                MusicHomeScreen(
                                    onNavigateToCollectionGrowth = { navController.navigate("collection_growth") },
                                    onNavigateToCollectionHealth = { navController.navigate("collection_health") },
                                    onNavigateToListeningStats = { navController.navigate("listening_stats") },"""
new_home_call = """                                MusicHomeScreen(
                                    onNavigateToCollectionGrowth = { navController.navigate("collection_growth") },
                                    onNavigateToCollectionHealth = { navController.navigate("collection_health") },"""
content = content.replace(old_home_call, new_home_call)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
