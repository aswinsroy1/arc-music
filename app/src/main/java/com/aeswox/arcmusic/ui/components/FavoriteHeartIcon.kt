package com.aeswox.arcmusic.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Animated micro-morphing Favorite/Heart icon.
 * When toggled to favorite, it triggers a squash-and-pop bounce expanding slightly
 * while transitioning smoothly into a filled red heart.
 */
@Composable
fun FavoriteHeartIcon(
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFFE53935),
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconSize: Dp = 24.dp
) {
    val scale = remember { Animatable(1f) }
    var isFirstComposition by remember { mutableStateOf(true) }

    LaunchedEffect(isFavorite) {
        if (isFirstComposition) {
            isFirstComposition = false
            return@LaunchedEffect
        }
        if (isFavorite) {
            // Tactile micro-morph: squash anticipation -> spring pop expansion -> settle
            scale.animateTo(
                targetValue = 0.72f,
                animationSpec = tween(durationMillis = 60, easing = FastOutSlowInEasing)
            )
            scale.animateTo(
                targetValue = 1.30f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        } else {
            // Subtle compress and return
            scale.animateTo(
                targetValue = 0.85f,
                animationSpec = tween(durationMillis = 70, easing = FastOutSlowInEasing)
            )
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    val heartColor by animateColorAsState(
        targetValue = if (isFavorite) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 180),
        label = "heartColor"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = isFavorite,
            animationSpec = tween(durationMillis = 140),
            label = "heartCrossfade"
        ) { favorited ->
            Icon(
                imageVector = if (favorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (favorited) "Remove from favorites" else "Add to favorites",
                tint = heartColor,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
