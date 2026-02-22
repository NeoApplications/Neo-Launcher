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

package com.neoapps.neolauncher.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.android.launcher3.SessionCommitReceiver.ADD_ICON_PREFERENCE_KEY
import com.android.launcher3.states.RotationHelper.ALLOW_ROTATION_PREFERENCE_KEY

object PrefKey {
    // OnBoarding
    val ONBOARDING_DOCK_BOUNCE_SEEN = booleanPreferencesKey("onboarding_dock_bounce_seen")

    // Profile
    val PROFILE_GLOBAL_LANGUAGE = stringPreferencesKey("profile_language")
    val PROFILE_GLOBAL_THEME = intPreferencesKey("profile_launcher_theme")
    val PROFILE_ACCENT_COLOR = stringPreferencesKey("profile_accent_color")
    val PROFILE_ICON_PACK = stringPreferencesKey("profile_icon_pack")
    val PROFILE_ICON_SHAPE = stringPreferencesKey("profile_icon_shape")
    val PROFILE_ICON_COLORED_BG = booleanPreferencesKey("profile_icon_colored_background")
    val PROFILE_ICON_TRANSPARENT_BG = booleanPreferencesKey("profile_transparent_icon")
    val PROFILE_THEMED_ICONS = booleanPreferencesKey("themed_icons")
    val PROFILE_ICON_ADAPTIFY = booleanPreferencesKey("profile_icon_adaptify")
    val PROFILE_ICON_RESET_CUSTOM = stringPreferencesKey("profile_icon_reset_custom")
    val PROFILE_ICON_SHAPE_LESS = booleanPreferencesKey("profile_icon_shape_less")
    val PROFILE_WINDOW_CORNER_RADIUS = floatPreferencesKey("profile_custom_window_corner_radius")
    val PROFILE_STATUSBAR_SHADOW = booleanPreferencesKey("profile_status_bar_shadow")

    // moved from Desktop
    val PROFILE_ROTATION_ALLOW = booleanPreferencesKey(ALLOW_ROTATION_PREFERENCE_KEY)

    // Desktop
    val DESKTOP_ICON_SCALE = floatPreferencesKey("desktop_icon_scale")
    val DESKTOP_LABELS_HIDE = booleanPreferencesKey("desktop_labels_hide")
    val DESKTOP_LABELS_MULTILINE = booleanPreferencesKey("desktop_labels_multiline")
    val DESKTOP_FREE_SCROLLING = booleanPreferencesKey("desktop_free_scrolling")
    val DESKTOP_LABELS_SCALE = floatPreferencesKey("desktop_labels_scale")
    val DESKTOP_POPUP_OPTIONS = stringSetPreferencesKey("desktop_popup_options")
    val DESKTOP_GRID_COLUMNS = intPreferencesKey("desktop_grid_columns")
    val DESKTOP_GRID_ROWS = intPreferencesKey("desktop_grid_rows")
    val DESKTOP_ICON_ADD_INSTALLED = booleanPreferencesKey(ADD_ICON_PREFERENCE_KEY)
    val DESKTOP_WIDGETS_FULL_WIDTH = booleanPreferencesKey("desktop_full_width_widgets")
    val DESKTOP_WIDGETS_CORNER_RADIUS = floatPreferencesKey("desktop_widget_corner_radius")
    val DESKTOP_EMPTY_SCREENS_ALLOW = booleanPreferencesKey("desktop_allow_empty_screens")
    val DESKTOP_FOLDER_CORNER_RADIUS = floatPreferencesKey("desktop_folder_corner_radius")
    val DESKTOP_FOLDER_COLUMNS = intPreferencesKey("desktop_folder_columns")
    val DESKTOP_FOLDER_ROWS = intPreferencesKey("desktop_folder_rows")
    val DESKTOP_FOLDER_BG_CUSTOM = booleanPreferencesKey("desktop_folder_custom_background")
    val DESKTOP_FOLDER_BG_COLOR = stringPreferencesKey("desktop_folder_custom_background_color")
    val DESKTOP_FOLDER_STROKE_COLOR = stringPreferencesKey("desktop_folder_stroke_color")
    val DESKTOP_FOLDER_BG_OPACITY = floatPreferencesKey("desktop_folder_custom_background_opacity")
    val DESKTOP_FOLDER_STROKE = booleanPreferencesKey("desktop_folder_draw_stroke")
    val DESKTOP_STATUS_BAR_HIDE = booleanPreferencesKey("desktop_hide_status_bar")
    val DESKTOP_LOCK_CHANGES = booleanPreferencesKey("desktop_lock")
    val DESKTOP_TASK_BAR_ON_PHONE = booleanPreferencesKey("desktop_task_bar_on_phone")

