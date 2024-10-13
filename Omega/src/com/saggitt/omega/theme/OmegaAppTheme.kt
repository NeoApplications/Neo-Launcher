/*
 *  This file is part of Neo Launcher
 *  Copyright (c) 2024   Neo Launcher Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.android.launcher3.Utilities
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.saggitt.omega.preferences.THEME_BLACK
import com.saggitt.omega.preferences.THEME_DARK
import com.saggitt.omega.preferences.THEME_LIGHT
import com.saggitt.omega.preferences.THEME_WALLPAPER
import com.saggitt.omega.preferences.THEME_WALLPAPER_BLACK
import com.saggitt.omega.util.prefs
import com.saggitt.omega.wallpaper.WallpaperColorsCompat
import com.saggitt.omega.wallpaper.WallpaperManagerCompat

@Composable
fun OmegaAppTheme(
    darkTheme: Boolean = isDarkTheme(),
    blackTheme: Boolean = LocalContext.current.isBlackTheme,
    content: @Composable () -> Unit,
) {
    val accentPref by LocalContext.current.prefs.profileAccentColor.getState()
    val accentColor by remember(accentPref) {
        mutableIntStateOf(AccentColorOption.fromString(accentPref).accentColor)
    }

    val colorScheme = dynamicColorScheme(
        seedColor = Color(accentColor),
        isDark = darkTheme,
        isAmoled = blackTheme,
        style = PaletteStyle.Fidelity,
    ) {
        it.copy(
            primary = Color(accentColor),
            scrim = it.background,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

@Composable
fun isDarkTheme(): Boolean {
    val theme = LocalContext.current.prefs.profileTheme.get().collectAsState(-1)
    return when (theme.value) {
        THEME_LIGHT
             -> false

        THEME_DARK,
        THEME_BLACK,
             -> true

        THEME_WALLPAPER,
        THEME_WALLPAPER_BLACK,
             -> wallpaperSupportsDarkTheme()

        else -> isAutoThemeDark()
    }
}

@Composable
fun isAutoThemeDark() = when {
    Utilities.ATLEAST_P -> isSystemInDarkTheme()
    else                -> wallpaperSupportsDarkTheme()
}

@Composable
fun wallpaperSupportsDarkTheme(): Boolean {
    val wallpaperManager = WallpaperManagerCompat.INSTANCE.get(LocalContext.current)
    var supportsDarkTheme by remember { mutableStateOf(wallpaperManager.supportsDarkTheme) }

    DisposableEffect(wallpaperManager) {
        val listener = object : WallpaperManagerCompat.OnColorsChangedListenerCompat {
            override fun onColorsChanged(colors: WallpaperColorsCompat?, which: Int) {
                supportsDarkTheme = wallpaperManager.supportsDarkTheme
            }
        }
        wallpaperManager.addOnChangeListener(listener)
        onDispose { wallpaperManager.removeOnChangeListener(listener) }
    }
    return supportsDarkTheme
}
