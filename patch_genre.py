import re

content = open("app/src/main/java/com/example/GenreHubScreen.kt").read()

imports = """
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
"""
content = content.replace("import androidx.compose.runtime.Composable", imports + "\nimport androidx.compose.runtime.Composable")

# 1. Top Tracks
tracks_pattern = r'val tracks = listOf\([\s\S]*?\n        \)\n        val images = listOf\([\s\S]*?\n        \)'
tracks_replacement = r'val viewModel: MusicViewModel = viewModel()\n        val trackEntities by viewModel.genreTopTracks.collectAsState()\n        val tracks = trackEntities.map { Triple(it.title, it.artist, it.extraData) }\n        val images = trackEntities.map { it.imageUrl }'
content = re.sub(tracks_pattern, tracks_replacement, content)

# 2. Top Albums
albums_pattern = r'val albums = listOf\([\s\S]*?\n        \)'
albums_replacement = r'val viewModel: MusicViewModel = viewModel()\n        val albumEntities by viewModel.genreTopAlbums.collectAsState()\n        val albums = albumEntities.map { Triple(it.title, it.artist, it.imageUrl) }'
content = re.sub(albums_pattern, albums_replacement, content)

# 3. Top Artists
artists_pattern = r'val artists = listOf\([\s\S]*?\n        \)'
artists_replacement = r'val viewModel: MusicViewModel = viewModel()\n        val artistEntities by viewModel.genreTopArtists.collectAsState()\n        val artists = artistEntities.map { Pair(it.title, it.imageUrl) }'
content = re.sub(artists_pattern, artists_replacement, content)

with open("app/src/main/java/com/example/GenreHubScreen.kt", "w") as f:
    f.write(content)

print("GenreHubScreen patched")
