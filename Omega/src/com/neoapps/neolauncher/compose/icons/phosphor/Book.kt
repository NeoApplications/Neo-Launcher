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

val Phosphor.Book: ImageVector
    get() {
        return Builder(
            name = "Book",
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
                moveTo(231.65f, 194.55f)
                lineToRelative(-33.19f, -157.8f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -19.0f, -12.39f)
                lineToRelative(-46.81f, 10.06f)
                arcToRelative(16.08f, 16.08f, 0.0f, false, false, -12.3f, 19.0f)
                lineToRelative(33.19f, 157.8f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 15.62f, 12.72f)
                arcToRelative(16.25f, 16.25f, 0.0f, false, false, 3.38f, -0.36f)
                lineToRelative(46.81f, -10.06f)
                arcTo(16.09f, 16.09f, 0.0f, false, false, 231.65f, 194.55f)
                close()

                moveTo(136.0f, 50.15f)
                lineToRelative(46.8f, -10.0f)
                lineToRelative(3.33f, 15.87f)
                lineToRelative(-46.8f, 10.0f)
                close()

                moveTo(142.62f, 81.62f)
                lineToRelative(46.82f, -10.05f)
                lineToRelative(3.34f, 15.9f)
                lineToRelative(-46.82f, 10.06f)
                close()

                moveTo(149.26f, 113.19f)
                lineToRelative(46.82f, -10.06f)
                lineToRelative(13.3f, 63.24f)
                lineToRelative(-46.82f, 10.06f)
                close()

                moveTo(216.0f, 197.94f)
                lineToRelative(-46.8f, 10.0f)
                lineToRelative(-3.33f, -15.87f)
                lineToRelative(46.84f, -10.07f)
                lineTo(216.0f, 197.85f)
                close()

                moveTo(104.0f, 32.0f)
                horizontalLineTo(56.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 40.0f, 48.0f)
                verticalLineTo(208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                horizontalLineTo(104.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                verticalLineTo(48.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 104.0f, 32.0f)
                close()

                moveTo(56.0f, 48.0f)
                horizontalLineToRelative(48.0f)
                verticalLineToRelative(16.0f)
                horizontalLineTo(56.0f)
                close()

                moveTo(56.0f, 80.0f)
                horizontalLineToRelative(48.0f)
                verticalLineToRelative(96.0f)
                horizontalLineTo(56.0f)
                close()

                moveTo(104.0f, 208.0f)
                horizontalLineTo(56.0f)
                verticalLineTo(192.0f)
                horizontalLineToRelative(48.0f)
                verticalLineToRelative(16.0f)
                close()
            }
        }
            .build()
    }
