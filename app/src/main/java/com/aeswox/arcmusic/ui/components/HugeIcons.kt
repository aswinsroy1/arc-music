package com.aeswox.arcmusic.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object HugeIcons {
    private var _MicVocal: ImageVector? = null

    val MicVocal: ImageVector
        get() {
            if (_MicVocal != null) {
                return _MicVocal!!
            }
            _MicVocal = ImageVector.Builder(
                name = "MicVocal",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 4f,
                    fill = null
                ) {
                    moveTo(19.7836f, 10.5355f)
                    curveTo(17.831f, 12.4882f, 14.6651f, 12.4882f, 12.7125f, 10.5355f)
                    curveTo(10.7599f, 8.58291f, 10.7599f, 5.41709f, 12.7125f, 3.46447f)
                    curveTo(14.6651f, 1.51184f, 17.831f, 1.51184f, 19.7836f, 3.46447f)
                    curveTo(21.7362f, 5.41709f, 21.7362f, 8.58291f, 19.7836f, 10.5355f)
                    close()
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(11.2538f, 8f)
                    lineTo(4.83517f, 15.3889f)
                    curveTo(4.75179f, 15.4848f, 4.7101f, 15.5328f, 4.67652f, 15.5761f)
                    curveTo(4.08338f, 16.3395f, 4.12122f, 17.4179f, 4.76642f, 18.1379f)
                    curveTo(4.80295f, 18.1786f, 4.8479f, 18.2236f, 4.9378f, 18.3135f)
                    curveTo(5.02765f, 18.4033f, 5.0726f, 18.4483f, 5.11335f, 18.4848f)
                    curveTo(5.83313f, 19.1298f, 6.91119f, 19.1679f, 7.67463f, 18.5751f)
                    curveTo(7.71785f, 18.5416f, 7.76584f, 18.4999f, 7.86182f, 18.4166f)
                    lineTo(15.2538f, 12f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(4.80431f, 18.5f)
                    lineTo(4.71852f, 18.5858f)
                    curveTo(3.26547f, 20.0388f, 2.53894f, 20.7654f, 2.79464f, 21.3827f)
                    curveTo(3.05034f, 22f, 4.07781f, 22f, 6.13274f, 22f)
                    lineTo(13.3043f, 22f)
                }
            }.build()
            return _MicVocal!!
        }
}
