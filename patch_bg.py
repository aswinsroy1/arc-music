import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

import_blur = """
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.shape.CircleShape
"""

content = content.replace("import androidx.compose.ui.draw.clip", "import androidx.compose.ui.draw.clip\n" + import_blur)

bg_code = """
        // Tint accent with blur overlay
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(300.dp)
                .blur(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF8B93FF).copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        LazyColumn(
"""

content = content.replace("        LazyColumn(", bg_code)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
