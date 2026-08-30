package com.aeswox.arcmusic

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aeswox.arcmusic.data.model.LyricsDisplayStyle
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
import com.aeswox.arcmusic.ui.components.CustomHorizontalSlider
import com.aeswox.arcmusic.ui.components.JellyIconButton
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricStyleScreen(
    lyricsDisplayStyle: LyricsDisplayStyle,
    onLyricsDisplayStyleChange: (LyricsDisplayStyle) -> Unit,
    lyricsShowControls: Boolean,
    onLyricsShowControlsChange: (Boolean) -> Unit,
    lyricsFadeSteepness: Float,
    onLyricsFadeSteepnessChange: (Float) -> Unit,
    lyricsFadeScaleCeiling: Float,
    onLyricsFadeScaleCeilingChange: (Float) -> Unit,
    lyricsFadeDistanceSizing: Boolean,
    onLyricsFadeDistanceSizingChange: (Boolean) -> Unit,
    lyricsBlurRadius: Float,
    onLyricsBlurRadiusChange: (Float) -> Unit,
    lyricsBlurDimming: Float,
    onLyricsBlurDimmingChange: (Float) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hazeState = remember { HazeState() }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(
                    state = hazeState,
                    style = dev.chrisbanes.haze.HazeStyle(
                        tint = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                        blurRadius = 30.dp,
                        noiseFactor = 0.05f
                    )
                )
        ) {
            LazyColumn(
                contentPadding = PaddingValues(top = 24.dp, bottom = 180.dp, start = 24.dp, end = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .physicsBounceOverscroll()
                    .fillMaxSize()
            ) {
                item {
                    AppearanceHeader(onNavigateBack = onNavigateBack, title = "Lyrics Style")
                }
                
                item {
                    // Centered Fade / Blur Selector
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f)
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LyricsStyleChip(
                                label = "Fade",
                                selected = lyricsDisplayStyle == LyricsDisplayStyle.FADE,
                                onClick = { onLyricsDisplayStyleChange(LyricsDisplayStyle.FADE) }
                            )
                            LyricsStyleChip(
                                label = "Blur",
                                selected = lyricsDisplayStyle == LyricsDisplayStyle.DISTANCE_BLUR,
                                onClick = { onLyricsDisplayStyleChange(LyricsDisplayStyle.DISTANCE_BLUR) }
                            )
                        }
                    }
                }

                item {
                    SettingsGroup(title = "GENERAL") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Show player controls",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = lyricsShowControls,
                                onCheckedChange = onLyricsShowControlsChange
                            )
                        }
                    }
                }
                
                item {
                    if (lyricsDisplayStyle == LyricsDisplayStyle.FADE) {
                        SettingsGroup(title = "FADE OPTIONS") {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Distance-based Sizing",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Switch(
                                        checked = lyricsFadeDistanceSizing,
                                        onCheckedChange = onLyricsFadeDistanceSizingChange
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Fade Steepness: ${String.format("%.2f", lyricsFadeSteepness)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                CustomHorizontalSlider(
                                    value = lyricsFadeSteepness,
                                    onValueChange = onLyricsFadeSteepnessChange,
                                    valueRange = 0.5f..3.0f
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "Inactive Scale Ceiling: ${String.format("%.2f", lyricsFadeScaleCeiling)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                CustomHorizontalSlider(
                                    value = lyricsFadeScaleCeiling,
                                    onValueChange = onLyricsFadeScaleCeilingChange,
                                    valueRange = 0.5f..1.0f
                                )

                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { 
                                        onLyricsFadeSteepnessChange(1.2f)
                                        onLyricsFadeScaleCeilingChange(0.85f)
                                        onLyricsFadeDistanceSizingChange(true)
                                    },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Reset to Defaults")
                                }
                            }
                        }
                    } else {
                        SettingsGroup(title = "BLUR OPTIONS") {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Blur Radius: ${String.format("%.0f", lyricsBlurRadius)}px",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                CustomHorizontalSlider(
                                    value = lyricsBlurRadius,
                                    onValueChange = onLyricsBlurRadiusChange,
                                    valueRange = 0f..20f
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "Inactive Dimming: ${String.format("%.2f", lyricsBlurDimming)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                CustomHorizontalSlider(
                                    value = lyricsBlurDimming,
                                    onValueChange = onLyricsBlurDimmingChange,
                                    valueRange = 0.0f..1.0f
                                )

                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { 
                                        onLyricsBlurRadiusChange(10f)
                                        onLyricsBlurDimmingChange(0.28f)
                                    },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Reset to Defaults")
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
private fun AppearanceHeader(
    onNavigateBack: () -> Unit,
    title: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        JellyIconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Box(modifier = Modifier.size(48.dp)) // Placeholder for balance
    }
}

@Composable
private fun LyricsStyleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
                      else         MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0f),
        animationSpec = tween(200),
        label = "chipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary
                      else         MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "chipText"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .jellyClick { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}


