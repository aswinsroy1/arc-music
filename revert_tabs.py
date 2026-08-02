import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# 1. Update MusicHomeScreen declaration
old_music_home_decl = """fun MusicHomeScreen(onNavigateToCollectionGrowth: () -> Unit = {}, onNavigateToCollectionHealth: () -> Unit = {}, onNavigateToArtistDetails: () -> Unit = {}, 
    onNavigateToPlaylistDetails: () -> Unit = {},modifier: Modifier = Modifier, tintTransparency: Float = 0.4f, noiseFactor: Float = 0.06f, glowIntensity: Float = 0.6f, onNavigateToSettings: () -> Unit = {}, onNavigateToNowPlaying: () -> Unit = {}, onNavigateToAlbumDetails: () -> Unit = {}) {"""

new_music_home_decl = """fun MusicHomeScreen(onNavigateToCollectionGrowth: () -> Unit = {}, onNavigateToCollectionHealth: () -> Unit = {}, onNavigateToArtistDetails: () -> Unit = {}, onNavigateToListeningStats: () -> Unit = {},
    onNavigateToPlaylistDetails: () -> Unit = {},modifier: Modifier = Modifier, tintTransparency: Float = 0.4f, noiseFactor: Float = 0.06f, glowIntensity: Float = 0.6f, onNavigateToSettings: () -> Unit = {}, onNavigateToNowPlaying: () -> Unit = {}, onNavigateToAlbumDetails: () -> Unit = {}) {"""
content = content.replace(old_music_home_decl, new_music_home_decl)

# 2. Revert `when (currentTab)`
old_when = """            when (currentTab) {
                0 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 24.dp, bottom = bottomPadding),
                        verticalArrangement = Arrangement.spacedBy(32.dp),
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        item {
                            Header(modifier = Modifier.padding(horizontal = 24.dp), onSettingsClick = onNavigateToSettings)
                        }
                        item {
                            RecentlyPlayedRow(onNavigateToAlbumDetails = onNavigateToAlbumDetails, onNavigateToPlaylistDetails = onNavigateToPlaylistDetails)
                        }
                        item {
                            YourCuratedMixesRow()
                        }
                        item {
                            NewReleasesRow()
                        }
                        item {
                            CollectionGrowthCard(modifier = Modifier.padding(horizontal = 24.dp), onClick = onNavigateToCollectionGrowth)
                        }
                        item {
                            CollectionHealthCard(modifier = Modifier.padding(horizontal = 24.dp), onClick = onNavigateToCollectionHealth)
                        }
                        item {
                            ListeningStatsSection(
                                modifier = Modifier.clickable { currentTab = 2 }
                            )
                        }
                    }
                }
                1 -> {
                    LibraryScreenContent(bottomPadding = if (isLibrarySelectionMode) 100.dp else bottomPadding, onNavigateToAlbumDetails = onNavigateToAlbumDetails, onNavigateToPlaylistDetails = onNavigateToPlaylistDetails, onNavigateToArtistDetails = onNavigateToArtistDetails, onSelectionModeChange = { isLibrarySelectionMode = it })
                }
                2 -> {
                    ListeningStatsScreenContent(bottomPadding = bottomPadding, onNavigateBack = { currentTab = 0 })
                }
                3 -> {
                    // Profile Placeholder
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Profile", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }"""

new_when = """            when (currentTab) {
                0 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 24.dp, bottom = bottomPadding),
                        verticalArrangement = Arrangement.spacedBy(32.dp),
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        item {
                            Header(modifier = Modifier.padding(horizontal = 24.dp), onSettingsClick = onNavigateToSettings)
                        }
                        item {
                            RecentlyPlayedSection(onSongClick = onSongClick)
                        }
                        item {
                            RandomPicksSection(onSongClick = onSongClick)
                        }
                        item {
                            RecommendedDownloadsSection(onSongClick = onSongClick, onNavigateToCollectionGrowth = onNavigateToCollectionGrowth)
                        }
                        item {
                            CollectionHealthSection(onClick = onNavigateToCollectionHealth)
                        }
                        item {
                            ListeningStatsSection(onClick = onNavigateToListeningStats)
                        }
                    }
                }
                1 -> {
                    SearchScreenContent(bottomPadding = bottomPadding, onNavigateToAlbumDetails = onNavigateToAlbumDetails, onNavigateToPlaylistDetails = onNavigateToPlaylistDetails, onNavigateToArtistDetails = onNavigateToArtistDetails)
                }
                2 -> {
                    LibraryScreenContent(bottomPadding = if (isLibrarySelectionMode) 100.dp else bottomPadding, onNavigateToAlbumDetails = onNavigateToAlbumDetails, onNavigateToPlaylistDetails = onNavigateToPlaylistDetails, onNavigateToArtistDetails = onNavigateToArtistDetails, onSelectionModeChange = { isLibrarySelectionMode = it })
                }
            }"""
content = content.replace(old_when, new_when)

# 3. Update ListeningStatsSection
old_stats_section = """@Composable
fun ListeningStatsSection(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        Text(
            text = "Listening Stats",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        GlassCard(
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {"""

new_stats_section = """@Composable
fun ListeningStatsSection(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        Text(
            text = "Listening Stats",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        GlassCard(
            modifier = Modifier.padding(horizontal = 24.dp).clickable { onClick() }
        ) {"""
content = content.replace(old_stats_section, new_stats_section)

# 4. Update NavHost
old_home_call = """                                MusicHomeScreen(
                                    onNavigateToCollectionGrowth = { navController.navigate("collection_growth") },
                                    onNavigateToCollectionHealth = { navController.navigate("collection_health") },
                                    tintTransparency = tintTransparency,
                                    noiseFactor = noiseFactor,
                                    glowIntensity = glowIntensity,
                                    onNavigateToSettings = { navController.navigate("settings") },
                                    onNavigateToNowPlaying = { navController.navigate("now_playing") },
                                    onNavigateToAlbumDetails = { navController.navigate("album_details") },
                                    onNavigateToPlaylistDetails = { navController.navigate("playlist_details") },
                                    onNavigateToArtistDetails = { navController.navigate("artist_details") }
                                )"""

new_home_call = """                                MusicHomeScreen(
                                    onNavigateToCollectionGrowth = { navController.navigate("collection_growth") },
                                    onNavigateToCollectionHealth = { navController.navigate("collection_health") },
                                    onNavigateToListeningStats = { navController.navigate("listening_stats") },
                                    tintTransparency = tintTransparency,
                                    noiseFactor = noiseFactor,
                                    glowIntensity = glowIntensity,
                                    onNavigateToSettings = { navController.navigate("settings") },
                                    onNavigateToNowPlaying = { navController.navigate("now_playing") },
                                    onNavigateToAlbumDetails = { navController.navigate("album_details") },
                                    onNavigateToPlaylistDetails = { navController.navigate("playlist_details") },
                                    onNavigateToArtistDetails = { navController.navigate("artist_details") }
                                )"""
content = content.replace(old_home_call, new_home_call)

old_nav_entries = """                        composable("collection_health") {"""
new_nav_entries = """                        composable("listening_stats") {
                            Box(modifier = Modifier.padding(innerPadding)) {
                                ListeningStatsScreenContent(
                                    bottomPadding = 0.dp,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable("collection_health") {"""
content = content.replace(old_nav_entries, new_nav_entries)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
