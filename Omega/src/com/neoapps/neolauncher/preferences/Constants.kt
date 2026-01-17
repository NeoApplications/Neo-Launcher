package com.neoapps.neolauncher.preferences

import com.android.launcher3.R
import com.neoapps.neolauncher.dash.actionprovider.AllAppsShortcut
import com.neoapps.neolauncher.dash.actionprovider.AudioPlayer
import com.neoapps.neolauncher.dash.actionprovider.ChangeWallpaper
import com.neoapps.neolauncher.dash.actionprovider.DeviceSettings
import com.neoapps.neolauncher.dash.actionprovider.EditDash
import com.neoapps.neolauncher.dash.actionprovider.LaunchAssistant
import com.neoapps.neolauncher.dash.actionprovider.ManageApps
import com.neoapps.neolauncher.dash.actionprovider.ManageVolume
import com.neoapps.neolauncher.dash.actionprovider.OmegaSettings
import com.neoapps.neolauncher.dash.actionprovider.SleepDevice
import com.neoapps.neolauncher.dash.actionprovider.Torch
import com.neoapps.neolauncher.dash.controlprovider.AutoRotation
import com.neoapps.neolauncher.dash.controlprovider.Bluetooth
import com.neoapps.neolauncher.dash.controlprovider.Location
import com.neoapps.neolauncher.dash.controlprovider.MobileData
import com.neoapps.neolauncher.dash.controlprovider.Sync
import com.neoapps.neolauncher.dash.controlprovider.Wifi
import com.neoapps.neolauncher.widget.Temperature

const val PREFS_LANGUAGE_DEFAULT_NAME = "System"
const val PREFS_LANGUAGE_DEFAULT_CODE = "en"

const val THEME_LIGHT = 0
const val THEME_DARK = 0b00001
const val THEME_USE_BLACK = 0b00010
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
const val PREFS_DESKTOP_POPUP_UNINSTALL = "desktop_popup_uninstall"

const val PREFS_DRAWER_POPUP_EDIT = "drawer_popup_edit"
const val PREFS_DRAWER_POPUP_UNINSTALL = "drawer_popup_uninstall"


const val LAYOUT_VERTICAL = 0
const val LAYOUT_HORIZONTAL = 1
const val LAYOUT_CATEGORIES = 2
const val LAYOUT_TABS = 3

val desktopPopupOptions = mutableMapOf(
    PREFS_DESKTOP_POPUP_REMOVE to R.string.remove_drop_target_label,
    PREFS_DESKTOP_POPUP_EDIT to R.string.action_preferences,
    PREFS_DESKTOP_POPUP_UNINSTALL to R.string.uninstall_drop_target_label,
)

val drawerPopupOptions = mutableMapOf(
    PREFS_DRAWER_POPUP_UNINSTALL to R.string.uninstall_drop_target_label,
    PREFS_DRAWER_POPUP_EDIT to R.string.action_preferences,
)

val drawerLayoutOptions = mutableMapOf(
    LAYOUT_VERTICAL to R.string.title_drawer_vertical,
    //LAYOUT_HORIZONTAL to R.string.title_drawer_horizontal, //TODO: Enable when implemented
    LAYOUT_CATEGORIES to R.string.title_drawer_categorized,
    LAYOUT_TABS to R.string.title_drawer_tabs
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
    PREFS_DESKTOP_POPUP_UNINSTALL to R.drawable.ic_uninstall_no_shadow,
    // Drawer Popup
    PREFS_DRAWER_POPUP_UNINSTALL to R.drawable.ic_uninstall_no_shadow,
    PREFS_DRAWER_POPUP_EDIT to R.drawable.ic_edit_no_shadow,
    // Dash Providers
    EditDash::class.java.name to R.drawable.ic_edit_dash,
    ChangeWallpaper::class.java.name to R.drawable.ic_palette,
    OmegaSettings::class.java.name to R.drawable.ic_omega_settings,
    ManageVolume::class.java.name to R.drawable.ic_volume,
    DeviceSettings::class.java.name to R.drawable.ic_setting,
    ManageApps::class.java.name to R.drawable.ic_build,
    AllAppsShortcut::class.java.name to R.drawable.ic_apps,
    SleepDevice::class.java.name to R.drawable.ic_sleep,
    LaunchAssistant::class.java.name to R.drawable.ic_assistant,
    Torch::class.java.name to R.drawable.ic_torch,
    AudioPlayer::class.java.name to R.drawable.ic_music_play,
    Wifi::class.java.name to R.drawable.ic_wifi,
    MobileData::class.java.name to R.drawable.ic_mobile_network,
    Location::class.java.name to R.drawable.ic_location,
    Bluetooth::class.java.name to R.drawable.ic_bluetooth,
    AutoRotation::class.java.name to R.drawable.ic_auto_rotation,
    Sync::class.java.name to R.drawable.ic_sync,
)