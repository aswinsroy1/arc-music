package com.aeswox.arcmusic

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.jelly
import com.aeswox.arcmusic.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerContent(
    isActive: Boolean,
    timeLeft: Long,
    pauseWhenSongEnd: Boolean,
    onDismiss: () -> Unit,
    onStart: (Int, Boolean) -> Unit,
    onClear: () -> Unit
) {
    // -1 represents "End of track", positive numbers represent minutes
    var selectedPreset by remember { 
        mutableIntStateOf(if (isActive && pauseWhenSongEnd) -1 else if (isActive) (timeLeft / 60000).toInt() else 30) 
    }
    
    var isCustomView by remember { mutableStateOf(false) }
    
    var customHours by remember { mutableIntStateOf(selectedPreset.coerceAtLeast(0) / 60) }
    var customMins by remember { mutableIntStateOf(selectedPreset.coerceAtLeast(0) % 60) }
    var finishCurrentSong by remember { mutableStateOf(false) }

    BackHandler(enabled = isCustomView) {
        isCustomView = false
    }

    AnimatedContent(
        targetState = isCustomView,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
            fadeOut(animationSpec = tween(300)) using
            SizeTransform { initialSize, targetSize ->
                tween(durationMillis = 300)
            }
        },
        label = "SleepTimerCustomViewMorph"
    ) { custom ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (custom) "Custom duration" else "Sleep timer",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    )
                )
                
                JellyIconButton(
                    onClick = { 
                        if (custom) isCustomView = false 
                        else onDismiss() 
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Icon(
                        imageVector = if (custom) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                        contentDescription = if (custom) "Back" else "Close",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (custom) {
                // Custom Timer View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Hours
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            JellyIconButton(onClick = { if (customHours < 23) customHours++ else customHours = 0 }) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = String.format("%02d", customHours),
                                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            JellyIconButton(onClick = { if (customHours > 0) customHours-- else customHours = 23 }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = "HOURS",
                                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)
                        )

                        // Mins
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            JellyIconButton(onClick = { if (customMins < 59) customMins++ else customMins = 0 }) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = String.format("%02d", customMins),
                                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            JellyIconButton(onClick = { if (customMins > 0) customMins-- else customMins = 59 }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = "MINS",
                                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Audio will pause after the selected duration.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
            } else {
                // Default View
                // Timer display
                Text(
                    text = "Timer set for",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                ) {
                    if (selectedPreset == -1) {
                        Text(
                            text = "End of track",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            )
                        )
                    } else {
                        Text(
                            text = selectedPreset.toString(),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 48.sp
                            )
                        )
                        Text(
                            text = " min",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                // Presets Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PresetBlock(
                        modifier = Modifier.weight(1f),
                        title = "15",
                        subtitle = "min",
                        isSelected = selectedPreset == 15,
                        onClick = { selectedPreset = 15 }
                    )
                    PresetBlock(
                        modifier = Modifier.weight(1f),
                        title = "30",
                        subtitle = "min",
                        isSelected = selectedPreset == 30,
                        onClick = { selectedPreset = 30 }
                    )
                    PresetBlock(
                        modifier = Modifier.weight(1f),
                        title = "45",
                        subtitle = "min",
                        isSelected = selectedPreset == 45,
                        onClick = { selectedPreset = 45 }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PresetBlock(
                        modifier = Modifier.weight(1f),
                        title = "1",
                        subtitle = "hour",
                        isSelected = selectedPreset == 60,
                        onClick = { selectedPreset = 60 }
                    )
                    
                    // End of track (spans 2 columns)
                    val isEotSelected = selectedPreset == -1
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .height(110.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isEotSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            .jellyClick { selectedPreset = -1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DarkMode,
                                contentDescription = "End of track",
                                tint = if (isEotSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp).padding(bottom = 8.dp)
                            )
                            Text(
                                text = "End of track",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = if (isEotSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        .jellyClick { 
                            customHours = selectedPreset.coerceAtLeast(0) / 60
                            customMins = selectedPreset.coerceAtLeast(0) % 60
                            isCustomView = true 
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "Custom duration",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Custom duration",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Go",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = custom || selectedPreset != -1,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Finish current song",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "When the timer ends, pause after this song.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = finishCurrentSong,
                            onCheckedChange = { finishCurrentSong = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Button
            if (custom) {
                JellyButton(
                    onClick = { 
                        val totalMins = customHours * 60 + customMins
                        if (totalMins > 0) onStart(totalMins, finishCurrentSong)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Set Timer",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            } else {
                AnimatedContent(
                    targetState = isActive,
                    transitionSpec = {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(300)) using SizeTransform { _, _ -> tween(300) }
                    },
                    label = "TimerActionPillMorph"
                ) { active ->
                    if (active) {
                        var expanded by remember { mutableStateOf(false) }
                        val remainingMins = (timeLeft / 60000).toInt()
                        val remainingSecs = ((timeLeft % 60000) / 1000).toInt()
                        
                        val pillWidthFraction by androidx.compose.animation.core.animateFloatAsState(
                            targetValue = if (expanded) 1f else 0.85f, 
                            label = "PillWidth",
                            animationSpec = tween(300)
                        )
                        
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Pill
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(pillWidthFraction)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .jellyClick { expanded = !expanded }
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "Timer Active",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "${String.format("%02d:%02d", remainingMins, remainingSecs)} remaining",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                
                                JellyIconButton(
                                    onClick = { onClear() },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel Timer",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Expanded Options
                            AnimatedVisibility(
                                visible = expanded,
                                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    JellyButton(
                                        onClick = { onStart(remainingMins + 5, finishCurrentSong) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Text("+5 min", fontWeight = FontWeight.Bold)
                                    }
                                    JellyButton(
                                        onClick = { onStart(remainingMins + 15, finishCurrentSong) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Text("+15 min", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        JellyButton(
                            onClick = { onStart(selectedPreset, finishCurrentSong) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSurface,
                                contentColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text(
                                text = "Start Timer",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PresetBlock(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            .jellyClick { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.surface.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
