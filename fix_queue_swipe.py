import re
with open('app/src/main/java/com/example/QueueScreen.kt', 'r') as f:
    content = f.read()

replacement = """                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .pointerInput(Unit) {
                        var totalDrag = 0f
                        androidx.compose.foundation.gestures.detectVerticalDragGestures(
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
                    },"""

content = content.replace("""                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp),""", replacement)

with open('app/src/main/java/com/example/QueueScreen.kt', 'w') as f:
    f.write(content)
