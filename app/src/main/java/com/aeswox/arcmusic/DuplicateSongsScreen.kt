package com.aeswox.arcmusic

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aeswox.arcmusic.db.entities.Track
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
import com.aeswox.arcmusic.ui.components.JellyButton
import com.aeswox.arcmusic.ui.components.JellyExtendedFloatingActionButton
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateSongsScreen(
    viewModel: MusicViewModel,
    onNavigateBack: () -> Unit
) {
    val healthState by viewModel.healthState.collectAsState()
    val duplicateGroups = healthState.duplicateGroups
    val isMiniPlayerVisible by viewModel.isMiniPlayerVisible.collectAsState()
    val currentlyPlaying by viewModel.currentlyPlaying.collectAsState()
    val hasMiniPlayer = isMiniPlayerVisible && currentlyPlaying != null

    val fabBottomPadding by animateDpAsState(
        targetValue = if (hasMiniPlayer) 112.dp else 16.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "duplicateFabBottom"
    )

    val listBottomSpacer by animateDpAsState(
        targetValue = if (hasMiniPlayer) 190.dp else 90.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "duplicateListBottom"
    )

    // Map of group id to the track ID that is selected to be KEPT
    val selectedTracksToKeep = remember { mutableStateMapOf<String, String>() }
    
    var showBatchDeleteDialog by remember { mutableStateOf(false) }
    var pendingTracksToDelete by remember { mutableStateOf<List<String>>(emptyList()) }

    var totalDuplicatesCount by remember { mutableStateOf(0) }
    var totalSavedMb by remember { mutableStateOf(0.0) }

    // Initialize default selections (best track per group)
    LaunchedEffect(duplicateGroups) {
        duplicateGroups.forEach { group ->
            val groupId = group.id
            if (!selectedTracksToKeep.containsKey(groupId)) {
                // Find the best track by file size and bitrate as a simple heuristic
                val bestTrack = group.tracks.maxByOrNull { (it.bitrate ?: 0) * 1000 + (it.fileSizeBytes) } ?: group.tracks.first()
                selectedTracksToKeep[groupId] = bestTrack.id
            }
        }
    }

    LaunchedEffect(duplicateGroups, selectedTracksToKeep.toMap()) {
        var count = 0
        var savedSize = 0L
        duplicateGroups.forEach { group ->
            val keepId = selectedTracksToKeep[group.id]
            group.tracks.forEach { track ->
                if (track.id != keepId) {
                    count++
                    savedSize += track.fileSizeBytes
                }
            }
        }
        totalDuplicatesCount = count
        totalSavedMb = savedSize / (1024.0 * 1024.0)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Duplicate Songs",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .jellyClick { onNavigateBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .jellyClick { /* TODO */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent,
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                if (duplicateGroups.isNotEmpty() && totalDuplicatesCount > 0) {
                    val savedMbStr = String.format(Locale.getDefault(), "%.1f", totalSavedMb)
                    JellyExtendedFloatingActionButton(
                        onClick = {
                            val tracksToDelete = mutableListOf<String>()
                            duplicateGroups.forEach { group ->
                                val groupId = group.id
                                val keepId = selectedTracksToKeep[groupId]
                                group.tracks.forEach { track ->
                                    if (track.id != keepId) {
                                        tracksToDelete.add(track.id)
                                    }
                                }
                            }
                            if (tracksToDelete.isNotEmpty()) {
                                pendingTracksToDelete = tracksToDelete
                                showBatchDeleteDialog = true
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier.padding(bottom = fabBottomPadding).fillMaxWidth(0.9f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE57373))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete $totalDuplicatesCount Duplicates", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.background)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("• Saves $savedMbStr MB", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        ) { innerPadding ->
            if (duplicateGroups.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.Surface(
                        modifier = Modifier.size(160.dp),
                        shape = RoundedCornerShape(32.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 16.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LayersClear,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = "Your library is\nspotless",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            lineHeight = 36.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No duplicate tracks were found in your collection. Everything is perfectly organized.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    JellyButton(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    ) {
                        Text("Back to Health", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.physicsBounceOverscroll()
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Library Optimization Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(32.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "Library Optimization",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "We identified identical audio titles in multiple qualities. Keep the lossless version and reclaim storage.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Green dot pill
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.onBackground)
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$totalDuplicatesCount duplicates found",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.background
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                // Reclaims size pill
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.InsertDriveFile,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val savedMbStr = String.format(Locale.getDefault(), "%.1f", totalSavedMb)
                                    Text(
                                        text = "Reclaims $savedMbStr MB",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    items(duplicateGroups) { group ->
                        val groupId = group.id
                        val bestTrack = remember(group) {
                            group.tracks.maxByOrNull { (it.bitrate ?: 0) * 1000 + (it.fileSizeBytes) } ?: group.tracks.first()
                        }
                        
                        DuplicateGroupCard(
                            group = group,
                            bestTrackId = bestTrack.id,
                            selectedKeepId = selectedTracksToKeep[groupId] ?: bestTrack.id,
                            onSelectKeep = { selectedId ->
                                selectedTracksToKeep[groupId] = selectedId
                            },
                            onDeleteIndividual = { trackId ->
                                pendingTracksToDelete = listOf(trackId)
                                showBatchDeleteDialog = true
                            },
                            onPlayPreview = { track ->
                                viewModel.setCurrentlyPlaying(track, group.tracks)
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(listBottomSpacer))
                    }
                }
            }
        }
        
        if (showBatchDeleteDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showBatchDeleteDialog = false 
                    pendingTracksToDelete = emptyList()
                },
                title = {
                    Text(text = "Delete Duplicates")
                },
                text = {
                    Text("Are you sure you want to delete ${pendingTracksToDelete.size} track(s)? This will remove them from your library.")
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteTracks(pendingTracksToDelete)
                        showBatchDeleteDialog = false
                        pendingTracksToDelete = emptyList()
                        onNavigateBack()
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showBatchDeleteDialog = false 
                        pendingTracksToDelete = emptyList()
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun DuplicateGroupCard(
    group: DuplicateGroup,
    bestTrackId: String,
    selectedKeepId: String,
    onSelectKeep: (String) -> Unit,
    onDeleteIndividual: (String) -> Unit,
    onPlayPreview: (Track) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = group.tracks.firstOrNull()?.let { "${it.artist} • ${it.album}" } ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${group.tracks.size} files",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Tracks
        group.tracks.forEachIndexed { index, track ->
            val isSelected = track.id == selectedKeepId
            
            val containerModifier = if (isSelected) {
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.onBackground)
                    .jellyClick { onSelectKeep(track.id) }
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            } else {
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .jellyClick { onSelectKeep(track.id) }
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            }

            Row(
                modifier = containerModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Radio Button Custom
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.background)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = getCodecFriendlyName(track),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        val isLossless = track.codec?.lowercase() in listOf("flac", "alac", "wav")
                        if (isLossless) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onBackground)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "BEST QUALITY",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                    color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.background
                                )
                            }
                        } else if (track.codec?.lowercase()?.contains("eac3") == true || track.codec?.lowercase()?.contains("ac3") == true) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "ATMOS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                    color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val sampleRateStr = track.sampleRate?.let { "${it / 1000.0} kHz" } ?: ""
                    val bitDepthStr = track.bitDepth?.takeIf { it > 16 }?.let { "${it}-bit" } ?: ""
                    val sizeMb = track.fileSizeBytes / (1024.0 * 1024.0)
                    val sizeStr = String.format(Locale.getDefault(), "%.1f MB", sizeMb)
                    
                    val details = listOf(
                        bitDepthStr,
                        sampleRateStr,
                        sizeStr
                    ).filter { it.isNotEmpty() }.joinToString(" • ")
                    
                    Text(
                        text = details,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) MaterialTheme.colorScheme.background.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))

                // Play Button Custom
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface)
                        .jellyClick { onPlayPreview(track) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Preview Track",
                        tint = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                if (!isSelected) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE57373),
                        modifier = Modifier
                            .size(24.dp)
                            .jellyClick { onDeleteIndividual(track.id) }
                    )
                }
            }
            if (index < group.tracks.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

fun getCodecFriendlyName(track: Track): String {
    val codec = track.codec?.lowercase() ?: return "Unknown"
    return when {
        codec.contains("flac") -> "Lossless FLAC"
        codec.contains("alac") -> "Lossless ALAC"
        codec.contains("eac3") && codec.contains("joc") -> "Spatial EAC3-JOC"
        codec.contains("eac3") -> "Dolby Digital Plus (E-AC-3)"
        codec.contains("aac") -> "AAC Low Bitrate"
        codec.contains("mp3") -> "MP3 Audio"
        codec.contains("wav") -> "Lossless WAV"
        else -> codec.uppercase(Locale.getDefault())
    }
}
