import re

with open("app/src/main/java/com/example/SettingsScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    "import androidx.compose.ui.unit.dp",
    "import androidx.compose.ui.unit.dp\nimport androidx.compose.ui.graphics.Color"
)

content = content.replace(
    "Scaffold(",
    "Scaffold(\n        containerColor = Color.Transparent,"
)

content = content.replace(
    "TopAppBar(",
    "TopAppBar(\n                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),"
)

with open("app/src/main/java/com/example/SettingsScreen.kt", "w") as f:
    f.write(content)
