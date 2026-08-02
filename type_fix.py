import re
content = open("app/src/main/java/com/example/LibraryComponents.kt").read()

content = content.replace("val sortedPlaylists = if", "val sortedPlaylists: List<Track> = if")
content = content.replace("val sortedAlbums = if", "val sortedAlbums: List<Track> = if")
content = content.replace("val sortedArtists = if", "val sortedArtists: List<Track> = if")
content = content.replace("val sortedTracks = if", "val sortedTracks: List<Track> = if")

with open("app/src/main/java/com/example/LibraryComponents.kt", "w") as f:
    f.write(content)
