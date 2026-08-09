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

val Phosphor.Home: ImageVector
    get() {
        return Builder(
            name = "Home",
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
                moveTo(240f, 208f)
                lineTo(224f, 208f)
                lineTo(224f, 136f)
                lineTo(226.34f, 138.34f)
                arcTo(8f, 8f, 0f, false, false, 237.66f, 127f)
                lineTo(139.31f, 28.68f)
                arcTo(16f, 16f, 0f, false, false, 116.69f, 28.68f)
                lineTo(18.34f, 127f)
                arcTo(8f, 8f, 0f, false, false, 29.66f, 138.31f)
                lineTo(32f, 136f)
                lineTo(32f, 208f)
                lineTo(16f, 208f)
                arcTo(8f, 8f, 0f, false, false, 16f, 224f)
                lineTo(240f, 224f)
                arcTo(8f, 8f, 0f, false, false, 240f, 208f)
                close()
            }
        }
            .build()
    }
