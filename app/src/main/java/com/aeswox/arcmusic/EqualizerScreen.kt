package com.aeswox.arcmusic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aeswox.arcmusic.playback.EqualizerViewModel
import com.aeswox.arcmusic.ui.components.CustomVerticalSlider
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.jelly
import com.aeswox.arcmusic.ui.components.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EqualizerScreen(
    onNavigateBack: () -> Unit,
    viewModel: EqualizerViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val isEnabled by viewModel.isEnabled.collectAsState()
    val bandLevels by viewModel.bandLevels.collectAsState()

    val freqLabels = listOf(
        "31", "62", "125", "250", "500",
        "1k", "2k", "4k", "8k", "16k"
    )

    val presets = mapOf(
        "Bass Boost" to listOf(6, 4, 2, 0, 0, 0, 0, 0, 0, 0),
        "Flat" to listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "Acoustic" to listOf(3, 2, 0, 1, 1, 1, 0, 0, 2, 3),
        "Electronic" to listOf(4, 3, 1, 0, -2, 2, 1, 0, 3, 4),
        "Rock" to listOf(5, 3, 0, -1, -2, -1, 0, 2, 3, 4),
        "Jazz" to listOf(3, 2, 0, 2, -1, -1, 0, 1, 2, 3)
    )
    
    var selectedPreset by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Equalizer", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    JellyIconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { viewModel.setEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            checkedIconColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                            uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Equalizer Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(32.dp), spotColor = Color(0x1A000000)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                val pagerState = rememberPagerState(pageCount = { 2 })
                Column(
                    modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Y-Axis Labels
                        Column(
                            modifier = Modifier
                                .height(220.dp)
                                .padding(end = 16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("+12dB", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("0dB", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("-12dB", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Sliders Pager
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.weight(1f)
                        ) { page ->
                            val startIdx = page * 5
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (i in startIdx until startIdx + 5) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CustomVerticalSlider(
                                            value = bandLevels.getOrElse(i) { 0 },
                                            onValueChange = { 
                                                viewModel.setBandLevel(i, it)
                                                selectedPreset = null 
                                            },
                                            enabled = isEnabled,
                                            modifier = Modifier.height(220.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = freqLabels.getOrElse(i) { "" },
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Pager Indicator
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (pagerState.currentPage == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (pagerState.currentPage == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Presets Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Presets",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presets.keys.forEach { presetName ->
                        val isSelected = selectedPreset == presetName
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .jellyClick {
                                    if (isEnabled) {
                                        selectedPreset = presetName
                                        presets[presetName]?.let { viewModel.applyPreset(it) }
                                    }
                                }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = presetName,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                JellyOutlinedButton(
                    onClick = { 
                        if (isEnabled) {
                            viewModel.applyPreset(presets["Flat"]!!)
                            selectedPreset = "Flat"
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("Reset", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }

                JellyButton(
                    onClick = { /* Save custom preset logic */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Save Preset", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
