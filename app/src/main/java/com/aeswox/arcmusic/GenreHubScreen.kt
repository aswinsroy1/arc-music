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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.jelly
import com.aeswox.arcmusic.ui.components.JellyIconButton
import com.aeswox.arcmusic.ui.components.JellyFilledIconButton
import com.aeswox.arcmusic.ui.components.JellyFilledTonalIconButton
import com.aeswox.arcmusic.ui.components.JellyOutlinedIconButton

@Composable
fun GenreHubScreenContent(
    genreName: String = "Pop",
    bottomPadding: Dp,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: MusicViewModel = hiltViewModel()
    val libraryTracks by viewModel.libraryTracks.collectAsState()
    val libraryAlbums by viewModel.libraryAlbums.collectAsState()
    val libraryArtists by viewModel.libraryArtists.collectAsState()
    
    val genreTracks = remember(genreName, libraryTracks) {
        libraryTracks.filter { it.genre?.trim().equals(genreName, ignoreCase = true) }
    }
    
    val genreAlbums = remember(genreName, genreTracks, libraryAlbums) {
        val albumNames = genreTracks.mapNotNull { it.album }.distinct()
        libraryAlbums.filter { albumNames.contains(it.title) }
    }
    
    val genreArtistNames = remember(genreName, genreTracks) {
        genreTracks.mapNotNull { it.artist }.flatMap { it.split(",").map(String::trim) }.distinct()
    }
    val genreArtists = remember(genreArtistNames, libraryArtists) {
        libraryArtists.filter { genreArtistNames.contains(it.name) }
    }

    LazyColumn(
        contentPadding = PaddingValues(top = 24.dp, bottom = bottomPadding + 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = modifier.physicsBounceOverscroll().fillMaxSize()
    ) {
        item {
            Header(
                modifier = Modifier.padding(horizontal = 24.dp),
                title = null,
                fontSize = 28.sp,
                onSettingsClick = { },
                onBackClick = onNavigateBack
            )
        }
        item {
            GenreHeroSection(genreName = genreName, modifier = Modifier.padding(horizontal = 24.dp))
        }
        if (genreTracks.isNotEmpty()) {
            item {
                GenreTopTracksSection(tracks = genreTracks.take(10), modifier = Modifier.padding(horizontal = 24.dp))
            }
        }
        if (genreAlbums.isNotEmpty()) {
            item {
                GenreEssentialAlbumsSection(albums = genreAlbums.take(10))
            }
        }
        if (genreArtists.isNotEmpty()) {
            item {
                GenreFeaturedArtistsSection(artists = genreArtists.take(10))
            }
        }
    }
}

@Composable
fun GenreHeroSection(genreName: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFFF6B6B).copy(alpha = 0.15f), Color.Transparent),
                    radius = 800f,
                    center = androidx.compose.ui.geometry.Offset(800f, 0f)
                )
            )
            .padding(vertical = 32.dp, horizontal = 16.dp)
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
                onClick = {},
                icon = Icons.Default.Shuffle
            )
        }
    }
}

@Composable
fun GenreTopTracksSection(tracks: List<com.aeswox.arcmusic.db.entities.Track>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Top Tracks",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "View All",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.jellyClick { }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        val tracksUi = tracks.map { Triple(it.title, it.artist, String.format("%d:%02d", (it.durationMs / 60000), (it.durationMs % 60000) / 1000)) }
        val images = tracks.map { it.artworkUri ?: it.albumId?.let { albumId -> "content://media/external/audio/albumart/$albumId" } ?: "" }
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tracksUi.forEachIndexed { index, track ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .jellyClick { }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = images[index],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.first,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = track.second,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = track.third,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        JellyIconButton(onClick = { }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.MoreHoriz, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GenreEssentialAlbumsSection(albums: List<com.aeswox.arcmusic.db.entities.Album>) {
    Column {
        Text(
            text = "Essential Albums",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        val albumsUi = albums.map { Triple(it.title, it.artist ?: "Unknown Artist", it.artworkUri ?: "") }
        
        LazyRow(
modifier = Modifier.physicsBounceOverscroll(isHorizontal = true),

            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(albumsUi.size) { index ->
                Column(modifier = Modifier.width(176.dp).jellyClick { }) {
                    AsyncImage(
                        model = albumsUi[index].third,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(176.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = albumsUi[index].first,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = albumsUi[index].second,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun GenreFeaturedArtistsSection(artists: List<com.aeswox.arcmusic.db.entities.Artist>) {
    Column {
        Text(
            text = "Featured Artists",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        val artistsUi = artists.map { Pair(it.name, it.photoUri ?: "") }
        
        LazyRow(
modifier = Modifier.physicsBounceOverscroll(isHorizontal = true),

            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(artistsUi.size) { index ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(112.dp).jellyClick { }) {
                    AsyncImage(
                        model = artistsUi[index].second,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = artistsUi[index].first,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
