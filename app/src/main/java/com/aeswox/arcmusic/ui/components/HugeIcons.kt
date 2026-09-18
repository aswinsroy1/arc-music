package com.aeswox.arcmusic.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object HugeIcons {
    private var _LyricsQuote: ImageVector? = null

    val LyricsQuote: ImageVector
        get() {
            if (_LyricsQuote != null) {
                return _LyricsQuote!!
            }
            _LyricsQuote = ImageVector.Builder(
                name = "LyricsQuote",
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
                    moveTo(17.9922f, 5.75f)
                    curveTo(20.2013f, 5.75f, 21.9922f, 7.63509f, 21.9922f, 9.96101f)
                    curveTo(21.9922f, 13.0258f, 20.5783f, 15.7433f, 18.3998f, 17.4295f)
                    curveTo(17.6167f, 18.0357f, 17.2252f, 18.3387f, 16.9588f, 18.2272f)
                    curveTo(16.8775f, 18.1932f, 16.8045f, 18.1362f, 16.7514f, 18.0654f)
                    curveTo(16.5774f, 17.8331f, 16.8036f, 17.2896f, 17.2561f, 16.2028f)
                    curveTo(17.5006f, 15.6154f, 17.6228f, 15.3217f, 17.5771f, 15.0357f)
                    curveTo(17.5587f, 14.9206f, 17.5428f, 14.8689f, 17.4933f, 14.7636f)
                    curveTo(17.3704f, 14.5018f, 16.8808f, 14.1853f, 15.9018f, 13.5523f)
                    curveTo(14.7562f, 12.8116f, 13.9922f, 11.4802f, 13.9922f, 9.96101f)
                    curveTo(13.9922f, 8.47071f, 14.7273f, 7.16135f, 15.8366f, 6.41291f)
                    curveTo(16.4587f, 5.99345f, 17.1985f, 5.75f, 17.9922f, 5.75f)
                    close()
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(5.99219f, 5.75f)
                    curveTo(8.20132f, 5.75f, 9.99219f, 7.63509f, 9.99219f, 9.96101f)
                    curveTo(9.99219f, 13.0258f, 8.57833f, 15.7433f, 6.39979f, 17.4295f)
                    curveTo(5.61672f, 18.0357f, 5.22518f, 18.3387f, 4.95877f, 18.2272f)
                    curveTo(4.87752f, 18.1932f, 4.80451f, 18.1362f, 4.75143f, 18.0654f)
                    curveTo(4.57737f, 17.8331f, 4.8036f, 17.2896f, 5.25607f, 16.2028f)
                    curveTo(5.50059f, 15.6154f, 5.62285f, 15.3217f, 5.57711f, 15.0357f)
                    curveTo(5.55871f, 14.9206f, 5.54279f, 14.8689f, 5.49331f, 14.7636f)
                    curveTo(5.37036f, 14.5018f, 4.88084f, 14.1853f, 3.9018f, 13.5523f)
                    curveTo(2.75622f, 12.8116f, 1.99219f, 11.4802f, 1.99219f, 9.96101f)
                    curveTo(1.99219f, 8.47071f, 2.72729f, 7.16135f, 3.83662f, 6.41291f)
                    curveTo(4.45869f, 5.99345f, 5.19849f, 5.75f, 5.99219f, 5.75f)
                    close()
                }
            }.build()
            return _LyricsQuote!!
        }

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

    private var _Previous: ImageVector? = null

    val Previous: ImageVector
        get() {
            if (_Previous != null) {
                return _Previous!!
            }
            _Previous = ImageVector.Builder(
                name = "Previous",
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
                    moveTo(8.06492f, 12.6258f)
                    curveTo(8.31931f, 13.8374f, 9.67295f, 14.7077f, 12.3802f, 16.4481f)
                    curveTo(15.3247f, 18.3411f, 16.797f, 19.2876f, 17.9895f, 18.9229f)
                    curveTo(18.3934f, 18.7994f, 18.7654f, 18.5823f, 19.0777f, 18.2876f)
                    curveTo(20f, 17.4178f, 20f, 15.6118f, 20f, 12f)
                    curveTo(20f, 8.38816f, 20f, 6.58224f, 19.0777f, 5.71235f)
                    curveTo(18.7654f, 5.41773f, 18.3934f, 5.20057f, 17.9895f, 5.07707f)
                    curveTo(16.797f, 4.71243f, 15.3247f, 5.6589f, 12.3802f, 7.55186f)
                    curveTo(9.67295f, 9.29233f, 8.31931f, 10.1626f, 8.06492f, 11.3742f)
                    curveTo(7.97836f, 11.7865f, 7.97836f, 12.2135f, 8.06492f, 12.6258f)
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
                    moveTo(4f, 4f)
                    lineTo(4f, 20f)
                }
            }.build()
            return _Previous!!
        }

    private var _Home: ImageVector? = null

    val Home: ImageVector
        get() {
            if (_Home != null) {
                return _Home!!
            }
            _Home = ImageVector.Builder(
                name = "Home",
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
                    moveTo(3f, 11.9896f)
                    lineTo(3f, 14.5f)
                    curveTo(3f, 17.7998f, 3f, 19.4497f, 4.02513f, 20.4749f)
                    curveTo(5.05025f, 21.5f, 6.70017f, 21.5f, 10f, 21.5f)
                    lineTo(14f, 21.5f)
                    curveTo(17.2998f, 21.5f, 18.9497f, 21.5f, 19.9749f, 20.4749f)
                    curveTo(21f, 19.4497f, 21f, 17.7998f, 21f, 14.5f)
                    lineTo(21f, 11.9896f)
                    curveTo(21f, 10.3083f, 21f, 9.46773f, 20.6441f, 8.74005f)
                    curveTo(20.2882f, 8.01237f, 19.6247f, 7.49628f, 18.2976f, 6.46411f)
                    lineTo(16.2976f, 4.90855f)
                    curveTo(14.2331f, 3.30285f, 13.2009f, 2.5f, 12f, 2.5f)
                    curveTo(10.7991f, 2.5f, 9.76689f, 3.30285f, 7.70242f, 4.90855f)
                    lineTo(5.70241f, 6.46411f)
                    curveTo(4.37533f, 7.49628f, 3.71179f, 8.01237f, 3.3559f, 8.74005f)
                    curveTo(3f, 9.46773f, 3f, 10.3083f, 3f, 11.9896f)
                    close()
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(17f, 17.5f)
                    lineTo(17f, 13.5f)
                }
            }.build()
            return _Home!!
        }

    private var _Search: ImageVector? = null

    val Search: ImageVector
        get() {
            if (_Search != null) {
                return _Search!!
            }
            _Search = ImageVector.Builder(
                name = "Search",
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
                    moveTo(17f, 10f)
                    curveTo(17f, 13.866f, 13.866f, 17f, 10f, 17f)
                    curveTo(6.13401f, 17f, 3f, 13.866f, 3f, 10f)
                    curveTo(3f, 6.13401f, 6.13401f, 3f, 10f, 3f)
                    curveTo(13.866f, 3f, 17f, 6.13401f, 17f, 10f)
                    close()
                }
                path(
                    fill = SolidColor(Color(0xFF000000)),
                    stroke = null,
                    strokeLineWidth = 0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 4f
                ) {
                    moveTo(20.4697f, 21.5303f)
                    curveTo(20.7626f, 21.8232f, 21.2374f, 21.8232f, 21.5303f, 21.5303f)
                    curveTo(21.8232f, 21.2374f, 21.8232f, 20.7626f, 21.5303f, 20.4697f)
                    lineTo(21f, 21f)
                    lineTo(20.4697f, 21.5303f)
                    close()
                    moveTo(15f, 15f)
                    lineTo(14.4697f, 15.5303f)
                    lineTo(20.4697f, 21.5303f)
                    lineTo(21f, 21f)
                    lineTo(21.5303f, 20.4697f)
                    lineTo(15.5303f, 14.4697f)
                    lineTo(15f, 15f)
                    close()
                }
            }.build()
            return _Search!!
        }

    private var _Library: ImageVector? = null

    val Library: ImageVector
        get() {
            if (_Library != null) {
                return _Library!!
            }
            _Library = ImageVector.Builder(
                name = "Library",
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
                    moveTo(3f, 15f)
                    curveTo(3f, 12.1911f, 3f, 10.7866f, 3.67412f, 9.77772f)
                    curveTo(3.96596f, 9.34096f, 4.34096f, 8.96596f, 4.77772f, 8.67412f)
                    curveTo(5.78661f, 8f, 7.19108f, 8f, 10f, 8f)
                    lineTo(14f, 8f)
                    curveTo(16.8089f, 8f, 18.2134f, 8f, 19.2223f, 8.67412f)
                    curveTo(19.659f, 8.96596f, 20.034f, 9.34096f, 20.3259f, 9.77772f)
                    curveTo(21f, 10.7866f, 21f, 12.1911f, 21f, 15f)
                    curveTo(21f, 17.8089f, 21f, 19.2134f, 20.3259f, 20.2223f)
                    curveTo(20.034f, 20.659f, 19.659f, 21.034f, 19.2223f, 21.3259f)
                    curveTo(18.2134f, 22f, 16.8089f, 22f, 14f, 22f)
                    lineTo(10f, 22f)
                    curveTo(7.19108f, 22f, 5.78661f, 22f, 4.77772f, 21.3259f)
                    curveTo(4.34096f, 21.034f, 3.96596f, 20.659f, 3.67412f, 20.2223f)
                    curveTo(3f, 19.2134f, 3f, 17.8089f, 3f, 15f)
                    close()
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(12.5f, 16.5f)
                    curveTo(12.5f, 17.3284f, 11.8284f, 18f, 11f, 18f)
                    curveTo(10.1716f, 18f, 9.5f, 17.3284f, 9.5f, 16.5f)
                    curveTo(9.5f, 15.6716f, 10.1716f, 15f, 11f, 15f)
                    curveTo(11.8284f, 15f, 12.5f, 15.6716f, 12.5f, 16.5f)
                    close()
                    moveTo(12.5f, 16.5f)
                    lineTo(12.5f, 11.5f)
                    curveTo(12.5f, 11.5f, 12.9f, 13.2333f, 14.5f, 13.5f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(19f, 8f)
                    curveTo(18.9821f, 6.76022f, 18.89f, 6.05733f, 18.4182f, 5.58579f)
                    curveTo(17.8321f, 5f, 16.8888f, 5f, 15.0022f, 5f)
                    lineTo(8.99783f, 5f)
                    curveTo(7.11118f, 5f, 6.16786f, 5f, 5.58176f, 5.58579f)
                    curveTo(5.10996f, 6.05733f, 5.01794f, 6.76022f, 5f, 8f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(17f, 5f)
                    curveTo(17f, 4.06812f, 17f, 3.60218f, 16.8478f, 3.23463f)
                    curveTo(16.6448f, 2.74458f, 16.2554f, 2.35523f, 15.7654f, 2.15224f)
                    curveTo(15.3978f, 2f, 14.9319f, 2f, 14f, 2f)
                    lineTo(10f, 2f)
                    curveTo(9.06812f, 2f, 8.60218f, 2f, 8.23463f, 2.15224f)
                    curveTo(7.74458f, 2.35523f, 7.35523f, 2.74458f, 7.15224f, 3.23463f)
                    curveTo(7f, 3.60218f, 7f, 4.06812f, 7f, 5f)
                }
            }.build()
            return _Library!!
        }

    private var _Add: ImageVector? = null

    val Add: ImageVector
        get() {
            if (_Add != null) {
                return _Add!!
            }
            _Add = ImageVector.Builder(
                name = "Add",
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
                    moveTo(12.001f, 5.00003f)
                    lineTo(12.001f, 19.002f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(19.002f, 12.002f)
                    lineTo(4.99998f, 12.002f)
                }
            }.build()
            return _Add!!
        }

    private var _Share: ImageVector? = null

    val Share: ImageVector
        get() {
            if (_Share != null) {
                return _Share!!
            }
            _Share = ImageVector.Builder(
                name = "Share",
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
                    moveTo(9.39584f, 4.5f)
                    lineTo(8.35417f, 4.5f)
                    curveTo(5.40789f, 4.5f, 3.93475f, 4.5f, 3.01946f, 5.37868f)
                    curveTo(2.10417f, 6.25736f, 2.10417f, 7.67157f, 2.10417f, 10.5f)
                    lineTo(2.10417f, 14.5f)
                    curveTo(2.10417f, 17.3284f, 2.10417f, 18.7426f, 3.01946f, 19.6213f)
                    curveTo(3.93475f, 20.5f, 5.40789f, 20.5f, 8.35417f, 20.5f)
                    lineTo(12.5608f, 20.5f)
                    curveTo(15.5071f, 20.5f, 16.9802f, 20.5f, 17.8955f, 19.6213f)
                    curveTo(18.4885f, 19.052f, 18.6973f, 18.2579f, 18.7708f, 17f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(16.1667f, 7f)
                    lineTo(16.1667f, 3.85355f)
                    curveTo(16.1667f, 3.65829f, 16.3316f, 3.5f, 16.535f, 3.5f)
                    curveTo(16.6326f, 3.5f, 16.7263f, 3.53725f, 16.7954f, 3.60355f)
                    lineTo(21.5275f, 8.14645f)
                    curveTo(21.7634f, 8.37282f, 21.8958f, 8.67986f, 21.8958f, 9f)
                    curveTo(21.8958f, 9.32014f, 21.7634f, 9.62718f, 21.5275f, 9.85355f)
                    lineTo(16.7954f, 14.3964f)
                    curveTo(16.7263f, 14.4628f, 16.6326f, 14.5f, 16.535f, 14.5f)
                    curveTo(16.3316f, 14.5f, 16.1667f, 14.3417f, 16.1667f, 14.1464f)
                    lineTo(16.1667f, 11f)
                    lineTo(13.1157f, 11f)
                    curveTo(8.875f, 11f, 7.3125f, 14.5f, 7.3125f, 14.5f)
                    lineTo(7.3125f, 12f)
                    curveTo(7.3125f, 9.23858f, 9.64435f, 7f, 12.5208f, 7f)
                    lineTo(16.1667f, 7f)
                    close()
                }
            }.build()
            return _Share!!
        }

    private var _Delete: ImageVector? = null

    val Delete: ImageVector
        get() {
            if (_Delete != null) {
                return _Delete!!
            }
            _Delete = ImageVector.Builder(
                name = "Delete",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    fill = null
                ) {
                    moveTo(19.5f, 5.5f)
                    lineTo(18.8803f, 15.5251f)
                    curveTo(18.7219f, 18.0864f, 18.6428f, 19.3671f, 18.0008f, 20.2879f)
                    curveTo(17.6833f, 20.7431f, 17.2747f, 21.1273f, 16.8007f, 21.416f)
                    curveTo(15.8421f, 22f, 14.559f, 22f, 11.9927f, 22f)
                    curveTo(9.42312f, 22f, 8.1383f, 22f, 7.17905f, 21.4149f)
                    curveTo(6.7048f, 21.1257f, 6.296f, 20.7408f, 5.97868f, 20.2848f)
                    curveTo(5.33688f, 19.3626f, 5.25945f, 18.0801f, 5.10461f, 15.5152f)
                    lineTo(4.5f, 5.5f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    fill = null
                ) {
                    moveTo(3f, 5.5f)
                    lineTo(21f, 5.5f)
                    moveTo(16.0557f, 5.5f)
                    lineTo(15.3731f, 4.09173f)
                    curveTo(14.9196f, 3.15626f, 14.6928f, 2.68852f, 14.3017f, 2.39681f)
                    curveTo(14.215f, 2.3321f, 14.1231f, 2.27454f, 14.027f, 2.2247f)
                    curveTo(13.5939f, 2f, 13.0741f, 2f, 12.0345f, 2f)
                    curveTo(10.9688f, 2f, 10.436f, 2f, 9.99568f, 2.23412f)
                    curveTo(9.8981f, 2.28601f, 9.80498f, 2.3459f, 9.71729f, 2.41317f)
                    curveTo(9.32164f, 2.7167f, 9.10063f, 3.20155f, 8.65861f, 4.17126f)
                    lineTo(8.05292f, 5.5f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    fill = null
                ) {
                    moveTo(9.5f, 16.5f)
                    lineTo(9.5f, 10.5f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    fill = null
                ) {
                    moveTo(14.5f, 16.5f)
                    lineTo(14.5f, 10.5f)
                }
            }.build()
            return _Delete!!
        }

    private var _Repeat: ImageVector? = null

    val Repeat: ImageVector
        get() {
            if (_Repeat != null) {
                return _Repeat!!
            }
            _Repeat = ImageVector.Builder(
                name = "Repeat",
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
                    moveTo(16.3884f, 3f)
                    lineTo(17.3913f, 3.97574f)
                    curveTo(17.8393f, 4.41165f, 18.0633f, 4.62961f, 17.9844f, 4.81481f)
                    curveTo(17.9056f, 5f, 17.5888f, 5f, 16.9552f, 5f)
                    lineTo(9.19422f, 5f)
                    curveTo(5.22096f, 5f, 2f, 8.13401f, 2f, 12f)
                    curveTo(2f, 13.4872f, 2.47668f, 14.8662f, 3.2895f, 16f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(7.61156f, 21f)
                    lineTo(6.60875f, 20.0243f)
                    curveTo(6.16074f, 19.5883f, 5.93673f, 19.3704f, 6.01557f, 19.1852f)
                    curveTo(6.09441f, 19f, 6.4112f, 19f, 7.04478f, 19f)
                    lineTo(14.8058f, 19f)
                    curveTo(18.779f, 19f, 22f, 15.866f, 22f, 12f)
                    curveTo(22f, 10.5128f, 21.5233f, 9.13383f, 20.7105f, 8f)
                }
            }.build()
            return _Repeat!!
        }

    private var _RepeatOne: ImageVector? = null

    val RepeatOne: ImageVector
        get() {
            if (_RepeatOne != null) {
                return _RepeatOne!!
            }
            _RepeatOne = ImageVector.Builder(
                name = "RepeatOne",
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
                    moveTo(16.3884f, 3f)
                    lineTo(17.3913f, 3.97574f)
                    curveTo(17.8393f, 4.41165f, 18.0633f, 4.62961f, 17.9844f, 4.81481f)
                    curveTo(17.9056f, 5f, 17.5888f, 5f, 16.9552f, 5f)
                    lineTo(9.19422f, 5f)
                    curveTo(5.22096f, 5f, 2f, 8.13401f, 2f, 12f)
                    curveTo(2f, 13.4872f, 2.47668f, 14.8662f, 3.2895f, 16f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(7.61156f, 21f)
                    lineTo(6.60875f, 20.0243f)
                    curveTo(6.16074f, 19.5883f, 5.93673f, 19.3704f, 6.01557f, 19.1852f)
                    curveTo(6.09441f, 19f, 6.4112f, 19f, 7.04478f, 19f)
                    lineTo(14.8058f, 19f)
                    curveTo(18.779f, 19f, 22f, 15.866f, 22f, 12f)
                    curveTo(22f, 10.5128f, 21.5233f, 9.13383f, 20.7105f, 8f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(13f, 15f)
                    lineTo(13f, 9.31633f)
                    curveTo(13f, 9.05613f, 12.7178f, 8.90761f, 12.52f, 9.06373f)
                    lineTo(11f, 10.2636f)
                }
            }.build()
            return _RepeatOne!!
        }

    private var _Autoplay: ImageVector? = null

    val Autoplay: ImageVector
        get() {
            if (_Autoplay != null) {
                return _Autoplay!!
            }
            _Autoplay = ImageVector.Builder(
                name = "Autoplay",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    fill = null
                ) {
                    moveTo(12f, 12f)
                    curveTo(12f, 12f, 9.26142f, 17f, 6.5f, 17f)
                    curveTo(3.73858f, 17f, 2f, 14.7614f, 2f, 12f)
                    curveTo(2f, 9.23858f, 3.73858f, 7f, 6.5f, 7f)
                    curveTo(9.26142f, 7f, 12f, 12f, 12f, 12f)
                    close()
                    moveTo(12f, 12f)
                    curveTo(12f, 12f, 14.7386f, 17f, 17.5f, 17f)
                    curveTo(20.2614f, 17f, 22f, 14.7614f, 22f, 12f)
                    curveTo(22f, 9.23858f, 20.2614f, 7f, 17.5f, 7f)
                    curveTo(14.7386f, 7f, 12f, 12f, 12f, 12f)
                    close()
                }
            }.build()
            return _Autoplay!!
        }

    private var _Shuffle: ImageVector? = null

    val Shuffle: ImageVector
        get() {
            if (_Shuffle != null) {
                return _Shuffle!!
            }
            _Shuffle = ImageVector.Builder(
                name = "Shuffle",
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
                    moveTo(19.5576f, 4f)
                    lineTo(20.4551f, 4.97574f)
                    curveTo(20.8561f, 5.41165f, 21.0566f, 5.62961f, 20.9861f, 5.81481f)
                    curveTo(20.9155f, 6f, 20.632f, 6f, 20.0649f, 6f)
                    curveTo(18.7956f, 6f, 17.2771f, 5.79493f, 16.1111f, 6.4733f)
                    curveTo(15.3903f, 6.89272f, 14.8883f, 7.62517f, 14.0392f, 9f)
                    moveTo(3f, 18f)
                    lineTo(4.58082f, 18f)
                    curveTo(6.50873f, 18f, 7.47269f, 18f, 8.2862f, 17.5267f)
                    curveTo(9.00708f, 17.1073f, 9.50904f, 16.3748f, 10.3582f, 15f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(19.5576f, 20f)
                    lineTo(20.4551f, 19.0243f)
                    curveTo(20.8561f, 18.5883f, 21.0566f, 18.3704f, 20.9861f, 18.1852f)
                    curveTo(20.9155f, 18f, 20.632f, 18f, 20.0649f, 18f)
                    curveTo(18.7956f, 18f, 17.2771f, 18.2051f, 16.1111f, 17.5267f)
                    curveTo(15.2976f, 17.0534f, 14.7629f, 16.1815f, 13.6935f, 14.4376f)
                    lineTo(10.7038f, 9.5624f)
                    curveTo(9.63441f, 7.81853f, 9.0997f, 6.9466f, 8.2862f, 6.4733f)
                    curveTo(7.47269f, 6f, 6.50873f, 6f, 4.58082f, 6f)
                    lineTo(3f, 6f)
                }
            }.build()
            return _Shuffle!!
        }

    private var _Heart: ImageVector? = null

    val Heart: ImageVector
        get() {
            if (_Heart != null) {
                return _Heart!!
            }
            _Heart = ImageVector.Builder(
                name = "Heart",
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
                    moveTo(10.4107f, 19.9677f)
                    curveTo(7.58942f, 17.858f, 2f, 13.0348f, 2f, 8.69444f)
                    curveTo(2f, 5.82563f, 4.10526f, 3.5f, 7f, 3.5f)
                    curveTo(8.5f, 3.5f, 10f, 4f, 12f, 6f)
                    curveTo(14f, 4f, 15.5f, 3.5f, 17f, 3.5f)
                    curveTo(19.8947f, 3.5f, 22f, 5.82563f, 22f, 8.69444f)
                    curveTo(22f, 13.0348f, 16.4106f, 17.858f, 13.5893f, 19.9677f)
                    curveTo(12.6399f, 20.6776f, 11.3601f, 20.6776f, 10.4107f, 19.9677f)
                    close()
                }
            }.build()
            return _Heart!!
        }

    private var _HeartFilled: ImageVector? = null

    val HeartFilled: ImageVector
        get() {
            if (_HeartFilled != null) {
                return _HeartFilled!!
            }
            _HeartFilled = ImageVector.Builder(
                name = "HeartFilled",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    stroke = null,
                    fill = SolidColor(Color(0xFF000000))
                ) {
                    moveTo(10.4107f, 19.9677f)
                    curveTo(7.58942f, 17.858f, 2f, 13.0348f, 2f, 8.69444f)
                    curveTo(2f, 5.82563f, 4.10526f, 3.5f, 7f, 3.5f)
                    curveTo(8.5f, 3.5f, 10f, 4f, 12f, 6f)
                    curveTo(14f, 4f, 15.5f, 3.5f, 17f, 3.5f)
                    curveTo(19.8947f, 3.5f, 22f, 5.82563f, 22f, 8.69444f)
                    curveTo(22f, 13.0348f, 16.4106f, 17.858f, 13.5893f, 19.9677f)
                    curveTo(12.6399f, 20.6776f, 11.3601f, 20.6776f, 10.4107f, 19.9677f)
                    close()
                }
            }.build()
            return _HeartFilled!!
        }

    private var _HeartCheck: ImageVector? = null

    val HeartCheck: ImageVector
        get() {
            if (_HeartCheck != null) {
                return _HeartCheck!!
            }
            _HeartCheck = ImageVector.Builder(
                name = "HeartCheck",
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
                    moveTo(13f, 20.3025f)
                    curveTo(12.1525f, 20.6505f, 11.1746f, 20.5389f, 10.4107f, 19.9677f)
                    curveTo(7.58942f, 17.858f, 2f, 13.0348f, 2f, 8.69444f)
                    curveTo(2f, 5.82563f, 4.10526f, 3.5f, 7f, 3.5f)
                    curveTo(8.5f, 3.5f, 10f, 4f, 12f, 6f)
                    curveTo(14f, 4f, 15.5f, 3.5f, 17f, 3.5f)
                    curveTo(19.8947f, 3.5f, 22f, 5.82563f, 22f, 8.69444f)
                    curveTo(22f, 9.12591f, 21.9448f, 9.56214f, 21.8425f, 10f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(14f, 17f)
                    curveTo(14f, 17f, 15f, 17f, 16f, 19f)
                    curveTo(16f, 19f, 19.1765f, 14f, 22f, 13f)
                }
            }.build()
            return _HeartCheck!!
        }

    private var _MoreVert: ImageVector? = null

    val MoreVert: ImageVector
        get() {
            if (_MoreVert != null) {
                return _MoreVert!!
            }
            _MoreVert = ImageVector.Builder(
                name = "MoreVert",
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
                    moveTo(11.9967f, 12.5f)
                    lineTo(11.9967f, 12f)
                    moveTo(11.9967f, 6.5f)
                    lineTo(11.9967f, 6f)
                    moveTo(11.9967f, 18.5f)
                    lineTo(11.9967f, 18f)
                    moveTo(12.9967f, 12.5f)
                    curveTo(12.9967f, 11.9477f, 12.549f, 11.5f, 11.9967f, 11.5f)
                    curveTo(11.4444f, 11.5f, 10.9967f, 11.9477f, 10.9967f, 12.5f)
                    curveTo(10.9967f, 13.0523f, 11.4444f, 13.5f, 11.9967f, 13.5f)
                    curveTo(12.549f, 13.5f, 12.9967f, 13.0523f, 12.9967f, 12.5f)
                    close()
                    moveTo(12.9967f, 6.5f)
                    curveTo(12.9967f, 5.94772f, 12.549f, 5.5f, 11.9967f, 5.5f)
                    curveTo(11.4444f, 5.5f, 10.9967f, 5.94772f, 10.9967f, 6.5f)
                    curveTo(10.9967f, 7.05228f, 11.4444f, 7.5f, 11.9967f, 7.5f)
                    curveTo(12.549f, 7.5f, 12.9967f, 7.05228f, 12.9967f, 6.5f)
                    close()
                    moveTo(12.9967f, 18.5f)
                    curveTo(12.9967f, 17.9477f, 12.549f, 17.5f, 11.9967f, 17.5f)
                    curveTo(11.4444f, 17.5f, 10.9967f, 17.9477f, 10.9967f, 18.5f)
                    curveTo(10.9967f, 19.0523f, 11.4444f, 19.5f, 11.9967f, 19.5f)
                    curveTo(12.549f, 19.5f, 12.9967f, 19.0523f, 12.9967f, 18.5f)
                    close()
                }
            }.build()
            return _MoreVert!!
        }

    private var _MoreHoriz: ImageVector? = null

    val MoreHoriz: ImageVector
        get() {
            if (_MoreHoriz != null) {
                return _MoreHoriz!!
            }
            _MoreHoriz = ImageVector.Builder(
                name = "MoreHoriz",
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
                    moveTo(6.00449f, 12.5f)
                    lineTo(6.00449f, 12f)
                    moveTo(18.0045f, 12.5f)
                    lineTo(18.0045f, 12f)
                    moveTo(12.0045f, 12.5f)
                    lineTo(12.0045f, 12f)
                    moveTo(7.00449f, 12.5f)
                    curveTo(7.00449f, 11.9477f, 6.55677f, 11.5f, 6.00449f, 11.5f)
                    curveTo(5.4522f, 11.5f, 5.00449f, 11.9477f, 5.00449f, 12.5f)
                    curveTo(5.00449f, 13.0523f, 5.4522f, 13.5f, 6.00449f, 13.5f)
                    curveTo(6.55677f, 13.5f, 7.00449f, 13.0523f, 7.00449f, 12.5f)
                    close()
                    moveTo(19.0045f, 12.5f)
                    curveTo(19.0045f, 11.9477f, 18.5568f, 11.5f, 18.0045f, 11.5f)
                    curveTo(17.4522f, 11.5f, 17.0045f, 11.9477f, 17.0045f, 12.5f)
                    curveTo(17.0045f, 13.0523f, 17.4522f, 13.5f, 18.0045f, 13.5f)
                    curveTo(18.5568f, 13.5f, 19.0045f, 13.0523f, 19.0045f, 12.5f)
                    close()
                    moveTo(13.0045f, 12.5f)
                    curveTo(13.0045f, 11.9477f, 12.5568f, 11.5f, 12.0045f, 11.5f)
                    curveTo(11.4522f, 11.5f, 11.0045f, 11.9477f, 11.0045f, 12.5f)
                    curveTo(11.0045f, 13.0523f, 11.4522f, 13.5f, 12.0045f, 13.5f)
                    curveTo(12.5568f, 13.5f, 13.0045f, 13.0523f, 13.0045f, 12.5f)
                    close()
                }
            }.build()
            return _MoreHoriz!!
        }

    private var _Filter: ImageVector? = null

    val Filter: ImageVector
        get() {
            if (_Filter != null) {
                return _Filter!!
            }
            _Filter = ImageVector.Builder(
                name = "Filter",
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
                    moveTo(3f, 6f)
                    lineTo(21f, 6f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(6f, 12f)
                    lineTo(18f, 12f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(9f, 18f)
                    lineTo(15f, 18f)
                }
            }.build()
            return _Filter!!
        }
    private var _Settings: ImageVector? = null

    val Settings: ImageVector
        get() {
            if (_Settings != null) {
                return _Settings!!
            }
            _Settings = ImageVector.Builder(
                name = "Settings",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    fill = null
                ) {
                    moveTo(15.5f, 12f)
                    curveTo(15.5f, 13.933f, 13.933f, 15.5f, 12f, 15.5f)
                    curveTo(10.067f, 15.5f, 8.5f, 13.933f, 8.5f, 12f)
                    curveTo(8.5f, 10.067f, 10.067f, 8.5f, 12f, 8.5f)
                    curveTo(13.933f, 8.5f, 15.5f, 10.067f, 15.5f, 12f)
                    close()
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    fill = null
                ) {
                    moveTo(21.011f, 14.0965f)
                    curveTo(21.5329f, 13.9558f, 21.7939f, 13.8854f, 21.8969f, 13.7508f)
                    curveTo(22f, 13.6163f, 22f, 13.3998f, 22f, 12.9669f)
                    lineTo(22f, 11.0332f)
                    curveTo(22f, 10.6003f, 22f, 10.3838f, 21.8969f, 10.2493f)
                    curveTo(21.7938f, 10.1147f, 21.5329f, 10.0443f, 21.011f, 9.90358f)
                    curveTo(19.0606f, 9.37759f, 17.8399f, 7.33851f, 18.3433f, 5.40087f)
                    curveTo(18.4817f, 4.86799f, 18.5509f, 4.60156f, 18.4848f, 4.44529f)
                    curveTo(18.4187f, 4.28902f, 18.2291f, 4.18134f, 17.8497f, 3.96596f)
                    lineTo(16.125f, 2.98673f)
                    curveTo(15.7528f, 2.77539f, 15.5667f, 2.66972f, 15.3997f, 2.69222f)
                    curveTo(15.2326f, 2.71472f, 15.0442f, 2.90273f, 14.6672f, 3.27873f)
                    curveTo(13.208f, 4.73448f, 10.7936f, 4.73442f, 9.33434f, 3.27864f)
                    curveTo(8.95743f, 2.90263f, 8.76898f, 2.71463f, 8.60193f, 2.69212f)
                    curveTo(8.43489f, 2.66962f, 8.24877f, 2.77529f, 7.87653f, 2.98663f)
                    lineTo(6.15184f, 3.96587f)
                    curveTo(5.77253f, 4.18123f, 5.58287f, 4.28891f, 5.51678f, 4.44515f)
                    curveTo(5.45068f, 4.6014f, 5.51987f, 4.86787f, 5.65825f, 5.4008f)
                    curveTo(6.16137f, 7.3385f, 4.93972f, 9.37763f, 2.98902f, 9.9036f)
                    curveTo(2.46712f, 10.0443f, 2.20617f, 10.1147f, 2.10308f, 10.2492f)
                    curveTo(2f, 10.3838f, 2f, 10.6003f, 2f, 11.0332f)
                    lineTo(2f, 12.9669f)
                    curveTo(2f, 13.3998f, 2f, 13.6163f, 2.10308f, 13.7508f)
                    curveTo(2.20615f, 13.8854f, 2.46711f, 13.9558f, 2.98902f, 14.0965f)
                    curveTo(4.9394f, 14.6225f, 6.16008f, 16.6616f, 5.65672f, 18.5992f)
                    curveTo(5.51829f, 19.1321f, 5.44907f, 19.3985f, 5.51516f, 19.5548f)
                    curveTo(5.58126f, 19.7111f, 5.77092f, 19.8188f, 6.15025f, 20.0341f)
                    lineTo(7.87495f, 21.0134f)
                    curveTo(8.24721f, 21.2247f, 8.43334f, 21.3304f, 8.6004f, 21.3079f)
                    curveTo(8.76746f, 21.2854f, 8.95588f, 21.0973f, 9.33271f, 20.7213f)
                    curveTo(10.7927f, 19.2644f, 13.2088f, 19.2643f, 14.6689f, 20.7212f)
                    curveTo(15.0457f, 21.0973f, 15.2341f, 21.2853f, 15.4012f, 21.3078f)
                    curveTo(15.5682f, 21.3303f, 15.7544f, 21.2246f, 16.1266f, 21.0133f)
                    lineTo(17.8513f, 20.034f)
                    curveTo(18.2307f, 19.8187f, 18.4204f, 19.711f, 18.4864f, 19.5547f)
                    curveTo(18.5525f, 19.3984f, 18.4833f, 19.132f, 18.3448f, 18.5991f)
                    curveTo(17.8412f, 16.6616f, 19.0609f, 14.6226f, 21.011f, 14.0965f)
                    close()
                }
            }.build()
            return _Settings!!
        }
    private var _SleepTimer: ImageVector? = null

    val SleepTimer: ImageVector
        get() {
            if (_SleepTimer != null) {
                return _SleepTimer!!
            }
            _SleepTimer = ImageVector.Builder(
                name = "SleepTimer",
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
                    moveTo(15f, 2f)
                    lineTo(10f, 2f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(4f, 13.5f)
                    curveTo(4f, 8.80558f, 7.80558f, 5f, 12.5f, 5f)
                    curveTo(14.8472f, 5f, 16.9722f, 5.95139f, 18.5104f, 7.48959f)
                    moveTo(18.5104f, 7.48959f)
                    curveTo(20.0486f, 9.02779f, 21f, 11.1528f, 21f, 13.5f)
                    curveTo(21f, 18.1944f, 17.1944f, 22f, 12.5f, 22f)
                    lineTo(3f, 22f)
                    moveTo(18.5104f, 7.48959f)
                    lineTo(20f, 6f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(8f, 19f)
                    lineTo(3f, 19f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(6f, 16f)
                    lineTo(3f, 16f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(12.5f, 13.5f)
                    lineTo(16f, 10f)
                }
            }.build()
            return _SleepTimer!!
        }
    private var _Moon: ImageVector? = null

    val Moon: ImageVector
        get() {
            if (_Moon != null) {
                return _Moon!!
            }
            _Moon = ImageVector.Builder(
                name = "Moon",
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
                    moveTo(21.5f, 14.0784f)
                    curveTo(20.3003f, 14.7189f, 18.9301f, 15.0821f, 17.4751f, 15.0821f)
                    curveTo(12.7491f, 15.0821f, 8.91792f, 11.2509f, 8.91792f, 6.52485f)
                    curveTo(8.91792f, 5.06986f, 9.28105f, 3.69968f, 9.92163f, 2.5f)
                    curveTo(5.66765f, 3.49698f, 2.5f, 7.31513f, 2.5f, 11.8731f)
                    curveTo(2.5f, 17.1899f, 6.8101f, 21.5f, 12.1269f, 21.5f)
                    curveTo(16.6849f, 21.5f, 20.503f, 18.3324f, 21.5f, 14.0784f)
                    close()
                }
            }.build()
            return _Moon!!
        }
    private var _Sun: ImageVector? = null

    val Sun: ImageVector
        get() {
            if (_Sun != null) {
                return _Sun!!
            }
            _Sun = ImageVector.Builder(
                name = "Sun",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    fill = null
                ) {
                    moveTo(17f, 12f)
                    curveTo(17f, 14.7614f, 14.7614f, 17f, 12f, 17f)
                    curveTo(9.23858f, 17f, 7f, 14.7614f, 7f, 12f)
                    curveTo(7f, 9.23858f, 9.23858f, 7f, 12f, 7f)
                    curveTo(14.7614f, 7f, 17f, 9.23858f, 17f, 12f)
                    close()
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    fill = null
                ) {
                    moveTo(12f, 2f)
                    curveTo(11.6227f, 2.33333f, 11.0945f, 3.2f, 12f, 4f)
                    moveTo(12f, 20f)
                    curveTo(12.3773f, 20.3333f, 12.9055f, 21.2f, 12f, 22f)
                    moveTo(19.5f, 4.50271f)
                    curveTo(18.9685f, 4.46982f, 17.9253f, 4.72293f, 18.0042f, 5.99847f)
                    moveTo(5.49576f, 17.5f)
                    curveTo(5.52865f, 18.0315f, 5.27555f, 19.0747f, 4f, 18.9958f)
                    moveTo(5.00271f, 4.5f)
                    curveTo(4.96979f, 5.03202f, 5.22315f, 6.0763f, 6.5f, 5.99729f)
                    moveTo(18f, 17.5026f)
                    curveTo(18.5315f, 17.4715f, 19.5747f, 17.7108f, 19.4958f, 18.9168f)
                    moveTo(22f, 12f)
                    curveTo(21.6667f, 11.6227f, 20.8f, 11.0945f, 20f, 12f)
                    moveTo(4f, 11.5f)
                    curveTo(3.66667f, 11.8773f, 2.8f, 12.4055f, 2f, 11.5f)
                }
            }.build()
            return _Sun!!
        }
    private var _Download: ImageVector? = null

    val Download: ImageVector
        get() {
            if (_Download != null) {
                return _Download!!
            }
            _Download = ImageVector.Builder(
                name = "Download",
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
                    moveTo(17.4776f, 9.01106f)
                    curveTo(17.485f, 9.01102f, 17.4925f, 9.01101f, 17.5f, 9.01101f)
                    curveTo(19.9853f, 9.01101f, 22f, 11.0294f, 22f, 13.5193f)
                    curveTo(22f, 15.8398f, 20.25f, 17.7508f, 18f, 18f)
                    moveTo(17.4776f, 9.01106f)
                    curveTo(17.4924f, 8.84606f, 17.5f, 8.67896f, 17.5f, 8.51009f)
                    curveTo(17.5f, 5.46695f, 15.0376f, 3f, 12f, 3f)
                    curveTo(9.12324f, 3f, 6.76233f, 5.21267f, 6.52042f, 8.03192f)
                    moveTo(17.4776f, 9.01106f)
                    curveTo(17.3753f, 10.1476f, 16.9286f, 11.1846f, 16.2428f, 12.0165f)
                    moveTo(6.52042f, 8.03192f)
                    curveTo(3.98398f, 8.27373f, 2f, 10.4139f, 2f, 13.0183f)
                    curveTo(2f, 15.4417f, 3.71776f, 17.4632f, 6f, 17.9273f)
                    moveTo(6.52042f, 8.03192f)
                    curveTo(6.67826f, 8.01687f, 6.83823f, 8.00917f, 7f, 8.00917f)
                    curveTo(8.12582f, 8.00917f, 9.16474f, 8.38194f, 10.0005f, 9.01101f)
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(12f, 21f)
                    lineTo(12f, 13f)
                    moveTo(12f, 21f)
                    curveTo(11.2998f, 21f, 9.99153f, 19.0057f, 9.5f, 18.5f)
                    moveTo(12f, 21f)
                    curveTo(12.7002f, 21f, 14.0085f, 19.0057f, 14.5f, 18.5f)
                }
            }.build()
            return _Download!!
        }
    private var _Edit: ImageVector? = null

    val Edit: ImageVector
        get() {
            if (_Edit != null) {
                return _Edit!!
            }
            _Edit = ImageVector.Builder(
                name = "Edit",
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
                    moveTo(3.78181f, 16.3092f)
                    lineTo(3f, 21f)
                    lineTo(7.69086f, 20.2182f)
                    curveTo(8.50544f, 20.0825f, 9.25725f, 19.6956f, 9.84119f, 19.1116f)
                    lineTo(20.4198f, 8.53288f)
                    curveTo(21.1934f, 7.75922f, 21.1934f, 6.5049f, 20.4197f, 5.73126f)
                    lineTo(18.2687f, 3.58024f)
                    curveTo(17.495f, 2.80658f, 16.2406f, 2.80659f, 15.4669f, 3.58027f)
                    lineTo(4.88841f, 14.159f)
                    curveTo(4.30447f, 14.7429f, 3.91757f, 15.4947f, 3.78181f, 16.3092f)
                    close()
                }
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    fill = null
                ) {
                    moveTo(14f, 6f)
                    lineTo(18f, 10f)
                }
            }.build()
            return _Edit!!
        }
}
