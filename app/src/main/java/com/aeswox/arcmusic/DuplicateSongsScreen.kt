package com.aeswox.arcmusic

import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
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
import androidx.compose.material.icons.filled.LayersClear
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
import java.util.Locale
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.jelly
import com.aeswox.arcmusic.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateSongsScreen(
    viewModel: MusicViewModel,
    onNavigateBack: () -> Unit
) {
    val healthState by viewModel.healthState.collectAsState()
    val duplicateGroups = healthState.duplicateGroups
    
    // Map of group Title+Artist string to the track ID that is selected to be KEPT
    val selectedTracksToKeep = remember { mutableStateMapOf<String, String>() }

    // Initialize default selections (best track per group)
    LaunchedEffect(duplicateGroups) {
        duplicateGroups.forEach { group ->
            val groupId = "${group.title}_${group.artist}"
            if (!selectedTracksToKeep.containsKey(groupId)) {
                // Find the best track by file size and bitrate as a simple heuristic
                val bestTrack = group.tracks.maxByOrNull { (it.bitrate ?: 0) * 1000 + (it.fileSizeBytes) } ?: group.tracks.first()
                selectedTracksToKeep[groupId] = bestTrack.id
            }
        }
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
                        JellyIconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
                if (duplicateGroups.isNotEmpty()) {
                    JellyExtendedFloatingActionButton(
                        onClick = {
                            val tracksToDelete = mutableListOf<String>()
                            duplicateGroups.forEach { group ->
                                val groupId = "${group.title}_${group.artist}"
                                val keepId = selectedTracksToKeep[groupId]
                                group.tracks.forEach { track ->
                                    if (track.id != keepId) {
                                        tracksToDelete.add(track.id)
                                    }
                                }
                            }
                            if (tracksToDelete.isNotEmpty()) {
                                viewModel.deleteTracks(tracksToDelete)
                            }
                            onNavigateBack()
                        },
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Selected Duplicates", fontWeight = FontWeight.Bold)
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
                        Text(
                            text = "Duplicate Songs",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "We found some matching tracks in your library. Choose which version to keep.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    items(duplicateGroups) { group ->
                        val groupId = "${group.title}_${group.artist}"
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
                                viewModel.deleteTracks(listOf(trackId))
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // padding for FAB
                    }
                }
            }
        }
    }
}

@Composable
fun DuplicateGroupCard(
    group: DuplicateGroup,
    bestTrackId: String,
    selectedKeepId: String,
    onSelectKeep: (String) -> Unit,
    onDeleteIndividual: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = group.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Tracks
        group.tracks.forEach { track ->
            val isSelected = track.id == selectedKeepId
            val isBest = track.id == bestTrackId
            
            val containerModifier = if (isSelected) {
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .jellyClick { onSelectKeep(track.id) }
                    .padding(12.dp)
            } else {
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Transparent)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .jellyClick { onSelectKeep(track.id) }
                    .padding(12.dp)
            }

            Row(
                modifier = containerModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onSelectKeep(track.id) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.onSurface,
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${track.artist} - ${track.album}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    val formatStr = track.codec?.uppercase(Locale.getDefault()) ?: "Unknown"
                    val sampleRateStr = track.sampleRate?.let { "${it / 1000.0}kHz" } ?: ""
                    val bitDepthStr = track.bitDepth?.let { "${it}bit" } ?: ""
                    val sizeMb = track.fileSizeBytes / (1024.0 * 1024.0)
                    val sizeStr = String.format(Locale.getDefault(), "%.1f MB", sizeMb)
                    
                    val details = listOf(
                        formatStr,
                        if (sampleRateStr.isNotEmpty() && bitDepthStr.isNotEmpty()) "$sampleRateStr / $bitDepthStr" else "",
                        sizeStr
                    ).filter { it.isNotEmpty() }.joinToString(" • ")
                    
                    Text(
                        text = details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                if (isBest) {
                    // Best Quality Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "BEST QUALITY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                            color = Color.White
                        )
                    }
                } else if (!isSelected) {
                    // Individual delete button
                    Row(
                        modifier = Modifier.jellyClick { onDeleteIndividual(track.id) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
