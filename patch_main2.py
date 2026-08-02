import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Add animation imports
animation_imports = """
import androidx.compose.animation.core.*
"""

content = content.replace("import androidx.compose.runtime.remember", "import androidx.compose.runtime.remember\n" + animation_imports)

# Update nav host and screen calls
old_nav_host_vars = """
                    val navController = rememberNavController()
                    val viewModel: MusicViewModel = viewModel()
                    val tintTransparency by viewModel.tintTransparency.collectAsState()
                    val noiseFactor by viewModel.noiseFactor.collectAsState()
"""

new_nav_host_vars = """
                    val navController = rememberNavController()
                    val viewModel: MusicViewModel = viewModel()
                    val tintTransparency by viewModel.tintTransparency.collectAsState()
                    val noiseFactor by viewModel.noiseFactor.collectAsState()
                    val glowIntensity by viewModel.glowIntensity.collectAsState()
"""

content = content.replace(old_nav_host_vars, new_nav_host_vars)

old_music_home = """
                            MusicHomeScreen(
                                tintTransparency = tintTransparency,
                                noiseFactor = noiseFactor,
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
"""

new_music_home = """
                            MusicHomeScreen(
                                tintTransparency = tintTransparency,
                                noiseFactor = noiseFactor,
                                glowIntensity = glowIntensity,
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
"""

content = content.replace(old_music_home, new_music_home)

old_settings = """
                            SettingsScreen(
                                tintTransparency = tintTransparency,
                                noiseFactor = noiseFactor,
                                onTintTransparencyChange = { viewModel.setTintTransparency(it) },
                                onNoiseFactorChange = { viewModel.setNoiseFactor(it) },
                                onNavigateBack = { navController.popBackStack() }
                            )
"""

new_settings = """
                            SettingsScreen(
                                tintTransparency = tintTransparency,
                                noiseFactor = noiseFactor,
                                glowIntensity = glowIntensity,
                                onTintTransparencyChange = { viewModel.setTintTransparency(it) },
                                onNoiseFactorChange = { viewModel.setNoiseFactor(it) },
                                onGlowIntensityChange = { viewModel.setGlowIntensity(it) },
                                onNavigateBack = { navController.popBackStack() }
                            )
"""

content = content.replace(old_settings, new_settings)

old_music_home_signature = """fun MusicHomeScreen(modifier: Modifier = Modifier, tintTransparency: Float = 0.4f, noiseFactor: Float = 0.06f, onNavigateToSettings: () -> Unit = {}) {"""
new_music_home_signature = """fun MusicHomeScreen(modifier: Modifier = Modifier, tintTransparency: Float = 0.4f, noiseFactor: Float = 0.06f, glowIntensity: Float = 0.6f, onNavigateToSettings: () -> Unit = {}) {"""

content = content.replace(old_music_home_signature, new_music_home_signature)

old_box_bg = """
        // Tint accent with blur overlay
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(400.dp)
                .blur(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF757BFF).copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
"""

new_box_bg = """
        // Tint accent with blur overlay
        val infiniteTransition = rememberInfiniteTransition(label = "glowTransition")
        val animatedAlpha by infiniteTransition.animateFloat(
            initialValue = glowIntensity * 0.4f,
            targetValue = glowIntensity,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAlpha"
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(400.dp)
                .blur(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF757BFF).copy(alpha = animatedAlpha),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
"""

content = content.replace(old_box_bg, new_box_bg)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