    // Dock
    val DOCK_ENABLED = booleanPreferencesKey("dock_enabled")
    val DOCK_EXPANDABLE = booleanPreferencesKey("dock_expandable")
    val DOCK_BG_CUSTOM = booleanPreferencesKey("dock_custom_background")
    val DOCK_BG_COLOR = stringPreferencesKey("dock_custom_background_color")
    val DOCK_PAGE_INDICATOR = booleanPreferencesKey("dock_page_indicator")
    val DOCK_PAGE_INDICATOR_DOT = booleanPreferencesKey("dock_page_indicator_dot")
    val DOCK_SCALE = floatPreferencesKey("dock_scale")
    val DOCK_ICON_SCALE = floatPreferencesKey("dock_icon_scale")
    val DOCK_COLUMNS = intPreferencesKey("dock_columns")
    val DOCK_ROWS = intPreferencesKey("dock_rows")

    // Drawer
    val DRAWER_ICON_SCALE = floatPreferencesKey("drawer_icon_scale")
    val DRAWER_LABELS_HIDE = booleanPreferencesKey("drawer_labels_hide")
    val DRAWER_LABELS_MULTILINE = booleanPreferencesKey("drawer_labels_multiline")
    val DRAWER_LABELS_SCALE = floatPreferencesKey("drawer_labels_scale")
    val DRAWER_HEIGHT_MULTIPLIER = floatPreferencesKey("drawer_cell_height")
    val DRAWER_POPUP_OPTIONS = stringSetPreferencesKey("drawer_popup_options")
    val DRAWER_GRID_COLUMNS = intPreferencesKey("drawer_grid_columns")
    val DRAWER_SORT_MODE = intPreferencesKey("drawer_sort_mode")
    val DRAWER_WORK_APPS_SEPARATE = booleanPreferencesKey("drawer_separate_work_apps")
    val DRAWER_SCROLL_POSITION_SAVE = booleanPreferencesKey("drawer_save_scroll_position")
    val DRAWER_SCROLLBAR_HIDDEN = booleanPreferencesKey("drawer_scrollbar_hidden")
    val DRAWER_HIDDEN_APPS_LIST = stringSetPreferencesKey("drawer_hidden_apps")
    val DRAWER_PROTECTED_APPS_ENABLED = booleanPreferencesKey("drawer_protected_apps_enabled")
    val DRAWER_PROTECTED_APPS_LIST = stringSetPreferencesKey("drawer_protected_apps")
    val DRAWER_BG_CUSTOM = booleanPreferencesKey("drawer_custom_background")
    val DRAWER_BG_COLOR = stringPreferencesKey("drawer_custom_background_color")
    val DRAWER_BG_OPACITY = floatPreferencesKey("drawer_custom_background_opacity")
    val DRAWER_CATEGORIZATION = stringPreferencesKey("drawer_categorization")
    val DRAWER_CATEGORIZATION_ENABLED = booleanPreferencesKey("drawer_categorization_enabled")
    val DRAWER_CATEGORIZATION_TYPE = stringPreferencesKey("drawer_categorization_type")
    val DRAWER_LAYOUT = intPreferencesKey("drawer_layout")
    val DRAWER_LAYOUT_CATEGORIES = stringSetPreferencesKey("drawer_layout_categories")

