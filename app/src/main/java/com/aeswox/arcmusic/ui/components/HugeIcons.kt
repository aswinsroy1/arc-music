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

    private var _Pause: ImageVector? = null

    val Pause: ImageVector
        get() {
            if (_Pause != null) {
                return _Pause!!
            }
            _Pause = ImageVector.Builder(
                name = "Pause",
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
                    moveTo(4f, 7f)
                    curveTo(4f, 5.58579f, 4f, 4.87868f, 4.43934f, 4.43934f)
                    curveTo(4.87868f, 4f, 5.58579f, 4f, 7f, 4f)
                    curveTo(8.41421f, 4f, 9.12132f, 4f, 9.56066f, 4.43934f)
                    curveTo(10f, 4.87868f, 10f, 5.58579f, 10f, 7f)
                    lineTo(10f, 17f)
                    curveTo(10f, 18.4142f, 10f, 19.1213f, 9.56066f, 19.5607f)
                    curveTo(9.12132f, 20f, 8.41421f, 20f, 7f, 20f)
                    curveTo(5.58579f, 20f, 4.87868f, 20f, 4.43934f, 19.5607f)
                    curveTo(4f, 19.1213f, 4f, 18.4142f, 4f, 17f)
                    lineTo(4f, 7f)
                    close()
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 4f,
                    fill = null
                ) {
                    moveTo(14f, 7f)
                    curveTo(14f, 5.58579f, 14f, 4.87868f, 14.4393f, 4.43934f)
                    curveTo(14.8787f, 4f, 15.5858f, 4f, 17f, 4f)
                    curveTo(18.4142f, 4f, 19.1213f, 4f, 19.5607f, 4.43934f)
                    curveTo(20f, 4.87868f, 20f, 5.58579f, 20f, 7f)
                    lineTo(20f, 17f)
                    curveTo(20f, 18.4142f, 20f, 19.1213f, 19.5607f, 19.5607f)
                    curveTo(19.1213f, 20f, 18.4142f, 20f, 17f, 20f)
                    curveTo(15.5858f, 20f, 14.8787f, 20f, 14.4393f, 19.5607f)
                    curveTo(14f, 19.1213f, 14f, 18.4142f, 14f, 17f)
                    lineTo(14f, 7f)
                    close()
                }
            }.build()
            return _Pause!!
        }

    private var _Play: ImageVector? = null

    val Play: ImageVector
        get() {
            if (_Play != null) {
                return _Play!!
            }
            _Play = ImageVector.Builder(
                name = "Play",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(18.8906f, 12.846f)
                    curveTo(18.5371f, 14.189f, 16.8667f, 15.138f, 13.5257f, 17.0361f)
                    curveTo(10.296f, 18.8709f, 8.6812f, 19.7884f, 7.37983f, 19.4196f)
                    curveTo(6.8418f, 19.2671f, 6.35159f, 18.9776f, 5.95624f, 18.5787f)
                    curveTo(5f, 17.6139f, 5f, 15.7426f, 5f, 12f)
                    curveTo(5f, 8.2574f, 5f, 6.3861f, 5.95624f, 5.42132f)
                    curveTo(6.35159f, 5.02245f, 6.8418f, 4.73288f, 7.37983f, 4.58042f)
                    curveTo(8.6812f, 4.21165f, 10.296f, 5.12907f, 13.5257f, 6.96393f)
                    curveTo(16.8667f, 8.86197f, 18.5371f, 9.811f, 18.8906f, 11.154f)
                    curveTo(19.0365f, 11.7084f, 19.0365f, 12.2916f, 18.8906f, 12.846f)
                    close()
                }
            }.build()
            return _Play!!
        }

    private var _Queue: ImageVector? = null

    val Queue: ImageVector
        get() {
            if (_Queue != null) {
                return _Queue!!
            }
            _Queue = ImageVector.Builder(
                name = "Queue",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(21f, 14f)
                    curveTo(21f, 15.4001f, 21f, 16.1002f, 20.7275f, 16.635f)
                    curveTo(20.4878f, 17.1054f, 20.1054f, 17.4878f, 19.635f, 17.7275f)
                    curveTo(19.1002f, 18f, 18.4001f, 18f, 17f, 18f)
                    lineTo(7f, 18f)
                    curveTo(5.59987f, 18f, 4.8998f, 18f, 4.36502f, 17.7275f)
                    curveTo(3.89462f, 17.4878f, 3.51217f, 17.1054f, 3.27248f, 16.635f)
                    curveTo(3f, 16.1002f, 3f, 15.4001f, 3f, 14f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(6f, 14f)
                    lineTo(18f, 14f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(6f, 10f)
                    lineTo(18f, 10f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(6f, 6f)
                    lineTo(18f, 6f)
                }
            }.build()
            return _Queue!!
        }

    private var _Next: ImageVector? = null

    val Next: ImageVector
        get() {
            if (_Next != null) {
                return _Next!!
            }
            _Next = ImageVector.Builder(
                name = "Next",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(15.9351f, 12.6258f)
                    curveTo(15.6807f, 13.8374f, 14.327f, 14.7077f, 11.6198f, 16.4481f)
                    curveTo(8.67528f, 18.3411f, 7.20303f, 19.2876f, 6.01052f, 18.9229f)
                    curveTo(5.60662f, 18.7994f, 5.23463f, 18.5823f, 4.92227f, 18.2876f)
                    curveTo(4f, 17.4178f, 4f, 15.6118f, 4f, 12f)
                    curveTo(4f, 8.38816f, 4f, 6.58224f, 4.92227f, 5.71235f)
                    curveTo(5.23463f, 5.41773f, 5.60662f, 5.20057f, 6.01052f, 5.07707f)
                    curveTo(7.20304f, 4.71243f, 8.67528f, 5.6589f, 11.6198f, 7.55186f)
                    curveTo(14.327f, 9.29233f, 15.6807f, 10.1626f, 15.9351f, 11.3742f)
                    curveTo(16.0216f, 11.7865f, 16.0216f, 12.2135f, 15.9351f, 12.6258f)
                    close()
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 4f,
                    fill = null
                ) {
                    moveTo(20f, 5f)
                    lineTo(20f, 19f)
                }
            }.build()
            return _Next!!
        }
}
