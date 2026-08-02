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
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "waveHeight"
    )
    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "thumbScale"
    )

    // Reusable path to avoid allocations
    val path = remember { Path() }

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
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f
            
            val activeWidth = width * progress
            val baseThumbRadius = 9.dp.toPx()
            val thumbRadius = baseThumbRadius * thumbScale
            
            val waveW = 80.dp.toPx()
            val waveH = waveHeight * density // Convert dp to px
            val trackHeight = 2.dp.toPx()

            val waveStartX = activeWidth - waveW / 2
            val waveEndX = activeWidth + waveW / 2

            // Build the continuous path
            path.reset()
            path.moveTo(minOf(0f, waveStartX), centerY)
            path.lineTo(waveStartX, centerY)

            // Curve up to the thumb
            val cp1x = activeWidth - waveW * 0.25f
            val cp1y = centerY
            val cp2x = activeWidth - waveW * 0.25f
            val cp2y = centerY - waveH
            val peakX = activeWidth
            val peakY = centerY - waveH
            path.cubicTo(cp1x, cp1y, cp2x, cp2y, peakX, peakY)

            // Curve down to the track
            val cp3x = activeWidth + waveW * 0.25f
            val cp3y = centerY - waveH
            val cp4x = activeWidth + waveW * 0.25f
            val cp4y = centerY
            path.cubicTo(cp3x, cp3y, cp4x, cp4y, waveEndX, centerY)

            path.lineTo(maxOf(width, waveEndX), centerY)

            // Draw inactive track
            drawPath(
                path = path,
                color = color.copy(alpha = 0.25f),
                style = Stroke(width = trackHeight, cap = StrokeCap.Round)
            )

            // Draw active track (clipped)
            clipRect(right = activeWidth) {
                // Glow on the active track
                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.3f),
                    style = Stroke(width = trackHeight * 3, cap = StrokeCap.Round)
                )
                // Solid active track
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = trackHeight, cap = StrokeCap.Round)
                )
            }

            val thumbCenter = Offset(activeWidth, peakY)

            // Draw thumb shadow
            val shadowBrush = Brush.radialGradient(
                colors = listOf(Color.Black.copy(alpha = 0.25f), Color.Transparent),
                center = thumbCenter.copy(y = thumbCenter.y + 4.dp.toPx()),
                radius = thumbRadius * 2f
            )
            drawCircle(
                brush = shadowBrush,
                radius = thumbRadius * 2f,
                center = thumbCenter.copy(y = thumbCenter.y + 4.dp.toPx())
            )

            // Draw thumb glow
            val glowBrush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.5f), Color.Transparent),
                center = thumbCenter,
                radius = thumbRadius * 2f
            )
            drawCircle(
                brush = glowBrush,
                radius = thumbRadius * 2f,
                center = thumbCenter
            )

            // Draw the thumb itself
            drawCircle(
                color = color,
                radius = thumbRadius,
                center = thumbCenter
            )
        }
    }
}"""

content = re.sub(r'@Composable\nfun WavySlider\(.*?\}\n\}\n', replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
    f.write(content)
