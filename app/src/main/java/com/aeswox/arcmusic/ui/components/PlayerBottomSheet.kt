package com.aeswox.arcmusic.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

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

        val collapsedOffset = screenHeightPx - with(density) { (bottomOffset + miniPlayerHeight).toPx() }
        val expandedOffset = 0f

        val sheetOffsetAnimatable = remember { androidx.compose.animation.core.Animatable(collapsedOffset) }
        val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

        val targetOffset = if (isExpanded) expandedOffset else collapsedOffset

        androidx.compose.runtime.LaunchedEffect(isExpanded, screenHeightPx, bottomOffset) {
            sheetOffsetAnimatable.animateTo(
                targetValue = targetOffset,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        val animatedOffset = sheetOffsetAnimatable.value
        val isDragging = false

        // Track max downward displacement during this drag gesture
        var maxDownwardDrag by remember { mutableFloatStateOf(0f) }

        // 0.0f = fully collapsed, 1.0f = fully expanded
        val expansionFraction = ((collapsedOffset - animatedOffset) / collapsedOffset).coerceIn(0f, 1f)

        // Shape animations
        val maxCornerRadius = 32.dp
        val cornerBlendProgress = ((expansionFraction - 0.82f) / 0.18f).coerceIn(0f, 1f)
        val smoothBlend = cornerBlendProgress * cornerBlendProgress * cornerBlendProgress
        val topCornerSize = maxCornerRadius * (1f - smoothBlend)
        val bottomCornerSize = 28.dp * (1f - expansionFraction)
        val sheetShape = RoundedCornerShape(
            topStart = topCornerSize,
            topEnd = topCornerSize,
            bottomStart = bottomCornerSize,
            bottomEnd = bottomCornerSize
        )

        val bottomEdgePx = screenHeightPx - with(density) { bottomOffset.toPx() } * (1f - expansionFraction)
        val sheetHeightPx = (bottomEdgePx - animatedOffset).coerceAtLeast(0f)
        val sheetHeightDp = with(density) { sheetHeightPx.toDp() }

        // ── Enter / Exit spring animation ─────────────────────────────────────────
        // visibilityProgress = 1f → fully on-screen; 0f → fully off-screen (below).
        // ENTER: medium-bouncy spring  → satisfying pop up from below.
        // EXIT:  no-bounce spring      → snappy, clean slide away.
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

        // Extra downward shift applied ON TOP of the normal sheet position.
        // When visibilityProgress = 0 the mini-player is one full miniPlayerHeight below its rest.
        // When visibilityProgress = 1 it sits exactly at its rest position.
        val enterExitOffsetPx = with(density) { miniPlayerHeight.toPx() } * (1f - visibilityProgress)

        val dragModifier = Modifier.draggable(
            orientation = Orientation.Vertical,
            state = rememberDraggableState { delta ->
                coroutineScope.launch {
                    val newOffset = (sheetOffsetAnimatable.value + delta).coerceIn(
                        minimumValue = expandedOffset - with(density) { 150.dp.toPx() },
                        maximumValue = screenHeightPx
                    )
                    sheetOffsetAnimatable.snapTo(newOffset)
                    if (newOffset > targetOffset) {
                        maxDownwardDrag = maxOf(maxDownwardDrag, newOffset - targetOffset)
                    }
                }
            },
            onDragStarted = {
                maxDownwardDrag = 0f
            },
            onDragStopped = { velocity ->
                coroutineScope.launch {
                    val currentOffset = sheetOffsetAnimatable.value
                    if (isExpanded) {
                        if (currentOffset > collapsedOffset * 0.3f || velocity > 1000f) {
                            onCollapse()
                        } else if (maxDownwardDrag < with(density) { 10.dp.toPx() } && (currentOffset < expandedOffset - with(density) { 50.dp.toPx() } || (currentOffset <= expandedOffset && velocity < -1000f))) {
                            // Only trigger swipe up if we haven't dragged downwards significantly
                            onSwipeUp?.invoke()
                            sheetOffsetAnimatable.animateTo(expandedOffset, spring())
                        } else {
                            sheetOffsetAnimatable.animateTo(expandedOffset, spring())
                        }
                    } else {
                        if (currentOffset > collapsedOffset + with(density) { 40.dp.toPx() } || velocity > 1000f) {
                            onMiniPlayerDismiss()
                        } else if (currentOffset < collapsedOffset * 0.7f || velocity < -1000f) {
                            onExpand()
                        } else {
                            sheetOffsetAnimatable.animateTo(collapsedOffset, spring())
                        }
                    }
                }
            }
        )

        // Main app content (always rendered underneath)
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }

        // Keep the sheet in composition while partially or fully visible, including
        // during the exit animation (visibilityProgress > 0.01f catches the tail of it).
        if (isVisible || isExpanded || sheetOffsetAnimatable.value != targetOffset || visibilityProgress > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        IntOffset(
                            x = 0,
                            y = (animatedOffset + enterExitOffsetPx).roundToInt()
                        )
                    }
            ) {
                // Animate horizontal padding from 24.dp (pill) → 0.dp (full-width) as it expands
                val horizontalPaddingDp = 24.dp * (1f - expansionFraction)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPaddingDp)
                        .height(sheetHeightDp)
                        .clip(sheetShape)
                        .then(dragModifier)
                ) {
                    // ── Collapsed Miniplayer Content ──────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(miniPlayerHeight)
                            .offset {
                                // Counter-offset to keep the mini player visually anchored near the bottom
                                val upwardDrift = (collapsedOffset - animatedOffset) * 0.5f
                                IntOffset(0, upwardDrift.roundToInt())
                            }
                            .zIndex(if (expansionFraction < 0.5f) 1f else 0f)
                            .graphicsLayer { alpha = (1f - expansionFraction * 2.5f).coerceIn(0f, 1f) }
                            .clip(sheetShape)
                            .clickable(enabled = expansionFraction < 0.15f && sheetOffsetAnimatable.value == targetOffset) {
                                onExpand()
                            }
                    ) {
                        miniPlayerContent()
                    }

                    // ── Expanded Player Content ───────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(with(density) { screenHeightPx.toDp() })
                            .zIndex(if (expansionFraction >= 0.5f) 1f else 0f)
                            .graphicsLayer { alpha = expansionFraction }
                            .clip(sheetShape)
                    ) {
                        nowPlayingContent()
                    }
                }
            }
        }
    }
}
