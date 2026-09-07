package com.aeswox.arcmusic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aeswox.arcmusic.ui.components.CustomHorizontalSlider
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasSettingsScreen(
    canvasEnabled: Boolean,
    onCanvasEnabledChange: (Boolean) -> Unit,
    cacheLimitMb: Int,
    onCacheLimitMbChange: (Int) -> Unit,
    currentCacheSizeMb: Long,
    onClearCache: () -> Unit,
    onFetchCanvases: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hazeState = remember { HazeState() }
    
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().haze(state = hazeState),
            contentPadding = PaddingValues(top = 100.dp, bottom = 40.dp)
        ) {
            item {
                SettingsGroup(title = "GENERAL") {
                    SettingsItem(
                        icon = Icons.Outlined.PlayCircle,
                        text = "Canvas",
                        trailingText = "Animated artwork",
                        trailingContent = {
                            Switch(
                                checked = canvasEnabled,
                                onCheckedChange = { onCanvasEnabledChange(it) }
                            )
                        },
                        showArrow = false
                    )
                }
            }

            item {
                SettingsGroup(title = "STORAGE") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Storage,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Cache limit",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${cacheLimitMb} MB",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        
                        var sliderPosition by remember(cacheLimitMb) { mutableStateOf(cacheLimitMb.toFloat()) }
                        CustomHorizontalSlider(
                            value = sliderPosition,
                            onValueChange = { 
                                sliderPosition = it
                                onCacheLimitMbChange(it.toInt())
                            },
                            valueRange = 250f..5000f,
                            steps = 18, 
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        
                        Text(
                            text = "Currently using ${currentCacheSizeMb} MB",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 40.dp)
                        )
                    }

                    SettingsItem(
                        icon = Icons.Outlined.DeleteOutline,
                        text = "Clear Cache",
                        onClick = onClearCache,
                        showArrow = false
                    )
                }
            }
            
            item {
                SettingsGroup(title = "OFFLINE SYNC") {
                    SettingsItem(
                        icon = Icons.Outlined.Download,
                        text = "Fetch All Canvases",
                        trailingText = "Downloads videos for offline playback",
                        onClick = onFetchCanvases,
                        showArrow = false
                    )
                }
            }
        }
        
        TopAppBar(
            title = {
                Text(
                    text = "Canvas Settings",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
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
