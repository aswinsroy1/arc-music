import re
with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add isLibrarySelectionMode state
content = content.replace("var currentTab by remember { mutableIntStateOf(0) }", "var currentTab by remember { mutableIntStateOf(0) }\n    var isLibrarySelectionMode by remember { mutableStateOf(false) }")

# Add onSelectionModeChange to LibraryScreenContent
old_lib = "LibraryScreenContent(bottomPadding = bottomPadding, onNavigateToAlbumDetails = onNavigateToAlbumDetails, onNavigateToPlaylistDetails = onNavigateToPlaylistDetails, onNavigateToArtistDetails = onNavigateToArtistDetails)"
new_lib = "LibraryScreenContent(bottomPadding = if (isLibrarySelectionMode) 100.dp else bottomPadding, onNavigateToAlbumDetails = onNavigateToAlbumDetails, onNavigateToPlaylistDetails = onNavigateToPlaylistDetails, onNavigateToArtistDetails = onNavigateToArtistDetails, onSelectionModeChange = { isLibrarySelectionMode = it })"
content = content.replace(old_lib, new_lib)

# Hide bottom column if in selection mode
old_col = """        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                .onSizeChanged { size ->
                    bottomPadding = with(density) { size.height.toDp() } + 48.dp
                },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {"""
new_col = """        if (!isLibrarySelectionMode) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                    .onSizeChanged { size ->
                        bottomPadding = with(density) { size.height.toDp() } + 48.dp
                    },
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {"""
content = content.replace(old_col, new_col)

# Close the if block
old_bottom_nav = """            BottomNavigation(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                hazeState = hazeState, 
                tintTransparency = tintTransparency, 
                noiseFactor = noiseFactor
            )
        }
    }"""
new_bottom_nav = """            BottomNavigation(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                hazeState = hazeState, 
                tintTransparency = tintTransparency, 
                noiseFactor = noiseFactor
            )
        }
        }
    }"""
content = content.replace(old_bottom_nav, new_bottom_nav)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
