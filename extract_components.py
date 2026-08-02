import re
import os

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Extract MiniPlayer
miniplayer_match = re.search(r'(@Composable\s+fun MiniPlayer.*?)(?=\n@Composable|\nfun BottomNavigation)', content, re.DOTALL)
miniplayer_code = miniplayer_match.group(1)

# Extract BottomNavigation
bottomnav_match = re.search(r'(@Composable\s+fun BottomNavigation.*?)(?=\n@Composable|\nfun [A-Z]|\Z)', content, re.DOTALL)
bottomnav_code = bottomnav_match.group(1)

# Create ReusableComponents.kt
reusable_code = """package com.example

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild

val AppCornerRadius = 32.dp

fun Modifier.glassEffect(
    hazeState: HazeState?,
    tintTransparency: Float,
    noiseFactor: Float,
    shape: Shape = RoundedCornerShape(AppCornerRadius)
): Modifier = this.let { m ->
    if (hazeState != null) {
        m.hazeChild(
            state = hazeState,
            shape = shape,
            style = HazeStyle(
                blurRadius = 24.dp,
                tint = Color.White.copy(alpha = tintTransparency),
                noiseFactor = noiseFactor
            )
        )
    } else {
        m.background(Color.White.copy(alpha = tintTransparency))
    }
}

@Composable
fun AnimatedGlowBackground(modifier: Modifier = Modifier, glowIntensity: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "glowTransition")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = glowIntensity * 0.75f,
        targetValue = glowIntensity,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val animatedOffsetX by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowOffsetX"
    )
    val animatedOffsetY by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowOffsetY"
    )
    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = animatedOffsetX.dp, y = animatedOffsetY.dp)
                .size(400.dp)
                .blur(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF5E90A7).copy(alpha = animatedAlpha),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
    }
}

"""

# modify MiniPlayer to use glassEffect
miniplayer_new = miniplayer_code
miniplayer_new = re.sub(
    r'\.clip\(RoundedCornerShape\(32\.dp\)\)[\s\S]*?\.clickable',
    r'.clip(RoundedCornerShape(AppCornerRadius))\n            .glassEffect(hazeState, tintTransparency, noiseFactor)\n            .clickable',
    miniplayer_new
)

# modify BottomNavigation to use glassEffect
bottomnav_new = bottomnav_code
bottomnav_new = re.sub(
    r'\.clip\(RoundedCornerShape\(32\.dp\)\)[\s\S]*?\.padding\(horizontal = 24\.dp\)',
    r'.clip(RoundedCornerShape(AppCornerRadius))\n            .glassEffect(hazeState, tintTransparency, noiseFactor)\n            .padding(horizontal = 24.dp)',
    bottomnav_new
)

reusable_code += miniplayer_new + "\n\n" + bottomnav_new + "\n"

with open("app/src/main/java/com/example/ReusableComponents.kt", "w") as f:
    f.write(reusable_code)

# Remove old MiniPlayer and BottomNavigation from MainActivity
content = content.replace(miniplayer_code, "")
content = content.replace(bottomnav_code, "")

# Remove old AnimatedGlowBackground code from MusicHomeScreen
glow_bg_regex = r'// Tint accent with blur overlay[\s\S]*?shape = CircleShape\s*\)\s*\)'
content = re.sub(glow_bg_regex, '', content)

# Inject AnimatedGlowBackground in NavHost level
old_nav_host_start = """
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {"""

new_nav_host_start = """
                    Box(modifier = Modifier.fillMaxSize()) {
                        AnimatedGlowBackground(glowIntensity = glowIntensity)
                        NavHost(
                            navController = navController,
                            startDestination = "home",
                            modifier = Modifier.padding(innerPadding)
                        ) {"""

content = content.replace(old_nav_host_start, new_nav_host_start)

# Add closing bracket for the Box around NavHost
old_nav_host_end = """                        }
                    }

                }"""

new_nav_host_end = """                        }
                    }
                    }

                }"""

content = content.replace(old_nav_host_end, new_nav_host_end)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

