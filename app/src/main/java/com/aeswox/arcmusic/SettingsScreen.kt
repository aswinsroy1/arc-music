package com.aeswox.arcmusic

import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.jelly
import com.aeswox.arcmusic.ui.components.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.items
import com.aeswox.arcmusic.data.model.LyricsDisplayStyle
import com.aeswox.arcmusic.db.entities.Playlist

@Composable
fun SettingsScreen(
    tintTransparency: Float,
    noiseFactor: Float,
    glowIntensity: Float,
    themeMode: ThemeMode,
    lightThemeForNowPlaying: Boolean,
    lyricsDisplayStyle: LyricsDisplayStyle,
    lastFmApiKey: String?,
    fanartTvApiKey: String?,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLightThemeForNowPlayingChange: (Boolean) -> Unit,
    onLyricsDisplayStyleChange: (LyricsDisplayStyle) -> Unit,
    onLastFmApiKeyChange: (String) -> Unit,
    onFanartTvApiKeyChange: (String) -> Unit,
    coilDiskCacheLimitMb: Int,
    onCoilDiskCacheLimitMbChange: (Int) -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToJigglePhysics: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToMediaManagement: () -> Unit,
    onNavigateToLyricStyleSettings: () -> Unit,
    onNavigateToCanvasSettings: () -> Unit,
    onNavigateBack: () -> Unit,
    onScanMediaStore: () -> Unit = {},
    onRunDeepScan: () -> Unit = {},
    onTestEac3: () -> Unit = {},
    onImportM3u: (android.net.Uri) -> Unit = {},
    onExportM3u: (android.net.Uri, String) -> Unit = { _, _ -> },
    playlists: List<Playlist> = emptyList(),
    canvasEnabled: Boolean = true,
    onCanvasEnabledChange: (Boolean) -> Unit = {},
    bottomPadding: androidx.compose.ui.unit.Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    val hazeState = remember { HazeState() }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf("") }
    
    var showExportPlaylistDialog by remember { mutableStateOf(false) }
    var playlistToExport by remember { mutableStateOf<String?>(null) }
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportM3u(uri)
        }
    }
    
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("audio/mpegurl")
    ) { uri ->
        if (uri != null && playlistToExport != null) {
            onExportM3u(uri, playlistToExport!!)
        }
    }
    
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
                JellyTextButton(onClick = {
                    onLastFmApiKeyChange(apiKeyInput)
                    showApiKeyDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                JellyTextButton(onClick = { showApiKeyDialog = false }) { Text("Cancel") }
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
                JellyTextButton(onClick = {
                    onFanartTvApiKeyChange(fanartTvApiKeyInput)
                    showFanartTvApiKeyDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                JellyTextButton(onClick = { showFanartTvApiKeyDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showExportPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showExportPlaylistDialog = false },
            title = { Text("Select Playlist to Export") },
            text = {
                if (playlists.isEmpty()) {
                    Text("No playlists available")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(playlists) { playlist ->
                            TextButton(
                                onClick = {
                                    playlistToExport = playlist.id
                                    exportLauncher.launch("${playlist.name}.m3u")
                                    showExportPlaylistDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(playlist.name, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportPlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Transparent)) {
        Box(
            modifier = Modifier
                .fillMaxSize()

        ) {
            LazyColumn(
                contentPadding = PaddingValues(top = 24.dp, bottom = bottomPadding, start = 24.dp, end = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.physicsBounceOverscroll().fillMaxSize()
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
                        SettingsItem(icon = Icons.Outlined.Animation, text = "Jiggle physics", onClick = onNavigateToJigglePhysics)
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
                        SettingsItem(
                            icon = Icons.Outlined.MusicNote,
                            text = "Lyrics style",
                            onClick = onNavigateToLyricStyleSettings,
                            showArrow = true,
                            trailingContent = {
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
                        )
                        SettingsItem(
                            icon = Icons.Outlined.PlayCircle,
                            text = "Canvas",
                            trailingText = "Animated artwork",
                            onClick = onNavigateToCanvasSettings,
                            showArrow = true
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
                    SettingsGroup(title = "LIBRARY") {
                        SettingsItem(
                            icon = Icons.Outlined.Folder,
                            text = "Media Management",
                            onClick = onNavigateToMediaManagement,
                            showArrow = true
                        )
                        SettingsItem(
                            icon = Icons.Outlined.Download,
                            text = "Import M3U Playlist",
                            onClick = { importLauncher.launch(arrayOf("*/*")) },
                            showArrow = false
                        )
                        SettingsItem(
                            icon = Icons.Outlined.Upload,
                            text = "Export M3U Playlist",
                            onClick = { showExportPlaylistDialog = true },
                            showArrow = false
                        )
                    }
                }
                
                item {
                    SettingsGroup(title = "DATA & STORAGE") {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Storage, 
                                            contentDescription = null, 
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "Image Cache Limit",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Requires app restart",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                val displayValue = if (coilDiskCacheLimitMb >= 1000) {
                                    String.format("%.1f GB", coilDiskCacheLimitMb / 1000f)
                                } else {
                                    "${coilDiskCacheLimitMb} MB"
                                }
                                Text(
                                    text = displayValue,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Slider(
                                value = coilDiskCacheLimitMb.toFloat(),
                                onValueChange = { onCoilDiskCacheLimitMbChange(it.toInt()) },
                                valueRange = 250f..5000f,
                                steps = 18, // (5000 - 250) / 250 = 19 points -> 18 steps
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
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

                item {
                    SettingsGroup(title = "ABOUT") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.Black)
                            ) {
                                Image(
                                    painter = painterResource(id = R.mipmap.ic_launcher_background),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Image(
                                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                                    contentDescription = "Arc Music Logo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Arc Music",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Version 1.0 (Build 1)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "High-Fidelity Audio Player",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsHeader(modifier: Modifier = Modifier, title: String = "Settings", fontSize: androidx.compose.ui.unit.TextUnit = 34.sp, onNavigateBack: () -> Unit = {}) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title, 
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            ), 
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
            .jellyClick(enabled = enabled) { onClick() }
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

/**
 * A small animated chip used in the Lyrics Style row of Settings.
 * Highlights with the theme's primary colour when selected.
 */
@Composable
private fun LyricsStyleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
                      else         MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0f),
        animationSpec = androidx.compose.animation.core.tween(200),
        label = "chipBg"
    )
    val textColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary
                      else         MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = androidx.compose.animation.core.tween(200),
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
