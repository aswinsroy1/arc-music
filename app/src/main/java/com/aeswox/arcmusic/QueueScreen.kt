package com.aeswox.arcmusic

import androidx.compose.animation.core.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aeswox.arcmusic.db.entities.Track

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(onNavigateBack: () -> Unit) {
    val viewModel: MusicViewModel = hiltViewModel()
    val currentQueue by viewModel.currentQueue.collectAsState()
    val currentQueueIndex by viewModel.currentQueueIndex.collectAsState()
    val currentlyPlaying by viewModel.currentlyPlaying.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    
    val upNextWithIndex = currentQueue.mapIndexed { index, track -> index to track }
        .filter { it.first > currentQueueIndex }

    var showAddToPlaylistSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .pointerInput(Unit) {
                        var totalDrag = 0f
                        detectVerticalDragGestures(
                            onDragStart = { totalDrag = 0f },
                            onDragEnd = {
                                if (totalDrag > 100) {
                                    onNavigateBack()
                                }
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount
                            }
                        )
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Queue",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val queueCount = upNextWithIndex.size + (if (currentlyPlaying != null) 1 else 0)
                    Text(
                        text = "$queueCount songs",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Queue Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .border(1.dp, MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f), RoundedCornerShape(36.dp))
            ) {
                if (currentQueue.isEmpty()) {
                    QueueEmptyState()
                } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    item {
                        // Now Playing
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                AsyncImage(
                                    model = currentlyPlaying?.albumId?.let { "content://media/external/audio/albumart/$it" },
                                    contentDescription = "Now Playing Cover",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            // Playing Indicator
                            Row(
                                modifier = Modifier.height(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                if (isPlaying) {
                                    val infiniteTransition = rememberInfiniteTransition()
                                    val height1 by infiniteTransition.animateFloat(initialValue = 0.4f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(400, easing = LinearEasing), repeatMode = RepeatMode.Reverse))
                                    val height2 by infiniteTransition.animateFloat(initialValue = 0.8f, targetValue = 0.2f, animationSpec = infiniteRepeatable(animation = tween(600, easing = LinearEasing), repeatMode = RepeatMode.Reverse))
                                    val height3 by infiniteTransition.animateFloat(initialValue = 0.2f, targetValue = 0.7f, animationSpec = infiniteRepeatable(animation = tween(500, easing = LinearEasing), repeatMode = RepeatMode.Reverse))
                                    Box(modifier = Modifier.width(4.dp).fillMaxHeight(height1).clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)).background(MaterialTheme.colorScheme.primary))
                                    Box(modifier = Modifier.width(4.dp).fillMaxHeight(height2).clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)).background(MaterialTheme.colorScheme.primary))
                                    Box(modifier = Modifier.width(4.dp).fillMaxHeight(height3).clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)).background(MaterialTheme.colorScheme.primary))
                                } else {
                                    Box(modifier = Modifier.width(4.dp).fillMaxHeight(0.2f).clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)).background(MaterialTheme.colorScheme.primary))
                                    Box(modifier = Modifier.width(4.dp).fillMaxHeight(0.4f).clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)).background(MaterialTheme.colorScheme.primary))
                                    Box(modifier = Modifier.width(4.dp).fillMaxHeight(0.3f).clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)).background(MaterialTheme.colorScheme.primary))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentlyPlaying?.title ?: "Unknown",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = currentlyPlaying?.artist ?: "Unknown Artist",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { }) {
                                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth())
                    }

                    item {
                        Text(
                            text = "UP NEXT",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp)
                        )
                    }

                    itemsIndexed(upNextWithIndex) { listIndex, (originalIndex, track) ->
                        QueueItemRow(
                            index = listIndex + 1,
                            track = track,
                            isDragHandleVisible = true,
                            onClick = { viewModel.skipToQueueItem(originalIndex) }
                        )
                    }

                    item {
                        // Bottom Actions
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { }) {
                                Text("Clear queue", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = { showAddToPlaylistSheet = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text("Save as playlist")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(imageVector = Icons.Default.PlaylistAdd, contentDescription = null)
                            }
                        }
                    }
                }
                }
            }
        }
    }

    if (showAddToPlaylistSheet) {
        val trackIds = currentQueue.map { it.id }
        AddToPlaylistSheet(trackIds = trackIds, onDismissRequest = { showAddToPlaylistSheet = false })
    }
}

@Composable
fun QueueItemRow(index: Int, track: Track, isDragHandleVisible: Boolean, onClick: () -> Unit = {}) {
    var isHovered by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .defaultMinSize(minHeight = 64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = index.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(24.dp)
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            AsyncImage(
                model = track.albumId?.let { "content://media/external/audio/albumart/$it" },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isDragHandleVisible) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Reorder",
                tint = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun QueueEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = "Queue is empty",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Queue is empty",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Add some songs to the queue to keep the\nmusic playing.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        AppPrimaryButton(
            text = "Go to Library",
            onClick = { /* TODO: Navigate to Library */ },
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
            modifier = Modifier.width(220.dp)
        )
    }
}
