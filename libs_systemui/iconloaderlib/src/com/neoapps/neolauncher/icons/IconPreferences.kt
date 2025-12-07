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

package com.neoapps.neolauncher.icons

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette

class IconPreferences(context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences("com.android.launcher3.prefs", Context.MODE_PRIVATE)

    fun shouldWrapAdaptive(): Boolean {
        return prefs.getBoolean("profile_icon_adaptify", false)
    }

    fun coloredIconBackground(): Boolean {
        return prefs.getBoolean("profile_icon_colored_background", false)
    }

    fun getWrapperBackgroundColor(icon: Drawable): Int {
        val lightness = prefs.getFloat("pref_coloredBackgroundLightness", 0.95f)
        val palette = Palette.Builder(drawableToBitmap(icon)).generate()
        val dominantColor = palette.getDominantColor(Color.WHITE)
        return setLightness(dominantColor, lightness)
    }

    fun shouldTransparentBGIcons(): Boolean {
        return prefs.getBoolean("prefs_transparent_icon_background", false)
    }

    private fun setLightness(color: Int, lightness: Float): Int {
        if (color == Color.WHITE) {
            return color
        }
        val outHsl = floatArrayOf(0f, 0f, 0f)
        ColorUtils.colorToHSL(color, outHsl)
        outHsl[2] = lightness
        return ColorUtils.HSLToColor(outHsl)
    }
}

fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }

    val width = drawable.intrinsicWidth.coerceAtLeast(1)
    val height = drawable.intrinsicHeight.coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
