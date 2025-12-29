package com.neoapps.neolauncher.util

import com.neoapps.neolauncher.theme.AccentColorOption

val staticColors = listOf(
    AccentColorOption.CustomColor(0xFFF32020),
    AccentColorOption.CustomColor(0xFFF20D69),
    AccentColorOption.CustomColor(0xFFEF5350),
    AccentColorOption.CustomColor(0xFF2C41C9),
    AccentColorOption.CustomColor(0xFF00BAD6),
    AccentColorOption.CustomColor(0xFF00796B),
    AccentColorOption.CustomColor(0xFF47B84F),
    AccentColorOption.CustomColor(0xFFFFBB00),
    AccentColorOption.CustomColor(0xFF512DA8),
    AccentColorOption.CustomColor(0xFF7C5445),
    AccentColorOption.CustomColor(0xFF67818E)
)

val dynamicColors = listOf(
    AccentColorOption.SystemAccent,
    AccentColorOption.WallpaperPrimary,
    AccentColorOption.WallpaperSecondary,
    AccentColorOption.WallpaperTertiary
)
    .filter(AccentColorOption::isSupported)
