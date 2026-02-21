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

val Phosphor.Code: ImageVector
    get() {
        return Builder(
            name = "Code",
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

                moveTo(69.12f, 94.15f)
                lineTo(28.5f, 128.0f)
                lineTo(69.12f, 161.85f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, -10.24f, 12.29f)
                lineToRelative(-48.0f, -40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -12.29f)
                lineToRelative(48.0f, -40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 10.24f, 12.3f)
                close()

                moveTo(245.12f, 121.85f)
                lineToRelative(-48.0f, -40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, -10.24f, 12.3f)
                lineTo(227.5f, 128.0f)
                lineToRelative(-40.62f, 33.85f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, 10.24f, 12.29f)
                lineToRelative(48.0f, -40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -12.29f)
                close()

                moveTo(162.73f, 32.48f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -10.25f, 4.79f)
                lineToRelative(-64.0f, 176.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 4.79f, 10.26f)
                arcTo(8.14f, 8.14f, 0.0f, false, false, 96.0f, 224.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 7.52f, -5.27f)
                lineToRelative(64.0f, -176.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 162.73f, 32.48f)
                close()
            }
        }
            .build()
    }
