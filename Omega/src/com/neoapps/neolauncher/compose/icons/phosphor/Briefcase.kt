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

val Phosphor.Briefcase: ImageVector
    get() {
        return Builder(
            name = "Briefcase",
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
                moveTo(216f, 56f)
                horizontalLineTo(176f)
                verticalLineTo(48f)
                arcToRelative(24f, 24f, 0f, false, false, -24f, -24f)
                horizontalLineTo(104f)
                arcToRelative(24f, 24f, 0f, false, false, -24f, 24f)
                verticalLineTo(56f)
                horizontalLineTo(40f)
                arcToRelative(16f, 16f, 0f, false, false, -16f, 16f)
                verticalLineTo(200f)
                arcToRelative(16f, 16f, 0f, false, false, 16f, 16f)
                horizontalLineTo(216f)
                arcToRelative(16f, 16f, 0f, false, false, 16f, -16f)
                verticalLineTo(72f)
                arcTo(16f, 16f, 0f, false, false, 216f, 56f)
                close()
                moveTo(96f, 48f)
                arcToRelative(8f, 8f, 0f, false, true, 8f, -8f)
                horizontalLineToRelative(48f)
                arcToRelative(8f, 8f, 0f, false, true, 8f, 8f)
                verticalLineTo(56f)
                horizontalLineTo(96f)
                verticalLineTo(48f)
                close()
                moveTo(216f, 72f)
                verticalLineToRelative(41.61f)
                arcTo(184f, 184f, 0f, false, true, 128f, 136f)
                arcTo(184.07f, 184.07f, 0f, false, true, 40f, 113.62f)
                verticalLineTo(72f)
                horizontalLineTo(216f)
                close()
                moveTo(216f, 200f)
                horizontalLineTo(40f)
                verticalLineTo(131.64f)
                arcTo(200.19f, 200.19f, 0f, false, false, 128f, 152f)
                arcTo(200.25f, 200.25f, 0f, false, false, 216f, 131.63f)
                verticalLineTo(200f)
                close()
                moveTo(104f, 112f)
                arcToRelative(8f, 8f, 0f, false, true, 8f, -8f)
                horizontalLineToRelative(32f)
                arcToRelative(8f, 8f, 0f, false, true, 0f, 16f)
                horizontalLineTo(112f)
                arcTo(8f, 8f, 0f, false, true, 104f, 112f)
                close()
            }
        }
            .build()
    }
