package com.aeswox.arcmusic

import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.components.CustomHorizontalSlider
import com.aeswox.arcmusic.ui.components.JellyButton
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import dev.chrisbanes.haze.HazeState

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun JigglePhysicsScreen(
    physicsMass: Float,
    physicsStiffness: Float,
    physicsDampingRatio: Float,
    physicsAmplitude: Float,
    physicsGravity: Float,
    onPhysicsMassChange: (Float) -> Unit,
    onPhysicsStiffnessChange: (Float) -> Unit,
    onPhysicsDampingRatioChange: (Float) -> Unit,
    onPhysicsAmplitudeChange: (Float) -> Unit,
    onPhysicsGravityChange: (Float) -> Unit,
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

        ) {
            LazyColumn(
                contentPadding = PaddingValues(top = 24.dp, bottom = 180.dp, start = 24.dp, end = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.physicsBounceOverscroll().fillMaxSize()
            ) {
                item {
                    SettingsHeader(
                        title = "Jiggle physics",
                        fontSize = 30.sp,
                        onNavigateBack = onNavigateBack
                    )
                }

                item {
                    SettingsGroup(title = "PHYSICS PARAMETERS") {
                        SliderSettingItem(
                            title = "Mass",
                            value = physicsMass,
                            onValueChange = { onPhysicsMassChange(Math.round(it * 10f) / 10f) },
                            valueRange = 0.1f..1.0f,
                            steps = 8
                        )
                        SliderSettingItem(
                            title = "Stiffness",
                            value = physicsStiffness,
                            onValueChange = { onPhysicsStiffnessChange(Math.round(it / 10f) * 10f) },
                            valueRange = 10.0f..200.0f,
                            steps = 18
                        )
                        SliderSettingItem(
                            title = "Damping Ratio",
                            value = physicsDampingRatio,
                            onValueChange = { onPhysicsDampingRatioChange(Math.round(it * 20f) / 20f) },
                            valueRange = 0.1f..0.5f,
                            steps = 7
                        )
                        SliderSettingItem(
                            title = "Impact Amplitude",
                            value = physicsAmplitude,
                            onValueChange = { onPhysicsAmplitudeChange(Math.round(it * 5f) / 5f) },
                            valueRange = 0.1f..2.0f,
                            steps = 18
                        )
                    }
                }

                item {
                    SettingsGroup(title = "GRAVITY") {
                        val gravityPresets = mapOf(
                            "Earth" to 9.81f,
                            "Mars" to 3.72f,
                            "Moon" to 1.62f,
                            "Pluto" to 0.62f
                        )
                        
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            gravityPresets.forEach { (presetName, presetValue) ->
                                val isSelected = Math.abs(physicsGravity - presetValue) < 0.01f
                                Box(
                                    modifier = Modifier
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                                        )
                                        .jellyClick {
                                            onPhysicsGravityChange(presetValue)
                                        }
                                        .padding(horizontal = 20.dp, vertical = 12.dp),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
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
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    JellyButton(
                        onClick = {
                            onPhysicsMassChange(0.2f)
                            onPhysicsStiffnessChange(100.0f)
                            onPhysicsDampingRatioChange(0.25f)
                            onPhysicsAmplitudeChange(1.0f)
                            onPhysicsGravityChange(9.81f)
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface,
                            contentColor = MaterialTheme.colorScheme.surface
                        ),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Text(
                            text = "Reset to Defaults",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SliderSettingItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = String.format("%.2f", value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        CustomHorizontalSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}
