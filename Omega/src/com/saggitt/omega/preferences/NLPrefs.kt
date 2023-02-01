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

package com.saggitt.omega.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.android.launcher3.R
import com.android.launcher3.util.MainThreadInitializedObject
import com.android.launcher3.util.Themes
import com.saggitt.omega.OmegaApp
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.languageOptions
import kotlin.math.roundToInt

private const val USER_PREFERENCES_NAME = "neo_launcher"

class NLPrefs private constructor(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES_NAME)
    private val dataStore: DataStore<Preferences> = context.dataStore

    // Profile
    // TODO themeResetCustomIcons, themeIconShape, themeIconPackGlobal, themePrimaryColor (restore or revamp?)
    val profileAllowRotation = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.PROFILE_ROTATION_ALLOW,
        titleId = R.string.allow_rotation_title,
        summaryId = R.string.allow_rotation_desc,
        defaultValue = false,
    )

    var profileLanguage = StringSelectionPref(
        dataStore = dataStore,
        key = PrefKey.PROFILE_GLOBAL_LANGUAGE,
        titleId = R.string.title__advanced_language,
        defaultValue = "",
        entries = context.languageOptions(),
    )

    var profileTheme = IntSelectionPref(
        dataStore = dataStore,
        key = PrefKey.PROFILE_GLOBAL_THEME,
        titleId = R.string.title__general_theme,
        defaultValue = if (OmegaApp.minSDK(31)) THEME_SYSTEM else THEME_WALLPAPER,
        entries = themeItems,
    )

    var profileBlurEnable = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.PROFILE_BLUR_ENABLED,
        titleId = R.string.title__theme_blur,
        summaryId = R.string.summary__theme_blur,
        defaultValue = false,
    )

    var profileBlurRadius = FloatPref(
        dataStore = dataStore,
        key = PrefKey.PROFILE_BLUR_RADIUS,
        titleId = R.string.title__theme_blur_radius,
        defaultValue = 0.75f,
        maxValue = 1.5f,
        minValue = 0.1f,
        steps = 27,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
    )

    var profileIconColoredBackground = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.PROFILE_ICON_COLORED_BG,
        titleId = R.string.title_colored_backgrounds,
        summaryId = R.string.summary_colored_backgrounds,
        defaultValue = false,
    )

    var profileIconAdaptify = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.PROFILE_ICON_ADAPTIFY,
        titleId = R.string.title_adaptify_pack,
        defaultValue = false,
    )
    var profileIconForceShapeless = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.PROFILE_ICON_SHAPELESS,
        titleId = R.string.title_force_shapeless,
        summaryId = R.string.summary_force_shapeless,
        defaultValue = false,
    )

    var profileWindowCornerRadius = FloatPref(
        dataStore = dataStore,
        titleId = R.string.title_override_corner_radius_value,
        key = PrefKey.PROFILE_WINDOW_CORNER_RADIUS,
        defaultValue = 8f,
        maxValue = 24f,
        minValue = -1f,
        steps = 24,
        specialOutputs = {
            when {
                it < 0f -> context.getString(R.string.automatic_short)
                else    -> "${it.roundToInt()}dp"
            }
        }
    )

    val profileShowTopShadow = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.PROFILE_STATUSBAR_SHADOW,
        titleId = R.string.show_top_shadow,
        defaultValue = true,
    )

    // Desktop
    // TODO desktop_rows, desktop_columns,
    val desktopIconAddInstalled = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_ICON_ADD_INSTALLED,
        titleId = R.string.auto_add_shortcuts_label,
        summaryId = R.string.auto_add_shortcuts_description,
        defaultValue = false,
    )

    val desktopIconScale = FloatPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_ICON_SCALE,
        titleId = R.string.title__desktop_icon_size,
        defaultValue = 1f,
        maxValue = 2f,
        minValue = 0.5f,
        steps = 150,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
    )

    val desktopLock = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_LOCK_CHANGES,
        titleId = R.string.title_desktop_lock_desktop,
        defaultValue = false,
    )

    val desktopHideStatusBar = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_STATUS_BAR_HIDE,
        titleId = R.string.title_desktop_hide_statusbar,
        defaultValue = false,
    )

    var desktopAllowEmptyScreens = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_EMPTY_SCREENS_ALLOW,
        titleId = R.string.title_desktop_keep_empty,
        defaultValue = false
    )

    val desktopHideAppLabels = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_LABELS_HIDE,
        titleId = R.string.title__desktop_hide_icon_labels,
        defaultValue = false,
    )

    val desktopLabelsScale = FloatPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_LABELS_SCALE,
        titleId = R.string.title_desktop_text_size,
        defaultValue = 1f,
        maxValue = 2f,
        minValue = 0.5f,
        steps = 150,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
    )

    val desktopAllowFullWidthWidgets = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_WIDGETS_FULL_WIDTH,
        titleId = R.string.title_desktop_full_width_widgets,
        summaryId = R.string.summary_full_width_widgets,
        defaultValue = false,
    )

    // TODO DimensionPref?
    var desktopWidgetCornerRadius = FloatPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_WIDGETS_CORNER_RADIUS,
        titleId = R.string.title_desktop_widget_corner_radius,
        defaultValue = 16f,
        maxValue = 24f,
        minValue = 1f,
        steps = 22,
        specialOutputs = { "${it.roundToInt()}dp" },
    )

    var desktopPopup = StringMultiSelectionPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_POPUP_OPTIONS,
        titleId = R.string.title_desktop_icon_popup_menu,
        defaultValue = setOf(PREFS_DESKTOP_POPUP_EDIT),
        entries = desktopPopupOptions,
        //withIcons = true,
    )

    // TODO DimensionPref?
    val desktopGridColumns = IntPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_GRID_COLUMNS,
        titleId = R.string.title__drawer_columns,
        defaultValue = 5, // TODO get from profile
        minValue = 2,
        maxValue = 16,
        steps = 15,
    )
    val desktopGridRows = IntPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_GRID_ROWS,
        titleId = R.string.title__drawer_rows,
        defaultValue = 5, // TODO get from profile
        minValue = 2,
        maxValue = 16,
        steps = 15,
    )
    var desktopFolderCornerRadius = FloatPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_FOLDER_CORNER_RADIUS,
        titleId = R.string.folder_radius,
        defaultValue = -1f,
        maxValue = 24f,
        minValue = -1f,
        steps = 24,
        specialOutputs = {
            when {
                it < 0f -> context.getString(R.string.automatic_short)
                else    -> "${it.roundToInt()}dp"
            }
        },
    )

    val desktopCustomFolderBackground = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_FOLDER_BG_CUSTOM,
        titleId = R.string.folder_custom_background,
        defaultValue = false
    )

    // TODO ColorPref?
    val desktopFolderBackgroundColor = IntPref(
        titleId = R.string.folder_background,
        dataStore = dataStore,
        key = PrefKey.DESKTOP_FOLDER_BG_COLOR,
        defaultValue = Themes.getAttrColor(context, R.attr.colorSurface),
        //withAlpha = true,
    )

    val desktopFolderColumns = IntPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_FOLDER_COLUMNS,
        titleId = R.string.folder_columns,
        defaultValue = 4,
        minValue = 2,
        maxValue = 5,
        steps = 2,
    )

    val desktopFolderRows = IntPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_FOLDER_ROWS,
        titleId = R.string.folder_rows,
        defaultValue = 4,
        minValue = 2,
        maxValue = 5,
        steps = 2,
    )

    val desktopFolderOpacity = FloatPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_FOLDER_BG_OPACITY,
        titleId = R.string.folder_opacity,
        defaultValue = 1f,
        maxValue = 1f,
        minValue = 0f,
        steps = 10,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
    )

    val desktopMultilineLabel = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_LABELS_MULTILINE,
        titleId = R.string.title__multiline_labels,
        defaultValue = false,
    )

    // Dock
    var dockHide = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DOCK_ENABLED,
        titleId = R.string.title__dock_hide,
        defaultValue = false,
    )

    val dockIconScale = FloatPref(
        dataStore = dataStore,
        key = PrefKey.DOCK_ICON_SCALE,
        titleId = R.string.title__dock_icon_size,
        defaultValue = 1f,
        maxValue = 2f,
        minValue = 0.5f,
        steps = 150,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
    )

    var dockScale = FloatPref(
        dataStore = dataStore,
        key = PrefKey.DOCK_SCALE,
        titleId = R.string.title__dock_scale,
        defaultValue = 1f,
        maxValue = 1.75f,
        minValue = 0.70f,
        steps = 100,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
    )

    val dockCustomBackground = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DOCK_BG_CUSTOM,
        titleId = R.string.title_dock_fill,
        defaultValue = false
    )

    // TODO ColorPref?
    val dockBackgroundColor = IntPref(
        dataStore = dataStore,
        key = PrefKey.DOCK_BG_COLOR,
        titleId = R.string.title_dock_background_color,
        defaultValue = (0xff101010).toInt(),
        //withAlpha = false,
    )

    // TODO AlphaPref?
    var dockBackgroundOpacity = FloatPref(
        dataStore = dataStore,
        key = PrefKey.DOCK_BG_OPACITY,
        titleId = R.string.title_opacity,
        defaultValue = 0.9f,
        minValue = 0f,
        maxValue = 1f,
        steps = 100,
    )

    val dockShowPageIndicator = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DOCK_PAGE_INDICATOR,
        titleId = R.string.hotseat_show_page_indicator,
        defaultValue = true,
    )

    val dockNumIcons = IntPref(
        dataStore = dataStore,
        key = PrefKey.DOCK_COLUMNS,
        titleId = R.string.num_hotseat_icons_pref_title,
        minValue = 2,
        maxValue = 16,
        steps = 15
    )

    // Drawer
    var drawerSortMode = IntSelectionPref(
        titleId = R.string.title__sort_mode,
        dataStore = dataStore,
        key = PrefKey.DRAWER_SORT_MODE,
        defaultValue = Config.SORT_AZ,
        entries = Config.drawerSortOptions,
    )


    companion object {
        private val INSTANCE = MainThreadInitializedObject(::NLPrefs)

        @JvmStatic
        fun getInstance(context: Context) = INSTANCE.get(context)!!

    }
}