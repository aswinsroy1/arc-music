package com.aeswox.arcmusic

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aeswox.arcmusic.playback.EqualizerViewModel
import com.aeswox.arcmusic.ui.components.CustomVerticalSlider
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.components.*

// ─────────────────────────────────────────────────────────
// Preset data  (calibrated to the ±15 dB scale)
// ─────────────────────────────────────────────────────────
private data class EqPreset(val name: String, val levels: List<Int>)

private val EQ_PRESETS = listOf(
    EqPreset("Flat",          listOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0)),
    EqPreset("Bass Boost",    listOf( 7,  9,  6,  3,  0,  0,  0,  0,  0,  0)),
    EqPreset("Treble Boost",  listOf( 0,  0,  0,  0,  0,  1,  3,  6,  8,  9)),
    EqPreset("Rock",          listOf( 5,  4,  3,  1, -1, -1,  1,  3,  4,  5)),
    EqPreset("Pop",           listOf(-1,  2,  4,  5,  5,  4,  2,  1,  2,  2)),
    EqPreset("Hip Hop",       listOf( 6,  8,  4,  1, -1, -1,  1,  1,  3,  4)),
    EqPreset("Jazz",          listOf( 3,  2,  1,  2, -1, -1,  0,  2,  3,  4)),
    EqPreset("Classical",     listOf( 4,  3,  2,  1, -1, -1,  0,  2,  4,  4)),
    EqPreset("Electronic",    listOf( 5,  6,  2,  0, -1,  1,  0,  2,  6,  7)),
    EqPreset("Vocal",         listOf(-3, -2, -1,  2,  5,  6,  5,  3,  1,  0)),
    EqPreset("Acoustic",      listOf( 3,  2,  0,  1,  1,  1,  0,  0,  2,  3)),
)

private val FREQ_LABELS = listOf(
    "31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k"
)

