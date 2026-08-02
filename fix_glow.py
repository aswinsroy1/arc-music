import re
with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'r') as f:
    content = f.read()

# Fix glow
old_glow = """                // Glow on the active track
                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.3f),
                    style = Stroke(width = trackHeight * 3, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
                )"""

new_glow = """                // Glow on the active track
                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.2f),
                    style = Stroke(width = trackHeight * 6, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
                )
                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.4f),
                    style = Stroke(width = trackHeight * 3, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
                )"""

if old_glow in content:
    content = content.replace(old_glow, new_glow)
    with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
        f.write(content)
    print("Updated glow")
else:
    print("Glow block not found")
