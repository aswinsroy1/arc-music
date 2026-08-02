with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

# Replace playlists
old_playlists_loop = """
                if (isGridView) {
                    for (i in playlists.indices step 2) {
"""
new_playlists_loop = """
                val sortedPlaylists = if (sortOption == "Name") {
                    if (sortOrder == "Ascending") playlists.sortedBy { it.title } else playlists.sortedByDescending { it.title }
                } else {
                    if (sortOrder == "Ascending") playlists else playlists.reversed()
                }
                if (isGridView) {
                    for (i in sortedPlaylists.indices step 2) {
"""
content = content.replace(old_playlists_loop, new_playlists_loop)
content = content.replace("playlists[i]", "sortedPlaylists[i]")
content = content.replace("playlists[i + 1]", "sortedPlaylists[i + 1]")
content = content.replace("playlists.size", "sortedPlaylists.size")
content = content.replace("for (p in playlists)", "for (p in sortedPlaylists)")


# Replace albums
old_albums_loop = """
                if (isGridView) {
                    for (i in albums.indices step 2) {
"""
new_albums_loop = """
                val sortedAlbums = if (sortOption == "Name") {
                    if (sortOrder == "Ascending") albums.sortedBy { it.title } else albums.sortedByDescending { it.title }
                } else {
                    if (sortOrder == "Ascending") albums else albums.reversed()
                }
                if (isGridView) {
                    for (i in sortedAlbums.indices step 2) {
"""
content = content.replace(old_albums_loop, new_albums_loop)
content = content.replace("albums[i]", "sortedAlbums[i]")
content = content.replace("albums[i + 1]", "sortedAlbums[i + 1]")
content = content.replace("albums.size", "sortedAlbums.size")
content = content.replace("for (a in albums)", "for (a in sortedAlbums)")

# Replace artists
old_artists_loop = """
                if (isGridView) {
                    for (i in artists.indices step 3) {
"""
new_artists_loop = """
                val sortedArtists = if (sortOption == "Name") {
                    if (sortOrder == "Ascending") artists.sortedBy { it.title } else artists.sortedByDescending { it.title }
                } else {
                    if (sortOrder == "Ascending") artists else artists.reversed()
                }
                if (isGridView) {
                    for (i in sortedArtists.indices step 3) {
"""
content = content.replace(old_artists_loop, new_artists_loop)
content = content.replace("artists[i]", "sortedArtists[i]")
content = content.replace("artists[i + 1]", "sortedArtists[i + 1]")
content = content.replace("artists[i + 2]", "sortedArtists[i + 2]")
content = content.replace("artists.size", "sortedArtists.size")
content = content.replace("for (a in artists)", "for (a in sortedArtists)")

# Replace tracks
old_tracks_loop = """
                if (isGridView) {
                    for (i in tracks.indices step 2) {
"""
new_tracks_loop = """
                val sortedTracks = if (sortOption == "Name") {
                    if (sortOrder == "Ascending") tracks.sortedBy { it.title } else tracks.sortedByDescending { it.title }
                } else {
                    if (sortOrder == "Ascending") tracks else tracks.reversed()
                }
                if (isGridView) {
                    for (i in sortedTracks.indices step 2) {
"""
content = content.replace(old_tracks_loop, new_tracks_loop)
content = content.replace("tracks[i]", "sortedTracks[i]")
content = content.replace("tracks[i + 1]", "sortedTracks[i + 1]")
content = content.replace("tracks.size", "sortedTracks.size")
content = content.replace("for (t in tracks)", "for (t in sortedTracks)")

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
