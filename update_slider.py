import re

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'r') as f:
    content = f.read()

start_idx = content.find("@Composable\nfun WavySlider")
if start_idx != -1:
    new_slider = """@Composable
fun WavySlider(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    var sliderWidth by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.2f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "thumbScale"
    )

    val path = remember { Path() }

    Box(
        modifier = modifier
            .onSizeChanged { sliderWidth = it.width.toFloat() }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown()
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
            val trackHeight = 2.dp.toPx()
            val thumbRadius = 4.dp.toPx() * thumbScale 
            
            // Inactive track (right of thumb)
            drawLine(
                color = color.copy(alpha = 0.3f),
                start = Offset(activeWidth, centerY),
                end = Offset(width, centerY),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round
            )

            // Active track (Solid symmetric waveform left of thumb)
            path.reset()
            path.moveTo(0f, centerY)
            
            val maxAmplitude = 24.dp.toPx()
            val numPoints = 60
            val step = activeWidth / numPoints
            
            val points = FloatArray(numPoints + 1)
            for (i in 0..numPoints) {
                val x = i * step
                // Taper edges to 0 so it smoothly starts and ends
                val distanceToRight = activeWidth - x
                val distanceToLeft = x
                val dampingRight = if (distanceToRight < 40.dp.toPx()) distanceToRight / 40.dp.toPx() else 1f
                val dampingLeft = if (distanceToLeft < 20.dp.toPx()) distanceToLeft / 20.dp.toPx() else 1f
                val damping = minOf(dampingRight, dampingLeft)
                
                // Base frequency based on density to keep it consistent across screens
                val f1 = 0.03f / density
                val f2 = 0.07f / density
                val f3 = 0.13f / density
                
                var v = (
                    sin(x * f1 + phase) * 0.5f +
                    sin(x * f2 - phase * 0.8f) * 0.3f +
                    sin(x * f3 + phase * 1.2f) * 0.2f
                )
                // Map from [-1, 1] to [0, 1]
                v = (v + 1f) / 2f
                
                // Accentuate peaks and widen valleys like an audio waveform
                v = v * v * v
                
                // Base thickness (half of trackHeight since we mirror it)
                val baseHeight = trackHeight / 2f
                
                points[i] = baseHeight + v * maxAmplitude * damping
            }
            
            // Draw top half
            for (i in 0 until numPoints) {
                val p0x = i * step
                val p0y = centerY - points[i]
                val p1x = (i + 1) * step
                val p1y = centerY - points[i + 1]
                val cx = (p0x + p1x) / 2f
                path.cubicTo(cx, p0y, cx, p1y, p1x, p1y)
            }
            
            // Draw bottom half (right to left)
            for (i in numPoints downTo 1) {
                val p0x = i * step
                val p0y = centerY + points[i]
                val p1x = (i - 1) * step
                val p1y = centerY + points[i - 1]
                val cx = (p0x + p1x) / 2f
                path.cubicTo(cx, p0y, cx, p1y, p1x, p1y)
            }
            
            path.close()
            
            drawPath(
                path = path,
                color = color,
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
            
            // Draw thumb
            val thumbCenter = Offset(activeWidth, centerY)
            
            // Translucent ring
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = thumbRadius * 2.5f,
                center = thumbCenter
            )
            
            // Solid center dot
            drawCircle(
                color = color,
                radius = thumbRadius,
                center = thumbCenter
            )
        }
    }
}
"""
    new_content = content[:start_idx] + new_slider
    with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
        f.write(new_content)
    print("Success")
else:
    print("Failed to find WavySlider")

