package com.saggitt.omega.util

import com.saggitt.omega.theme.AccentColorOption

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
    AccentColorOption.WallpaperSecondary
)
    .filter(AccentColorOption::isSupported)

/*


val LIGHT_BLUE = mapOf(
    KEY_50 to 0xFFE1F5FE,
    KEY_100 to 0xFFB3E5FC,
    KEY_200 to 0xFF81D4FA,
    KEY_300 to 0xFF4FC3F7,
    KEY_400 to 0xFF29B6F6,
    KEY_500 to 0xFF03A9F4,
    KEY_600 to 0xFF039BE5,
    KEY_700 to 0xFF0288D1,
    KEY_800 to 0xFF0277BD,
    KEY_900 to 0xFF01579B,
    KEY_A100 to 0xFF80D8FF,
    KEY_A200 to 0xFF40C4FF,
    KEY_A400 to 0xFF00B0FF,
    KEY_A700 to 0xFF0091EA,
).withDefault { 0xFF03A9F4 }
val LIGHT_BLUES = LIGHT_BLUE.values


val TEAL = mapOf(
    KEY_50 to 0xFFE0F2F1,
    KEY_100 to 0xFFB2DFDB,
    KEY_200 to 0xFF80CBC4,
    KEY_300 to 0xFF4DB6AC,
    KEY_400 to 0xFF26A69A,
    KEY_500 to 0xFF009688,
    KEY_600 to 0xFF00897B,
    KEY_700 to 0xFF00796B,
    KEY_800 to 0xFF00695C,
    KEY_900 to 0xFF004D40,
    KEY_A100 to 0xFFA7FFEB,
    KEY_A200 to 0xFF64FFDA,
    KEY_A400 to 0xFF1DE9B6,
    KEY_A700 to 0xFF00BFA5,
).withDefault { 0xFF009688 }
val TEALS = TEAL.values

// GREEN
val GREEN = mapOf(
    KEY_50 to 0xFFE8F5E9,
    KEY_100 to 0xFFC8E6C9,
    KEY_200 to 0xFFA5D6A7,
    KEY_300 to 0xFF81C784,
    KEY_400 to 0xFF66BB6A,
    KEY_500 to 0xFF4CAF50,
    KEY_600 to 0xFF43A047,
    KEY_700 to 0xFF388E3C,
    KEY_800 to 0xFF2E7D32,
    KEY_900 to 0xFF1B5E20,
    KEY_A100 to 0xFFB9F6CA,
    KEY_A200 to 0xFF69F0AE,
    KEY_A400 to 0xFF00E676,
    KEY_A700 to 0xFF00C853,
).withDefault { 0xFF4CAF50 }
val GREENS = GREEN.values

val LIGHT_GREEN = mapOf(
    KEY_50 to 0xFFF1F8E9,
    KEY_100 to 0xFFDCEDC8,
    KEY_200 to 0xFFC5E1A5,
    KEY_300 to 0xFFAED581,
    KEY_400 to 0xFF9CCC65,
    KEY_500 to 0xFF8BC34A,
    KEY_600 to 0xFF7CB342,
    KEY_700 to 0xFF689F38,
    KEY_800 to 0xFF558B2F,
    KEY_900 to 0xFF33691E,
    KEY_A100 to 0xFFCCFF90,
    KEY_A200 to 0xFFB2FF59,
    KEY_A400 to 0xFF76FF03,
    KEY_A700 to 0xFF64DD17,
).withDefault { 0xFF8BC34A }
val LIGHT_GREENS = LIGHT_GREEN.values

val LIME = mapOf(
    KEY_50 to 0xFFF9FBE7,
    KEY_100 to 0xFFF0F4C3,
    KEY_200 to 0xFFE6EE9C,
    KEY_300 to 0xFFDCE775,
    KEY_400 to 0xFFD4E157,
    KEY_500 to 0xFFCDDC39,
    KEY_600 to 0xFFC0CA33,
    KEY_700 to 0xFFAFB42B,
    KEY_800 to 0xFF9E9D24,
    KEY_900 to 0xFF827717,
    KEY_A100 to 0xFFF4FF81,
    KEY_A200 to 0xFFEEFF41,
    KEY_A400 to 0xFFC6FF00,
    KEY_A700 to 0xFFAEEA00,
).withDefault { 0xFFCDDC39 }
val LIMES = LIME.values

val YELLOW = mapOf(
    KEY_50 to 0xFFFFFDE7,
    KEY_100 to 0xFFFFF9C4,
    KEY_200 to 0xFFFFF59D,
    KEY_300 to 0xFFFFF176,
    KEY_400 to 0xFFFFEE58,
    KEY_500 to 0xFFFFEB3B,
    KEY_600 to 0xFFFDD835,
    KEY_700 to 0xFFFBC02D,
    KEY_800 to 0xFFF9A825,
    KEY_900 to 0xFFF57F17,
    KEY_A100 to 0xFFFFFF8D,
    KEY_A200 to 0xFFFFFF00,
    KEY_A400 to 0xFFFFEA00,
    KEY_A700 to 0xFFFFD600,
).withDefault { 0xFFFFEB3B }
val YELLOWS = YELLOW.values

val AMBER = mapOf(
    KEY_50 to 0xFFFFF8E1,
    KEY_100 to 0xFFFFECB3,
    KEY_200 to 0xFFFFE082,
    KEY_300 to 0xFFFFD54F,
    KEY_400 to 0xFFFFCA28,
    KEY_500 to 0xFFFFC107,
    KEY_600 to 0xFFFFB300,
    KEY_700 to 0xFFFFA000,
    KEY_800 to 0xFFFF8F00,
    KEY_900 to 0xFFFF6F00,
    KEY_A100 to 0xFFFFE57F,
    KEY_A200 to 0xFFFFD740,
    KEY_A400 to 0xFFFFC400,
    KEY_A700 to 0xFFFFAB00,
).withDefault { 0xFFFFC107 }
val AMBERS = AMBER.values

val ORANGE = mapOf(
    KEY_50 to 0xFFFFF3E0,
    KEY_100 to 0xFFFFE0B2,
    KEY_200 to 0xFFFFCC80,
    KEY_300 to 0xFFFFB74D,
    KEY_400 to 0xFFFFA726,
    KEY_500 to 0xFFFF9800,
    KEY_600 to 0xFFFB8C00,
    KEY_700 to 0xFFF57C00,
    KEY_800 to 0xFFEF6C00,
    KEY_900 to 0xFFE65100,
    KEY_A100 to 0xFFFFD180,
    KEY_A200 to 0xFFFFAB40,
    KEY_A400 to 0xFFFF9100,
    KEY_A700 to 0xFFFF6D00,
).withDefault { 0xFFFF9800 }
val ORANGES = ORANGE.values

val DEEP_ORANGE = mapOf(
    KEY_50 to 0xFFFBE9E7,
    KEY_100 to 0xFFFFCCBC,
    KEY_200 to 0xFFFFAB91,
    KEY_300 to 0xFFFF8A65,
    KEY_400 to 0xFFFF7043,
    KEY_500 to 0xFFFF5722,
    KEY_600 to 0xFFF4511E,
    KEY_700 to 0xFFE64A19,
    KEY_800 to 0xFFD84315,
    KEY_900 to 0xFFBF360C,
    KEY_A100 to 0xFFFF9E80,
    KEY_A200 to 0xFFFF6E40,
    KEY_A400 to 0xFFFF3D00,
    KEY_A700 to 0xFFDD2C00,
).withDefault { 0xFFFF5722 }
val DEEP_ORANGES = DEEP_ORANGE.values

val BROWN = mapOf(
    KEY_50 to 0xFFEFEBE9,
    KEY_100 to 0xFFD7CCC8,
    KEY_200 to 0xFFBCAAA4,
    KEY_300 to 0xFFA1887F,
    KEY_400 to 0xFF8D6E63,
    KEY_500 to 0xFF795548,
    KEY_600 to 0xFF6D4C41,
    KEY_700 to 0xFF5D4037,
    KEY_800 to 0xFF4E342E,
    KEY_900 to 0xFF3E2723,
).withDefault { 0xFF795548 }
val BROWNS = BROWN.values

val GREY = mapOf(
    KEY_50 to 0xFFFAFAFA,
    KEY_100 to 0xFFF5F5F5,
    KEY_200 to 0xFFEEEEEE,
    KEY_300 to 0xFFE0E0E0,
    KEY_400 to 0xFFBDBDBD,
    KEY_500 to 0xFF9E9E9E,
    KEY_600 to 0xFF757575,
    KEY_700 to 0xFF616161,
    KEY_800 to 0xFF424242,
    KEY_900 to 0xFF212121,
).withDefault { 0xFF9E9E9E }
val GREYS = GREY.values

val BLUE_GREY = mapOf(
    KEY_50 to 0xFFECEFF1,
    KEY_100 to 0xFFCFD8DC,
    KEY_200 to 0xFFB0BEC5,
    KEY_300 to 0xFF90A4AE,
    KEY_400 to 0xFF78909C,
    KEY_500 to 0xFF607D8B,
    KEY_600 to 0xFF546E7A,
    KEY_700 to 0xFF455A64,
    KEY_800 to 0xFF37474F,
    KEY_900 to 0xFF263238,
).withDefault { 0xFF607D8B }
val BLUE_GREYS = BLUE_GREY.values

val MATERIAL_COLOR_MAPS = listOf(
    RED, PINK, PURPLE, DEEP_PURPLE, INDIGO, BLUE, LIGHT_BLUE, CYAN, TEAL,
    GREEN, LIGHT_GREEN, LIME, YELLOW, AMBER, ORANGE, DEEP_ORANGE,
    BROWN, GREY, BLUE_GREY
)
val ALL_MATERIAL_COLORS =
    REDS + PINKS + PURPLES + DEEP_PURPLES + INDIGOS + BLUES + LIGHT_BLUES + CYANS + TEALS +
            GREENS + LIGHT_GREENS + LIMES + YELLOWS + AMBERS + ORANGES + DEEP_ORANGES +
            BROWNS + GREYS + BLUE_GREYS*/