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

val Phosphor.ArrowsDownUp: ImageVector
    get() {
        if (_arrows_down_up != null) {
            return _arrows_down_up!!
        }
        _arrows_down_up = Builder(
            name = "Arrows-down-up",
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
                moveTo(117.7f, 170.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, 11.4f)
                lineToRelative(-32.0f, 32.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                lineToRelative(-32.0f, -32.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, -11.4f)
                lineTo(72.0f, 188.7f)
                lineTo(72.0f, 48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                lineTo(88.0f, 188.7f)
                lineToRelative(18.3f, -18.4f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 117.7f, 170.3f)
                close()
                moveTo(213.7f, 74.3f)
                lineTo(181.7f, 42.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -11.4f, 0.0f)
                lineToRelative(-32.0f, 32.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 11.4f, 11.4f)
                lineTo(168.0f, 67.3f)
                lineTo(168.0f, 208.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(184.0f, 67.3f)
                lineToRelative(18.3f, 18.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 11.4f, 0.0f)
                arcTo(8.1f, 8.1f, 0.0f, false, false, 213.7f, 74.3f)
                close()
            }
        }
            .build()
        return _arrows_down_up!!
    }

private var _arrows_down_up: ImageVector? = null
