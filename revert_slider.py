import re

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'r') as f:
    content = f.read()

# Replace WavySlider usage
old_usage = """            var progress by remember { mutableFloatStateOf(0.42f) } 
            WavySlider(
                progress = progress,
                onProgressChange = { progress = it },
                modifier = Modifier.fillMaxWidth().height(24.dp),
                color = textColor
            )"""

new_usage = """            var progress by remember { mutableFloatStateOf(0.42f) } 
            Slider(
                value = progress,
                onValueChange = { progress = it },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = textColor,
                    activeTrackColor = textColor,
                    inactiveTrackColor = textColor.copy(alpha = 0.3f)
                )
            )"""

content = content.replace(old_usage, new_usage)

# Remove WavySlider function
start_idx = content.find("@Composable\nfun WavySlider")
if start_idx != -1:
    content = content[:start_idx].rstrip() + "\n"

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
    f.write(content)
print("Done")
