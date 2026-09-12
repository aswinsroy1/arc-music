package com.aeswox.arcmusic.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.aeswox.arcmusic.AppIconButton
import com.aeswox.arcmusic.ui.animations.jellyClick
import kotlinx.coroutines.launch

data class MorphingMenuItem(
    val text: String,
    val icon: ImageVector,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

/**
 * A Dynamic Island style morphing dropdown menu.
 * When collapsed, renders a 3-dot icon button.
 * When clicked, the button smoothly morphs into an elevated menu card using spring physics.
 */
@Composable
fun MorphingMenu(
    items: List<MorphingMenuItem>,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 40.dp,
    menuWidth: Dp = 210.dp,
    contentDescription: String? = "More options",
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    var isOpen by remember { mutableStateOf(false) }
    var isDismissing by remember { mutableStateOf(false) }
    val animProgress = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val menuHeight = remember(items.size) { (items.size * 52 + 16).dp }

    fun dismiss(onFinished: (() -> Unit)? = null) {
        if (isDismissing) return
        isDismissing = true
        coroutineScope.launch {
            animProgress.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 1.0f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            isOpen = false
            isDismissing = false
            onFinished?.invoke()
        }
    }

    fun open() {
        if (isOpen) return
        isOpen = true
        coroutineScope.launch {
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.76f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    Box(
        modifier = modifier.size(buttonSize),
        contentAlignment = Alignment.Center
    ) {
        // The 3-dot button in the top bar (covered by the opaque card when open,
        // seamlessly revealed as the card dissolves when shrinking below 50%)
        AppIconButton(
            icon = Icons.Default.MoreVert,
            contentDescription = contentDescription,
            onClick = { open() },
            tint = tint,
            size = 24.dp
        )

        // Morphing Popup container
        if (isOpen) {
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(0, 0),
                onDismissRequest = { dismiss() },
                properties = PopupProperties(focusable = true)
            ) {
                // Fixed outer frame to prevent Android window re-measurement stutter,
                // padded slightly to accommodate spring overshoot without clipping.
                Box(
                    modifier = Modifier
                        .size(menuWidth + 16.dp, menuHeight + 16.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            dismiss()
                        },
                    contentAlignment = Alignment.TopEnd
                ) {
                    val rawProgress = animProgress.value
                    val progress = rawProgress.coerceIn(0f, 1f)
                    val currentWidth = lerp(buttonSize, menuWidth, rawProgress)
                    val currentHeight = lerp(buttonSize, menuHeight, rawProgress)
                    val currentCorner = lerp(buttonSize / 2, 28.dp, progress)

                    // Non-linear transparency curve:
                    // 100% opaque until reduced to 50% size (progress >= 0.5f).
                    // Below 50%, smoothly dissolves using a Hermite S-curve (3t² - 2t³).
                    val popupAlpha = if (progress >= 0.5f) {
                        1f
                    } else {
                        val t = (progress / 0.5f).coerceIn(0f, 1f)
                        t * t * (3f - 2f * t)
                    }

                    val currentElevation = lerp(0.dp, 12.dp, progress) * popupAlpha
                    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f * popupAlpha)

                    Surface(
                        modifier = Modifier
                            .size(currentWidth, currentHeight)
                            .graphicsLayer {
                                alpha = popupAlpha
                            }
                            .shadow(
                                elevation = currentElevation,
                                shape = RoundedCornerShape(currentCorner)
                            )
                            .border(
                                width = 1.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(currentCorner)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                // Consume clicks inside the card so it doesn't dismiss
                            },
                        shape = RoundedCornerShape(currentCorner),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Menu items list that fades out cleanly between 1.0 and 0.45
                            val itemsAlpha = ((progress - 0.45f) / 0.55f).coerceIn(0f, 1f)
                            if (itemsAlpha > 0.01f) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                        .graphicsLayer {
                                            alpha = itemsAlpha
                                            translationY = ((1f - itemsAlpha) * 10.dp.toPx())
                                        },
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items.forEach { item ->
                                        MorphingMenuItemRow(
                                            item = item,
                                            onItemClick = {
                                                dismiss { item.onClick() }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MorphingMenuItemRow(
    item: MorphingMenuItem,
    onItemClick: () -> Unit
) {
    val textColor = if (item.isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val iconTint = if (item.isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(20.dp))
            .jellyClick(onClick = onItemClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = item.text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = textColor,
            maxLines = 1
        )
    }
}
