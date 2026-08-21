package com.aeswox.arcmusic.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath

@Composable
fun PlayPauseMorphIcon(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    // Progress: 0f = Play, 1f = Pause
    val progress by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "PlayPauseMorph"
    )

    val morph = remember {
        // Lucide Play:
        // <path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z" />
        // Triangle with vertices (5,5), (19,12), (5,19) and corner radius 2.
        val playPolygon = RoundedPolygon(
            vertices = floatArrayOf(
                5f, 5f,
                19f, 12f,
                5f, 19f
            ),
            rounding = CornerRounding(2f),
            centerX = 12f,
            centerY = 12f
        )

        // Lucide Pause:
        // <rect x="14" y="3" width="5" height="18" rx="1" />
        // <rect x="5" y="3" width="5" height="18" rx="1" />
        // To morph cleanly with RoundedPolygon, we create a single continuous path
        // that traces both rectangles by bridging across the gap at y=12 with a zero-width line.
        val r = CornerRounding(1f)
        val z = CornerRounding(0f)
        val pausePolygon = RoundedPolygon(
            vertices = floatArrayOf(
                5f, 3f,    // 0: TL 1
                10f, 3f,   // 1: TR 1
                10f, 12f,  // 2: Bridge start
                14f, 12f,  // 3: Bridge end
                14f, 3f,   // 4: TL 2
                19f, 3f,   // 5: TR 2
                19f, 21f,  // 6: BR 2
                14f, 21f,  // 7: BL 2
                14f, 12f,  // 8: Bridge return start
                10f, 12f,  // 9: Bridge return end
                10f, 21f,  // 10: BR 1
                5f, 21f    // 11: BL 1
            ),
            perVertexRounding = listOf(r, r, z, z, r, r, r, r, z, z, r, r),
            centerX = 12f,
            centerY = 12f
        )

        Morph(playPolygon, pausePolygon)
    }

    Canvas(modifier = modifier.size(24.dp)) {
        // The original shapes are defined in a 24x24 coordinate space (Lucide default).
        // We need to scale them to fit the actual size of the Canvas.
        val scaleX = size.width / 24f
        val scaleY = size.height / 24f

        scale(scaleX, scaleY, pivot = androidx.compose.ui.geometry.Offset.Zero) {
            val path = morph.toPath(progress).asComposePath()
            drawPath(
                path = path,
                color = tint
            )
        }
    }
}
