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

val Phosphor.ScribbleLoop: ImageVector
    get() {
        if (`_scribble-loop` != null) {
            return `_scribble-loop`!!
        }
        `_scribble-loop` = Builder(
            name = "Scribble-loop",
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
                moveTo(246.1f, 154.9f)
                curveToRelative(-1.2f, -1.5f, -22.9f, -27.1f, -59.8f, -41.4f)
                curveToRelative(-2.1f, -17.8f, -8.9f, -34.0f, -19.6f, -46.6f)
                curveTo(151.7f, 49.3f, 130.0f, 40.0f, 104.0f, 40.0f)
                curveTo(52.5f, 40.0f, 18.9f, 86.2f, 17.5f, 88.1f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 13.0f, 9.4f)
                curveTo(30.8f, 97.0f, 60.8f, 56.0f, 104.0f, 56.0f)
                curveToRelative(21.2f, 0.0f, 38.6f, 7.4f, 50.5f, 21.3f)
                arcToRelative(68.0f, 68.0f, 0.0f, false, true, 14.7f, 30.8f)
                arcTo(134.2f, 134.2f, 0.0f, false, false, 136.0f, 104.0f)
                curveToRelative(-26.1f, 0.0f, -47.9f, 6.8f, -63.3f, 19.7f)
                curveTo(59.2f, 135.1f, 51.4f, 151.0f, 51.4f, 167.2f)
                arcToRelative(47.4f, 47.4f, 0.0f, false, false, 13.9f, 34.1f)
                curveToRelative(9.6f, 9.6f, 23.0f, 14.7f, 38.7f, 14.7f)
                curveToRelative(25.2f, 0.0f, 46.7f, -10.0f, 62.1f, -28.8f)
                curveToRelative(12.2f, -15.0f, 19.6f, -35.1f, 20.8f, -56.0f)
                arcToRelative(146.3f, 146.3f, 0.0f, false, true, 47.0f, 33.9f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, 12.2f, -10.2f)
                close()
                moveTo(104.0f, 200.0f)
                curveToRelative(-25.3f, 0.0f, -36.6f, -16.4f, -36.6f, -32.8f)
                curveToRelative(0.0f, -22.7f, 21.5f, -47.2f, 68.6f, -47.2f)
                arcToRelative(117.3f, 117.3f, 0.0f, false, true, 35.0f, 5.3f)
                verticalLineToRelative(0.3f)
                curveTo(171.0f, 162.6f, 148.0f, 200.0f, 104.0f, 200.0f)
                close()
            }
        }
            .build()
        return `_scribble-loop`!!
    }

private var `_scribble-loop`: ImageVector? = null
