package com.aeswox.arcmusic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aeswox.arcmusic.db.entities.Track

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionHealthScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMissingContent: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel = hiltViewModel(),
    glowIntensity: Float = 0.6f
) {
    val healthState by viewModel.healthState.collectAsState()
    var showDuplicateSheet by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Collection Health", 
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO */ }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                item {
                    CollectionHealthScoreSection(healthState.healthScore)
                }
                
                item {
                    CollectionHealthBreakdownSection(healthState)
                }
                
                item {
                    CollectionHealthGapsSection(
                        missingTracks = healthState.missingTracksFromOwnedArtists,
                        missingAlbums = healthState.missingAlbumsFromOwnedArtists,
                        favoritedArtistsCount = healthState.favoritedArtistsCount,
                        onNavigateToMissingContent = onNavigateToMissingContent
                    )
                }
                
                if (healthState.duplicateGroups.isNotEmpty()) {
                    item {
                        CollectionHealthDuplicatesCard(
                            groupCount = healthState.duplicateGroups.size,
                            onReviewClick = { showDuplicateSheet = true }
                        )
                    }
                }
            }
        }
    }
    
    if (showDuplicateSheet) {
        DuplicateReviewSheet(
            duplicateGroups = healthState.duplicateGroups,
            onDismiss = { showDuplicateSheet = false },
            onDeleteTracks = { tracksToDelete ->
                viewModel.deleteTracks(tracksToDelete)
            }
        )
    }
}

@Composable
fun CollectionHealthScoreSection(score: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            CircularProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.onSurface,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${score}%",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 64.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "HEALTH SCORE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = if (score == 100) "Your collection is perfect!" else "Your collection is in great shape. Some metadata updates could improve your experience.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(280.dp)
        )
    }
}

@Composable
fun CollectionHealthBreakdownSection(state: CollectionHealthState) {
    GlassCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            HealthBreakdownItem(
                icon = if (state.missingArtworkCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                iconTint = if (state.missingArtworkCount > 0) Color(0xFFE53935) else Color(0xFF43A047),
                iconBg = if (state.missingArtworkCount > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                title = "Missing artwork",
                subtitle = if (state.missingArtworkCount > 0) "${state.missingArtworkCount} songs missing artwork" else "All songs have artwork"
            )
            HealthBreakdownItem(
                icon = Icons.Default.Info,
                iconTint = Color(0xFFFDD835),
                iconBg = Color(0xFFFFF9C4),
                title = "Missing lyrics",
                subtitle = "Not yet available"
            )
            HealthBreakdownItem(
                icon = if (state.missingMetadataCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                iconTint = if (state.missingMetadataCount > 0) Color(0xFFFDD835) else Color(0xFF43A047),
                iconBg = if (state.missingMetadataCount > 0) Color(0xFFFFF9C4) else Color(0xFFE8F5E9),
                title = "Missing metadata",
                subtitle = if (state.missingMetadataCount > 0) "${state.missingMetadataCount} items with incomplete tags" else "All tags complete"
            )
            HealthBreakdownItem(
                icon = if (state.duplicateGroups.isNotEmpty()) Icons.Default.FilterNone else Icons.Default.CheckCircle,
                iconTint = if (state.duplicateGroups.isNotEmpty()) Color(0xFF1E88E5) else Color(0xFF43A047),
                iconBg = if (state.duplicateGroups.isNotEmpty()) Color(0xFFE3F2FD) else Color(0xFFE8F5E9),
                title = "Duplicate songs",
                subtitle = if (state.duplicateGroups.isNotEmpty()) "${state.duplicateGroups.size} duplicate groups found" else "No duplicates found"
            )
            HealthBreakdownItem(
                icon = if (state.corruptedTagsCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                iconTint = if (state.corruptedTagsCount > 0) Color(0xFFE53935) else Color(0xFF43A047),
                iconBg = if (state.corruptedTagsCount > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                title = "Corrupted tags",
                subtitle = if (state.corruptedTagsCount > 0) "${state.corruptedTagsCount} items with unreadable data" else "No corrupted tags found"
            )
            HealthBreakdownItem(
                icon = if (state.lowQualityCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                iconTint = if (state.lowQualityCount > 0) Color(0xFFE53935) else Color(0xFF43A047),
                iconBg = if (state.lowQualityCount > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                title = "Low quality files",
                subtitle = if (state.lowQualityCount > 0) "${state.lowQualityCount} tracks below 192kbps" else "All tracks high quality"
            )
        }
    }
}

@Composable
fun HealthBreakdownItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun CollectionHealthGapsSection(
    missingTracks: Int, 
    missingAlbums: Int, 
    favoritedArtistsCount: Int,
    onNavigateToMissingContent: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Cloud,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Requires internet",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (favoritedArtistsCount > 0) {
                GapCard(
                    count = missingTracks.toString(),
                    label = "Missing tracks",
                    subLabel = "From favorite artists",
                    onClick = onNavigateToMissingContent,
                    modifier = Modifier.weight(1f)
                )
                GapCard(
                    count = missingAlbums.toString(),
                    label = "Missing albums",
                    subLabel = "From favorite artists",
                    onClick = onNavigateToMissingContent,
                    modifier = Modifier.weight(1f)
                )
            } else {
                GapCard(
                    count = "-",
                    label = "Missing tracks",
                    subLabel = "Favorite artists to track gaps",
                    modifier = Modifier.weight(1f)
                )
                GapCard(
                    count = "-",
                    label = "Missing albums",
                    subLabel = "Favorite artists to track gaps",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun GapCard(
    count: String,
    label: String,
    subLabel: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = count,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun CollectionHealthDuplicatesCard(groupCount: Int, onReviewClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CloudDownload,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Refine your library",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We found $groupCount duplicate groups. Review them to free up space.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(280.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onReviewClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(
                "Review & Clean Up",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateReviewSheet(
    duplicateGroups: List<DuplicateGroup>,
    onDismiss: () -> Unit,
    onDeleteTracks: (List<String>) -> Unit
) {
    val selectedTracksToDelete = remember { mutableStateListOf<String>() }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Review Duplicates",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(24.dp)
            )
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(duplicateGroups) { group ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "${group.title} • ${group.artist}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        group.tracks.forEach { track ->
                            val isSelected = selectedTracksToDelete.contains(track.id)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (isSelected) {
                                            selectedTracksToDelete.remove(track.id)
                                        } else {
                                            selectedTracksToDelete.add(track.id)
                                        }
                                    }
                                    .padding(8.dp)
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${(track.bitrate ?: 0) / 1000}kbps • ${track.codec ?: "Unknown"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = track.filePath.substringAfterLast('/'),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isSelected) 0.3f else 0.7f),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        
                        // Suggestion: Select all but highest bitrate
                        Button(
                            onClick = {
                                val highestQuality = group.tracks.maxByOrNull { it.bitrate ?: 0 }
                                group.tracks.forEach { track ->
                                    if (track.id != highestQuality?.id && !selectedTracksToDelete.contains(track.id)) {
                                        selectedTracksToDelete.add(track.id)
                                    } else if (track.id == highestQuality?.id) {
                                        selectedTracksToDelete.remove(track.id)
                                    }
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Keep best quality")
                        }
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp)
            ) {
                Button(
                    onClick = {
                        if (selectedTracksToDelete.isNotEmpty()) {
                            onDeleteTracks(selectedTracksToDelete)
                        }
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (selectedTracksToDelete.isNotEmpty()) "Delete ${selectedTracksToDelete.size} items" else "Done",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
