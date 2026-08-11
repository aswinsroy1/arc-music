package com.aeswox.arcmusic.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun CustomVerticalSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange = -12..12,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var height by remember { mutableStateOf(0f) }
    
    val rangeSize = (valueRange.last - valueRange.first).toFloat()
    
    val trackWidth = 14.dp
    val thumbRadius = 14.dp
    val thumbStrokeWidth = 2.dp
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val fillTrackColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    
    val thumbColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val thumbStrokeColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Canvas(
        modifier = modifier
            .width(thumbRadius * 2)
            .fillMaxHeight()
            .onSizeChanged { height = it.height.toFloat() }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectVerticalDragGestures { change, _ ->
                    val y = change.position.y.coerceIn(0f, height)
                    val newNormalized = 1f - (y / height)
                    val newValue = (newNormalized * rangeSize + valueRange.first).roundToInt()
                    val clampedValue = newValue.coerceIn(valueRange)
                    onValueChange(clampedValue)
                }
            }
    ) {
        if (height == 0f) return@Canvas

        val trackWidthPx = trackWidth.toPx()
        val thumbRadiusPx = thumbRadius.toPx()
        
        // Track boundaries
        val topY = thumbRadiusPx
        val bottomY = height - thumbRadiusPx
        val trackHeight = bottomY - topY
        
        // Thumb position
        val thumbY = topY + (1f - ((value - valueRange.first) / rangeSize)) * trackHeight
        val centerX = size.width / 2f
        val centerY = topY + trackHeight / 2f // 0dB position

        // 1. Draw Background Track
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(centerX - trackWidthPx / 2f, topY),
            size = Size(trackWidthPx, trackHeight),
            cornerRadius = CornerRadius(trackWidthPx / 2f, trackWidthPx / 2f)
        )

        // 2. Draw Fill Track (from Center to Thumb)
        val fillTop = minOf(centerY, thumbY)
        val fillBottom = maxOf(centerY, thumbY)
        drawRoundRect(
            color = fillTrackColor,
            topLeft = Offset(centerX - trackWidthPx / 2f, fillTop),
            size = Size(trackWidthPx, fillBottom - fillTop),
            cornerRadius = CornerRadius(trackWidthPx / 2f, trackWidthPx / 2f)
        )

        // 3. Draw Thumb
        drawCircle(
            color = thumbColor,
            radius = thumbRadiusPx,
            center = Offset(centerX, thumbY)
        )
        drawCircle(
            color = thumbStrokeColor,
            radius = thumbRadiusPx - thumbStrokeWidth.toPx() / 2f,
            center = Offset(centerX, thumbY),
            style = Stroke(width = thumbStrokeWidth.toPx())
        )
    }
}
