package com.aeswox.arcmusic

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aeswox.arcmusic.data.AppIconVariant
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
import com.aeswox.arcmusic.ui.components.HugeIcons
import com.aeswox.arcmusic.ui.components.JellyIconButton
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppIconScreen(
    selectedVariant: AppIconVariant,
    onVariantSelect: (AppIconVariant) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hazeState = remember { HazeState() }

    Box(modifier = modifier.fillMaxSize()) {
        val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 64.dp
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .physicsBounceOverscroll()
                .haze(state = hazeState),
            contentPadding = PaddingValues(top = topPadding, bottom = 48.dp, start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = "Choose which icon appears on your home screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            item {
                SettingsGroup(title = "ICON STYLE") {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppIconOption(
                            label = "Classic",
                            description = "Black background, white logo",
                            bgColor = Color(0xFF000000),
                            fgColor = Color(0xFFFFFFFF),
                            isSelected = selectedVariant == AppIconVariant.Default,
                            onClick = { onVariantSelect(AppIconVariant.Default) }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        AppIconOption(
                            label = "Light",
                            description = "White background, black logo",
                            bgColor = Color(0xFFFFFFFF),
                            fgColor = Color(0xFF000000),
                            isSelected = selectedVariant == AppIconVariant.Light,
                            onClick = { onVariantSelect(AppIconVariant.Light) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "The icon change may take a moment to appear on your home screen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        // Top app bar overlaid with haze blur
        TopAppBar(
            title = {
                Text(
                    text = "App Icon",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )
            },
            navigationIcon = {
                JellyIconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = HugeIcons.ArrowLeft,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .hazeChild(state = hazeState)
        )
    }
}

@Composable
private fun AppIconOption(
    label: String,
    description: String,
    bgColor: Color,
    fgColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.97f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "icon_scale"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "border_width"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isSelected) Modifier.border(
                    width = borderWidth,
                    brush = Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) else Modifier
            )
            .background(
                MaterialTheme.colorScheme.surfaceContainerHighest.copy(
                    alpha = if (isSelected) 0.3f else 0.15f
                )
            )
            .jellyClick(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon preview
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(bgColor)
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Visual approximation of the arc icon: two concentric ring shapes
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(width = 3.dp, color = fgColor, shape = CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(width = 3.dp, color = fgColor, shape = CircleShape)
            )
        }

        // Labels
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }

        // Selection indicator
        val checkBg = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
        val checkBorder = if (isSelected) Color.Transparent
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(checkBg)
                .border(width = 1.5.dp, color = checkBorder, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
