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

class WallpaperColorsCompat(
    primaryColor: Int,
    secondaryColor: Int?,
    tertiaryColor: Int?,
    colorHints: Int,
) {
    private val mPrimaryColor = primaryColor
    private val mSecondaryColor = secondaryColor
    private val mTertiaryColor = tertiaryColor
    private val mColorHints = colorHints
    fun getPrimaryColor(): Int {
        return mPrimaryColor
    }

    fun getSecondaryColor(): Int? {
        return mSecondaryColor
    }

    fun getTertiaryColor(): Int? {
        return mTertiaryColor
    }

    fun getColorHints(): Int {
        return mColorHints
    }

    companion object {
        const val HINT_SUPPORTS_DARK_TEXT = 0x1
        const val HINT_SUPPORTS_DARK_THEME = 0x2
    }
}