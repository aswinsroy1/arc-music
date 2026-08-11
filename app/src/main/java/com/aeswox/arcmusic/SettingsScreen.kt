package com.aeswox.arcmusic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@Composable
fun SettingsScreen(
    tintTransparency: Float,
    noiseFactor: Float,
    glowIntensity: Float,
    themeMode: ThemeMode,
    lightThemeForNowPlaying: Boolean,
    lastFmApiKey: String?,
    fanartTvApiKey: String?,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLightThemeForNowPlayingChange: (Boolean) -> Unit,
    onLastFmApiKeyChange: (String) -> Unit,
    onFanartTvApiKeyChange: (String) -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateBack: () -> Unit,
    onScanMediaStore: () -> Unit = {},
    onTestEac3: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hazeState = remember { HazeState() }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf("") }
    
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("Last.fm API Key") },
            text = {
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("Enter API Key") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onLastFmApiKeyChange(apiKeyInput)
                    showApiKeyDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) { Text("Cancel") }
            }
        )
    }

    var showFanartTvApiKeyDialog by remember { mutableStateOf(false) }
    var fanartTvApiKeyInput by remember { mutableStateOf("") }

    if (showFanartTvApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showFanartTvApiKeyDialog = false },
            title = { Text("Fanart.tv API Key") },
            text = {
                OutlinedTextField(
                    value = fanartTvApiKeyInput,
                    onValueChange = { fanartTvApiKeyInput = it },
                    label = { Text("Enter API Key") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onFanartTvApiKeyChange(fanartTvApiKeyInput)
                    showFanartTvApiKeyDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showFanartTvApiKeyDialog = false }) { Text("Cancel") }
            }
        )
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .applyHazeAndBackdrop(hazeState = hazeState)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(top = 24.dp, bottom = 180.dp, start = 24.dp, end = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    SettingsHeader(onNavigateBack = onNavigateBack)
                }
                
                item {
                    SettingsGroup(title = "PLAYBACK") {
                        SettingsItem(icon = Icons.Outlined.MusicNote, text = "Playback", enabled = false)
                        SettingsItem(icon = Icons.Outlined.GraphicEq, text = "Audio quality", enabled = false)
                        SettingsItem(icon = Icons.Outlined.Tune, text = "Equalizer", onClick = onNavigateToEqualizer)
                        SettingsItem(icon = Icons.Outlined.Bedtime, text = "Sleep timer", enabled = false)
                        SettingsItem(icon = Icons.Outlined.Shuffle, text = "Crossfade", trailingText = "Off", enabled = false)
                    }
                }
                
                item {
                    SettingsGroup(title = "APPEARANCE") {
                        SettingsItem(icon = Icons.Outlined.Brush, text = "Appearance", onClick = onNavigateToAppearance)
                        SettingsItem(
                            icon = Icons.Outlined.DarkMode, 
                            text = "Dark mode", 
                            trailingContent = {
                                Switch(
                                    checked = themeMode == ThemeMode.Dark, 
                                    onCheckedChange = { isDark -> 
                                        onThemeModeChange(if (isDark) ThemeMode.Dark else ThemeMode.Light)
                                    }
                                )
                            },
                            showArrow = false
                        )
                        SettingsItem(
                            icon = Icons.Outlined.LightMode, 
                            text = "Dynamic colors", 
                            trailingContent = {
                                Switch(
                                    checked = true, 
                                    onCheckedChange = {},
                                    enabled = false
                                )
                            },
                            showArrow = false,
                            enabled = false
                        )
                        SettingsItem(
                            icon = Icons.Outlined.LightMode, 
                            text = "Light theme for now playing", 
                            trailingContent = {
                                Switch(
                                    checked = lightThemeForNowPlaying, 
                                    onCheckedChange = { onLightThemeForNowPlayingChange(it) }
                                )
                            },
                            showArrow = false
                        )
                        SettingsItem(icon = Icons.Outlined.Apps, text = "App icon", enabled = false)
                    }
                }
                
                item {
                    SettingsGroup(title = "INTEGRATIONS") {
                        val isKeySet = !lastFmApiKey.isNullOrBlank()
                        val maskedKey = if (isKeySet) "••••" + lastFmApiKey!!.takeLast(4.coerceAtMost(lastFmApiKey.length)) else "Not set"
                        SettingsItem(
                            icon = Icons.Outlined.Key,
                            text = "Last.fm API Key",
                            trailingText = maskedKey,
                            onClick = {
                                apiKeyInput = lastFmApiKey ?: ""
                                showApiKeyDialog = true
                            },
                            showArrow = false
                        )
                        
                        val isFanartKeySet = !fanartTvApiKey.isNullOrBlank()
                        val maskedFanartKey = if (isFanartKeySet) "••••" + fanartTvApiKey!!.takeLast(4.coerceAtMost(fanartTvApiKey.length)) else "Not set"
                        SettingsItem(
                            icon = Icons.Outlined.Key,
                            text = "Fanart.tv API Key",
                            trailingText = maskedFanartKey,
                            onClick = {
                                fanartTvApiKeyInput = fanartTvApiKey ?: ""
                                showFanartTvApiKeyDialog = true
                            },
                            showArrow = false
                        )
                    }
                }

                item {
                    SettingsGroup(title = "DEVELOPER") {
                        SettingsItem(
                            icon = Icons.Outlined.Storage,
                            text = "Scan MediaStore",
                            onClick = onScanMediaStore,
                            showArrow = false
                        )
                        SettingsItem(
                            icon = Icons.Outlined.GraphicEq,
                            text = "Test EAC3 Playback",
                            onClick = onTestEac3,
                            showArrow = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsHeader(modifier: Modifier = Modifier, onNavigateBack: () -> Unit = {}) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Settings", 
            style = MaterialTheme.typography.displayLarge, 
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


@Composable
fun SettingsGroup(modifier: Modifier = Modifier, title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title, 
            style = MaterialTheme.typography.labelSmall, 
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppCornerRadius))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f))
                .padding(vertical = 8.dp),
            content = content
        )
    }
}

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    trailingText: String? = null,
    showArrow: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .alpha(if (enabled) 1f else 0.5f)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f))
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text, 
            style = MaterialTheme.typography.bodyLarge, 
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (trailingText != null) {
            Text(
                text = trailingText, 
                style = MaterialTheme.typography.bodyMedium, 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        if (trailingContent != null) {
            trailingContent()
        } else if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
