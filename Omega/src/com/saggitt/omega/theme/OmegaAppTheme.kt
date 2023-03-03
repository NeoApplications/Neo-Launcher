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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.saggitt.omega.util.isBlackTheme
import com.saggitt.omega.util.prefs

@Composable
fun OmegaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    blackTheme: Boolean = isBlackTheme,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = when {
            darkTheme && blackTheme -> BlackColors
            darkTheme               -> DarkColors
            else                    -> LightColors
        }.copy(
            primary = Color(LocalContext.current.prefs.profileAccentColor.getValue()),
            surfaceTint = Color(LocalContext.current.prefs.profileAccentColor.getValue())
        ),
        content = content
    )
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
