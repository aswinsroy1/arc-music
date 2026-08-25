package com.aeswox.arcmusic

import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
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
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.jelly
import com.aeswox.arcmusic.ui.components.JellyIconButton
import com.aeswox.arcmusic.ui.components.JellyFilledIconButton
import com.aeswox.arcmusic.ui.components.JellyFilledTonalIconButton
import com.aeswox.arcmusic.ui.components.JellyOutlinedIconButton

@Composable
fun PlaylistDetailsScreen(
    playlistId: String,
    onNavigateBack: () -> Unit,
    onNavigateToShare: (String, String) -> Unit = { _, _ -> },
    viewModel: MusicViewModel = hiltViewModel()
) {
    val playlist by viewModel.getPlaylist(playlistId).collectAsState(initial = null)
    val tracks by viewModel.getTracksForPlaylist(playlistId).collectAsState(initial = emptyList())
    val currentlyPlaying by viewModel.currentlyPlaying.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (playlist == null) {
        PlaylistDetailsSkeleton(onNavigateBack = onNavigateBack)
        return
    }

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
            modifier = Modifier.physicsBounceOverscroll().fillMaxSize(),
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
                        Box {
                            AppIconButton(
                                icon = Icons.Default.MoreVert,
                                contentDescription = "More",
                                onClick = { showMenu = true },
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier
                                    .width(220.dp)
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                                    .padding(8.dp),
                                shape = RoundedCornerShape(32.dp),
                                shadowElevation = 16.dp
                            ) {
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            "Edit playlist",
                                            style = MaterialTheme.typography.bodyLarge
                                        ) 
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        showEditDialog = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp)),
                                    colors = MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            "Share playlist",
                                            style = MaterialTheme.typography.bodyLarge
                                        ) 
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.IosShare,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onNavigateToShare("playlist", playlistId)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp)),
                                    colors = MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            "Delete playlist",
                                            style = MaterialTheme.typography.bodyLarge
                                        ) 
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        showDeleteConfirmDialog = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp)),
                                    colors = MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.error
                                    )
                                )
                            }
                        }
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

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Playlist") },
            text = { Text("Are you sure you want to delete '${playlist?.name}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog = false
                    viewModel.deletePlaylists(listOf(playlistId))
                    onNavigateBack()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    if (showEditDialog && playlist != null) {
        EditPlaylistDialog(
            playlist = playlist!!,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newDescription, newCoverUri ->
                viewModel.updatePlaylist(playlistId, newName, newDescription, newCoverUri) {
                    showEditDialog = false
                    if (newName != playlistId) {
                        onNavigateBack()
                    }
                }
            }
        )
    }
}

@Composable
fun EditPlaylistDialog(
    playlist: Playlist,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String?, coverUri: String?) -> Unit
) {
    var name by remember { mutableStateOf(playlist.name) }
    var description by remember { mutableStateOf(playlist.description ?: "") }
    var coverUri by remember { mutableStateOf(playlist.coverArtUri) }

    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                coverUri = uri.toString()
            }
        }
    )

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit Playlist",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .clickable {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (coverUri != null) {
                        AsyncImage(
                            model = coverUri,
                            contentDescription = "Cover preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { coverUri = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove cover",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Choose Image",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(name, description.takeIf { it.isNotBlank() }, coverUri) },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
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
            .jellyClick(onClick = onClick)
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

@Composable
fun PlaylistDetailsSkeleton(onNavigateBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.physicsBounceOverscroll().fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .shimmerLoading()
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmerLoading()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .shimmerLoading()
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .shimmerLoading()
                        )
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .shimmerLoading()
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            item {
                Box(
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
            items(5) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f))
                ) {
                    TrackListItemSkeleton(showCover = false, showTrackNumber = true)
                }
            }
            item {
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
