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

val Phosphor.BracketsCurly: ImageVector
    get() {
        if (_brackets_curly != null) {
            return _brackets_curly!!
        }
        _brackets_curly = Builder(
            name = "Brackets-curly",
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
                moveTo(43.2f, 128.0f)
                arcToRelative(30.4f, 30.4f, 0.0f, false, true, 8.0f, 10.3f)
                curveToRelative(4.8f, 9.9f, 4.8f, 22.0f, 4.8f, 33.7f)
                curveToRelative(0.0f, 24.3f, 1.0f, 36.0f, 24.0f, 36.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                curveToRelative(-17.5f, 0.0f, -29.3f, -6.1f, -35.2f, -18.3f)
                curveTo(40.0f, 195.8f, 40.0f, 183.7f, 40.0f, 172.0f)
                curveToRelative(0.0f, -24.3f, -1.0f, -36.0f, -24.0f, -36.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                curveToRelative(23.0f, 0.0f, 24.0f, -11.7f, 24.0f, -36.0f)
                curveToRelative(0.0f, -11.7f, 0.0f, -23.8f, 4.8f, -33.7f)
                curveTo(50.7f, 38.1f, 62.5f, 32.0f, 80.0f, 32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                curveTo(57.0f, 48.0f, 56.0f, 59.7f, 56.0f, 84.0f)
                curveToRelative(0.0f, 11.7f, 0.0f, 23.8f, -4.8f, 33.7f)
                arcTo(30.4f, 30.4f, 0.0f, false, true, 43.2f, 128.0f)
                close()
                moveTo(240.0f, 120.0f)
                curveToRelative(-23.0f, 0.0f, -24.0f, -11.7f, -24.0f, -36.0f)
                curveToRelative(0.0f, -11.7f, 0.0f, -23.8f, -4.8f, -33.7f)
                curveTo(205.3f, 38.1f, 193.5f, 32.0f, 176.0f, 32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                curveToRelative(23.0f, 0.0f, 24.0f, 11.7f, 24.0f, 36.0f)
                curveToRelative(0.0f, 11.7f, 0.0f, 23.8f, 4.8f, 33.7f)
                arcToRelative(30.4f, 30.4f, 0.0f, false, false, 8.0f, 10.3f)
                arcToRelative(30.4f, 30.4f, 0.0f, false, false, -8.0f, 10.3f)
                curveToRelative(-4.8f, 9.9f, -4.8f, 22.0f, -4.8f, 33.7f)
                curveToRelative(0.0f, 24.3f, -1.0f, 36.0f, -24.0f, 36.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                curveToRelative(17.5f, 0.0f, 29.3f, -6.1f, 35.2f, -18.3f)
                curveToRelative(4.8f, -9.9f, 4.8f, -22.0f, 4.8f, -33.7f)
                curveToRelative(0.0f, -24.3f, 1.0f, -36.0f, 24.0f, -36.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
            }
        }
            .build()
        return _brackets_curly!!
    }

private var _brackets_curly: ImageVector? = null
