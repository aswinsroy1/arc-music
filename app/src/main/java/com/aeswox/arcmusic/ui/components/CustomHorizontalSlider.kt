package com.aeswox.arcmusic.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

@Composable
fun CustomHorizontalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var width by remember { mutableStateOf(0f) }
    
    val rangeSize = valueRange.endInclusive - valueRange.start
    
    val trackHeight = 14.dp
    val thumbRadius = 14.dp
    val thumbStrokeWidth = 2.dp
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val fillTrackColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    
    val thumbColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val thumbStrokeColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Canvas(
        modifier = modifier
            .height(thumbRadius * 2)
            .fillMaxWidth()
            .onSizeChanged { width = it.width.toFloat() }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.isNotEmpty()) {
                            val change = event.changes.first()
                            if (change.pressed) {
                                val x = change.position.x
                                val thumbRadiusPx = thumbRadius.toPx()
                                val trackWidthPx = width - thumbRadiusPx * 2
                                if (trackWidthPx > 0) {
                                    val newNormalized = ((x - thumbRadiusPx) / trackWidthPx).coerceIn(0f, 1f)
                                    val newValue = newNormalized * rangeSize + valueRange.start
                                    onValueChange(newValue)
                                }
                                change.consume()
                            }
                        }
                    }
                }
            }
    ) {
        if (width == 0f) return@Canvas

        val trackHeightPx = trackHeight.toPx()
        val thumbRadiusPx = thumbRadius.toPx()
        
        // Track boundaries
        val leftX = thumbRadiusPx
        val rightX = width - thumbRadiusPx
        val trackWidthPx = rightX - leftX
        
        // Thumb position
        val normalizedValue = ((value - valueRange.start) / rangeSize).coerceIn(0f, 1f)
        val thumbX = leftX + normalizedValue * trackWidthPx
        val centerY = size.height / 2f

        // 1. Draw Background Track
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(leftX, centerY - trackHeightPx / 2f),
            size = Size(trackWidthPx, trackHeightPx),
            cornerRadius = CornerRadius(trackHeightPx / 2f, trackHeightPx / 2f)
        )

        // 2. Draw Fill Track (from Left to Thumb)
        val fillWidth = maxOf(0f, thumbX - leftX)
        if (fillWidth > 0) {
            drawRoundRect(
                color = fillTrackColor,
                topLeft = Offset(leftX, centerY - trackHeightPx / 2f),
                size = Size(fillWidth, trackHeightPx),
                cornerRadius = CornerRadius(trackHeightPx / 2f, trackHeightPx / 2f)
            )
        }

        // 3. Draw Thumb
        drawCircle(
            color = thumbColor,
            radius = thumbRadiusPx,
            center = Offset(thumbX, centerY)
        )
        drawCircle(
            color = thumbStrokeColor,
            radius = thumbRadiusPx - thumbStrokeWidth.toPx() / 2f,
            center = Offset(thumbX, centerY),
            style = Stroke(width = thumbStrokeWidth.toPx())
        )
    }
}
