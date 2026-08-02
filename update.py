import sys

with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    lines = f.readlines()

start_idx = -1
end_idx = -1

for i, line in enumerate(lines):
    if "allSections.forEachIndexed { index, section ->" in line:
        start_idx = i - 1  # Include the line before if we need to insert variables
        # actually let's just find the start of Column
    if "Spacer(modifier = Modifier.height(24.dp))" in line:
        end_idx = i
        break

# Let's find "HorizontalDivider"
for i, line in enumerate(lines):
    if "HorizontalDivider(" in line:
        start_idx = i + 1
        break

if start_idx != -1 and end_idx != -1:
    new_content = """
                var draggedIndex by remember { mutableStateOf<Int?>(null) }
                var dragOffset by remember { mutableStateOf(0f) }
                val density = LocalDensity.current
                val itemHeightPx = with(density) { 56.dp.toPx() }

                allSections.forEachIndexed { index, section ->
                    val isDragged = index == draggedIndex
                    val yOffset = if (isDragged) dragOffset else 0f

                    Row(
                        modifier = Modifier
                            .zIndex(if (isDragged) 1f else 0f)
                            .offset { androidx.compose.ui.unit.IntOffset(0, yOffset.roundToInt()) }
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Switch(
                                checked = section.isVisible,
                                onCheckedChange = { checked ->
                                    allSections[index] = section.copy(isVisible = checked)
                                },
                                modifier = Modifier.scale(0.8f)
                            )
                            Text(
                                text = section.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = if (section.isVisible) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.DragHandle, 
                            contentDescription = "Reorder",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.pointerInput(Unit) {
                                androidx.compose.foundation.gestures.detectVerticalDragGestures(
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
                                        
                                        if (dragOffset > itemHeightPx / 1.5 && currentIndex < allSections.size - 1) {
                                            val temp = allSections[currentIndex]
                                            allSections[currentIndex] = allSections[currentIndex + 1]
                                            allSections[currentIndex + 1] = temp
                                            draggedIndex = currentIndex + 1
                                            dragOffset -= itemHeightPx
                                        } else if (dragOffset < -itemHeightPx / 1.5 && currentIndex > 0) {
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
"""
    lines = lines[:start_idx] + [new_content] + lines[end_idx:]
    with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
        f.writelines(lines)
    print("Success")
else:
    print("Could not find bounds")
