import re

main_kt = open("app/src/main/java/com/example/MainActivity.kt").read()
# just extract lists from each function manually since we know where they are
def extract_category(func_name, content, category):
    match = re.search(r'fun ' + func_name + r'(.*?)\n\n', content, re.DOTALL)
    if not match:
        match = re.search(r'fun ' + func_name + r'(.*?)fun', content, re.DOTALL)
    if not match:
        return []
    matches = re.findall(r'Song\s*\(\s*"(.*?)"\s*,\s*"(.*?)"\s*,\s*"(.*?)"\s*\)', match.group(1))
    return [(m[0], m[1], m[2], category) for m in matches]

items = []
items.extend(extract_category("RandomPicksSection", main_kt, "RandomPicks"))
items.extend(extract_category("RecentlyPlayedSection", main_kt, "RecentlyPlayed"))
items.extend(extract_category("RecommendedDownloadsSection", main_kt, "RecommendedDownloads"))

lib_kt = open("app/src/main/java/com/example/LibraryComponents.kt").read()
# for library components, they are just variables: albums, artists, tracks
albums = re.search(r'val albums = remember \{ mutableStateListOf\((.*?)\) \}', lib_kt, re.DOTALL).group(1)
for m in re.findall(r'Track\s*\(\s*"(.*?)"\s*,\s*"(.*?)"\s*,\s*"(.*?)"\s*\)', albums):
    items.append((m[0], m[1], m[2], "LibraryAlbums"))

artists = re.search(r'val artists = remember \{ mutableStateListOf\((.*?)\) \}', lib_kt, re.DOTALL).group(1)
for m in re.findall(r'Track\s*\(\s*"(.*?)"\s*,\s*"(.*?)"\s*,\s*"(.*?)"\s*\)', artists):
    items.append((m[0], m[1], m[2], "LibraryArtists"))

tracks = re.search(r'val tracks = remember \{ mutableStateListOf\((.*?)\) \}', lib_kt, re.DOTALL).group(1)
for m in re.findall(r'Track\s*\(\s*"(.*?)"\s*,\s*"(.*?)"\s*,\s*"(.*?)"\s*\)', tracks):
    items.append((m[0], m[1], m[2], "LibraryTracks"))

# Genre tracks, albums, artists
genre_kt = open("app/src/main/java/com/example/GenreHubScreen.kt").read()
# TopTracksSection
top_tracks_section = re.search(r'fun GenreTopTracksSection(.*?)fun ', genre_kt, re.DOTALL).group(1)
genre_tracks = re.findall(r'Triple\("(.*?)", "(.*?)", "(.*?)"\)', top_tracks_section)
genre_images = re.findall(r'"(https://.*?)"', top_tracks_section)
for i in range(len(genre_tracks)):
    items.append((genre_tracks[i][0], genre_tracks[i][1], genre_images[i] if i < len(genre_images) else "", "GenreTopTracks", genre_tracks[i][2]))

# TopAlbumsSection
top_albums_section = re.search(r'fun GenreEssentialAlbumsSection(.*?)fun ', genre_kt, re.DOTALL).group(1)
genre_albums = re.findall(r'Triple\("(.*?)", "(.*?)", "(.*?)"\)', top_albums_section)
for i in range(len(genre_albums)):
    items.append((genre_albums[i][0], genre_albums[i][1], genre_albums[i][2], "GenreTopAlbums"))

# TopArtistsSection
top_artists_section = re.search(r'fun GenreFeaturedArtistsSection(.*?)$', genre_kt, re.DOTALL).group(1)
genre_artists = re.findall(r'Pair\("(.*?)", "(.*?)"\)', top_artists_section)
for i in range(len(genre_artists)):
    items.append((genre_artists[i][0], "Artist", genre_artists[i][1], "GenreTopArtists"))


seed_data_code = """package com.example.db

val initialSeedData = listOf(
"""

for e in items:
    extra = e[4] if len(e) > 4 else ""
    seed_data_code += f'    SongEntity(title = "{e[0]}", artist = "{e[1]}", imageUrl = "{e[2]}", category = "{e[3]}", extraData = "{extra}"),\n'

seed_data_code += """)
"""

with open('app/src/main/java/com/example/db/SeedData.kt', 'w') as f:
    f.write(seed_data_code)

print("Total items:", len(items))
