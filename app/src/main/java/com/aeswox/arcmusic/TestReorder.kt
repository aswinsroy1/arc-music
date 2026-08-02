package com.aeswox.arcmusic

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

@Composable
fun TestReorder() {
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val itemHeightPx = with(density) { 56.dp.toPx() }
    val allSections = remember { mutableStateListOf("A", "B", "C") }

    Column {
        allSections.forEachIndexed { index, section ->
            val isDragged = index == draggedIndex
            val yOffset = if (isDragged) dragOffset else 0f

            Row(
                modifier = Modifier
                    .offset { IntOffset(0, yOffset.roundToInt()) }
                    .zIndex(if (isDragged) 1f else 0f)
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(section)
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = null,
                    modifier = Modifier.pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = {
                                draggedIndex = index
                                dragOffset = 0f
                            },
                            onDragEnd = { draggedIndex = null },
                            onDragCancel = { draggedIndex = null },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount

                                val currentIndex = draggedIndex ?: return@detectVerticalDragGestures

                                if (dragOffset > itemHeightPx && currentIndex < allSections.size - 1) {
                                    val temp = allSections[currentIndex]
                                    allSections[currentIndex] = allSections[currentIndex + 1]
                                    allSections[currentIndex + 1] = temp
                                    draggedIndex = currentIndex + 1
                                    dragOffset -= itemHeightPx
                                } else if (dragOffset < -itemHeightPx && currentIndex > 0) {
                                    val temp = allSections[currentIndex]
                                    allSections[currentIndex] = allSections[currentIndex - 1]
                                    allSections[currentIndex - 1] = temp
                                    draggedIndex = currentIndex - 1
                                    dragOffset += itemHeightPx
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}
