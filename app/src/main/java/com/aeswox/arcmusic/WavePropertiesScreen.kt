package com.aeswox.arcmusic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
import com.aeswox.arcmusic.ui.components.JellyIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WavePropertiesScreen(
    bottomPadding: PaddingValues = PaddingValues(0.dp),
    onBack: () -> Unit,
    baselineHeight: Float,
    onBaselineHeightChange: (Float) -> Unit,
    waveMaxAmp: Float,
    onWaveMaxAmpChange: (Float) -> Unit,
    cycleLength: Float,
    onCycleLengthChange: (Float) -> Unit,
    shadowOffset: Float,
    onShadowOffsetChange: (Float) -> Unit,
    shadowOpacity: Float,
    onShadowOpacityChange: (Float) -> Unit,
    primaryOpacity: Float,
    onPrimaryOpacityChange: (Float) -> Unit,
    thumbRadius: Float,
    onThumbRadiusChange: (Float) -> Unit,
    unplayedStroke: Float,
    onUnplayedStrokeChange: (Float) -> Unit,
    bloomDuration: Float,
    onBloomDurationChange: (Float) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wave Properties", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    JellyIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .physicsBounceOverscroll()
                .padding(padding)
                .padding(bottom = bottomPadding.calculateBottomPadding()),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Text(
                    text = "Geometry & Size",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }
            
            item {
                WaveSliderItem(
                    title = "Baseline Height (dp)",
                    description = "Thickness of the solid non-wavy track part.",
                    value = baselineHeight,
                    onValueChange = onBaselineHeightChange,
                    valueRange = 0f..15f,
                    steps = 150
                )
            }
            item {
                WaveSliderItem(
                    title = "Wave Max Amplitude (dp)",
                    description = "Max height the wave crest rises above baseline.",
                    value = waveMaxAmp,
                    onValueChange = onWaveMaxAmpChange,
                    valueRange = 0f..10f,
                    steps = 100
                )
            }
            item {
                WaveSliderItem(
                    title = "Cycle Length (dp)",
                    description = "Frequency of the wave. Smaller = more crests.",
                    value = cycleLength,
                    onValueChange = onCycleLengthChange,
                    valueRange = 20f..200f,
                    steps = 180
                )
            }
            
            item {
                Text(
                    text = "Layers & Opacity",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                )
            }
            item {
                WaveSliderItem(
                    title = "Shadow Offset (radians)",
                    description = "Phase shift of the background shadow layer.",
                    value = shadowOffset,
                    onValueChange = onShadowOffsetChange,
                    valueRange = 0f..6.28f,
                    steps = 100
                )
            }
            item {
                WaveSliderItem(
                    title = "Shadow Opacity",
                    description = "Alpha of the background shadow layer.",
                    value = shadowOpacity,
                    onValueChange = onShadowOpacityChange,
                    valueRange = 0f..1f,
                    steps = 100
                )
            }
            item {
                WaveSliderItem(
                    title = "Primary Wave Opacity",
                    description = "Alpha of the main foreground wave.",
                    value = primaryOpacity,
                    onValueChange = onPrimaryOpacityChange,
                    valueRange = 0f..1f,
                    steps = 100
                )
            }
            
            item {
                Text(
                    text = "Seekbar Extras",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                )
            }
            item {
                WaveSliderItem(
                    title = "Thumb Radius (dp)",
                    description = "Size of the draggable circle.",
                    value = thumbRadius,
                    onValueChange = onThumbRadiusChange,
                    valueRange = 0f..15f,
                    steps = 150
                )
            }
            item {
                WaveSliderItem(
                    title = "Unplayed Stroke (dp)",
                    description = "Thickness of the thin line right of the thumb.",
                    value = unplayedStroke,
                    onValueChange = onUnplayedStrokeChange,
                    valueRange = 1f..10f,
                    steps = 90
                )
            }
            item {
                WaveSliderItem(
                    title = "Bloom Duration (ms)",
                    description = "Time it takes to bloom when playing or collapse when paused.",
                    value = bloomDuration,
                    onValueChange = onBloomDurationChange,
                    valueRange = 100f..2000f,
                    steps = 190
                )
            }
        }
    }
}

@Composable
fun WaveSliderItem(
    title: String,
    description: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, end = 8.dp)
                )
            }
            Text(
                text = String.format("%.2f", value),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