    // Widgets
    val WIDGETS_SMARTSPACE_ENABLED = booleanPreferencesKey("widgets_smartspace_enable")
    val WIDGETS_SMARTSPACE_CALENDAR = stringPreferencesKey("widgets_smartspace_calendar")
    val WIDGETS_SMARTSPACE_BACKGROUND = booleanPreferencesKey("widgets_smartspace_background")
    val WIDGETS_SMARTSPACE_DATE = booleanPreferencesKey("widgets_smartspace_date")
    val WIDGETS_SMARTSPACE_TIME = booleanPreferencesKey("widgets_smartspace_time")
    val WIDGETS_SMARTSPACE_TIME_24H = booleanPreferencesKey("widgets_smartspace_24h_clock")
    val WIDGETS_SMARTSPACE_EVENTS_PROVIDER =
        stringSetPreferencesKey("widgets_smartspace_events_provider")
    val WIDGETS_SMARTSPACE_WEATHER_UNIT = stringPreferencesKey("widgets_smartspace_weather_unit")
    val WIDGETS_SMARTSPACE_WEATHER_PROVIDER =
        stringPreferencesKey("widgets_smartspace_weather_provider")
    val WIDGETS_SMARTSPACE_WEATHER_API_KEY = stringPreferencesKey("widgets_smartspace_weather_api")
    val WIDGETS_SMARTSPACE_WEATHER_CITY = stringPreferencesKey("widgets_smartspace_weather_city")

    // Notification
    val NOTIFICATION_DOTS = booleanPreferencesKey("notification_dots")
    val NOTIFICATION_DOTS_CUSTOM = booleanPreferencesKey("notification_dots_custom")
    val NOTIFICATION_DOTS_COLOR = stringPreferencesKey("notification_dots_color")
    val NOTIFICATION_DOTS_COUNT = booleanPreferencesKey("notification_dots_count")
    val NOTIFICATION_DOTS_FOLDER_ENABLED = booleanPreferencesKey("notification_dots_folder_enable")
    const val NOTIFICATION_ENABLED_LISTENERS = "enabled_notification_listeners"

    // Search
    val SEARCH_PROVIDER = longPreferencesKey("search_provider")
    val SEARCH_PROVIDERS_EDIT = stringPreferencesKey("search_providers_edit")
    val SEARCH_PROVIDERS = stringSetPreferencesKey("search_providers")
    val SEARCH_SHOW_MIC = booleanPreferencesKey("search_bar_show_mic")
    val SEARCH_SHOW_ASSISTANT = booleanPreferencesKey("search_bar_show_assistant")
    val SEARCH_GLOBAL_ENABLED = booleanPreferencesKey("search_global_enable") // is it needed?
    val SEARCH_HIDDEN_APPS_ENABLED = booleanPreferencesKey("search_hidden_apps_enable")
    val SEARCH_CONTACTS_ENABLED = booleanPreferencesKey("search_contacts_enable")
    val SEARCH_FUZZY_ENABLED = booleanPreferencesKey("search_bar_show_assistant")
    val SEARCH_DRAWER_ENABLED = booleanPreferencesKey("search_bar_drawer_enable")
    val SEARCH_DOCK_ENABLED = booleanPreferencesKey("search_bar_dock_enable")

    // Feed
    val FEED_PROVIDER = stringPreferencesKey("feed_provider")

    // Gestures
    val GESTURES_DOUBLE_TAP = stringPreferencesKey("gesture_double_tap")
    val GESTURES_LONG_TAP = stringPreferencesKey("gesture_long_tap")
    val GESTURES_HOME_PRESS = stringPreferencesKey("gesture_press_home")
    val GESTURES_BACK_PRESS = stringPreferencesKey("gesture_press_back")
    val GESTURES_SWIPE_UP = stringPreferencesKey("gesture_swipe_up")
    val GESTURES_SWIPE_DOWN = stringPreferencesKey("gesture_swipe_down")
    val GESTURES_SWIPE_UP_DOCK = stringPreferencesKey("gesture_swipe_up_on_dock")
    val GESTURES_LAUNCH_ASSISTANT = stringPreferencesKey("gesture_launch_assistant")

    // Dash
    val DASH_LINE_SIZE = intPreferencesKey("dash_line_size")
    val DASH_EDIT = stringPreferencesKey("dash_edit")
    val DASH_PROVIDERS = stringSetPreferencesKey("dash_providers")
    val DASH_TORCH_STATE = booleanPreferencesKey("dash_torch")

    //DEVELOPER
    val SHOW_DEV_OPTIONS = booleanPreferencesKey("pref_show_dev_options")
    val KILL_LAUNCHER = stringPreferencesKey("pref_kill_launcher")
    val DEBUG_MODE = booleanPreferencesKey("pref_debug_mode")
}