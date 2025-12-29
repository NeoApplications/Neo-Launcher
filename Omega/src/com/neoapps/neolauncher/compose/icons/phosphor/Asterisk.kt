/*
 * This file is part of Neo Launcher
 * Copyright (c) 2024   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

val Phosphor.Asterisk: ImageVector
    get() {
        if (_asterisk != null) {
            return _asterisk!!
        }
        _asterisk = Builder(
            name = "Asterisk",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(211.1f, 176.0f)
                arcToRelative(7.7f, 7.7f, 0.0f, false, true, -6.9f, 4.0f)
                arcToRelative(7.3f, 7.3f, 0.0f, false, true, -4.0f, -1.1f)
                lineToRelative(-64.2f, -37.0f)
                verticalLineTo(216.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                verticalLineTo(141.9f)
                lineToRelative(-64.2f, 37.0f)
                arcToRelative(7.3f, 7.3f, 0.0f, false, true, -4.0f, 1.1f)
                arcToRelative(7.7f, 7.7f, 0.0f, false, true, -6.9f, -4.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 2.9f, -10.9f)
                lineTo(112.0f, 128.0f)
                lineTo(47.8f, 90.9f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 8.0f, -13.8f)
                lineToRelative(64.2f, 37.0f)
                verticalLineTo(40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                verticalLineToRelative(74.1f)
                lineToRelative(64.2f, -37.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, 8.0f, 13.8f)
                lineTo(144.0f, 128.0f)
                lineToRelative(64.2f, 37.1f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 211.1f, 176.0f)
                close()
            }
        }
            .build()
        return _asterisk!!
    }

private var _asterisk: ImageVector? = null