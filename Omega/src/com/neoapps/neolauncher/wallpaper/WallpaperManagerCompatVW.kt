/*
 * This file is part of Neo Launcher
 * Copyright (c) 2026   Neo Launcher Team
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
import android.util.Log
import androidx.annotation.RequiresApi
import com.neoapps.neolauncher.util.Permissions.hasWallpaperAccess
import com.neoapps.neolauncher.wallpaper.WallpaperColorsCompat.Companion.HINT_SUPPORTS_DARK_TEXT
import com.neoapps.neolauncher.wallpaper.WallpaperColorsCompat.Companion.HINT_SUPPORTS_DARK_THEME

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
class WallpaperManagerCompatVW(context: Context) : WallpaperManagerCompat(context) {
    override var wallpaperColors: WallpaperColorsCompat? = null
        private set

    init {
        wallpaperManager.addOnColorsChangedListener(
            { colors, which ->
                if ((which and WallpaperManager.FLAG_SYSTEM) != 0) {
                    if (colors != null) {
                        update(colors)
                    } else {
                        extractColors()
                    }
                }
            },
            Handler(Looper.getMainLooper())
        )
        extractColors()
    }

    private fun extractColors() {
        try {
            if (context.hasWallpaperAccess) {
                val drawable = wallpaperManager.drawable
                if (drawable != null) {
                    update(WallpaperColors.fromDrawable(drawable))
                    return
                }
            }
            update(wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM))
        } catch (e: Exception) {
            Log.w("WallpaperManagerCompatVW", "Error extracting colors: ${e.message}")
            try {
                update(wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM))
            } catch (ex: Exception) {
                Log.w("WallpaperManagerCompatVW", "Error in fallback extract colors: ${ex.message}")
            }
        }
    }

    private fun update(wallpaperColors: WallpaperColors?) {
        if (wallpaperColors == null) {
            this.wallpaperColors = null
            return
        }
        val platformHints = wallpaperColors.colorHints
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