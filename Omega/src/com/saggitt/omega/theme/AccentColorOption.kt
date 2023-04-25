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

package com.saggitt.omega.theme

import android.app.WallpaperManager.FLAG_SYSTEM
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.uioverrides.dynamicui.WallpaperManagerCompat
import com.saggitt.omega.OmegaApp
import com.saggitt.omega.util.getColorAttr
import com.saggitt.omega.util.getSystemAccent

sealed class AccentColorOption {
    abstract val isSupported: Boolean
    abstract val displayName: Int
    abstract val accentColor: Int

    object SystemAccent : AccentColorOption() {
        override val isSupported = true
        override val displayName = R.string.icon_shape_system_default

        override val accentColor: Int
            get() = OmegaApp.instance?.applicationContext!!.getSystemAccent(false)
    }

    object WallpaperPrimary : AccentColorOption() {
        override val isSupported = Utilities.ATLEAST_OREO_MR1
        override val displayName = R.string.theme_auto
        override val accentColor: Int
            get() = WallpaperManagerCompat.getInstance(OmegaApp.instance?.applicationContext)
                .getWallpaperColors(FLAG_SYSTEM)!!.primaryColor

        override fun toString() = "wallpaper_primary"
    }

    object WallpaperSecondary : AccentColorOption() {
        override val isSupported = Utilities.ATLEAST_OREO_MR1
        override val displayName = R.string.color_wallpaper_secondary
        override val accentColor: Int
            get() = WallpaperManagerCompat.getInstance(OmegaApp.instance?.applicationContext)
                .getWallpaperColors(FLAG_SYSTEM)!!.secondaryColor

        override fun toString() = "wallpaper_primary"
    }

    class CustomColor(override val accentColor: Int) : AccentColorOption() {
        override val isSupported = true
        override val displayName = R.string.app_name

        constructor(color: Long) : this(color.toInt())

        override fun equals(other: Any?) = other is CustomColor && other.accentColor == accentColor

        override fun hashCode() = accentColor
    }

    object LauncherDefault : AccentColorOption() {
        override val isSupported = true

        override val displayName = -1
        override val accentColor: Int
            get() = OmegaApp.instance?.applicationContext!!.getColorAttr(R.attr.colorAccent)

    }
}