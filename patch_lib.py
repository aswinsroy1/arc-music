import re

content = open("app/src/main/java/com/example/LibraryComponents.kt").read()

imports = """
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
"""
content = content.replace("import androidx.compose.runtime.remember", imports + "\nimport androidx.compose.runtime.remember")

albums_pattern = r'val albums = remember \{ mutableStateListOf\([\s\S]*?\n    \) \}'
albums_replacement = r'val viewModel: MusicViewModel = viewModel()\n    val albumEntities by viewModel.libraryAlbums.collectAsState()\n    val albums = albumEntities.map { Track(it.title, it.artist, it.imageUrl) }'
content = re.sub(albums_pattern, albums_replacement, content)

artists_pattern = r'val artists = remember \{ mutableStateListOf\([\s\S]*?\n    \) \}'
artists_replacement = r'val artistEntities by viewModel.libraryArtists.collectAsState()\n    val artists = artistEntities.map { Track(it.title, it.artist, it.imageUrl) }'
content = re.sub(artists_pattern, artists_replacement, content)

tracks_pattern = r'val tracks = remember \{ mutableStateListOf\([\s\S]*?\n    \) \}'
tracks_replacement = r'val trackEntities by viewModel.libraryTracks.collectAsState()\n    val tracks = trackEntities.map { Track(it.title, it.artist, it.imageUrl) }'
content = re.sub(tracks_pattern, tracks_replacement, content)

with open("app/src/main/java/com/example/LibraryComponents.kt", "w") as f:
    f.write(content)

print("LibraryComponents patched")
