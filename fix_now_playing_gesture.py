import re
with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("""    Box(modifier = Modifier.fillMaxSize().background(if (isDarkTheme) Color.Black else Color.White)) {""", """    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color.Black else Color.White)
            .pointerInput(Unit) {
                var totalDrag = 0f
                androidx.compose.foundation.gestures.detectVerticalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        if (totalDrag < -100) {
                            onNavigateToQueue()
                        } else if (totalDrag > 100) {
                            onNavigateBack()
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                    }
                )
            }
    ) {""")

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
    f.write(content)
