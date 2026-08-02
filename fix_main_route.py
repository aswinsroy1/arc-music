import re
with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace("""                        composable("now_playing") {
                            NowPlayingScreen(
                                tintTransparency = tintTransparency,
                                noiseFactor = noiseFactor,
                                glowIntensity = glowIntensity,
                                isDarkTheme = !lightThemeForNowPlaying,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }""", """                        composable("now_playing") {
                            NowPlayingScreen(
                                tintTransparency = tintTransparency,
                                noiseFactor = noiseFactor,
                                glowIntensity = glowIntensity,
                                isDarkTheme = !lightThemeForNowPlaying,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToQueue = { navController.navigate("queue") }
                            )
                        }
                        composable("queue") {
                            QueueScreen(onNavigateBack = { navController.popBackStack() })
                        }""")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
