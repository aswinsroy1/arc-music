package com.aeswox.arcmusic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.jelly
import com.aeswox.arcmusic.ui.components.*

@Composable
fun AlbumDetailsScreen(
    albumId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToArtist: (String) -> Unit = {},
    onNavigateToAlbum: (String) -> Unit = {}
) {
    val viewModel: MusicViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val album by viewModel.getAlbumById(albumId).collectAsState(initial = null)
    val tracks by viewModel.getTracksByAlbum(albumId).collectAsState(initial = emptyList())
    val sortedTracks = remember(tracks) { tracks.sortedBy { it.trackNumber } }
    val moreAlbums by viewModel.getAlbumsByArtist(album?.artist ?: "").collectAsState(initial = emptyList())
    val filteredMoreAlbums = remember(moreAlbums, albumId) { moreAlbums.filter { it.id != albumId } }
    
    val currentlyPlaying by viewModel.currentlyPlaying.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    if (album == null) {
        AlbumDetailsSkeleton(onNavigateBack = onNavigateBack)
    } else {
        LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .physicsBounceOverscroll()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 48.dp, bottom = 120.dp)
    ) {
        item {
            AlbumDetailsHeader(onNavigateBack = onNavigateBack)
        }
        item {
            AlbumDetailsInfo(
                album = album,
                tracks = sortedTracks,
                onPlay = { viewModel.setCurrentlyPlaying(sortedTracks.firstOrNull(), sortedTracks) },
                onShuffle = { 
                    val shuffled = sortedTracks.shuffled()
                    viewModel.setCurrentlyPlaying(shuffled.firstOrNull(), shuffled) 
                },
                onNavigateToArtist = onNavigateToArtist
            )
        }
        item {
            AlbumTracksList(
                tracks = sortedTracks,
                currentlyPlaying = currentlyPlaying,
                isPlaying = isPlaying,
                onTrackClick = { track -> viewModel.setCurrentlyPlaying(track, sortedTracks) }
            )
        }
        if (filteredMoreAlbums.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
                MoreByArtistSection(
                    artistName = album?.artist ?: "",
                    albums = filteredMoreAlbums,
                    onNavigateToArtist = onNavigateToArtist,
                    onNavigateToAlbum = onNavigateToAlbum
                )
            }
        }
    }
    }
}

@Composable
fun AlbumDetailsHeader(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        JellyIconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            JellyIconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun AlbumDetailsInfo(
    album: com.aeswox.arcmusic.db.entities.Album?,
    tracks: List<com.aeswox.arcmusic.db.entities.Track>,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onNavigateToArtist: (String) -> Unit
) {
    val totalDurationMs = tracks.sumOf { it.durationMs }
    val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(totalDurationMs)
    val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(totalDurationMs) % 60
    val durationText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

    val genreText = tracks.firstOrNull()?.genre?.takeIf { it.isNotBlank() } ?: "Unknown"
    val yearText = album?.year?.takeIf { it > 0 }?.toString() ?: ""
    val subtitleText = listOf(genreText, yearText).filter { it.isNotBlank() }.joinToString(" • ")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        AsyncImage(
            model = album?.artworkUri,
            contentDescription = "Album Cover",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(36.dp))
        )
        Spacer(modifier = Modifier.height(32.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = album?.title ?: "Unknown Album",
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .jellyClick { onNavigateToArtist(album?.artist ?: "") }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = album?.artist ?: "Unknown Artist",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${tracks.size} songs",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = durationText,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JellyButton(
                    onClick = onPlay,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Play",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                JellyButton(
                    onClick = onShuffle,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Shuffle",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AlbumTracksList(
    tracks: List<com.aeswox.arcmusic.db.entities.Track>,
    currentlyPlaying: com.aeswox.arcmusic.db.entities.Track?,
    isPlaying: Boolean,
    onTrackClick: (com.aeswox.arcmusic.db.entities.Track) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayTracks = if (expanded || tracks.size <= 6) tracks else tracks.take(5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f))
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "TRACKS",
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)
        )
        
        displayTracks.forEachIndexed { index, track ->
            val isCurrentTrack = currentlyPlaying?.id == track.id
            val mins = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(track.durationMs)
            val secs = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(track.durationMs) % 60
            val durString = String.format("%d:%02d", mins, secs)
            
            AlbumTrackItem(
                number = (index + 1).toString(),
                title = track.title,
                duration = durString,
                isPlaying = isCurrentTrack && isPlaying,
                isExplicit = false,
                onClick = { onTrackClick(track) }
            )
        }
        
        if (!expanded && tracks.size > 6) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .jellyClick { expanded = true }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Show more",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AlbumTrackItem(
    number: String,
    title: String,
    duration: String,
    isPlaying: Boolean = false,
    isExplicit: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .jellyClick { onClick() }
            .background(if (isPlaying) MaterialTheme.colorScheme.surfaceContainer else Color.Transparent)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(32.dp), contentAlignment = Alignment.Center) {
            if (isPlaying) {
                // Playing animation placeholder
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.height(16.dp)) {
                    Box(modifier = Modifier.width(3.dp).height(8.dp).background(MaterialTheme.colorScheme.primary))
                    Box(modifier = Modifier.width(3.dp).height(14.dp).background(MaterialTheme.colorScheme.primary))
                    Box(modifier = Modifier.width(3.dp).height(10.dp).background(MaterialTheme.colorScheme.primary))
                }
            } else {
                Text(
                    text = number,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium),
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isExplicit) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "E",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Text(
            text = duration,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Options",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun MoreByArtistSection(
    artistName: String,
    albums: List<com.aeswox.arcmusic.db.entities.Album>,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToAlbum: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "More by $artistName",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "See all",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.jellyClick { onNavigateToArtist(artistName) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            modifier = Modifier.physicsBounceOverscroll(isHorizontal = true),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(albums) { album ->
                AlbumResultItem(
                    title = album.title, 
                    year = "${album.trackCount} tracks", 
                    imageUrl = album.artworkUri ?: "",
                    onClick = { onNavigateToAlbum(album.id) }
                )
            }
        }
    }
}

@Composable
fun AlbumDetailsSkeleton(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 120.dp)
    ) {
        AlbumDetailsHeader(onNavigateBack = onNavigateBack)
        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .shimmerLoading()
            )
            Spacer(modifier = Modifier.height(32.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmerLoading()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .shimmerLoading()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .shimmerLoading()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .shimmerLoading()
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .shimmerLoading()
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .shimmerLoading()
                    )
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .shimmerLoading()
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        repeat(5) {
            TrackListItemSkeleton(
                modifier = Modifier.padding(horizontal = 0.dp),
                showCover = false, 
                showTrackNumber = true
            )
        }
    }
}
