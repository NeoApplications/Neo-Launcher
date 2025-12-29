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

val Phosphor.Palette: ImageVector
    get() {
        if (_palette != null) {
            return _palette!!
        }
        _palette = Builder(
            name = "Palette",
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
                moveTo(200.8f, 53.9f)
                arcTo(103.4f, 103.4f, 0.0f, false, false, 128.0f, 24.0f)
                horizontalLineToRelative(-1.1f)
                arcTo(104.0f, 104.0f, 0.0f, false, false, 93.4f, 226.1f)
                arcTo(32.0f, 32.0f, 0.0f, false, false, 136.0f, 195.9f)
                lineTo(136.0f, 192.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 16.0f, -16.0f)
                horizontalLineToRelative(46.2f)
                arcToRelative(31.7f, 31.7f, 0.0f, false, false, 31.2f, -24.9f)
                arcToRelative(101.5f, 101.5f, 0.0f, false, false, 2.6f, -24.0f)
                arcTo(102.9f, 102.9f, 0.0f, false, false, 200.8f, 53.9f)
                close()
                moveTo(213.8f, 147.6f)
                arcTo(15.9f, 15.9f, 0.0f, false, true, 198.2f, 160.0f)
                lineTo(152.0f, 160.0f)
                arcToRelative(32.1f, 32.1f, 0.0f, false, false, -32.0f, 32.0f)
                verticalLineToRelative(3.9f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 98.7f, 211.0f)
                arcTo(88.2f, 88.2f, 0.0f, false, true, 40.0f, 128.0f)
                arcToRelative(88.1f, 88.1f, 0.0f, false, true, 87.1f, -88.0f)
                horizontalLineToRelative(0.9f)
                arcToRelative(88.3f, 88.3f, 0.0f, false, true, 88.0f, 87.2f)
                arcTo(86.8f, 86.8f, 0.0f, false, true, 213.8f, 147.6f)
                close()
                moveTo(140.0f, 76.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, -12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 140.0f, 76.0f)
                close()
                moveTo(93.4f, 108.0f)
                arcTo(12.0f, 12.0f, 0.0f, true, true, 89.0f, 91.6f)
                arcTo(12.1f, 12.1f, 0.0f, false, true, 93.4f, 108.0f)
                close()
                moveTo(93.4f, 148.0f)
                arcTo(12.0f, 12.0f, 0.0f, true, true, 77.0f, 143.6f)
                arcTo(12.1f, 12.1f, 0.0f, false, true, 93.4f, 148.0f)
                close()
                moveTo(183.4f, 96.0f)
                arcTo(12.0f, 12.0f, 0.0f, true, true, 167.0f, 91.6f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 183.4f, 96.0f)
                close()
            }
        }
            .build()
        return _palette!!
    }

private var _palette: ImageVector? = null
