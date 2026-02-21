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

val Phosphor.Bank: ImageVector
    get() {
        return Builder(
            name = "Bank",
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
                moveTo(24.0f, 104.0f)
                horizontalLineTo(48.0f)
                verticalLineToRelative(64.0f)
                horizontalLineTo(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineTo(224.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                horizontalLineTo(208.0f)
                verticalLineTo(104.0f)
                horizontalLineToRelative(24.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 4.19f, -14.81f)
                lineToRelative(-104.0f, -64.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.38f, 0.0f)
                lineToRelative(-104.0f, 64.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 24.0f, 104.0f)
                close()

                moveTo(64.0f, 104.0f)
                horizontalLineTo(96.0f)
                verticalLineToRelative(64.0f)
                horizontalLineTo(64.0f)
                close()

                moveTo(144.0f, 104.0f)
                verticalLineToRelative(64.0f)
                horizontalLineTo(112.0f)
                verticalLineTo(104.0f)
                close()

                moveTo(192.0f, 168.0f)
                horizontalLineTo(160.0f)
                verticalLineTo(104.0f)
                horizontalLineToRelative(32.0f)
                close()

                moveTo(128.0f, 41.39f)
                lineTo(203.74f, 88.0f)
                horizontalLineTo(52.26f)
                close()

                moveTo(248.0f, 208.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                horizontalLineTo(16.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineTo(240.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 248.0f, 208.0f)
                close()
            }
        }
            .build()
    }
