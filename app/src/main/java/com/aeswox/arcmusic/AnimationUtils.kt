package com.aeswox.arcmusic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StaggeredListItem(
    index: Int,
    modifier: Modifier = Modifier,
    maxAnimatedItems: Int = 10,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val delayMs = (index.coerceAtMost(maxAnimatedItems - 1) * 40).toLong()
        kotlinx.coroutines.delay(delayMs)
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing),
        label = "stagger_alpha"
    )

    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else 20f,
        animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing),
        label = "stagger_translation"
    )

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            this.translationY = translationY
        }
    ) {
        content()
    }
}

fun Modifier.shimmerLoading(
    durationMillis: Int = 1200,
    colors: List<Color>? = null
): Modifier = composed {
    val defaultColors = listOf(
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    )
    val shimmerColors = colors ?: defaultColors

    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    this.background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation, y = translateAnimation)
        )
    )
}

@Composable
fun SkeletonCrossfade(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    skeleton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Crossfade(
        targetState = isLoading,
        animationSpec = tween(durationMillis = 250),
        modifier = modifier,
        label = "skeleton_crossfade"
    ) { loading ->
        if (loading) {
            skeleton()
        } else {
            content()
        }
    }
}

@Composable
fun AnimatedSelectionCheckbox(
    selected: Boolean,
    visible: Boolean = true,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "checkbox_scale"
    )

    val color by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "checkbox_color"
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(durationMillis = 150),
        label = "checkbox_border"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(size)
            .background(color = color, shape = CircleShape)
            .border(width = 2.dp, color = borderColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = selected,
            enter = scaleIn(tween(150)) + fadeIn(tween(150)),
            exit = scaleOut(tween(150)) + fadeOut(tween(150))
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(size * 0.7f)
            )
        }
    }
}

@Composable
fun AnimatedBadge(
    count: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var previousCount by remember { mutableIntStateOf(count) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(count) {
        if (count > previousCount) {
            scale.animateTo(
                targetValue = 1.3f,
                animationSpec = tween(durationMillis = 150)
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 150)
            )
        }
        previousCount = count
    }

    Box(modifier = modifier.scale(scale.value)) {
        content()
    }
}

/**
 * A vertically-fading scrim gradient rendered behind the bottom chrome
 * (navigation bar + miniplayer). Height and alpha are animated externally —
 * caller drives them via [animateDpAsState] / [animateFloatAsState] so the
 * gradient grows/shrinks smoothly as UI elements appear and disappear.
 *
 * Color stops match the Rhythm reference implementation:
 *   transparent → 28% bg → 76% bg → 100% bg
 */
@Composable
fun BottomChromeGradient(
    height: Dp,
    alpha: Float = 1f,
    modifier: Modifier = Modifier
) {
    if (height <= 0.dp) return
    val bgColor = MaterialTheme.colorScheme.background
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer { this.alpha = alpha }
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to Color.Transparent,
                        0.25f to bgColor.copy(alpha = 0.28f),
                        0.62f to bgColor.copy(alpha = 0.76f),
                        1f to bgColor.copy(alpha = 1f)
                    )
                )
            )
    )
}
