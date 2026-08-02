import re
with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'r') as f:
    content = f.read()

old_block = """            path.moveTo(0f, centerY)
            path.lineTo(waveStartX, centerY)

            // Curve up to the thumb
            val cp1x = activeWidth - waveW * 0.35f
            val cp1y = centerY
            val cp2x = activeWidth - waveW * 0.15f
            val cp2y = centerY - waveH
            val peakX = activeWidth
            val peakY = centerY - waveH
            path.cubicTo(cp1x, cp1y, cp2x, cp2y, peakX, peakY)

            // Curve down to the track
            val cp3x = activeWidth + waveW * 0.15f
            val cp3y = centerY - waveH
            val cp4x = activeWidth + waveW * 0.35f
            val cp4y = centerY
            path.cubicTo(cp3x, cp3y, cp4x, cp4y, waveEndX, centerY)

            path.lineTo(width, centerY)

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
            }"""

new_block = """            val startX = minOf(0f, waveStartX)
            val endX = maxOf(width, waveEndX)

            path.moveTo(startX, centerY)
            path.lineTo(waveStartX, centerY)

            // Curve up to the thumb
            val cp1x = activeWidth - waveW * 0.35f
            val cp1y = centerY
            val cp2x = activeWidth - waveW * 0.15f
            val cp2y = centerY - waveH
            val peakX = activeWidth
            val peakY = centerY - waveH
            path.cubicTo(cp1x, cp1y, cp2x, cp2y, peakX, peakY)

            // Curve down to the track
            val cp3x = activeWidth + waveW * 0.15f
            val cp3y = centerY - waveH
            val cp4x = activeWidth + waveW * 0.35f
            val cp4y = centerY
            path.cubicTo(cp3x, cp3y, cp4x, cp4y, waveEndX, centerY)

            path.lineTo(endX, centerY)

            // Bottom edge to close the filled shape
            val bottomY = centerY + trackHeight
            path.lineTo(endX, bottomY)
            path.lineTo(startX, bottomY)
            path.close()

            // Draw inactive track (whole path, low opacity)
            drawPath(
                path = path,
                color = color.copy(alpha = 0.25f),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )

            // Draw active track (clipped)
            clipRect(right = activeWidth) {
                // Glow on the active track
                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.3f),
                    style = Stroke(width = trackHeight * 3, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
                )
                // Solid active track
                drawPath(
                    path = path,
                    color = color,
                    style = androidx.compose.ui.graphics.drawscope.Fill
                )
            }"""

if old_block in content:
    content = content.replace(old_block, new_block)
    with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Block not found")
