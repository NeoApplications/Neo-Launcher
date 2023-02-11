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
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.util.MainThreadInitializedObject
import com.android.launcher3.util.Themes
import com.saggitt.omega.OmegaApp
import com.saggitt.omega.search.getSearchProvidersMap
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.KEY_A400
import com.saggitt.omega.util.PINK
import com.saggitt.omega.util.getFeedProviders
import com.saggitt.omega.util.languageOptions
import com.saggitt.omega.widget.Temperature
import com.saggitt.omega.widget.WidgetConstants
import com.saggitt.omega.widget.weatherprovider.BlankDataProvider
import com.saggitt.omega.widget.weatherprovider.PEWeatherDataProvider
import kotlin.math.roundToInt

private const val USER_PREFERENCES_NAME = "neo_launcher"

class NLPrefs private constructor(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES_NAME)
    private val dataStore: DataStore<Preferences> = context.dataStore

    fun reloadApps() {
        val las = LauncherAppState.getInstance(context)
        val idp = las.invariantDeviceProfile
        idp.onPreferencesChanged(context)
    }

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
    // TODO drawerLayout, drawerProtectedAppSet, drawerHiddenAppSet, drawerGroupsMode, drawerGridRows?
    var drawerSortMode = IntSelectionPref(
        dataStore = dataStore,
        titleId = R.string.title__sort_mode,
        key = PrefKey.DRAWER_SORT_MODE,
        defaultValue = Config.SORT_AZ,
        entries = Config.drawerSortOptions,
    )

    var drawerPopup = StringMultiSelectionPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_POPUP_OPTIONS,
        titleId = R.string.title__drawer_icon_popup_menu,
        defaultValue = setOf(PREFS_DRAWER_POPUP_EDIT),
        entries = drawerPopupOptions,
        //withIcons = true,
    )

    // TODO Show lock screen when the app is enabled and is clicked
    var drawerEnableProtectedApps = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_PROTECTED_APPS_ENABLED,
        titleId = R.string.enable_protected_apps,
        defaultValue = false
    )

    var drawerIconScale = FloatPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_ICON_SCALE,
        titleId = R.string.title__drawer_icon_size,
        defaultValue = 1f,
        maxValue = 2f,
        minValue = 0.5f,
        steps = 150,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
    )

    val drawerLabelScale = FloatPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_LABELS_SCALE,
        titleId = R.string.title_desktop_text_size,
        defaultValue = 1f,
        maxValue = 1.8f,
        minValue = 0.3f,
        steps = 150,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
    )

    val drawerHideLabels = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_LABELS_HIDE,
        titleId = R.string.title__drawer_hide_icon_labels,
        defaultValue = false,
    )

    val drawerMultilineLabel = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_LABELS_MULTILINE,
        titleId = R.string.title__multiline_labels,
        defaultValue = false,
    )

    val drawerRowHeightScale = FloatPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_CELL_HEIGHT_SCALE,
        titleId = R.string.title_drawer_row_height,
        defaultValue = 1F,
        maxValue = 2f,
        minValue = 0.5f,
        steps = 150,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
    )

    val drawerSeparateWorkApps = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_WORK_APPS_SEPARATE,
        titleId = R.string.title_separate_work_apps,
        defaultValue = false,
    )

    val drawerSaveScrollPosition = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_SCROLL_POSITION_SAVE,
        titleId = R.string.title_all_apps_keep_scroll_state,
        defaultValue = false,
    )

    val drawerGridColumns = IntPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_GRID_COLUMNS,
        titleId = R.string.title__drawer_columns,
        defaultValue = 5,
        minValue = 2,
        maxValue = 16,
        steps = 15
    )

    val drawerCustomBackground = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_BG_CUSTOM,
        titleId = R.string.title_drawer_enable_background,
        defaultValue = false,
    )

    // TODO ColorPref?
    val drawerBackgroundColor = IntPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_BG_COLOR,
        titleId = R.string.title_dock_background_color,
        defaultValue = (0xff101010).toInt(),
        //withAlpha = false,
    )

    // TODO AlphaPref?
    val drawerBackgroundOpacity = FloatPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_BG_OPACITY,
        titleId = R.string.title_opacity,
        defaultValue = 1f,
        maxValue = 1f,
        minValue = 0f,
        steps = 10,
    )

    // Notifications & Widgets/Smartspace
    // TODO notificationDots, Smartspace
    val notificationCount = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.NOTIFICATION_DOTS_COUNT,
        titleId = R.string.title__notification_count,
        defaultValue = false,
    )

    val notificationCustomColor = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.NOTIFICATION_DOTS_CUSTOM,
        titleId = R.string.notification_custom_color,
        defaultValue = false
    )

    val smartspaceEnable = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_ENABLED,
        titleId = R.string.title_smartspace,
        defaultValue = false,
    )

    var smartspaceWidgetId = IntPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_WIDGET_ID,
        titleId = -1,
        defaultValue = -1,
    )

    var smartspaceUsePillQsb = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_PILL_STYLE,
        titleId = R.string.title_use_pill_qsb,
        defaultValue = false,
    )

    val smartspaceTime = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_TIME,
        titleId = R.string.title_smartspace_time,
        defaultValue = false,
    )

    val smartspaceDate = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_DATE,
        titleId = R.string.title_smartspace_date,
        defaultValue = true,
    )

    val smartspaceTimeLarge = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_TIME_LARGE,
        titleId = R.string.title_smartspace_time_above,
        defaultValue = false,
    )
    val smartspaceTime24H = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_TIME_24H,
        titleId = R.string.title_smartspace_time_24_h,
        defaultValue = false,
    )

    var smartspaceWeatherApiKey = StringTextPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_WEATHER_API_KEY,
        titleId = R.string.weather_api_key,
        defaultValue = context.getString(R.string.default_owm_key),
    )

    var smartspaceWeatherCity = StringTextPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_WEATHER_CITY,
        titleId = R.string.weather_city,
        defaultValue = context.getString(R.string.default_city),
    )

    val smartspaceWeatherUnit = StringSelectionPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_WEATHER_UNIT,
        titleId = R.string.title_smartspace_weather_units,
        defaultValue = Temperature.Unit.Celsius.toString(),
        entries = temperatureUnitOptions,
    )

    var smartspaceWeatherProvider = StringSelectionPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_WEATHER_PROVIDER,
        titleId = R.string.title_smartspace_widget_provider,
        defaultValue = BlankDataProvider::class.java.name, //SmartSpaceDataWidget::class.java.name,
        entries = listOfNotNull(
            BlankDataProvider::class.java.name,
            //SmartSpaceDataWidget::class.java.name,
            //OWMWeatherDataProvider::class.java.name,
            if (PEWeatherDataProvider.isAvailable(context)) PEWeatherDataProvider::class.java.name else null
        ).associateBy(
            keySelector = { it },
            valueTransform = { WidgetConstants.getDisplayName(context, it) }
        )
    )

    // TODO does order have a function? if so, customize dialog to respect it?
    var smartspaceEventProvidersNew = StringMultiSelectionPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_EVENTS_PROVIDER,
        titleId = R.string.title_smartspace_event_providers,
        defaultValue = setOf(
            //SmartSpaceDataWidget::class.java.name,
            //NotificationUnreadProvider::class.java.name,
            //NowPlayingProvider::class.java.name,
            //BatteryStatusProvider::class.java.name,
            //PersonalityProvider::class.java.name
        ),
        entries = emptyMap(), // smartspaceProviderOptions,
        //withIcons = true,
    )

    // TODO ColorPref?
    val notificationBackground = IntPref(
        dataStore = dataStore,
        key = PrefKey.NOTIFICATION_DOTS_COLOR,
        titleId = R.string.title__notification_background,
        defaultValue = PINK.getValue(KEY_A400).toInt(),
        //withAlpha = true,
    )

    val notificationCountFolder = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.NOTIFICATION_DOTS_FOLDER_ENABLED,
        titleId = R.string.title__folder_badge_count,
        defaultValue = true,
    )

    // Search & Feed
    val searchDrawerEnabled = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.SEARCH_DRAWER_ENABLED,
        titleId = R.string.title_all_apps_search,
        defaultValue = true,
    )

    val searchDockEnabled = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.SEARCH_DOCK_ENABLED,
        titleId = R.string.title_all_apps_search,
        defaultValue = false,
    )

    // TODO DimensionPref?
    var searchBarRadius = FloatPref(
        dataStore = dataStore,
        key = PrefKey.SEARCH_CORNER_RADIUS,
        titleId = R.string.title__search_bar_radius,
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

    var searchProvider = StringSelectionPref(
        dataStore = dataStore,
        key = PrefKey.SEARCH_PROVIDER,
        titleId = R.string.title_search_provider,
        defaultValue = "",
        entries = getSearchProvidersMap(context),
    )

    val searchShowMic = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.SEARCH_SHOW_MIC,
        titleId = R.string.title__search_show_assistant,
        defaultValue = false
    )

    val searchShowAssistant = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.SEARCH_SHOW_ASSISTANT,
        titleId = R.string.title__search_action_assistant,
        summaryId = R.string.summary__search_show_as_assistant_summary,
        defaultValue = false
    )

    val searchHiddenApps = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.SEARCH_HIDDEN_APPS_ENABLED,
        titleId = R.string.title_search_hidden_apps,
        summaryId = R.string.summary_search_hidden_apps,
        defaultValue = false
    )

    val searchFuzzy = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.SEARCH_FUZZY_ENABLED,
        titleId = R.string.title_fuzzy_search,
        summaryId = R.string.summary_fuzzy_search,
        defaultValue = false,
    )

    var searchGlobal = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.SEARCH_GLOBAL_ENABLED,
        titleId = R.string.title_all_apps_google_search,
        summaryId = R.string.summary_all_apps_google_search,
        defaultValue = true,
    )

    var searchContacts = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.SEARCH_CONTACTS_ENABLED,
        titleId = R.string.title_search_contacts,
        defaultValue = false,
    )

    var feedProvider = StringSelectionPref(
        dataStore = dataStore,
        key = PrefKey.FEED_PROVIDER,
        titleId = R.string.title_feed_provider,
        defaultValue = "",
        entries = context.getFeedProviders(),
    )

    // GESTURES & Dash
    // TODO GesturePref, dash_providers, dashEdit?
    var dashLineSize = IntPref(
        dataStore = dataStore,
        key = PrefKey.DASH_LINE_SIZE,
        titleId = R.string.dash_line_size,
        defaultValue = 6,
        maxValue = 6,
        minValue = 4,
        steps = 1,
    )

    //Dev options
    var developerOptionsEnabled = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.SHOW_DEV_OPTIONS,
        titleId = R.string.title__dev_show_Dev,
        defaultValue = false
    )

    companion object {
        private val INSTANCE = MainThreadInitializedObject(::NLPrefs)

        @JvmStatic
        fun getInstance(context: Context) = INSTANCE.get(context)!!

    }
}