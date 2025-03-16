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
 */

package com.neoapps.launcher.theme

import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import com.android.launcher3.R
import com.neoapps.launcher.NeoApp
import com.neoapps.launcher.util.CoreUtils.Companion.minSDK
import com.neoapps.launcher.util.getColorAttr
import com.neoapps.launcher.util.getSystemAccent
import com.neoapps.launcher.wallpaper.WallpaperManagerCompat

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
        override val isSupported = minSDK(Build.VERSION_CODES.O_MR1)
        override val displayName = R.string.theme_auto
        override val accentColor: Int
            get() = WallpaperManagerCompat.INSTANCE.get(NeoApp.instance?.applicationContext)
                .wallpaperColors?.getPrimaryColor() ?: LightPrimary.toArgb()

        override fun toString() = "wallpaper_primary"
    }

    object WallpaperSecondary : AccentColorOption() {
        override val isSupported = minSDK(Build.VERSION_CODES.O_MR1)
        override val displayName = R.string.color_wallpaper_secondary
        override val accentColor: Int
            get() = WallpaperManagerCompat.INSTANCE.get(NeoApp.instance?.applicationContext)
                .wallpaperColors?.getSecondaryColor() ?: LightPrimary.toArgb()

        override fun toString() = "wallpaper_secondary"
    }

    object WallpaperTertiary : AccentColorOption() {
        override val isSupported = minSDK(Build.VERSION_CODES.O_MR1)
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
            get() = NeoApp.instance?.applicationContext!!.getColorAttr(R.attr.colorAccent)

    }

    companion object {
        fun fromString(stringValue: String) = when (stringValue) {
            "system_accent" -> SystemAccent
            "wallpaper_primary" -> WallpaperPrimary
            "wallpaper_secondary" -> WallpaperSecondary
            "wallpaper_tertiary" -> WallpaperTertiary
            "default" -> LauncherDefault
            else -> instantiateCustomColor(stringValue)
        }

        private fun instantiateCustomColor(stringValue: String): AccentColorOption {
            try {
                if (stringValue.startsWith("custom")) {
                    val color = stringValue.substring(7).toColorInt()
                    return CustomColor(color)
                }
            } catch (_: IllegalArgumentException) {
            }
            return when {
                minSDK(Build.VERSION_CODES.S) -> SystemAccent
                minSDK(Build.VERSION_CODES.O_MR1) -> WallpaperPrimary
                else -> LauncherDefault
            }
        }
    }
}