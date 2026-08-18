package com.aeswox.arcmusic

import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.jelly
import com.aeswox.arcmusic.ui.components.JellyIconButton
import com.aeswox.arcmusic.ui.components.JellyFilledIconButton
import com.aeswox.arcmusic.ui.components.JellyFilledTonalIconButton
import com.aeswox.arcmusic.ui.components.JellyOutlinedIconButton

@Composable
fun ArtistTracksScreen(
    artistId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: MusicViewModel = hiltViewModel()
) {
    val tracks by viewModel.getTracksByArtist(artistId).collectAsState(initial = emptyList())
    val artist by viewModel.getArtistById(artistId).collectAsState(initial = null)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JellyIconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.8f))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "${artist?.name ?: "Artist"} - Tracks",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        LazyColumn(
            modifier = Modifier.physicsBounceOverscroll().fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tracks.size) { index ->
                val track = tracks[index]
                ArtistTrackItem(
                    number = index + 1,
                    title = track.title,
                    subtitle = if (track.playCount > 0) "${track.playCount} plays" else "",
                    imageUrl = track.albumId?.let { "content://media/external/audio/albumart/$it" } ?: "",
                    onClick = { viewModel.setCurrentlyPlaying(track, tracks) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}

@Composable
fun ArtistAlbumsScreen(
    artistId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToAlbum: (String) -> Unit = {},
    viewModel: MusicViewModel = hiltViewModel()
) {
    val albums by viewModel.getAlbumsByArtist(artistId).collectAsState(initial = emptyList())
    val artist by viewModel.getArtistById(artistId).collectAsState(initial = null)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JellyIconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.8f))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "${artist?.name ?: "Artist"} - Albums",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(140.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.physicsBounceOverscroll().fillMaxSize()
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
            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}
