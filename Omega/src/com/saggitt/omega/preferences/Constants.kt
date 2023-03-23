package com.saggitt.omega.preferences

import com.android.launcher3.R
import com.saggitt.omega.widget.Temperature

const val PREFS_LANGUAGE_DEFAULT_NAME = "System"
const val PREFS_LANGUAGE_DEFAULT_CODE = "en"

// TODO revamp theming management
const val THEME_USE_BLACK = 0b00010
const val THEME_LIGHT = 0
const val THEME_DARK = 0b00001
const val THEME_BLACK = THEME_DARK or THEME_USE_BLACK
const val THEME_WALLPAPER = 0b00100
const val THEME_WALLPAPER_BLACK = THEME_WALLPAPER or THEME_USE_BLACK
const val THEME_SYSTEM = 0b01000
const val THEME_SYSTEM_BLACK = THEME_SYSTEM or THEME_USE_BLACK

val themeItems = mutableMapOf(
    THEME_LIGHT to R.string.theme_light,
    THEME_DARK to R.string.theme_dark,
    THEME_BLACK to R.string.theme_black,
    THEME_SYSTEM to R.string.theme_auto_night_mode,
    THEME_SYSTEM_BLACK to R.string.theme_auto_night_mode_black,
    THEME_WALLPAPER to R.string.theme_dark_theme_mode_follow_wallpaper,
    THEME_WALLPAPER_BLACK to R.string.theme_dark_theme_mode_follow_wallpaper_black,
)

const val PREFS_DESKTOP_POPUP_EDIT = "desktop_popup_edit"
const val PREFS_DESKTOP_POPUP_REMOVE = "desktop_popup_remove"

const val PREFS_DRAWER_POPUP_EDIT = "drawer_popup_edit"
const val PREFS_DRAWER_POPUP_UNINSTALL = "drawer_popup_uninstall"

val desktopPopupOptions = mutableMapOf(
    PREFS_DESKTOP_POPUP_REMOVE to R.string.remove_drop_target_label,
    PREFS_DESKTOP_POPUP_EDIT to R.string.action_preferences,
)

val drawerPopupOptions = mutableMapOf(
    PREFS_DRAWER_POPUP_UNINSTALL to R.string.uninstall_drop_target_label,
    PREFS_DRAWER_POPUP_EDIT to R.string.action_preferences,
)

val temperatureUnitOptions = listOfNotNull(
    Temperature.Unit.Celsius,
    Temperature.Unit.Fahrenheit,
    Temperature.Unit.Kelvin,
    Temperature.Unit.Rakine,
    Temperature.Unit.Delisle,
    Temperature.Unit.Newton,
    Temperature.Unit.Reaumur,
    Temperature.Unit.Romer
).associateBy(
    keySelector = { it.toString() },
    valueTransform = { "${it.name} (${it.suffix})" }
)

val iconIds = mapOf(
    // Desktop Popup
    PREFS_DESKTOP_POPUP_REMOVE to R.drawable.ic_remove_no_shadow,
    PREFS_DESKTOP_POPUP_EDIT to R.drawable.ic_edit_no_shadow,
    // Drawer Popup
    PREFS_DRAWER_POPUP_UNINSTALL to R.drawable.ic_uninstall_no_shadow,
    PREFS_DRAWER_POPUP_EDIT to R.drawable.ic_edit_no_shadow,
)