// ─────────────────────────────────────────────────────────
// Main Screen
// ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EqualizerScreen(
    onNavigateBack: () -> Unit,
    viewModel: EqualizerViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val isEnabled by viewModel.isEnabled.collectAsState()
    val bandLevels by viewModel.bandLevels.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val selectedPreset = remember(bandLevels) {
        EQ_PRESETS.firstOrNull { it.levels == bandLevels }?.name
    }

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
                    // Animated power toggle
                    val containerColor by animateColorAsState(
                        targetValue = if (isEnabled) MaterialTheme.colorScheme.primary
                                      else MaterialTheme.colorScheme.surfaceVariant,
                        label = "EqPowerBtn"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isEnabled) MaterialTheme.colorScheme.onPrimary
                                      else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "EqPowerBtnContent"
                    )
                    FilledIconButton(
                        onClick = { viewModel.setEnabled(!isEnabled) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = containerColor,
                            contentColor = contentColor
                        ),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(Icons.Rounded.PowerSettingsNew, contentDescription = if (isEnabled) "Disable EQ" else "Enable EQ")
                    }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // ── Equalizer Sliders Card ──────────────────────
            EqSlidersCard(
                bandLevels = bandLevels,
                isEnabled = isEnabled,
                onBandChanged = { i, v ->
                    viewModel.setBandLevel(i, v)
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Presets ─────────────────────────────────────
            PresetsSection(
                presets = EQ_PRESETS,
                selectedPreset = selectedPreset,
                isEnabled = isEnabled,
                onPresetSelected = { preset ->
                    viewModel.applyPreset(preset.levels)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Audio Effects ────────────────────────────────
            if (uiState.isBassBoostSupported || uiState.isVirtualizerSupported || uiState.isLoudnessEnhancerSupported) {
                Text(
                    text = "Audio Effects",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState.isBassBoostSupported) {
                EffectCard(
                    title = "Bass Boost",
                    subtitle = "Enhance low frequencies",
                    icon = Icons.Rounded.GraphicEq,
                    enabled = uiState.bassBoostEnabled,
                    strength = uiState.bassBoostStrength.toFloat(),
                    strengthRange = 0f..1000f,
                    onEnabledChange = { viewModel.setBassBoostEnabled(it) },
                    onStrengthChange = { viewModel.setBassBoostStrength(it.toInt()) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState.isVirtualizerSupported) {
                EffectCard(
                    title = "Surround Sound",
                    subtitle = "Widen the stereo field",
                    icon = Icons.Rounded.GraphicEq,
                    enabled = uiState.virtualizerEnabled,
                    strength = uiState.virtualizerStrength.toFloat(),
                    strengthRange = 0f..1000f,
                    onEnabledChange = { viewModel.setVirtualizerEnabled(it) },
                    onStrengthChange = { viewModel.setVirtualizerStrength(it.toInt()) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState.isLoudnessEnhancerSupported) {
                EffectCard(
                    title = "Loudness Enhancer",
                    subtitle = "Boost perceived volume",
                    icon = Icons.Rounded.GraphicEq,
                    enabled = uiState.loudnessEnabled,
                    strength = uiState.loudnessStrength.toFloat(),
                    strengthRange = 0f..1000f,
                    onEnabledChange = { viewModel.setLoudnessEnabled(it) },
                    onStrengthChange = { viewModel.setLoudnessStrength(it.toInt()) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Reset Button ─────────────────────────────────
            Spacer(modifier = Modifier.height(12.dp))
            JellyOutlinedButton(
                onClick = {
                    viewModel.applyPreset(EQ_PRESETS.first { it.name == "Flat" }.levels)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("Reset to Flat", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────
// EQ Sliders Card
// ─────────────────────────────────────────────────────────
@Composable
private fun EqSlidersCard(
    bandLevels: List<Int>,
    isEnabled: Boolean,
    onBandChanged: (Int, Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(28.dp), spotColor = Color(0x18000000)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(top = 24.dp, bottom = 20.dp, start = 12.dp, end = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Y-axis labels
                Column(
                    modifier = Modifier
                        .height(240.dp)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text("+15", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("+8",  fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("0",   fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("-8",  fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("-15", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

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
                            val level = bandLevels.getOrElse(i) { 0 }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // dB value label
                                Text(
                                    text = if (level > 0) "+$level" else "$level",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = when {
                                        !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        level > 0  -> MaterialTheme.colorScheme.primary
                                        level < 0  -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                        else       -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                CustomVerticalSlider(
                                    value = level,
                                    onValueChange = { onBandChanged(i, it) },
                                    valueRange = -15..15,
                                    enabled = isEnabled,
                                    modifier = Modifier.height(200.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = FREQ_LABELS.getOrElse(i) { "" },
                                    fontSize = 11.sp,
                                    color = if (isEnabled)
                                        MaterialTheme.colorScheme.onSurface
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pager indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(2) { idx ->
                    val isSelected = pagerState.currentPage == idx
                    val size by animateDpAsState(
                        targetValue = if (isSelected) 20.dp else 6.dp,
                        animationSpec = spring(Spring.DampingRatioMediumBouncy),
                        label = "PagerDot"
                    )
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(size)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                            )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// Presets Section
// ─────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetsSection(
    presets: List<EqPreset>,
    selectedPreset: String?,
    isEnabled: Boolean,
    onPresetSelected: (EqPreset) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Presets",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            presets.forEach { preset ->
                val isSelected = selectedPreset == preset.name

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                                  else MaterialTheme.colorScheme.surfaceContainerHighest,
                    label = "PresetBg_${preset.name}"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                  else MaterialTheme.colorScheme.onSurface,
                    label = "PresetText_${preset.name}"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(bgColor)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .jellyClick { onPresetSelected(preset) }
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preset.name,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// Effect Card  (Bass Boost / Virtualizer / Loudness)
// ─────────────────────────────────────────────────────────
@Composable
private fun EffectCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    enabled: Boolean,
    strength: Float,
    strengthRange: ClosedFloatingPointRange<Float>,
    onEnabledChange: (Boolean) -> Unit,
    onStrengthChange: (Float) -> Unit
) {
    val cardAlpha = if (enabled) 1f else 0.55f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (enabled) 8.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 0.15f else 0f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (enabled) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = cardAlpha)
                        )
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = cardAlpha)
                        )
                    }
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }

            if (enabled) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        value = strength,
                        onValueChange = onStrengthChange,
                        valueRange = strengthRange,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${(strength / strengthRange.endInclusive * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }
        }
    }
}
