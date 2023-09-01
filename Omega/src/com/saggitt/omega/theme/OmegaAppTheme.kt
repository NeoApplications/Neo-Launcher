/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Omega Launcher Team
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
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.android.launcher3.Utilities
import com.saggitt.omega.preferences.NeoPrefs
import com.saggitt.omega.preferences.THEME_BLACK
import com.saggitt.omega.preferences.THEME_DARK
import com.saggitt.omega.preferences.THEME_LIGHT
import com.saggitt.omega.util.prefs
import com.saggitt.omega.wallpaper.WallpaperColorsCompat
import com.saggitt.omega.wallpaper.WallpaperManagerCompat

@Composable
fun OmegaAppTheme(
        darkTheme: Boolean = isDarkTheme(),
        blackTheme: Boolean = isBlackTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        darkTheme && blackTheme -> BlackColors
        darkTheme -> DarkColors
        else -> LightColors
    }.copy(
            primary = Color(LocalContext.current.prefs.profileAccentColor.getColor()),
            surfaceTint = Color(LocalContext.current.prefs.profileAccentColor.getColor())
    )

    MaterialTheme(
            colorScheme = colorScheme
    ) {
        content()
    }
}

@Composable
fun isDarkTheme(): Boolean {
    val theme = NeoPrefs.INSTANCE.get(LocalContext.current).profileTheme.get().collectAsState(-1)
    return when (theme.value) {
        THEME_LIGHT -> false
        THEME_DARK -> true
        else -> isAutoThemeDark()
    }
}

@Composable
fun isBlackTheme(): Boolean {
    val theme = NeoPrefs.INSTANCE.get(LocalContext.current).profileTheme.get().collectAsState(-1)
    return when (theme.value) {
        THEME_BLACK -> true
        else -> false
    }
}

@Composable
fun isAutoThemeDark() = when {
    Utilities.ATLEAST_P -> isSystemInDarkTheme()
    else -> wallpaperSupportsDarkTheme()
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

private val LightColors = lightColorScheme(
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

private val DarkColors = darkColorScheme(
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

private val BlackColors = darkColorScheme(
    background = BlackBackground,
    onBackground = BlackOnBackground,
    surface = BlackSurface,
    onSurface = BlackOnSurface,
    primary = BlackPrimary,
    onPrimary = BlackOnPrimary,
    surfaceVariant = BlackSurfaceVariant,
    onSurfaceVariant = BlackOnSurfaceVariant,
    outline = BlackOutline
)
