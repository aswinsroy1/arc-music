package com.aeswox.arcmusic.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.aeswox.arcmusic.LocalNavAnimatedVisibilityScope
import kotlin.math.roundToInt

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PlayerBottomSheet(
    isExpanded: Boolean,
    isVisible: Boolean = true,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onSwipeUp: (() -> Unit)? = null,
    onMiniPlayerDismiss: () -> Unit,
    miniPlayerHeight: Dp = 80.dp,
    bottomOffset: Dp = 88.dp, // Default bottom navigation height approx
    miniPlayerContent: @Composable () -> Unit,
    nowPlayingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenHeightPx = constraints.maxHeight.toFloat()

        // ── Enter / Exit spring animation for when mini player appears / disappears ──
        val shouldBeVisible = isVisible || isExpanded
        val visibilityProgress by animateFloatAsState(
            targetValue = if (shouldBeVisible) 1f else 0f,
            animationSpec = if (shouldBeVisible) {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            } else {
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            },
            label = "miniPlayerVisibility"
        )
        val enterExitOffsetPx = with(density) { miniPlayerHeight.toPx() } * (1f - visibilityProgress)

        // Main app content (always rendered underneath)
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }

        if (isVisible || isExpanded || visibilityProgress > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, enterExitOffsetPx.roundToInt()) },
                contentAlignment = Alignment.BottomCenter
            ) {
                androidx.compose.animation.AnimatedContent(
                    targetState = isExpanded,
                    transitionSpec = {
                        val springSpec = spring<androidx.compose.ui.unit.IntSize>(
                            dampingRatio = 0.88f,
                            stiffness = 380f
                        )
                        if (targetState) {
                            // Expand: fade in full player + subtle scale in
                            (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(280, delayMillis = 40)) +
                             androidx.compose.animation.scaleIn(initialScale = 0.94f, animationSpec = spring(0.88f, 380f)))
                                .togetherWith(androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150)))
                        } else {
                            // Collapse: fade in mini player
                            (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(220, delayMillis = 40)) +
                             androidx.compose.animation.scaleIn(initialScale = 1.0f, animationSpec = spring(0.88f, 380f)))
                                .togetherWith(androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150)) +
                                              androidx.compose.animation.scaleOut(targetScale = 0.94f, animationSpec = spring(0.88f, 380f)))
                        }.using(
                            androidx.compose.animation.SizeTransform(
                                clip = false,
                                sizeAnimationSpec = { _, _ -> springSpec }
                            )
                        )
                    },
                    contentAlignment = Alignment.BottomCenter,
                    label = "PlayerMorphTransition"
                ) { expanded ->
                    androidx.compose.runtime.CompositionLocalProvider(
                        com.aeswox.arcmusic.LocalNavAnimatedVisibilityScope provides this
                    ) {
                        if (expanded) {
                            var dragOffsetY by remember { mutableFloatStateOf(0f) }
                            val animatedDragOffset by animateFloatAsState(
                                targetValue = dragOffsetY,
                                animationSpec = spring(
                                    dampingRatio = 0.88f,
                                    stiffness = 380f
                                ),
                                label = "playerDragOffset"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset { IntOffset(0, animatedDragOffset.roundToInt()) }
                                    .pointerInput(Unit) {
                                        detectVerticalDragGestures(
                                            onDragEnd = {
                                                if (dragOffsetY > 140f) {
                                                    onCollapse()
                                                }
                                                dragOffsetY = 0f
                                            },
                                            onDragCancel = { dragOffsetY = 0f },
                                            onVerticalDrag = { change: PointerInputChange, dragAmount: Float ->
                                                if (dragAmount > 0f || dragOffsetY > 0f) {
                                                    dragOffsetY = (dragOffsetY + dragAmount).coerceAtLeast(0f)
                                                    change.consume()
                                                }
                                            }
                                        )
                                    }
                            ) {
                                nowPlayingContent()
                            }
                        } else {
                            var miniDragY by remember { mutableFloatStateOf(0f) }
                            val animatedMiniDragY by animateFloatAsState(
                                targetValue = miniDragY,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "miniPlayerDragOffset"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .padding(bottom = bottomOffset)
                                    .height(miniPlayerHeight)
                                    .offset { IntOffset(0, animatedMiniDragY.roundToInt()) }
                                    .pointerInput(Unit) {
                                        detectVerticalDragGestures(
                                            onDragEnd = {
                                                if (miniDragY > 100f) {
                                                    onMiniPlayerDismiss()
                                                } else if (miniDragY < -50f) {
                                                    onExpand()
                                                }
                                                miniDragY = 0f
                                            },
                                            onDragCancel = { miniDragY = 0f },
                                            onVerticalDrag = { change: PointerInputChange, dragAmount: Float ->
                                                miniDragY = (miniDragY + dragAmount).coerceIn(-80f, 160f)
                                                change.consume()
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                miniPlayerContent()
                            }
                        }
                    }
                }
            }
        }
    }
}
