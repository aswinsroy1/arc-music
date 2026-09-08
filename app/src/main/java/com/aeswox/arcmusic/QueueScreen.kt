package com.aeswox.arcmusic

import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
import androidx.compose.animation.core.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PlaylistAdd
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
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.components.*

// ──────────────────────────────────────────────────────────────────────────────
// QueueItemRow — reusable row used by ArcQueueContent
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun QueueItemRow(
    index: Int,
    track: Track,
    isDragHandleVisible: Boolean,
    textColor: Color = Color.White,
    onClick: () -> Unit = {},
    onMove: (Int) -> Unit = {}
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val rowHeightPx = with(density) { 64.dp.toPx() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (offsetY != 0f) 1f else 0f)
            .graphicsLayer { translationY = offsetY }
            .jellyClick(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .defaultMinSize(minHeight = 60.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Artwork
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(textColor.copy(alpha = 0.08f))
        ) {
            AsyncImage(
                model = track.artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = textColor,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        if (isDragHandleVisible) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Reorder",
                tint = textColor.copy(alpha = 0.35f),
                modifier = Modifier
                    .size(20.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                val itemsMoved = (offsetY / rowHeightPx).roundToInt()
                                if (itemsMoved != 0) onMove(itemsMoved)
                                offsetY = 0f
                            },
                            onDragCancel = { offsetY = 0f },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                offsetY += dragAmount
                            }
                        )
                    }
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// QueueEmptyState
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun QueueEmptyState(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    onNavigateToLibrary: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(textColor.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = "Queue is empty",
                tint = textColor.copy(alpha = 0.5f),
                modifier = Modifier.size(52.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Queue is empty",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Add some songs to keep the music going.",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
