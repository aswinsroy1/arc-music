import re

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'r') as f:
    content = f.read()

# Add import for scale
if "import androidx.compose.ui.draw.scale" not in content:
    content = content.replace("import androidx.compose.ui.draw.clip", "import androidx.compose.ui.draw.clip\nimport androidx.compose.ui.draw.scale")

# Move imageUrl up
image_url_line = '        val imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB_z_1f8NhiW_8mcmSyiqL7RWZDpFDeKXoILNYiDmR4CPfI2FIlZFT819EuMyXyUdmGjEJWBSf0uQEAidGehVCMDK9DlfZIRVn7oMfjndksjG9C-j9sbflNcKXmIs5K2uHHQAU9yxz2t5RSbllP53--kr3KyrNqO43MKos1i4C7zzL2uzZjSp9cuNhF8bJeWZvOp5J2HcDvqxbpnlnSBanu9AWGHTOlyHWHEL0GTkOiI67-8LLjLm8KMzdNuCE9G24PXbFSAD_DCJF1"'

content = content.replace(image_url_line, "")

insertion_point = "    val textAlpha = if (isDarkTheme) 0.7f else 0.6f"
content = content.replace(insertion_point, insertion_point + "\n" + image_url_line)

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
    f.write(content)
print("Done")
