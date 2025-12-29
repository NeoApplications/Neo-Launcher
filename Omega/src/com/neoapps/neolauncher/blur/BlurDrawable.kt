/*
 * This file is part of Neo Launcher
 * Copyright (c) 2023   Neo Launcher Team
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.neoapps.neolauncher.blur

import android.graphics.Canvas
import android.graphics.drawable.Drawable

abstract class BlurDrawable : Drawable(), BlurWallpaperProvider.Listener {

    abstract var blurRadii: Radii
    abstract var viewOffsetX: Float

    open var blurScaleX = 0f
    open var blurScaleY = 0f
    open var blurPivotX = 0f
    open var blurPivotY = 0f

    override fun draw(canvas: Canvas) {
        draw(canvas, false)
    }

    abstract fun draw(canvas: Canvas, noRadius: Boolean = false)

    abstract fun setBlurBounds(left: Float, top: Float, right: Float, bottom: Float)

    abstract fun startListening()
    abstract fun stopListening()

    data class Radii(
        val topLeft: Float = 0f,
        val topRight: Float = 0f,
        val bottomLeft: Float = 0f,
        val bottomRight: Float = 0f) {

        constructor(radius: Float) : this(radius, radius, radius, radius)
        constructor(topRadius: Float, bottomRadius: Float)
                : this(topRadius, topRadius, bottomRadius, bottomRadius)
    }
}
