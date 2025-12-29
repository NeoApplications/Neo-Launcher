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

val Phosphor.WifiHigh: ImageVector
    get() {
        if (_wifi_high != null) {
            return _wifi_high!!
        }
        _wifi_high = Builder(
            name = "Wifi-high",
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
                moveTo(168.6f, 160.3f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, 0.2f, 11.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -5.7f, 2.5f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -5.6f, -2.3f)
                arcToRelative(42.8f, 42.8f, 0.0f, false, false, -59.0f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, -11.1f, -11.5f)
                arcToRelative(58.7f, 58.7f, 0.0f, false, true, 81.2f, 0.0f)
                close()
                moveTo(128.0f, 96.0f)
                arcToRelative(105.6f, 105.6f, 0.0f, false, false, -74.6f, 30.4f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, 11.2f, 11.4f)
                arcToRelative(90.8f, 90.8f, 0.0f, false, true, 126.8f, 0.0f)
                arcToRelative(7.8f, 7.8f, 0.0f, false, false, 5.6f, 2.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 5.6f, -13.7f)
                arcTo(105.6f, 105.6f, 0.0f, false, false, 128.0f, 96.0f)
                close()
                moveTo(236.6f, 92.5f)
                arcToRelative(154.8f, 154.8f, 0.0f, false, false, -217.2f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 11.3f, 11.4f)
                arcToRelative(138.6f, 138.6f, 0.0f, false, true, 194.6f, 0.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 5.6f, 2.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 5.7f, -13.7f)
                close()
                moveTo(128.0f, 188.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, false, 12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, false, 128.0f, 188.0f)
                close()
            }
        }
            .build()
        return _wifi_high!!
    }

private var _wifi_high: ImageVector? = null
