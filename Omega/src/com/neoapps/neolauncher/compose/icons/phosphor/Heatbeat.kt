/*
 * This file is part of Neo Launcher
 * Copyright (c) 2026   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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

val Phosphor.Heatbeat: ImageVector
    get() {
        return Builder(
            name = "Heatbeat",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = null,
                strokeLineWidth = 0.0f,
                strokeLineCap = Butt,
                strokeLineJoin = Miter,
                strokeLineMiter = 1f,
                pathFillType = NonZero
            ) {
                moveTo(72.0f, 144.0f)
                horizontalLineTo(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(35.72f)
                lineToRelative(13.62f, -20.44f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 13.32f, 0.0f)
                lineToRelative(25.34f, 38.0f)
                lineToRelative(9.34f, -14.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 136.0f, 128.0f)
                horizontalLineToRelative(24.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                horizontalLineTo(140.28f)
                lineToRelative(-13.62f, 20.44f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -13.32f, 0.0f)
                lineTo(88.0f, 126.42f)
                lineToRelative(-9.34f, 14.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 72.0f, 144.0f)
                close()

                moveTo(178.0f, 40.0f)
                curveToRelative(-20.65f, 0.0f, -38.73f, 8.88f, -50.0f, 23.89f)
                curveTo(116.73f, 48.88f, 98.65f, 40.0f, 78.0f, 40.0f)
                arcToRelative(62.07f, 62.07f, 0.0f, false, false, -62.0f, 62.0f)
                curveToRelative(0.0f, 0.75f, 0.0f, 1.5f, 0.0f, 2.25f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, 16.0f, -0.5f)
                curveToRelative(0.0f, -0.58f, 0.0f, -1.17f, 0.0f, -1.75f)
                arcTo(46.06f, 46.06f, 0.0f, false, true, 78.0f, 56.0f)
                curveToRelative(19.45f, 0.0f, 35.78f, 10.36f, 42.6f, 27.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 14.8f, 0.0f)
                curveToRelative(6.82f, -16.67f, 23.15f, -27.0f, 42.6f, -27.0f)
                arcToRelative(46.06f, 46.06f, 0.0f, false, true, 46.0f, 46.0f)
                curveToRelative(0.0f, 53.61f, -77.76f, 102.15f, -96.0f, 112.8f)
                curveToRelative(-10.83f, -6.31f, -42.63f, -26.0f, -66.68f, -52.21f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, -11.8f, 10.82f)
                curveToRelative(31.17f, 34.0f, 72.93f, 56.68f, 74.69f, 57.63f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 7.58f, 0.0f)
                curveTo(136.21f, 228.66f, 240.0f, 172.0f, 240.0f, 102.0f)
                arcTo(62.07f, 62.07f, 0.0f, false, false, 178.0f, 40.0f)
                close()
            }
        }
            .build()
    }
