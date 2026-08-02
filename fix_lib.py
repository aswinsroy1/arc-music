import re

content = open("app/src/main/java/com/example/LibraryComponents.kt").read()

content = content.replace("import androidx.compose.runtime.getValue", "import androidx.compose.runtime.getValue\nimport androidx.compose.runtime.toMutableStateList\nimport androidx.compose.runtime.remember")

albums_pattern = r'val viewModel: MusicViewModel = viewModel\(\)\n    val albumEntities by viewModel.libraryAlbums.collectAsState\(\)\n    val albums = albumEntities.map \{ Track\(it.title, it.artist, it.imageUrl\) \}'
albums_replacement = r'val viewModel: MusicViewModel = viewModel()\n    val albumEntities by viewModel.libraryAlbums.collectAsState()\n    val albums = remember(albumEntities) { albumEntities.map { Track(it.title, it.artist, it.imageUrl) }.toMutableStateList() }'
content = re.sub(albums_pattern, albums_replacement, content)

artists_pattern = r'val artistEntities by viewModel.libraryArtists.collectAsState\(\)\n    val artists = artistEntities.map \{ Track\(it.title, it.artist, it.imageUrl\) \}'
artists_replacement = r'val artistEntities by viewModel.libraryArtists.collectAsState()\n    val artists = remember(artistEntities) { artistEntities.map { Track(it.title, it.artist, it.imageUrl) }.toMutableStateList() }'
content = re.sub(artists_pattern, artists_replacement, content)

tracks_pattern = r'val trackEntities by viewModel.libraryTracks.collectAsState\(\)\n    val tracks = trackEntities.map \{ Track\(it.title, it.artist, it.imageUrl\) \}'
tracks_replacement = r'val trackEntities by viewModel.libraryTracks.collectAsState()\n    val tracks = remember(trackEntities) { trackEntities.map { Track(it.title, it.artist, it.imageUrl) }.toMutableStateList() }'
content = re.sub(tracks_pattern, tracks_replacement, content)

# Check if viewModel import is missing
if "import androidx.lifecycle.viewmodel.compose.viewModel" not in content:
    content = "import androidx.lifecycle.viewmodel.compose.viewModel\n" + content

with open("app/src/main/java/com/example/LibraryComponents.kt", "w") as f:
    f.write(content)
