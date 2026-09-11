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
 * This replaces the system SplashScreen's default static icon: the theme should point
 * windowSplashScreenAnimatedIcon at a transparent placeholder (see drawable/splash_transparent.xml)
 * so nothing is drawn before this composable takes over. The result is a continuous
 * black background from process start through this animation into real content, with
 * no flash of a static launcher icon in between.
 */
@Composable
fun AnimatedSplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    durationMillis: Int = 900
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing)
        )
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            drawArcMark(sweepFraction = progress.value)
        }
    }
}

/**
 * Draws the same open-arc soundwave shape as the app icon (svg3/foreground.svg),
 * on a 108-unit design grid, scaled to fill the Canvas. sweepFraction 0..1 controls
 * how much of the arc has been "drawn" so far.
 */
private fun DrawScope.drawArcMark(sweepFraction: Float) {
    if (sweepFraction <= 0f) return

    val gridSize = 108f
    val scale = min(size.width, size.height) / gridSize
    val canvasCenterX = size.width / 2f
    val canvasCenterY = size.height / 2f

    // Icon design uses center (54, 50) on the 108-unit grid — 4 units above the
    // visual grid center — so shift up to match the static icon's optical center.
    val cx = canvasCenterX
    val cy = canvasCenterY - (54f - 50f) * scale

    val R = 30f * scale
    val startDeg = -215f
    val endDeg = 35f
    val fullSweep = endDeg - startDeg
    val midDeg = (startDeg + endDeg) / 2f

    val totalSamples = 240
    val visibleSamples = (totalSamples * sweepFraction).toInt().coerceAtLeast(1)

    val outerPoints = ArrayList<Offset>(visibleSamples + 1)
    val innerPoints = ArrayList<Offset>(visibleSamples + 1)

    for (i in 0..visibleSamples) {
        val tt = i / totalSamples.toFloat()
        val deg = startDeg + fullSweep * tt
        val rad = Math.toRadians(deg.toDouble()).toFloat()
        val phi = Math.toRadians((deg - midDeg).toDouble()).toFloat()

        val tipTaper = sin(Math.PI * tt).coerceAtLeast(0.0).pow(0.7).toFloat()
        val lobe = 0.55f + 0.45f * cos(2f * phi)
        val w = (6.0f + 6.5f * tipTaper * lobe) * scale

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
    drawPath(path, color = Color.White)

    // Rounded cap at the trailing (fixed) start of the stroke, matching the static
    // icon's rounded terminal. The leading/growing end is intentionally left as a
    // flat cut while animating — it reads as the stroke actively being drawn.
    drawCircle(Color.White, radius = 3f * scale, center = outerPoints.first())
    if (sweepFraction >= 0.999f) {
        drawCircle(Color.White, radius = 3f * scale, center = outerPoints.last())
    }
}
