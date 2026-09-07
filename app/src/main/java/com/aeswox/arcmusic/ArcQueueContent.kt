package com.aeswox.arcmusic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import com.aeswox.arcmusic.db.entities.Track
import kotlin.math.roundToInt

@Composable
fun ArcQueueContent(
    textColor: Color = Color.White,
    isDarkTheme: Boolean = true,
    accentColor: Color = Color(0xFFB28D84),
    onDismiss: () -> Unit = {}
) {
    val viewModel: MusicViewModel = hiltViewModel()
    val currentQueue by viewModel.currentQueue.collectAsState()
    val currentQueueIndex by viewModel.currentQueueIndex.collectAsState()
    val currentlyPlaying by viewModel.currentlyPlaying.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val randomPicks by viewModel.randomPicks.collectAsState()

    val upNextWithIndex = currentQueue.mapIndexed { index, track -> index to track }
        .filter { it.first > currentQueueIndex }

    val bgColor = if (isDarkTheme) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.5f)
    val pillBgColor = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)
    val pillActiveColor = accentColor.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            // We do NOT provide a background here so the parent's dynamic gradient is visible
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 48.dp, bottom = 330.dp)
        ) {
            // Top Section: Currently Playing
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = currentlyPlaying?.artworkUri ?: currentlyPlaying?.albumId?.let { "content://media/external/audio/albumart/$it" },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(pillBgColor)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentlyPlaying?.title ?: "Unknown",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentlyPlaying?.artist ?: "Unknown Artist",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { /* Handle favorite */ },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(pillBgColor)
                    ) {
                        Icon(imageVector = Icons.Default.StarBorder, contentDescription = "Favorite", tint = textColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { /* Handle menu */ },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(pillBgColor)
                    ) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = textColor, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Controls Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val shuffleActive by viewModel.shuffleModeEnabled.collectAsState()
                    val repeatMode by viewModel.repeatMode.collectAsState()
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (shuffleActive) pillActiveColor else pillBgColor)
                            .clickable { viewModel.toggleShuffleMode() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Shuffle, contentDescription = "Shuffle", tint = textColor)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (repeatMode > 0) pillActiveColor else pillBgColor)
                            .clickable { viewModel.toggleRepeatMode() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Repeat, contentDescription = "Repeat", tint = textColor)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(pillActiveColor) // Assume autoplay is active
                            .clickable { /* Handle autoplay toggle */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.AllInclusive, contentDescription = "Autoplay", tint = textColor)
                    }
                }
            }

            // Up Next Header
            if (upNextWithIndex.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        Text(
                            text = "Playing Next",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                        Text(
                            text = "From Queue",
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Up Next List
            itemsIndexed(upNextWithIndex, key = { _, item -> "queue_${item.second.id}_${item.first}" }) { listIndex, (originalIndex, track) ->
                QueueTrackRow(
                    track = track,
                    textColor = textColor,
                    pillBgColor = pillBgColor,
                    onClick = { viewModel.skipToQueueItem(originalIndex) },
                    onMove = { itemsMoved ->
                        val targetIndex = (originalIndex + itemsMoved).coerceIn(currentQueueIndex + 1, currentQueue.lastIndex)
                        if (targetIndex != originalIndex) {
                            viewModel.moveQueueItem(originalIndex, targetIndex)
                        }
                    }
                )
            }

            // Autoplay Section
            if (randomPicks.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AllInclusive, contentDescription = null, tint = textColor.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Autoplay",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = textColor
                            )
                        }
                        Text(
                            text = "Similar music will keep playing",
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    }
                }

                itemsIndexed(randomPicks, key = { _, track -> "random_${track.id}" }) { _, track ->
                    QueueTrackRow(
                        track = track,
                        textColor = textColor,
                        pillBgColor = pillBgColor,
                        onClick = { viewModel.playTrack(track) },
                        onMove = { }
                    )
                }
            }
        }
    }
}

@Composable
fun QueueTrackRow(
    track: Track,
    textColor: Color,
    pillBgColor: Color,
    onClick: () -> Unit,
    onMove: (Int) -> Unit
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val rowHeightPx = with(density) { 56.dp.toPx() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (offsetY != 0f) 1f else 0f)
            .graphicsLayer { translationY = offsetY }
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.artworkUri ?: track.albumId?.let { "content://media/external/audio/albumart/$it" },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(pillBgColor)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Default.Menu, // Closest to iOS drag handle
            contentDescription = "Reorder",
            tint = textColor.copy(alpha = 0.4f),
            modifier = Modifier
                .padding(start = 8.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            val itemsMoved = (offsetY / rowHeightPx).roundToInt()
                            if (itemsMoved != 0) {
                                onMove(itemsMoved)
                            }
                            offsetY = 0f
                        },
                        onDragCancel = {
                            offsetY = 0f
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            offsetY += dragAmount
                        }
                    )
                }
        )
    }
}
