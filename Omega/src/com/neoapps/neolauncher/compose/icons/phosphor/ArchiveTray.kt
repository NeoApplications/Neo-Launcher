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

val Phosphor.ArchiveTray: ImageVector
    get() {
        if (_archive_tray != null) {
            return _archive_tray!!
        }
        _archive_tray = Builder(
            name = "Archive-tray",
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
                moveTo(208.0f, 32.0f)
                lineTo(48.0f, 32.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 32.0f, 48.0f)
                lineTo(32.0f, 159.9f)
                horizontalLineToRelative(0.0f)
                lineTo(32.0f, 208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                lineTo(208.0f, 224.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(224.0f, 48.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 208.0f, 32.0f)
                close()
                moveTo(208.0f, 48.0f)
                lineTo(208.0f, 152.0f)
                lineTo(179.3f, 152.0f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, -11.3f, 4.7f)
                lineTo(148.7f, 176.0f)
                lineTo(107.3f, 176.0f)
                lineTo(88.0f, 156.7f)
                arcTo(15.9f, 15.9f, 0.0f, false, false, 76.7f, 152.0f)
                lineTo(48.0f, 152.0f)
                lineTo(48.0f, 48.0f)
                close()
                moveTo(208.0f, 208.0f)
                lineTo(48.0f, 208.0f)
                lineTo(48.0f, 168.0f)
                lineTo(76.7f, 168.0f)
                lineTo(96.0f, 187.3f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, 11.3f, 4.7f)
                horizontalLineToRelative(41.4f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, 11.3f, -4.7f)
                lineTo(179.3f, 168.0f)
                lineTo(208.0f, 168.0f)
                verticalLineToRelative(40.0f)
                close()
                moveTo(88.4f, 123.7f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 11.3f, -11.3f)
                lineTo(120.0f, 132.7f)
                lineTo(120.0f, 72.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                verticalLineToRelative(60.7f)
                lineToRelative(20.3f, -20.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 11.3f, 11.3f)
                lineToRelative(-33.9f, 34.0f)
                horizontalLineToRelative(-0.2f)
                lineToRelative(-0.4f, 0.4f)
                horizontalLineToRelative(-0.2f)
                lineToRelative(-0.5f, 0.3f)
                curveToRelative(0.0f, 0.1f, -0.1f, 0.1f, -0.1f, 0.2f)
                lineToRelative(-0.5f, 0.3f)
                horizontalLineToRelative(-0.2f)
                lineToRelative(-0.5f, 0.3f)
                lineTo(131.0f, 159.2f)
                lineToRelative(-0.7f, 0.3f)
                horizontalLineToRelative(-4.6f)
                lineToRelative(-0.7f, -0.3f)
                horizontalLineToRelative(-0.1f)
                lineToRelative(-0.5f, -0.3f)
                horizontalLineToRelative(-0.2f)
                lineToRelative(-0.5f, -0.3f)
                curveToRelative(0.0f, -0.1f, -0.1f, -0.1f, -0.1f, -0.2f)
                lineToRelative(-0.5f, -0.3f)
                horizontalLineToRelative(-0.2f)
                lineToRelative(-0.4f, -0.4f)
                horizontalLineToRelative(-0.2f)
                close()
            }
        }
            .build()
        return _archive_tray!!
    }

private var _archive_tray: ImageVector? = null
