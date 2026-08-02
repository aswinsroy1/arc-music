import re
with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'r') as f:
    content = f.read()

replacement = """@Composable
fun WavySlider(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    var sliderWidth by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    // Spring animations for the liquid effect
    val waveHeight by animateFloatAsState(
        targetValue = if (isDragging) 14.dp.value else 8.dp.value,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "waveHeight"
    )
    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "thumbScale"
    )

    Box(
        modifier = modifier
            .onSizeChanged { sliderWidth = it.width.toFloat() }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = androidx.compose.foundation.gestures.awaitFirstDown()
                        isDragging = true
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        if (sliderWidth > 0) {
                            onProgressChange((down.position.x / sliderWidth).coerceIn(0f, 1f))
                        }
                        down.consume()
                        
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null && change.pressed) {
                                if (sliderWidth > 0) {
                                    onProgressChange((change.position.x / sliderWidth).coerceIn(0f, 1f))
                                }
                                change.consume()
                            } else {
                                break
                            }
                        }
                        isDragging = false
                    }
                }
            }
    ) {"""

content = re.sub(r'@Composable\nfun WavySlider\(.*?\.pointerInput\(Unit\) \{.*?\}\n            \}\n    \) \{', replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
    f.write(content)
