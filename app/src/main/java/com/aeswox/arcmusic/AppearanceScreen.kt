package com.aeswox.arcmusic

import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aeswox.arcmusic.ui.components.CustomHorizontalSlider
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.jelly
import com.aeswox.arcmusic.ui.components.JellyIconButton
import com.aeswox.arcmusic.ui.components.JellyFilledIconButton
import com.aeswox.arcmusic.ui.components.JellyFilledTonalIconButton
import com.aeswox.arcmusic.ui.components.JellyOutlinedIconButton

@Composable
fun AppearanceScreen(
    tintTransparency: Float,
    noiseFactor: Float,
    glowIntensity: Float,
    onTintTransparencyChange: (Float) -> Unit,
    onNoiseFactorChange: (Float) -> Unit,
    onGlowIntensityChange: (Float) -> Unit,
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
                    AppearanceHeader(onNavigateBack = onNavigateBack)
                }
                
                item {
                    SettingsGroup(title = "GLASS PROPERTIES") {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Glass Tint Transparency: ${String.format("%.2f", tintTransparency)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            CustomHorizontalSlider(
                                value = tintTransparency,
                                onValueChange = onTintTransparencyChange,
                                valueRange = 0.0f..0.8f
                            )
                        }
                        
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Monochromatic Noise Factor: ${String.format("%.2f", noiseFactor)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            CustomHorizontalSlider(
                                value = noiseFactor,
                                onValueChange = onNoiseFactorChange,
                                valueRange = 0.0f..0.12f
                            )
                        }
                    }
                }
                
                item {
                    SettingsGroup(title = "BACKGROUND ELEMENTS") {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Glow Intensity: ${String.format("%.2f", glowIntensity)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            CustomHorizontalSlider(
                                value = glowIntensity,
                                onValueChange = onGlowIntensityChange,
                                valueRange = 0.0f..1.0f
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppearanceHeader(modifier: Modifier = Modifier, onNavigateBack: () -> Unit = {}) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        JellyIconButton(onClick = onNavigateBack, modifier = Modifier.padding(end = 8.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                contentDescription = "Back", 
                tint = MaterialTheme.colorScheme.onSurface, 
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = "Appearance", 
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold), 
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
