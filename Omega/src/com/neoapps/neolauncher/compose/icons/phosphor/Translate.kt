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

val Phosphor.Translate: ImageVector
    get() {
        if (_translate != null) {
            return _translate!!
        }
        _translate = Builder(
            name = "Translate",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(239.2f, 212.4f)
                lineToRelative(-56.0f, -112.0f)
                arcTo(8.2f, 8.2f, 0.0f, false, false, 176.0f, 96.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, -7.1f, 4.4f)
                lineToRelative(-21.7f, 43.4f)
                arcTo(87.4f, 87.4f, 0.0f, false, true, 100.0f, 126.9f)
                arcTo(103.5f, 103.5f, 0.0f, false, false, 127.7f, 64.0f)
                horizontalLineTo(152.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                horizontalLineTo(96.0f)
                verticalLineTo(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                verticalLineTo(48.0f)
                horizontalLineTo(24.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(87.6f)
                arcTo(87.0f, 87.0f, 0.0f, false, true, 88.0f, 116.3f)
                arcToRelative(87.1f, 87.1f, 0.0f, false, true, -19.0f, -31.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -15.1f, 5.4f)
                arcTo(103.8f, 103.8f, 0.0f, false, false, 76.0f, 126.9f)
                arcTo(87.1f, 87.1f, 0.0f, false, true, 24.0f, 144.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                arcToRelative(103.6f, 103.6f, 0.0f, false, false, 64.0f, -22.1f)
                arcToRelative(103.6f, 103.6f, 0.0f, false, false, 51.5f, 21.3f)
                lineToRelative(-26.6f, 53.2f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 3.5f, 10.8f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 10.8f, -3.6f)
                lineTo(141.0f, 192.0f)
                horizontalLineToRelative(70.1f)
                lineToRelative(13.8f, 27.6f)
                arcTo(7.9f, 7.9f, 0.0f, false, false, 232.0f, 224.0f)
                arcToRelative(9.4f, 9.4f, 0.0f, false, false, 3.6f, -0.8f)
                arcTo(8.2f, 8.2f, 0.0f, false, false, 239.2f, 212.4f)
                close()
                moveTo(149.0f, 176.0f)
                lineToRelative(27.0f, -54.1f)
                lineTo(203.1f, 176.0f)
                close()
            }
        }
            .build()
        return _translate!!
    }

private var _translate: ImageVector? = null
