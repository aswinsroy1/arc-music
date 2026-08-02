package com.aeswox.arcmusic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aeswox.arcmusic.db.entities.Playlist
import com.aeswox.arcmusic.db.entities.Track

@Composable
fun PlaylistDetailsScreen(
    playlistId: String,
    onNavigateBack: () -> Unit,
    viewModel: MusicViewModel = hiltViewModel()
) {
    val playlist by viewModel.getPlaylist(playlistId).collectAsState(initial = null)
    val tracks by viewModel.getTracksForPlaylist(playlistId).collectAsState(initial = emptyList())
    val currentlyPlaying by viewModel.currentlyPlaying.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    val totalDurationMs = tracks.sumOf { it.durationMs }
    val totalHours = totalDurationMs / (1000 * 60 * 60)
    val totalMinutes = (totalDurationMs % (1000 * 60 * 60)) / (1000 * 60)
    val durationText = if (totalHours > 0) "${totalHours}h ${totalMinutes}m" else "${totalMinutes}m"

    val firstTrackWithArt = tracks.firstOrNull { it.albumId != null }
    val coverUrl = playlist?.coverArtUri
        ?: firstTrackWithArt?.albumId?.let { "content://media/external/audio/albumart/$it" }
        ?: "https://lh3.googleusercontent.com/aida-public/AB6AXuDK2gSPmhFiKqcqPLlCJlIp7lxpTt2scS9SuOmzxmZKXa1UQIjSKITZh8tGxaLLsMWtK_rqugpIF6kWjdqifIFpbIHQ51KFkHHGCwprGn7T1jWwAFiUiOgft22mJtHc311emev_Y9qChhO44k-VwJC7dvX80Zs-JHFurqrp7BRfflgHO2uz-vspGyR9BoWhQUaXuELDgddlmK__JFlAjdrkjKUgyxH0SVRHhhE0iqWq7lQMTieDIl6s1Oh1frE5nhxruwt9dXwi3SRK" // Fallback

    Box(modifier = Modifier.fillMaxSize()) {
        // Ambient glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFA3DEFE).copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp) // space for mini player
        ) {
            item {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIconButton(
                        icon = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        onClick = onNavigateBack,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppIconButton(
                            icon = Icons.Default.MoreVert,
                            contentDescription = "More",
                            onClick = { },
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item {
                // Hero Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = "Playlist Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(36.dp))
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = playlist?.name ?: playlistId,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "My Playlist",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (!playlist?.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = playlist?.description ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${tracks.size} songs",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = durationText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppPrimaryButton(
                        text = "Play",
                        onClick = { 
                            if (tracks.isNotEmpty()) {
                                viewModel.setCurrentlyPlaying(tracks.first(), tracks)
                            }
                        },
                        icon = Icons.Default.PlayArrow,
                        modifier = Modifier.width(140.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    AppPrimaryButton(
                        text = "Shuffle",
                        onClick = { 
                            if (tracks.isNotEmpty()) {
                                val shuffled = tracks.shuffled()
                                viewModel.setCurrentlyPlaying(shuffled.first(), shuffled)
                            }
                        },
                        icon = Icons.Default.Shuffle,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(140.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    AppIconButton(
                        icon = Icons.Default.MoreHoriz,
                        contentDescription = "More",
                        onClick = { },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            item {
                // Tracks Container Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f))
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "TRACKS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 12.dp)
                    )
                }
            }
            
            itemsIndexed(tracks) { index, track ->
                val trackIsPlaying = currentlyPlaying?.id == track.id
                val trackDurationMins = track.durationMs / (1000 * 60)
                val trackDurationSecs = (track.durationMs % (1000 * 60)) / 1000
                val trackDurationFormatted = String.format("%d:%02d", trackDurationMins, trackDurationSecs)
                
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f))
                ) {
                    PlaylistTrackItem(
                        number = (index + 1).toString(),
                        title = track.title,
                        artist = track.artist,
                        duration = trackDurationFormatted,
                        isPlaying = trackIsPlaying,
                        onClick = {
                            viewModel.setCurrentlyPlaying(track, tracks)
                        }
                    )
                }
            }
            
            item {
                // Tracks Container Footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f))
                )
            }
        }
    }
}

@Composable
fun PlaylistTrackItem(
    number: String, 
    title: String, 
    artist: String, 
    duration: String, 
    isPlaying: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isPlaying) {
            Icon(
                imageVector = Icons.Default.Equalizer,
                contentDescription = "Playing",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
        } else {
            Text(
                text = number,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.width(24.dp).padding(end = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = artist,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Text(
            text = duration,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}
