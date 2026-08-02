import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Update MusicHomeScreen to map tabs correctly
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
                            ListeningStatsSection()
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
                    ListeningStatsScreenContent(bottomPadding = bottomPadding)
                }
                3 -> {
                    // Profile Placeholder
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Profile", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }"""

content = content.replace(old_when, new_when)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
