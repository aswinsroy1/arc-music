import re
with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'r') as f:
    content = f.read()

old_block = """            // Bottom edge to close the filled shape
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

new_block = """            // Draw inactive track (whole path, low opacity)
            drawPath(
                path = path,
                color = color.copy(alpha = 0.25f),
                style = Stroke(width = trackHeight, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
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
                    style = Stroke(width = trackHeight, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
                )
            }"""

if old_block in content:
    content = content.replace(old_block, new_block)
    with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Block not found")
