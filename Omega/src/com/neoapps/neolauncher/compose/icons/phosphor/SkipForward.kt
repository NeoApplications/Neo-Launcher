package com.neoapps.neolauncher.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.neoapps.neolauncher.compose.icons.Phosphor

val Phosphor.SkipForward: ImageVector
    get() {
        if (_skip_forward != null) {
            return _skip_forward!!
        }
        _skip_forward = Builder(
            name = "Skip-forward",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(200.0f, 32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                verticalLineToRelative(73.7f)
                lineTo(72.3f, 40.6f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 48.0f, 54.3f)
                verticalLineTo(201.7f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, 8.2f, 14.0f)
                arcToRelative(15.4f, 15.4f, 0.0f, false, false, 7.8f, 2.0f)
                arcToRelative(16.2f, 16.2f, 0.0f, false, false, 8.3f, -2.3f)
                lineTo(192.0f, 142.3f)
                verticalLineTo(216.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                verticalLineTo(40.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 200.0f, 32.0f)
                close()
                moveTo(64.0f, 201.7f)
                verticalLineTo(54.3f)
                lineTo(184.7f, 128.0f)
                close()
            }
        }
            .build()
        return _skip_forward!!
    }

private var _skip_forward: ImageVector? = null
