package com.aeswox.arcmusic.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/**
 * Black-background intro that draws the Arc Music mark on screen, matching the shape
 * used for the app icon (open arc, stroke width modulated like a soundwave envelope).
 * Plays once, then calls onFinished so the caller can swap to real app content.
 *
 * Geometry here (R=22, centered at 54,54 on a 108-unit grid) matches svg4/foreground.svg —
 * corrected from an earlier version that was centered at (54,50) with R=30, which put
 * the top of the arc ~7 units past the adaptive-icon safe-zone radius (33 units) and
 * rendered cramped/clipped-looking against the top edge on-device. Don't change these
 * numbers here without also regenerating the icon assets to match, or the splash and
 * the static launcher icon will visibly disagree.
 */
@Composable
fun AnimatedSplashScreen(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val arcColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            drawArcMark(sweepFraction = progress, arcColor = arcColor)
        }
    }
}

/**
 * Draws the same open-arc soundwave shape as the app icon (svg4/foreground.svg),
 * on a 108-unit design grid, scaled to fill the Canvas. sweepFraction 0..1 controls
 * how much of the arc has been "drawn" so far.
 */
private fun DrawScope.drawArcMark(sweepFraction: Float, arcColor: Color) {
    if (sweepFraction <= 0f) return

    val gridSize = 108f
    val scale = min(size.width, size.height) / gridSize
    val cx = size.width / 2f
    val cy = size.height / 2f

    val R = 22f * scale
    val baseW = 5.0f
    val ampW = 5.0f
    val startDeg = -215f
    val endDeg = 35f
    val fullSweep = endDeg - startDeg
    val midDeg = (startDeg + endDeg) / 2f

    val totalSamples = 1200
    val drawProgress = sweepFraction.coerceIn(0f, 1f)
    val eraseProgress = (sweepFraction - 1f).coerceIn(0f, 1f)
    
    val visibleSamplesDraw = (totalSamples * drawProgress).toInt()
    val visibleSamplesErase = (totalSamples * eraseProgress).toInt()
    
    if (visibleSamplesErase >= visibleSamplesDraw && visibleSamplesDraw > 0) return

    val visibleSamplesCount = visibleSamplesDraw - visibleSamplesErase
    if (visibleSamplesCount <= 0) return

    val outerPoints = ArrayList<Offset>(visibleSamplesCount + 1)
    val innerPoints = ArrayList<Offset>(visibleSamplesCount + 1)

    for (i in visibleSamplesErase..visibleSamplesDraw) {
        val tt = i / totalSamples.toFloat()
        val deg = startDeg + fullSweep * tt
        val rad = Math.toRadians(deg.toDouble()).toFloat()
        val phi = Math.toRadians((deg - midDeg).toDouble()).toFloat()

        val tipTaper = sin(Math.PI * tt).coerceAtLeast(0.0).pow(0.7).toFloat()
        val lobe = 0.55f + 0.45f * cos(2f * phi)
        val w = (baseW + ampW * tipTaper * lobe) * scale

        val rOut = R + w / 2f
        val rIn = R - w / 2f

        outerPoints.add(Offset(cx + rOut * cos(rad), cy + rOut * sin(rad)))
        innerPoints.add(Offset(cx + rIn * cos(rad), cy + rIn * sin(rad)))
    }

    val path = Path().apply {
        moveTo(outerPoints[0].x, outerPoints[0].y)
        for (p in outerPoints.drop(1)) lineTo(p.x, p.y)
        for (p in innerPoints.reversed()) lineTo(p.x, p.y)
        close()
    }
    drawPath(path, color = arcColor)

    val tipRadius = (baseW / 2f) * scale
    
    // Trailing edge cap
    if (eraseProgress < 1f) {
        val centerFirst = Offset(
            (outerPoints.first().x + innerPoints.first().x) / 2f,
            (outerPoints.first().y + innerPoints.first().y) / 2f
        )
        drawCircle(arcColor, radius = tipRadius, center = centerFirst)
    }
    
    // Leading edge cap (only fully rounded when finished drawing, but before erased)
    if (drawProgress >= 0.999f && eraseProgress < 1f) {
        val centerLast = Offset(
            (outerPoints.last().x + innerPoints.last().x) / 2f,
            (outerPoints.last().y + innerPoints.last().y) / 2f
        )
        drawCircle(arcColor, radius = tipRadius, center = centerLast)
    }
}
