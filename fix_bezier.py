import re
with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'r') as f:
    content = f.read()

old_path = """            // Build the continuous path
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

            path.lineTo(maxOf(width, waveEndX), centerY)"""

new_path = """            // Build the continuous path
            path.reset()
            path.moveTo(0f, centerY)
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

            path.lineTo(width, centerY)"""

if old_path in content:
    content = content.replace(old_path, new_path)
    with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Failed to find old path")
