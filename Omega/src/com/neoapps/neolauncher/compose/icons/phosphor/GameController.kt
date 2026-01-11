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

val Phosphor.GameController: ImageVector
    get() {
        return Builder(
            name = "GameController",
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
                moveTo(176.0f, 112.0f)
                horizontalLineTo(152.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(24.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                close()

                moveTo(104.0f, 96.0f)
                horizontalLineTo(96.0f)
                verticalLineTo(88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                verticalLineToRelative(8.0f)
                horizontalLineTo(72.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                verticalLineToRelative(-8.0f)
                horizontalLineToRelative(8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()

                moveTo(241.48f, 200.65f)
                arcToRelative(36.0f, 36.0f, 0.0f, false, true, -54.94f, 4.81f)
                curveToRelative(-0.12f, -0.12f, -0.24f, -0.24f, -0.35f, -0.37f)
                lineTo(146.48f, 160.0f)
                horizontalLineToRelative(-37.0f)
                lineTo(69.81f, 205.09f)
                lineToRelative(-0.35f, 0.37f)
                arcTo(36.08f, 36.08f, 0.0f, false, true, 44.0f, 216.0f)
                arcToRelative(36.0f, 36.0f, 0.0f, false, true, -35.44f, -42.25f)
                arcToRelative(0.68f, 0.68f, 0.0f, false, true, 0.0f, -0.14f)
                lineToRelative(16.37f, -84.09f)
                arcTo(59.88f, 59.88f, 0.0f, false, true, 83.89f, 40.0f)
                horizontalLineTo(172.0f)
                arcToRelative(60.08f, 60.08f, 0.0f, false, true, 59.0f, 49.25f)
                curveToRelative(0.0f, 0.06f, 0.0f, 0.12f, 0.0f, 0.18f)
                lineToRelative(16.37f, 84.17f)
                arcToRelative(0.68f, 0.68f, 0.0f, false, true, 0.0f, 0.14f)
                arcTo(35.74f, 35.74f, 0.0f, false, true, 241.48f, 200.65f)
                close()

                moveTo(172.0f, 144.0f)
                arcToRelative(44.0f, 44.0f, 0.0f, false, false, 0.0f, -88.0f)
                horizontalLineTo(83.89f)
                arcTo(43.9f, 43.9f, 0.0f, false, false, 40.68f, 92.37f)
                lineToRelative(0.0f, 0.13f)
                lineToRelative(-16.38f, 84.09f)
                arcTo(20.0f, 20.0f, 0.0f, false, false, 58.0f, 194.3f)
                lineToRelative(41.92f, -47.59f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 6.0f, -2.71f)
                close()

                moveTo(231.7f, 176.59f)
                lineToRelative(-8.74f, -45.0f)
                arcTo(60.0f, 60.0f, 0.0f, false, true, 172.0f, 160.0f)
                horizontalLineToRelative(-4.2f)
                lineTo(198.0f, 194.31f)
                arcToRelative(20.09f, 20.09f, 0.0f, false, false, 17.46f, 5.39f)
                arcToRelative(20.0f, 20.0f, 0.0f, false, false, 16.23f, -23.11f)
                close()
            }
        }
            .build()
    }
