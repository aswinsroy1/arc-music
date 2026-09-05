package com.aeswox.arcmusic.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val LucideSettings: ImageVector
    get() {
        if (_settings != null) {
            return _settings!!
        }
        _settings = ImageVector.Builder(
            name = "LucideSettings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                fillAlpha = 1.0f,
                stroke = SolidColor(Color(0xFF000000)),
                strokeAlpha = 1.0f,
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 4.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12.22f, 2f)
                horizontalLineToRelative(-0.44f)
                arcToRelative(2f, 2f, 0f, false, false, -2f, 2f)
                verticalLineToRelative(0.18f)
                arcToRelative(2f, 2f, 0f, false, true, -1f, 1.73f)
                lineToRelative(-0.43f, 0.25f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, 0f)
                lineToRelative(-0.15f, -0.08f)
                arcToRelative(2f, 2f, 0f, false, false, -2.73f, 0.73f)
                lineToRelative(-0.22f, 0.38f)
                arcToRelative(2f, 2f, 0f, false, false, 0.73f, 2.73f)
                lineToRelative(0.15f, 0.1f)
                arcToRelative(2f, 2f, 0f, false, true, 1f, 1.72f)
                verticalLineToRelative(0.51f)
                arcToRelative(2f, 2f, 0f, false, true, -1f, 1.74f)
                lineToRelative(-0.15f, 0.09f)
                arcToRelative(2f, 2f, 0f, false, false, -0.73f, 2.73f)
                lineToRelative(0.22f, 0.38f)
                arcToRelative(2f, 2f, 0f, false, false, 2.73f, 0.73f)
                lineToRelative(0.15f, -0.08f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, 0f)
                lineToRelative(0.43f, 0.25f)
                arcToRelative(2f, 2f, 0f, false, true, 1f, 1.73f)
                lineTo(11.78f, 20f)
                arcToRelative(2f, 2f, 0f, false, false, 2f, 2f)
                horizontalLineToRelative(0.44f)
                arcToRelative(2f, 2f, 0f, false, false, 2f, -2f)
                verticalLineToRelative(-0.18f)
                arcToRelative(2f, 2f, 0f, false, true, 1f, -1.73f)
                lineToRelative(0.43f, -0.25f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, 0f)
                lineToRelative(0.15f, 0.08f)
                arcToRelative(2f, 2f, 0f, false, false, 2.73f, -0.73f)
                lineToRelative(0.22f, -0.39f)
                arcToRelative(2f, 2f, 0f, false, false, -0.73f, -2.73f)
                lineToRelative(-0.15f, -0.08f)
                arcToRelative(2f, 2f, 0f, false, true, -1f, -1.74f)
                verticalLineToRelative(-0.5f)
                arcToRelative(2f, 2f, 0f, false, true, 1f, -1.74f)
                lineToRelative(0.15f, -0.09f)
                arcToRelative(2f, 2f, 0f, false, false, 0.73f, -2.73f)
                lineToRelative(-0.22f, -0.38f)
                arcToRelative(2f, 2f, 0f, false, false, -2.73f, -0.73f)
                lineToRelative(-0.15f, 0.08f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, 0f)
                lineToRelative(-0.43f, -0.25f)
                arcToRelative(2f, 2f, 0f, false, true, -1f, -1.73f)
                lineTo(14.22f, 4f)
                arcToRelative(2f, 2f, 0f, false, false, -2f, -2f)
                close()
                moveTo(12f, 9f)
                arcToRelative(3f, 3f, 0f, true, false, 0f, 6f)
                arcToRelative(3f, 3f, 0f, false, false, 0f, -6f)
                close()
            }
        }.build()
        return _settings!!
    }

val LucideMoreHorizontal: ImageVector
    get() {
        if (_moreHorizontal != null) {
            return _moreHorizontal!!
        }
        _moreHorizontal = ImageVector.Builder(
            name = "LucideMoreHorizontal",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                fillAlpha = 1.0f,
                stroke = SolidColor(Color(0xFF000000)),
                strokeAlpha = 1.0f,
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 4.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 12f)
                arcToRelative(1f, 1f, 0f, true, false, 0f, 2f)
                arcToRelative(1f, 1f, 0f, false, false, 0f, -2f)
                close()
                moveTo(19f, 12f)
                arcToRelative(1f, 1f, 0f, true, false, 0f, 2f)
                arcToRelative(1f, 1f, 0f, false, false, 0f, -2f)
                close()
                moveTo(5f, 12f)
                arcToRelative(1f, 1f, 0f, true, false, 0f, 2f)
                arcToRelative(1f, 1f, 0f, false, false, 0f, -2f)
                close()
            }
        }.build()
        return _moreHorizontal!!
    }

private var _settings: ImageVector? = null
private var _moreHorizontal: ImageVector? = null
