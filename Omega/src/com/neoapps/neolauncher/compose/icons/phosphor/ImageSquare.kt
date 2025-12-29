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

val Phosphor.ImageSquare: ImageVector
    get() {
        if (_image_square != null) {
            return _image_square!!
        }
        _image_square = Builder(
            name = "Image-square",
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
                moveTo(224.0f, 160.0f)
                lineTo(224.0f, 48.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -16.0f, -16.0f)
                lineTo(48.0f, 32.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 32.0f, 48.0f)
                lineTo(32.0f, 176.0f)
                horizontalLineToRelative(0.0f)
                verticalLineToRelative(32.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                lineTo(208.0f, 224.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(224.0f, 160.0f)
                close()
                moveTo(208.0f, 48.0f)
                verticalLineToRelative(92.7f)
                lineTo(179.3f, 112.0f)
                arcToRelative(16.1f, 16.1f, 0.0f, false, false, -22.6f, 0.0f)
                lineTo(112.0f, 156.7f)
                lineTo(91.3f, 136.0f)
                arcToRelative(16.1f, 16.1f, 0.0f, false, false, -22.6f, 0.0f)
                lineTo(48.0f, 156.7f)
                lineTo(48.0f, 48.0f)
                close()
                moveTo(208.0f, 208.0f)
                lineTo(48.0f, 208.0f)
                lineTo(48.0f, 179.3f)
                lineToRelative(32.0f, -32.0f)
                lineTo(100.7f, 168.0f)
                arcToRelative(16.1f, 16.1f, 0.0f, false, false, 22.6f, 0.0f)
                lineTo(168.0f, 123.3f)
                lineToRelative(40.0f, 40.0f)
                lineTo(208.0f, 208.0f)
                close()
                moveTo(91.5f, 100.5f)
                arcTo(11.9f, 11.9f, 0.0f, false, true, 88.0f, 92.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, false, true, 24.0f, 0.0f)
                horizontalLineToRelative(0.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, false, true, -12.0f, 12.0f)
                arcTo(12.3f, 12.3f, 0.0f, false, true, 91.5f, 100.5f)
                close()
            }
        }
            .build()
        return _image_square!!
    }

private var _image_square: ImageVector? = null
