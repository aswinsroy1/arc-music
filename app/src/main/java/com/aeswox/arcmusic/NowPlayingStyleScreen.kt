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
import androidx.compose.ui.unit.sp
import com.aeswox.arcmusic.data.model.NowPlayingStyle
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
import com.aeswox.arcmusic.ui.components.CustomHorizontalSlider
import com.aeswox.arcmusic.ui.components.JellyIconButton
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingStyleScreen(
    nowPlayingStyle: NowPlayingStyle,
    onNowPlayingStyleChange: (NowPlayingStyle) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hazeState = remember { HazeState() }
    
    // Placeholder states for aesthetic
    var dummySlider1 by remember { mutableFloatStateOf(0.5f) }
    var dummySlider2 by remember { mutableFloatStateOf(0.7f) }
    var dummyToggle by remember { mutableStateOf(true) }

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
                    AppearanceHeader(onNavigateBack = onNavigateBack, title = "Now Playing Style")
                }
                
                item {
                    // Centered Arc / Fruit Selector
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
                            NowPlayingStyleChip(
                                label = "Arc",
                                selected = nowPlayingStyle == NowPlayingStyle.ARC,
                                onClick = { onNowPlayingStyleChange(NowPlayingStyle.ARC) }
                            )
                            NowPlayingStyleChip(
                                label = "Fruit",
                                selected = nowPlayingStyle == NowPlayingStyle.FRUIT,
                                onClick = { onNowPlayingStyleChange(NowPlayingStyle.FRUIT) }
                            )
                        }
                    }
                }

                item {
                    SettingsGroup(title = "GENERAL OPTIONS") {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Enable Dynamic Background",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = dummyToggle,
                                    onCheckedChange = { dummyToggle = it }
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Album Art Scale: ${String.format("%.2f", dummySlider1)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            CustomHorizontalSlider(
                                value = dummySlider1,
                                onValueChange = { dummySlider1 = it },
                                valueRange = 0.5f..1.5f
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Blur Intensity: ${String.format("%.2f", dummySlider2)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            CustomHorizontalSlider(
                                value = dummySlider2,
                                onValueChange = { dummySlider2 = it },
                                valueRange = 0.0f..1.0f
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { 
                                    dummySlider1 = 1.0f
                                    dummySlider2 = 0.5f
                                    dummyToggle = true
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
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Box(modifier = Modifier.size(48.dp)) // Placeholder for balance
    }
}

@Composable
private fun NowPlayingStyleChip(
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
