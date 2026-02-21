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

val Phosphor.ChatCircle: ImageVector
    get() {
        return Builder(
            name = "ChatCircle",
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
                moveTo(140.0f, 128.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, -12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 140.0f, 128.0f)
                close()

                moveTo(84.0f, 116.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, false, 12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, false, 84.0f, 116.0f)
                close()

                moveTo(172.0f, 116.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, false, 12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, false, 172.0f, 116.0f)
                close()

                moveTo(232.0f, 128.0f)
                arcTo(104.0f, 104.0f, 0.0f, false, true, 79.12f, 219.82f)
                lineTo(45.07f, 231.17f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -20.24f, -20.24f)
                lineToRelative(11.35f, -34.05f)
                arcTo(104.0f, 104.0f, 0.0f, true, true, 232.0f, 128.0f)
                close()

                moveToRelative(-16.0f, 0.0f)
                arcTo(88.0f, 88.0f, 0.0f, true, false, 51.81f, 172.06f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.66f, 6.54f)
                lineTo(40.0f, 216.0f)
                lineTo(77.4f, 203.53f)
                arcToRelative(7.85f, 7.85f, 0.0f, false, true, 2.53f, -0.42f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 4.0f, 1.08f)
                arcTo(88.0f, 88.0f, 0.0f, false, false, 216.0f, 128.0f)
                close()
            }
        }
            .build()
    }
