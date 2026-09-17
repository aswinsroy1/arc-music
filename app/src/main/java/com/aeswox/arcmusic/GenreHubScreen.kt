package com.aeswox.arcmusic

import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.aeswox.arcmusic.ui.components.HugeIcons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.jelly
import com.aeswox.arcmusic.ui.components.JellyIconButton
import com.aeswox.arcmusic.ui.components.ArtistImage
import com.aeswox.arcmusic.utils.ArtistUtils
import com.aeswox.arcmusic.db.entities.Track
import com.aeswox.arcmusic.db.entities.Album
import com.aeswox.arcmusic.db.entities.Artist

import com.aeswox.arcmusic.db.entities.getQualityBadgeResId

@Composable
fun GenreHubScreenContent(
    genreName: String = "Pop",
    bottomPadding: Dp,
    onNavigateBack: () -> Unit = {},
    onNavigateToAlbum: (String) -> Unit = {},
    onNavigateToArtist: (String) -> Unit = {},
    onSongClick: (Track, List<Track>) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val viewModel: MusicViewModel = hiltViewModel()
    val libraryTracks by viewModel.libraryTracks.collectAsState()
    val libraryAlbums by viewModel.libraryAlbums.collectAsState()
    val libraryArtists by viewModel.libraryArtists.collectAsState()
    val currentlyPlaying by viewModel.currentlyPlaying.collectAsState()
    
    val genreTracks = remember(genreName, libraryTracks) {
        val target = genreName.trim()
        libraryTracks.filter { track ->
            val g = track.genre?.trim() ?: return@filter false
            g.split(",", "/", ";", "\\")
                .any { it.trim().equals(target, ignoreCase = true) }
        }
    }
    
    val genreAlbums = remember(genreTracks, libraryAlbums) {
        val albumTitles = genreTracks.mapNotNull { 
            it.album.trim().takeIf { a -> a.isNotEmpty() && !a.equals("Unknown Album", ignoreCase = true) }
        }.map { it.lowercase() }.toSet()
        libraryAlbums.filter { album ->
            albumTitles.contains(album.title.trim().lowercase())
        }
    }
    
    val genreArtists = remember(genreTracks, libraryArtists) {
        val artistNames = genreTracks.flatMap { 
            ArtistUtils.splitArtists(it.artist) + ArtistUtils.splitArtists(it.albumArtist)
        }.map { it.trim().lowercase() }.filter { it.isNotEmpty() && it != "unknown artist" }.toSet()
        libraryArtists.filter { artist ->
            artistNames.contains(artist.name.trim().lowercase())
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(top = 24.dp, bottom = bottomPadding + 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = modifier.physicsBounceOverscroll().fillMaxSize()
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JellyIconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        item {
            GenreHeroSection(
                genreName = genreName,
                onShuffleClick = {
                    if (genreTracks.isNotEmpty()) {
                        val shuffled = genreTracks.shuffled()
                        onSongClick(shuffled.first(), shuffled)
                    }
                },
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
        if (genreTracks.isNotEmpty()) {
            item {
                GenreTopTracksSection(
                    tracks = genreTracks.take(10),
                    allTracks = genreTracks,
                    currentPlayingId = currentlyPlaying?.id,
                    onTrackClick = onSongClick,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
        if (genreAlbums.isNotEmpty()) {
            item {
                GenreEssentialAlbumsSection(
                    albums = genreAlbums.take(15),
                    onNavigateToAlbum = onNavigateToAlbum
                )
            }
        }
        if (genreArtists.isNotEmpty()) {
            item {
                GenreFeaturedArtistsSection(
                    artists = genreArtists.take(15),
                    onNavigateToArtist = onNavigateToArtist
                )
            }
        }
    }
}

@Composable
fun GenreHeroSection(
    genreName: String,
    onShuffleClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
            .padding(vertical = 32.dp, horizontal = 24.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "GENRE SPOTLIGHT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = genreName,
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 52.sp, lineHeight = 62.sp, letterSpacing = (-0.02).sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "The pulse of modern sounds and global melodies.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            AppPrimaryButton(
                text = "Shuffle Genre",
                onClick = onShuffleClick,
                icon = HugeIcons.Shuffle
            )
        }
    }
}

@Composable
fun GenreTopTracksSection(
    tracks: List<Track>,
    allTracks: List<Track>,
    currentPlayingId: String? = null,
    onTrackClick: (Track, List<Track>) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var showAll by remember { mutableStateOf(false) }
    val displayTracks = if (showAll) allTracks else tracks

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Top Tracks",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (allTracks.size > 10) {
                Text(
                    text = if (showAll) "Show Less" else "View All",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.jellyClick { showAll = !showAll }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            displayTracks.forEach { track ->
                val artwork = track.artworkUri ?: track.albumId?.let { albumId -> "content://media/external/audio/albumart/$albumId" } ?: ""
                SongResultItem(
                    title = track.title,
                    artist = track.artist,
                    duration = formatDuration(track.durationMs),
                    imageUrl = artwork,
                    isActive = currentPlayingId == track.id,
                    qualityBadgeResId = track.getQualityBadgeResId(),
                    isExplicit = track.isExplicit == true,
                    onClick = { onTrackClick(track, allTracks) }
                )
            }
        }
    }
}

@Composable
fun GenreEssentialAlbumsSection(
    albums: List<Album>,
    onNavigateToAlbum: (String) -> Unit = {}
) {
    Column {
        Text(
            text = "Essential Albums",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            modifier = Modifier.physicsBounceOverscroll(isHorizontal = true),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(albums.size) { index ->
                val album = albums[index]
                val fallbackImage = R.drawable.ic_default_artwork
                AlbumResultItem(
                    title = album.title,
                    year = album.artist,
                    imageUrl = album.artworkUri ?: fallbackImage,
                    modifier = Modifier.width(140.dp),
                    onClick = { onNavigateToAlbum(album.id) }
                )
            }
        }
    }
}

@Composable
fun GenreFeaturedArtistsSection(
    artists: List<Artist>,
    onNavigateToArtist: (String) -> Unit = {}
) {
    Column {
        Text(
            text = "Featured Artists",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            modifier = Modifier.physicsBounceOverscroll(isHorizontal = true),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(artists.size) { index ->
                val artist = artists[index]
                val fallbackImage = R.drawable.ic_default_artwork
                ArtistResultItem(
                    name = artist.name,
                    imageUrl = artist.photoUri ?: fallbackImage,
                    modifier = Modifier.width(100.dp),
                    onClick = { onNavigateToArtist(artist.name) }
                )
            }
        }
    }
}
