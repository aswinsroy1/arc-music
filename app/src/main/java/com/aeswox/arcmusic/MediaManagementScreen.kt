package com.aeswox.arcmusic

import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Troubleshoot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.jelly
import com.aeswox.arcmusic.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToExcludedFolders: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel
) {
    val hazeState = remember { HazeState() }

    val minDurationSec        by viewModel.minSongDurationSec.collectAsState()
    val minTracksPerAlbum     by viewModel.minTracksPerAlbum.collectAsState()
    val excludedFolders       by viewModel.excludedFolders.collectAsState()
    val scanProgress          by viewModel.scanProgress.collectAsState()

    // Scan behavior
    val autoScanOnStartup     by viewModel.autoScanOnStartup.collectAsState()
    val deferScanDuringPlay   by viewModel.deferScanDuringPlayback.collectAsState()
    val autoScanLrcFiles      by viewModel.autoScanLrcFiles.collectAsState()
    val augmentFromTags       by viewModel.augmentMetadataFromTags.collectAsState()
    val extractArtists        by viewModel.extractArtistsFromTitle.collectAsState()
    val minBitrateKbps        by viewModel.minBitrateKbps.collectAsState()

    var showRebuildConfirm by remember { mutableStateOf(false) }

    if (showRebuildConfirm) {
        AlertDialog(
            onDismissRequest = { showRebuildConfirm = false },
            title = { Text("Rebuild Database?") },
            text = {
                Text(
                    "This will completely rebuild your music library from scratch. " +
                    "Favorites, play counts, and other customizations will be lost. " +
                    "This action cannot be undone."
                )
            },
            confirmButton = {
                JellyTextButton(
                    onClick = {
                        showRebuildConfirm = false
                        viewModel.rebuildDatabase()
                    }
                ) { Text("Rebuild", color = Color(0xFFC62828)) }
            },
            dismissButton = {
                JellyTextButton(onClick = { showRebuildConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(top = 24.dp, bottom = 180.dp, start = 24.dp, end = 24.dp),
                modifier = Modifier.physicsBounceOverscroll().fillMaxSize()
            ) {
                // ── Header ─────────────────────────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        JellyIconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Media Management",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // ── Scan progress card ──────────────────────────────────────────────
                item {
                    AnimatedVisibility(
                        visible = scanProgress.isRunning || scanProgress.isCompleted,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f))
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (scanProgress.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (scanProgress.isCompleted) "Scan complete" else scanProgress.phase.label,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    if (scanProgress.hasProgress && !scanProgress.isCompleted) {
                                        Text(
                                            text = "${scanProgress.current} / ${scanProgress.total}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else if (scanProgress.isCompleted) {
                                        Text(
                                            text = "${scanProgress.current} tracks found",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val animatedProgress by animateFloatAsState(
                                targetValue = if (scanProgress.isCompleted) 1f else if (scanProgress.hasProgress) scanProgress.fraction else 0f,
                                animationSpec = tween(300),
                                label = "scan_progress"
                            )

                            if (scanProgress.isCompleted) {
                                LinearProgressIndicator(
                                    progress = { 1f },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    strokeCap = StrokeCap.Round
                                )
                            } else if (scanProgress.hasProgress) {
                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    strokeCap = StrokeCap.Round
                                )
                            } else {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // ── LIBRARY ACTIONS ────────────────────────────────────────────────
                item {
                    Text(
                        text = "LIBRARY ACTIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                    )

                    // Scan Media
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f))
                            .jellyClick(enabled = !scanProgress.isRunning) { viewModel.scanMediaStore() }
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface)
                        ) {
                            if (scanProgress.isRunning &&
                                scanProgress.phase != com.aeswox.arcmusic.db.ScanPhase.CLEARING_DATABASE) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.surface
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.surface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Scan Media", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "Full scan – look for all files in selected folders",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick / Incremental Scan
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f))
                            .jellyClick(enabled = !scanProgress.isRunning) { viewModel.incrementalScan() }
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Quick Sync", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "Only picks up new or changed files since last scan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Force Refresh Metadata (deep scan)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f))
                            .jellyClick(enabled = !scanProgress.isRunning) { viewModel.runDeepScanBackground() }
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            if (scanProgress.isRunning &&
                                scanProgress.phase == com.aeswox.arcmusic.db.ScanPhase.PROCESSING_FILES) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Troubleshoot,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Force Refresh Metadata", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "Extract explicit tags, precise year, lyrics, and Atmos data",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rebuild Database
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f))
                            .jellyClick(enabled = !scanProgress.isRunning) { showRebuildConfirm = true }
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFEBEE))
                        ) {
                            if (scanProgress.isRunning &&
                                scanProgress.phase == com.aeswox.arcmusic.db.ScanPhase.CLEARING_DATABASE) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFFC62828)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = null,
                                    tint = Color(0xFFC62828)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Rebuild Database", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "Cleans cache and restores library from scratch.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // ── SCAN BEHAVIOR ──────────────────────────────────────────────────
                item {
                    Text(
                        text = "SCAN BEHAVIOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f))
                            .padding(16.dp)
                    ) {
                        // Auto Scan on Startup
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Scan on App Start", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "Automatically scan when you open the app.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = autoScanOnStartup,
                                onCheckedChange = { viewModel.setAutoScanOnStartup(it) }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        // Defer scan during playback
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Defer Scan During Playback", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "Wait 30s before scanning when music is playing.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = deferScanDuringPlay,
                                onCheckedChange = { viewModel.setDeferScanDuringPlayback(it) }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        // Auto Scan LRC files
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Auto-Detect .lrc Files", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "Automatically find and link .lrc lyric files.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = autoScanLrcFiles,
                                onCheckedChange = { viewModel.setAutoScanLrcFiles(it) }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        // Augment metadata from embedded tags
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Read Embedded Tags", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "Extract Dolby Atmos, explicit flags, lyrics & more from file tags.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = augmentFromTags,
                                onCheckedChange = { viewModel.setAugmentMetadataFromTags(it) }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        // Extract artists from title (e.g. "Song (ft. Artist)" → split)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Split Artists from Title", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "Detect \"ft.\" / \"feat.\" in track titles and add as artists.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = extractArtists,
                                onCheckedChange = { viewModel.setExtractArtistsFromTitle(it) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // ── REFINEMENT RULES ────────────────────────────────────────────────
                item {
                    Text(
                        text = "REFINEMENT RULES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f))
                            .padding(16.dp)
                    ) {
                        // Minimum Song Duration
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Minimum Song Duration", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "Ignore files shorter than this.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "${minDurationSec}s",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "0s", style = MaterialTheme.typography.labelSmall)
                            CustomHorizontalSlider(
                                value = minDurationSec.toFloat(),
                                onValueChange = { viewModel.setMinSongDurationSec(Math.round(it)) },
                                valueRange = 0f..60f,
                                steps = 5,
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            )
                            Text(text = "60s", style = MaterialTheme.typography.labelSmall)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        // Minimum Tracks per Album
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Minimum Tracks per Album", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "Only show albums with at least this many tracks.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "$minTracksPerAlbum",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "1", style = MaterialTheme.typography.labelSmall)
                            CustomHorizontalSlider(
                                value = minTracksPerAlbum.toFloat(),
                                onValueChange = { viewModel.setMinTracksPerAlbum(Math.round(it)) },
                                valueRange = 1f..10f,
                                steps = 8,
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            )
                            Text(text = "10", style = MaterialTheme.typography.labelSmall)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        // Minimum Bitrate
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Minimum Bitrate", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "Skip files below this quality threshold.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (minBitrateKbps == 0) "Off" else "${minBitrateKbps} kbps",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Off", style = MaterialTheme.typography.labelSmall)
                            CustomHorizontalSlider(
                                value = minBitrateKbps.toFloat(),
                                onValueChange = { viewModel.setMinBitrateKbps(Math.round(it)) },
                                valueRange = 0f..320f,
                                steps = 7,
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            )
                            Text(text = "320", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // ── FOLDER MANAGEMENT ───────────────────────────────────────────────
                item {
                    Text(
                        text = "FOLDER MANAGEMENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f))
                            .jellyClick { onNavigateToExcludedFolders() }
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Folders to Exclude", style = MaterialTheme.typography.bodyLarge)
                            val folderText = if (excludedFolders.isEmpty()) "None" else "${excludedFolders.size} folder(s) excluded"
                            Text(
                                text = folderText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
