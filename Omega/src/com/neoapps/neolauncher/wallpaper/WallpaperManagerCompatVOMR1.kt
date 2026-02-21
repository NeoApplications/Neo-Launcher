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

package com.neoapps.neolauncher.wallpaper

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import java.lang.reflect.Method

@RequiresApi(Build.VERSION_CODES.O_MR1)
internal class WallpaperManagerCompatVOMR1(context: Context) : WallpaperManagerCompat(context) {
    private var mWm: WallpaperManager? = null
    private var mWCColorHintsMethod: Method? = null
    override var wallpaperColors: WallpaperColorsCompat? = null
        private set

    init {
        mWm = context.getSystemService(WallpaperManager::class.java)
        try {
            mWCColorHintsMethod = WallpaperColors::class.java.getDeclaredMethod("getColorHints")
        } catch (exc: java.lang.Exception) {
            Log.e("WallpaperManagerCompatVOMR1", "getColorHints not available", exc)
        }
        wallpaperManager.addOnColorsChangedListener(
            { colors, which ->
                if ((which and WallpaperManager.FLAG_SYSTEM) != 0) {
                    getWallpaperColors(which)
                }
            },
            Handler(Looper.getMainLooper())
        )
    }

    private fun getWallpaperColors(which: Int): WallpaperColorsCompat? {
        return convertColorsObject(mWm!!.getWallpaperColors(which))
    }

    override fun addOnChangeListener(listener: OnColorsChangedListenerCompat) {
        val onChangeListener = WallpaperManager.OnColorsChangedListener { colors, which ->
            listener.onColorsChanged(
                convertColorsObject(colors),
                which
            )
        }
        mWm!!.addOnColorsChangedListener(onChangeListener, Handler(Looper.getMainLooper()))
    }

    private fun convertColorsObject(wallpaperColors: WallpaperColors?): WallpaperColorsCompat? {
        if (wallpaperColors == null) {
            return null
        }
        val primary: Color = wallpaperColors.primaryColor
        val secondary: Color? = wallpaperColors.secondaryColor
        val tertiary: Color? = wallpaperColors.tertiaryColor

        val primaryVal = primary.toArgb()
        val secondaryVal = secondary?.toArgb() ?: 0
        val tertiaryVal = tertiary?.toArgb() ?: 0

        var colorHints = 0

        try {
            if (mWCColorHintsMethod != null) {
                colorHints = mWCColorHintsMethod!!.invoke(wallpaperColors) as Int
            }
        } catch (exc: Exception) {
            Log.e("WallpaperManagerCompatVOMR1", "error calling color hints", exc)
        }
        return WallpaperColorsCompat(primaryVal, secondaryVal, tertiaryVal, colorHints)
    }
}