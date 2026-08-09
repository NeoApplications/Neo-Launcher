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

val Phosphor.Infinity: ImageVector
    get() {
        return Builder(
            name = "Infinity",
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
                moveTo(248f, 128f)
                arcToRelative(56f, 56f, 0f, false, true, -95.6f, 39.6f)
                lineToRelative(-0.33f, -0.35f)
                lineTo(92.12f, 99.55f)
                arcToRelative(40f, 40f, 0f, true, false, 0f, 56.9f)
                lineToRelative(8.52f, -9.62f)
                arcToRelative(8f, 8f, 0f, true, true, 12f, 10.61f)
                lineToRelative(-8.69f, 9.81f)
                lineToRelative(-0.33f, 0.35f)
                arcToRelative(56f, 56f, 0f, true, true, 0f, -79.2f)
                lineToRelative(0.33f, 0.35f)
                lineToRelative(59.95f, 67.7f)
                arcToRelative(40f, 40f, 0f, true, false, 0f, -56.9f)
                lineToRelative(-8.52f, 9.62f)
                arcToRelative(8f, 8f, 0f, true, true, -12f, -10.61f)
                lineToRelative(8.69f, -9.81f)
                lineToRelative(0.33f, -0.35f)
                arcTo(56f, 56f, 0f, false, true, 248f, 128f)
                close()
            }
        }
            .build()
    }
