import re

with open('app/src/main/java/com/aeswox/arcmusic/ArtistDetailsScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add imports
imports = """import androidx.hilt.navigation.compose.hiltViewModel
import com.aeswox.arcmusic.db.entities.Artist
import com.aeswox.arcmusic.db.entities.Track
import com.aeswox.arcmusic.db.entities.Album
"""
content = content.replace('import coil.compose.AsyncImage\n', 'import coil.compose.AsyncImage\n' + imports)

# Update ArtistDetailsScreen
old_sig = """@Composable
fun ArtistDetailsScreen(
    artistId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToAlbum: (String) -> Unit = {}
) {"""
new_sig = """@Composable
fun ArtistDetailsScreen(
    artistId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToAlbum: (String) -> Unit = {},
    viewModel: MusicViewModel = hiltViewModel()
) {
    val artist by viewModel.getArtistById(artistId).collectAsState(initial = null)
    val tracks by viewModel.getTracksByArtist(artistId).collectAsState(initial = emptyList())
    val albums by viewModel.getAlbumsByArtist(artistId).collectAsState(initial = emptyList())
"""
content = content.replace(old_sig, new_sig)

content = content.replace("ArtistHeroSection()", "ArtistHeroSection(artist = artist, tracks = tracks, viewModel = viewModel)")
content = content.replace("ArtistPopularSection()", "ArtistPopularSection(tracks = tracks, viewModel = viewModel)")
content = content.replace("ArtistAlbumsSection()", "ArtistAlbumsSection(albums = albums, onNavigateToAlbum = onNavigateToAlbum)")
content = content.replace("ArtistAboutSection()", "ArtistAboutSection(artist = artist)")

# Update ArtistHeroSection
old_hero_sig = """@Composable
fun ArtistHeroSection() {"""
new_hero_sig = """@Composable
fun ArtistHeroSection(artist: Artist?, tracks: List<Track>, viewModel: MusicViewModel) {"""
content = content.replace(old_hero_sig, new_hero_sig)

# Replace image
content = re.sub(
    r'model = "https://lh3.googleusercontent.com[^"]+",',
    'model = artist?.photoUri ?: "",',
    content
)

# Replace Conan Gray
content = content.replace('text = "Conan Gray",', 'text = artist?.name ?: "Unknown Artist",')

# Replace listeners
content = re.sub(r'Row\(\s*verticalAlignment = Alignment\.CenterVertically,\s*modifier = Modifier\s*\.clickable \{ \}\s*\.padding\(vertical = 8\.dp\)\s*\)\s*\{\s*Text\(\s*text = "12\.4M monthly listeners"[\s\S]*?Icon\([\s\S]*?\}\s*Spacer\(modifier = Modifier\.height\(24\.dp\)\)', 'Spacer(modifier = Modifier.height(24.dp))', content)


# Swap Play/Shuffle
old_buttons = """                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.8f), contentColor = MaterialTheme.colorScheme.onSurface),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Shuffle", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }"""
new_buttons = """                Button(
                    onClick = { if (tracks.isNotEmpty()) viewModel.setCurrentlyPlaying(tracks.first(), tracks) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                
                Button(
                    onClick = { if (tracks.isNotEmpty()) viewModel.setCurrentlyPlaying(tracks.random(), tracks.shuffled()) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.8f), contentColor = MaterialTheme.colorScheme.onSurface),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Shuffle", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }"""
content = content.replace(old_buttons, new_buttons)

# Update ArtistPopularSection
old_pop_sig = """@Composable
fun ArtistPopularSection() {"""
new_pop_sig = """@Composable
fun ArtistPopularSection(tracks: List<Track>, viewModel: MusicViewModel) {"""
content = content.replace(old_pop_sig, new_pop_sig)

old_pop_list = """        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.7f))
                .padding(8.dp)
        ) {
            ArtistTrackItem(number = 1, title = "Heather", views = "853M", imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDqBXafecewxgiq8OPzCFmZ8mHtbbckUmKOL_ZVtstCfUiaQjJGzU7SFGaCC17OIYdlZu7Aq6fbqZt2WJbu5HDUwrHZYYSPaxniYCtQ08GvqzMwkV7L9YL_tJdl3NAIpEznWtcAGLaW6UZh_pUwZ-QoN5BM64L8fWCU3KHh5jVa9w_TMdyNBHDvGK53122nfFn9AqZg4r1qw9r0rinGnSlq3Nxw5e_v1K4qmje08iZRCq2kL2CGi2ts9V9O6ttJGmwWfUXKXh7Un9At")
            ArtistTrackItem(number = 2, title = "Maniac", views = "612M", imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCj6wkNuKBVMc4fjRA_fwcoGm4CvnudCCMPeKFKUP4Ox1ZgJnZJIfEY6EPvUERCSUD5NdHoFKCHaDwVd1Y9aqIf_ubvXPVoHM1-Cv8LsLEYW4oHII46FDA3qsdIaMA3lsXH7Beo_6iW-PL7UbyiHswpc2Jr4dq11GBG3q6BsBh76FIr1_nzQBWQOHlgPGNN_0MoJKLs3f-CXi_JCp7Yk8dLzq10jeGNeM4dtLxClQinjAPjy9rVpOWct-X486L1Szxc2bIbVHmyXiyU")
            ArtistTrackItem(number = 3, title = "Astronomy", views = "467M", imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAu9JZwekXaY2x1XnRho24uEGI9q9qNTR15Wo47mZmDHSxr_OiaD8zgh1_XD0VnBtKAY5ajoQxZsQyUnQvAIYp-U_QNI6nbfGSK-Mtennc-bgZ2D6ZJnwnVgVVkwIxoS-6Ydcvrd2xFROZrkpwEQkk0EPLMeu21Y1tMlHVVQ69zD6RCe4X3uuj-mX34Ifiybaz9fVxB_G4ZLdX95xNyJIzrkH5QTzVVPzveCDDkzuAob6vaek50AgY8l9Wa3cDI1Dp6YGxUeW9eQfDw")
            ArtistTrackItem(number = 4, title = "People Watching", views = "312M", imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuADwb9Q_nwDvBwc0R6VXsfLxR_ErOe-b6zEjmO43j6CfyLEuDLu-OqdzMQUsgzWMm-BEAll6KEQrH6QG5zr-PlAyMo5-IEGieM694vfF8DklsHLbtXRvQRu3CwCHu-sGnUE6bmpsHerSGBs5v0rYUTa-uQS29k-MA1Bdmyguv8Vwf8ky8zr5J6bRPKNpMPQGSuEwUnKR16HlI7JFd_XclyoYEYNIGRZ8_kasjojHQz3gwILns4myQOIv3WWsKsuBGlEg3EvHQTUtqz7")
            ArtistTrackItem(number = 5, title = "Disaster", views = "243M", imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuApv5LSkCOk3oZgFhDrRwGbteTyogQsaIVgwYUy0qHaoo6V9DrnpkYWVF1EW7V4uaSWne31Ll94VBTHQt52ym4mJosKgGkj9M1wlKyRsFZvEdFaRjTMKPiBhwiY2MKBeywl02CtLKfqqA5Jj28BdF-fjs8kRKHK6gDaAkf9dzu_wWZQ5Jd27gevc5CjnUTufdkHZNcJswgHVVwez4CU2b_eU8sy24KjPTD3rEEmUUP3NmLdMQSh42B52mV6PltCP1sReoRl6rjpcuRj")
        }"""
new_pop_list = """        if (tracks.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.7f))
                    .padding(8.dp)
            ) {
                tracks.take(5).forEachIndexed { index, track ->
                    ArtistTrackItem(
                        number = index + 1, 
                        title = track.title, 
                        subtitle = if (track.playCount > 0) "${track.playCount} plays" else "", 
                        imageUrl = track.albumId?.let { "content://media/external/audio/albumart/$it" } ?: "",
                        onClick = { viewModel.setCurrentlyPlaying(track, tracks) }
                    )
                }
            }
        }"""
content = content.replace(old_pop_list, new_pop_list)

# Update ArtistTrackItem
old_track_sig = """@Composable
fun ArtistTrackItem(number: Int, title: String, views: String, imageUrl: String) {"""
new_track_sig = """@Composable
fun ArtistTrackItem(number: Int, title: String, subtitle: String, imageUrl: String, onClick: () -> Unit = {}) {"""
content = content.replace(old_track_sig, new_track_sig)

content = content.replace('.clickable { }', '.clickable { onClick() }')

old_views = """            Text(
                text = views,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )"""
new_views = """            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }"""
content = content.replace(old_views, new_views)

# Update ArtistAlbumsSection
old_album_sig = """@Composable
fun ArtistAlbumsSection() {"""
new_album_sig = """@Composable
fun ArtistAlbumsSection(albums: List<Album>, onNavigateToAlbum: (String) -> Unit) {"""
content = content.replace(old_album_sig, new_album_sig)

old_album_list = """        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ArtistAlbumItem(
                    title = "Found Heaven",
                    year = "2024 • 12 tracks",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBuyrSoRssqJJF9LR5_FNbUPMaxFDhPsneckpa5vT5ekJ_38LzWmEh3JG17oJ8bsaIOeIKax_R2pwlYUDrne_nZrhnh52X756Aj_pTMIsI6JwFapR5HOuHfLRdzhvpZlcUEyXD1e0-jdLgOf4CThQkZKX0cXMKqDXVQGhaR3QH2OMqlvjmulhI1udJ2WSjC79cGAr0bC2UPT7brQg5by_2UaxL5x-7spytO4ZatIEoJ1n3zCTBCH9NhQky-mFzn3gP0BjNz8ogr94hi"
                )
            }
            item {
                ArtistAlbumItem(
                    title = "Superache",
                    year = "2022 • 13 tracks",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB2SH3F5fc-JPdCaMU_ISLZUig0m-mj-cU41nZLTF4neCAyrba2Mn_evPlXckBmW-qDk5yRPsPDLT5ttsNIpOEFg4ZFevRrOqznic3BrGSe3y-cMPe5Dryfhw2bXVpqLDmHlkKWTLtjTviGYjk96TkHJBMKpdH2nNo73r6m2hGeyQJ1Mfi0IE1hTuFGe47MNOuzwa5yK-KuoFAWWsvuXhJAUGR68RKRsuBNfZ3l7Oq8XaqYR5BiT5-pnk67UkiEa6MTWYMbJoWy33Uc"
                )
            }
            item {
                ArtistAlbumItem(
                    title = "Kid Krow",
                    year = "2020 • 12 tracks",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDaN-fZq_PfcmUjHLX1WWrWIwl2OAu1vzPX_fhu5WbE07GpfquGwlymss1Obpt95UMnCOyNKswp0vMKD1Tgm-OZchjB0gYFI9-oiJL2IAuKB29FZCkS_wYEZt2qYe_lXYqJl0uKGGY-cOFk7Hi7RtnrKQdoosJosMO7DmPWLAQwHTrm4jZJsw1QVNN7hvCJTF4mj876MkSSKirAnr_IVhRtwu9daMU63PbnZLAxCicpxIdRmqN9pfKzoSMYqfMnQOC9AOB9qS3RhA3b"
                )
            }
        }"""
new_album_list = """        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(albums.size) { index ->
                val album = albums[index]
                ArtistAlbumItem(
                    title = album.title,
                    year = "${album.trackCount} tracks",
                    imageUrl = album.artworkUri ?: "",
                    onClick = { onNavigateToAlbum(album.id) }
                )
            }
        }"""
content = content.replace(old_album_list, new_album_list)

# Update ArtistAlbumItem
old_album_item_sig = """@Composable
fun ArtistAlbumItem(title: String, year: String, imageUrl: String) {"""
new_album_item_sig = """@Composable
fun ArtistAlbumItem(title: String, year: String, imageUrl: String, onClick: () -> Unit = {}) {"""
content = content.replace(old_album_item_sig, new_album_item_sig)

content = content.replace('Column(modifier = Modifier.width(140.dp).clickable { })', 'Column(modifier = Modifier.width(140.dp).clickable { onClick() })')

# Update ArtistAboutSection
old_about_sig = """@Composable
fun ArtistAboutSection() {"""
new_about_sig = """@Composable
fun ArtistAboutSection(artist: Artist?) {"""
content = content.replace(old_about_sig, new_about_sig)

old_bio = """Conan Gray is an American singer, songwriter, and record producer. He gained prominence through his bedroom pop sound and introspective lyrics."""
new_bio = """${artist?.bioText ?: "No artist info available yet."}"""
content = content.replace(old_bio, new_bio)

with open('app/src/main/java/com/aeswox/arcmusic/ArtistDetailsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)

print("Done")
