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

val Phosphor.Folder: ImageVector
    get() {
        if (_folder != null) {
            return _folder!!
        }
        _folder = Builder(
            name = "Folder",
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
                moveTo(216f, 72f)
                lineTo(131.31f, 72f)
                lineTo(104f, 44.69f)
                quadTo(96f, 40f, 92.69f, 40f)
                lineTo(40f, 40f)
                quadTo(24f, 40f, 24f, 56f)
                lineTo(24f, 200.62f)
                quadTo(24f, 216f, 39.38f, 216f)
                lineTo(216.89f, 216f)
                quadTo(232f, 216f, 232f, 200.89f)
                lineTo(232f, 88f)
                quadTo(232f, 72f, 216f, 72f)
                close()

                moveTo(40f, 56f)
                lineTo(92.69f, 56f)
                lineTo(108.69f, 72f)
                lineTo(40f, 72f)
                close()

                moveTo(216f, 200f)
                lineTo(40f, 200f)
                lineTo(40f, 88f)
                lineTo(216f, 88f)
                close()
            }
        }
            .build()
        return _folder!!
    }

private var _folder: ImageVector? = null
