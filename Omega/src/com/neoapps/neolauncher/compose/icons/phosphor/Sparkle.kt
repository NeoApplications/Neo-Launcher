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

val Phosphor.Sparkle: ImageVector
    get() {
        if (_sparkle != null) {
            return _sparkle!!
        }
        _sparkle = Builder(
            name = "Sparkle",
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
                moveTo(198.4f, 129.0f)
                lineToRelative(-52.2f, -19.2f)
                lineTo(127.0f, 57.6f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -30.0f, 0.0f)
                lineTo(77.8f, 109.8f)
                lineTo(25.6f, 129.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 0.0f, 30.0f)
                lineToRelative(52.2f, 19.2f)
                lineTo(97.0f, 230.4f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 30.0f, 0.0f)
                lineToRelative(19.2f, -52.2f)
                lineTo(198.4f, 159.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 0.0f, -30.0f)
                close()
                moveTo(140.7f, 163.2f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, -9.5f, 9.5f)
                lineTo(112.0f, 224.9f)
                lineTo(92.8f, 172.7f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, -9.5f, -9.5f)
                lineTo(31.1f, 144.0f)
                lineToRelative(52.2f, -19.2f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, 9.5f, -9.5f)
                lineTo(112.0f, 63.1f)
                lineToRelative(19.2f, 52.2f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, 9.5f, 9.5f)
                lineTo(192.9f, 144.0f)
                close()
                moveTo(144.0f, 40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 8.0f, -8.0f)
                horizontalLineToRelative(16.0f)
                lineTo(168.0f, 16.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                lineTo(184.0f, 32.0f)
                horizontalLineToRelative(16.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                lineTo(184.0f, 48.0f)
                lineTo(184.0f, 64.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                lineTo(168.0f, 48.0f)
                lineTo(152.0f, 48.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 144.0f, 40.0f)
                close()
                moveTo(248.0f, 88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                horizontalLineToRelative(-8.0f)
                verticalLineToRelative(8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                lineTo(216.0f, 96.0f)
                horizontalLineToRelative(-8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(8.0f)
                lineTo(216.0f, 72.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                verticalLineToRelative(8.0f)
                horizontalLineToRelative(8.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 248.0f, 88.0f)
                close()
            }
        }
            .build()
        return _sparkle!!
    }

private var _sparkle: ImageVector? = null
