package com.aeswox.arcmusic

import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
import androidx.compose.foundation.background
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
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.jelly
import com.aeswox.arcmusic.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionHealthScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMissingContent: () -> Unit = {},
    onNavigateToMissingArtwork: () -> Unit = {},
    onNavigateToMissingLyrics: () -> Unit,
    onNavigateToMissingMetadata: () -> Unit,
    onNavigateToDuplicateSongs: () -> Unit,
    onNavigateToCorruptedTags: () -> Unit,
    onNavigateToLowQualityFiles: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel = hiltViewModel(),
    glowIntensity: Float
) {
    val healthState by viewModel.healthState.collectAsState()

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
                        JellyIconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
                modifier = Modifier.physicsBounceOverscroll()
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
                    CollectionHealthBreakdownSection(
                        state = healthState,
                        onNavigateToMissingArtwork = onNavigateToMissingArtwork,
                        onNavigateToMissingLyrics = onNavigateToMissingLyrics,
                        onNavigateToMissingMetadata = onNavigateToMissingMetadata,
                        onNavigateToDuplicateSongs = onNavigateToDuplicateSongs,
                        onNavigateToCorruptedTags = onNavigateToCorruptedTags,
                        onNavigateToLowQualityFiles = onNavigateToLowQualityFiles
                    )
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
                            onReviewClick = onNavigateToDuplicateSongs
                        )
                    }
                }
            }
        }
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
fun CollectionHealthBreakdownSection(
    state: CollectionHealthState,
    onNavigateToMissingArtwork: () -> Unit = {},
    onNavigateToMissingLyrics: () -> Unit = {},
    onNavigateToMissingMetadata: () -> Unit = {},
    onNavigateToDuplicateSongs: () -> Unit = {},
    onNavigateToCorruptedTags: () -> Unit = {},
    onNavigateToLowQualityFiles: () -> Unit = {}
) {
    GlassCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            HealthBreakdownItem(
                icon = if (state.missingArtworkCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                iconTint = if (state.missingArtworkCount > 0) Color(0xFFE53935) else Color(0xFF43A047),
                iconBg = if (state.missingArtworkCount > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                title = "Missing artwork",
                subtitle = if (state.missingArtworkCount > 0) "${state.missingArtworkCount} songs missing artwork" else "All songs have artwork",
                onClick = onNavigateToMissingArtwork
            )
            HealthBreakdownItem(
                icon = Icons.Default.Info,
                iconTint = Color(0xFFFDD835),
                iconBg = Color(0xFFFFF9C4),
                title = "Missing lyrics",
                subtitle = "Not yet available",
                onClick = onNavigateToMissingLyrics
            )
            HealthBreakdownItem(
                icon = if (state.missingMetadataCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                iconTint = if (state.missingMetadataCount > 0) Color(0xFFFDD835) else Color(0xFF43A047),
                iconBg = if (state.missingMetadataCount > 0) Color(0xFFFFF9C4) else Color(0xFFE8F5E9),
                title = "Missing metadata",
                subtitle = if (state.missingMetadataCount > 0) "${state.missingMetadataCount} items with incomplete tags" else "All tags complete",
                onClick = onNavigateToMissingMetadata
            )
            HealthBreakdownItem(
                icon = if (state.duplicateGroups.isNotEmpty()) Icons.Default.FilterNone else Icons.Default.CheckCircle,
                iconTint = if (state.duplicateGroups.isNotEmpty()) Color(0xFF1E88E5) else Color(0xFF43A047),
                iconBg = if (state.duplicateGroups.isNotEmpty()) Color(0xFFE3F2FD) else Color(0xFFE8F5E9),
                title = "Duplicate songs",
                subtitle = if (state.duplicateGroups.isNotEmpty()) "${state.duplicateGroups.size} duplicate groups found" else "No duplicates found",
                onClick = onNavigateToDuplicateSongs
            )
            HealthBreakdownItem(
                icon = if (state.corruptedTagsCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                iconTint = if (state.corruptedTagsCount > 0) Color(0xFFE53935) else Color(0xFF43A047),
                iconBg = if (state.corruptedTagsCount > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                title = "Corrupted tags",
                subtitle = if (state.corruptedTagsCount > 0) "${state.corruptedTagsCount} items with unreadable data" else "No corrupted tags found",
                onClick = onNavigateToCorruptedTags
            )
            HealthBreakdownItem(
                icon = if (state.lowQualityCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                iconTint = if (state.lowQualityCount > 0) Color(0xFFE53935) else Color(0xFF43A047),
                iconBg = if (state.lowQualityCount > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                title = "Low quality files",

                subtitle = if (state.lowQualityCount > 0) "${state.lowQualityCount} tracks below 192kbps" else "All tracks high quality",
                onClick = onNavigateToLowQualityFiles
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
            .jellyClick(enabled = onClick != null) { onClick?.invoke() }
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
            .jellyClick { onClick() }
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
        JellyButton(
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


