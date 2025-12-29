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
import com.neoapps.neolauncher.compose.icons.PhosphorCustom

val PhosphorCustom.Rotate: ImageVector
    get() {
        if (_rotate != null) {
            return _rotate!!
        }
        _rotate = Builder(
            name = "Rotate",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.781219f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveToRelative(204.989f, 121.071f)
                verticalLineToRelative(56.248f)
                arcToRelative(6.25f, 6.25f, 0.0f, false, true, -12.5f, 0.0f)
                verticalLineToRelative(-41.17f)
                lineToRelative(-76.794f, 76.872f)
                arcToRelative(6.406f, 6.406f, 0.0f, false, true, -8.906f, 0.0f)
                lineTo(31.793f, 138.023f)
                arcToRelative(6.328f, 6.328f, 0.0f, false, true, 8.906f, -8.906f)
                lineToRelative(70.544f, 70.622f)
                lineToRelative(72.419f, -72.419f)
                horizontalLineToRelative(-41.17f)
                arcToRelative(6.25f, 6.25f, 0.0f, false, true, 0.0f, -12.5f)
                horizontalLineToRelative(56.248f)
                arcToRelative(6.25f, 6.25f, 0.0f, false, true, 6.25f, 6.25f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.783022f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveToRelative(51.657f, 120.0f)
                lineToRelative(0.411f, -56.376f)
                arcToRelative(6.264f, 6.264f, 0.0f, false, true, 12.528f, 0.091f)
                lineTo(64.295f, 104.98f)
                lineTo(141.826f, 28.494f)
                arcToRelative(6.421f, 6.421f, 0.0f, false, true, 8.926f, 0.065f)
                lineToRelative(74.62f, 75.717f)
                arcToRelative(6.342f, 6.342f, 0.0f, false, true, -8.991f, 8.861f)
                lineToRelative(-70.189f, -71.299f)
                lineToRelative(-73.114f, 72.055f)
                lineToRelative(41.264f, 0.301f)
                arcToRelative(6.264f, 6.264f, 0.0f, false, true, -0.091f, 12.528f)
                lineToRelative(-56.376f, -0.411f)
                arcToRelative(6.264f, 6.264f, 0.0f, false, true, -6.218f, -6.31f)
                close()
            }
        }
            .build()
        return _rotate!!
    }

private var _rotate: ImageVector? = null
