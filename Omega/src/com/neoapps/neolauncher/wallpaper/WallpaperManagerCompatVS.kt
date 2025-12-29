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
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import com.neoapps.neolauncher.wallpaper.WallpaperColorsCompat.Companion.HINT_SUPPORTS_DARK_TEXT
import com.neoapps.neolauncher.wallpaper.WallpaperColorsCompat.Companion.HINT_SUPPORTS_DARK_THEME

@RequiresApi(Build.VERSION_CODES.S)
internal class WallpaperManagerCompatVS(context: Context) : WallpaperManagerCompat(context) {
    override var wallpaperColors: WallpaperColorsCompat? = null
        private set

    init {
        wallpaperManager.addOnColorsChangedListener(
            { colors, which ->
                if ((which and WallpaperManager.FLAG_SYSTEM) != 0) {
                    update(colors)
                }
            },
            Handler(Looper.getMainLooper())
        )
        update(wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM))
    }

    private fun update(wallpaperColors: WallpaperColors?) {
        if (wallpaperColors == null) {
            this.wallpaperColors = null
            return
        }
        val platformHints = wallpaperManager
            .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            ?.colorHints ?: 0
        var hints = 0
        if ((platformHints and WallpaperColors.HINT_SUPPORTS_DARK_TEXT) != 0) {
            hints = hints or HINT_SUPPORTS_DARK_TEXT
        }
        if ((platformHints and WallpaperColors.HINT_SUPPORTS_DARK_THEME) != 0) {
            hints = hints or HINT_SUPPORTS_DARK_THEME
        }
        this.wallpaperColors = WallpaperColorsCompat(
            wallpaperColors.primaryColor.toArgb(),
            wallpaperColors.secondaryColor?.toArgb(),
            wallpaperColors.tertiaryColor?.toArgb(), hints
        )
    }
}