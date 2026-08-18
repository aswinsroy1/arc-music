package com.aeswox.arcmusic.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
        
        var dragOffset by remember { mutableFloatStateOf(0f) }
        
        val collapsedOffset = screenHeightPx - with(density) { (bottomOffset + miniPlayerHeight).toPx() }
        val expandedOffset = 0f
        
        val targetOffset = if (isExpanded) expandedOffset else collapsedOffset
        
        val animatedOffset by animateFloatAsState(
            targetValue = targetOffset + dragOffset,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "sheetOffset"
        )
        
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

        val dragModifier = Modifier.draggable(
            orientation = Orientation.Vertical,
            state = rememberDraggableState { delta ->
                dragOffset = (dragOffset + delta).coerceIn(
                    minimumValue = expandedOffset - targetOffset,
                    maximumValue = screenHeightPx - targetOffset
                )
            },
            onDragStopped = { velocity ->
                val currentOffset = targetOffset + dragOffset
                if (isExpanded) {
                    if (currentOffset > collapsedOffset * 0.3f || velocity > 1000f) {
                        onCollapse()
                    }
                } else {
                    if (currentOffset > collapsedOffset + with(density) { 40.dp.toPx() } || velocity > 1000f) {
                        onMiniPlayerDismiss()
                    } else if (currentOffset < collapsedOffset * 0.7f || velocity < -1000f) {
                        onExpand()
                    }
                }
                dragOffset = 0f
            }
        )

        // Background logic (transparent when fully collapsed, solid when expanded)
        // This fixes the instant flash issue

        // Main app content
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }

        if (isVisible || isExpanded || dragOffset != 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, animatedOffset.roundToInt()) }
            ) {
                // Animate horizontal padding from 24.dp (pill) to 0.dp (full width)
                val horizontalPaddingDp = 24.dp * (1f - expansionFraction)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPaddingDp)
                        .height(sheetHeightDp)
                        .clip(sheetShape)
                        .then(dragModifier)
                ) {
                // Collapsed Miniplayer Content
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
                        .clickable(enabled = expansionFraction < 0.15f && dragOffset == 0f) {
                            onExpand()
                        }
                ) {
                    miniPlayerContent()
                }
                
                // Expanded Player Content
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
