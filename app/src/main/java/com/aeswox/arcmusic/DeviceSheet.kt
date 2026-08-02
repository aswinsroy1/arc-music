package com.aeswox.arcmusic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.VolumeMute
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clipToBounds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSheet(
    volume: Int,
    maxVolume: Int,
    onVolumeChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .size(width = 32.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Current device",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    )
                )
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Connected Device card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Smartphone,
                    contentDescription = "This phone",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "This phone",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Volume Slider
            SamsungVolumeSlider(
                volume = volume,
                maxVolume = maxVolume,
                onVolumeChange = onVolumeChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun SamsungVolumeSlider(
    volume: Int,
    maxVolume: Int,
    onVolumeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val fraction = if (maxVolume > 0) (volume.toFloat() / maxVolume).coerceIn(0f, 1f) else 0f
    
    val isDark = isSystemInDarkTheme()
    // Samsung style: light/transparent background, solid black foreground
    val trackColor = if (isDark) Color.DarkGray.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.08f)
    val activeColor = if (isDark) Color(0xFFE0E0E0) else Color.Black
    val bgIconColor = if (isDark) Color.LightGray else Color.Black.copy(alpha = 0.5f)
    val fgIconColor = if (isDark) Color.Black else Color.White
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(CircleShape)
            .background(trackColor)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val width = size.width
                    if (width > 0) {
                        val newFraction = (offset.x / width).coerceIn(0f, 1f)
                        onVolumeChange((newFraction * maxVolume).roundToInt())
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val width = size.width
                    if (width > 0) {
                        val newFraction = (change.position.x / width).coerceIn(0f, 1f)
                        onVolumeChange((newFraction * maxVolume).roundToInt())
                    }
                }
            }
    ) {
        // Background Icon (visible when slider is empty)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                imageVector = Icons.Outlined.VolumeUp,
                contentDescription = null,
                tint = bgIconColor,
                modifier = Modifier.padding(start = 24.dp).size(28.dp)
            )
        }
        
        // Active Black Track (clips the white foreground icon exactly at the drag position)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = fraction)
                .clipToBounds() // Clips the foreground icon!
                .background(activeColor)
        ) {
            // Foreground Icon (visible when slider is full over this area)
            // We force the Box to not wrap the icon, but simply position it
            Box(
                modifier = Modifier.fillMaxSize(), // this tries to fill the active track, but wait, fillMaxSize inside fillMaxWidth(fraction) might be small!
                // Actually, just putting the icon with CenterStart alignment and NO required width works, because
                // Box does not wrap-content if it has an explicit size, and Alignment.CenterStart places it from the left edge!
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    imageVector = Icons.Outlined.VolumeUp,
                    contentDescription = null,
                    tint = fgIconColor,
                    modifier = Modifier.padding(start = 24.dp).size(28.dp)
                )
            }
        }
    }
}
