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

import android.R.attr.colorAccent
import android.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.neoapps.neolauncher.NeoApp
import com.saggitt.omega.util.getColorAttr
import com.saggitt.omega.util.getSystemAccent
import com.saggitt.omega.wallpaper.WallpaperManagerCompat

sealed class AccentColorOption {
    abstract val isSupported: Boolean
    abstract val displayName: Int
    abstract val accentColor: Int

    object SystemAccent : AccentColorOption() {
        override val isSupported = true
        override val displayName = R.string.icon_shape_system_default

        override val accentColor: Int
            get() = NeoApp.instance?.applicationContext!!.getSystemAccent(false)

        override fun toString() = "system_accent"
    }

    object WallpaperPrimary : AccentColorOption() {
        override val isSupported = true
        override val displayName = R.string.theme_auto
        override val accentColor: Int
            get() = WallpaperManagerCompat.INSTANCE.get(NeoApp.instance?.applicationContext)
                .wallpaperColors?.getPrimaryColor() ?: LightPrimary.toArgb()

        override fun toString() = "wallpaper_primary"
    }

    object WallpaperSecondary : AccentColorOption() {
        override val isSupported = true
        override val displayName = R.string.color_wallpaper_secondary
        override val accentColor: Int
            get() = WallpaperManagerCompat.INSTANCE.get(NeoApp.instance?.applicationContext)
                .wallpaperColors?.getSecondaryColor() ?: LightPrimary.toArgb()

        override fun toString() = "wallpaper_secondary"
    }

    object WallpaperTertiary : AccentColorOption() {
        override val isSupported = true
        override val displayName = R.string.color_wallpaper_tertiary
        override val accentColor: Int
            get() = WallpaperManagerCompat.INSTANCE.get(NeoApp.instance?.applicationContext)
                .wallpaperColors?.getTertiaryColor() ?: LightPrimary.toArgb()

        override fun toString() = "wallpaper_tertiary"
    }

    class CustomColor(override val accentColor: Int) : AccentColorOption() {
        override val isSupported = true
        override val displayName = R.string.app_name

        constructor(color: Long) : this(color.toInt())

        override fun equals(other: Any?) = other is CustomColor && other.accentColor == accentColor

        override fun hashCode() = accentColor
        override fun toString() = "custom|#${String.format("%08x", accentColor)}"
    }

    object LauncherDefault : AccentColorOption() {
        override val isSupported = true

        override val displayName = -1
        override val accentColor: Int
            get() = NeoApp.instance?.applicationContext!!.getColorAttr(colorAccent)

    }

    companion object {
        fun fromString(stringValue: String) = when (stringValue) {
            "system_accent"       -> SystemAccent
            "wallpaper_primary"   -> WallpaperPrimary
            "wallpaper_secondary" -> WallpaperSecondary
            "wallpaper_tertiary"  -> WallpaperTertiary
            "default"             -> LauncherDefault
            else                  -> instantiateCustomColor(stringValue)
        }

        private fun instantiateCustomColor(stringValue: String): AccentColorOption {
            try {
                if (stringValue.startsWith("custom")) {
                    val color = Color.parseColor(stringValue.substring(7))
                    return CustomColor(color)
                }
            } catch (_: IllegalArgumentException) {
            }
            return when {
                Utilities.ATLEAST_S -> SystemAccent
                else                -> WallpaperPrimary
            }
        }
    }
}