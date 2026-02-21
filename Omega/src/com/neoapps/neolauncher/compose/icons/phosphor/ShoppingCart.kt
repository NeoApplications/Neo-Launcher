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

val Phosphor.ShoppingCart: ImageVector
    get() {
        return Builder(
            name = "ShoppingCart",
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
                moveTo(230.14f, 58.87f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 224.0f, 56.0f)
                horizontalLineTo(62.68f)
                lineToRelative(-6.08f, -33.43f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 48.73f, 16.0f)
                horizontalLineTo(24.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(18.0f)
                lineToRelative(25.56f, 140.29f)
                arcToRelative(24.0f, 24.0f, 0.0f, false, false, 5.33f, 11.27f)
                arcToRelative(28.0f, 28.0f, 0.0f, true, false, 44.4f, 8.44f)
                horizontalLineToRelative(45.42f)
                arcTo(27.75f, 27.75f, 0.0f, false, false, 160.0f, 204.0f)
                arcToRelative(28.0f, 28.0f, 0.0f, true, false, 28.0f, -28.0f)
                horizontalLineTo(91.17f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -7.87f, -6.57f)
                lineTo(80.13f, 152.0f)
                horizontalLineToRelative(116.0f)
                arcToRelative(24.0f, 24.0f, 0.0f, false, false, 23.61f, -19.71f)
                lineToRelative(12.16f, -66.86f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 230.14f, 58.87f)
                close()

                moveTo(104.0f, 204.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, -12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 104.0f, 204.0f)
                close()

                moveTo(200.0f, 204.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, -12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 200.0f, 204.0f)
                close()

                moveTo(204.0f, 129.43f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -7.9f, 6.57f)
                horizontalLineTo(77.22f)
                lineTo(65.59f, 72.0f)
                horizontalLineTo(214.41f)
                close()
            }
        }
            .build()
    }
