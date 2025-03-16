/*
 * This file is part of Neo Launcher
 * Copyright (c) 2025   Neo Launcher Team
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.neoapps.launcher.util.CoreUtils.Companion.minSDK
import com.neoapps.launcher.wallpaper.WallpaperColorsCompat
import com.neoapps.launcher.wallpaper.WallpaperManagerCompat

@Composable
fun LauncherAppTheme(
    darkTheme: Boolean = isDarkTheme(),
    blackTheme: Boolean = isBlackTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme {
        content()
    }
}

@Composable
fun isDarkTheme(): Boolean {
    return false
    //TODO: Implement dark theme
}

@Composable
fun isBlackTheme(): Boolean {
    return false
    //TODO: Implement black theme
}

@Composable
fun isAutoThemeDark() = when {
    minSDK(Build.VERSION_CODES.P) -> isSystemInDarkTheme()
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
