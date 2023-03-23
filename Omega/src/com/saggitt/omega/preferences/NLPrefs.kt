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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.notification.NotificationListener
import com.android.launcher3.settings.SettingsActivity
import com.android.launcher3.util.MainThreadInitializedObject
import com.android.launcher3.util.SettingsCache
import com.android.launcher3.util.Themes
import com.saggitt.omega.OmegaApp
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.gestures.BlankGestureHandler
import com.saggitt.omega.gestures.handlers.NotificationsOpenGestureHandler
import com.saggitt.omega.gestures.handlers.OpenDrawerGestureHandler
import com.saggitt.omega.gestures.handlers.OpenOverviewGestureHandler
import com.saggitt.omega.iconpack.IconPackInfo
import com.saggitt.omega.iconpack.IconPackProvider
import com.saggitt.omega.icons.IconShape
import com.saggitt.omega.search.SearchProviderController
import com.saggitt.omega.smartspace.OmegaSmartSpaceController
import com.saggitt.omega.smartspace.SmartSpaceDataWidget
import com.saggitt.omega.smartspace.eventprovider.BatteryStatusProvider
import com.saggitt.omega.smartspace.eventprovider.NotificationUnreadProvider
import com.saggitt.omega.smartspace.eventprovider.NowPlayingProvider
import com.saggitt.omega.smartspace.eventprovider.PersonalityProvider
import com.saggitt.omega.smartspace.weather.OWMWeatherDataProvider
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.firstBlocking
import com.saggitt.omega.util.getFeedProviders
import com.saggitt.omega.util.languageOptions
import com.saggitt.omega.widget.Temperature
import com.saulhdev.neolauncher.icons.CustomAdaptiveIconDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random
import com.android.launcher3.graphics.IconShape as L3IconShape

class NLPrefs private constructor(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "neo_launcher")
    private val dataStore: DataStore<Preferences> = context.dataStore
    private val legacyPrefs = LegacyPreferences(context)
    private var onChangeCallback: PreferencesChangeCallback? = null

    private val _changePoker = MutableSharedFlow<Int>()
    val changePoker = _changePoker.asSharedFlow()
    val idp: InvariantDeviceProfile get() = InvariantDeviceProfile.INSTANCE.get(context)

    private val reloadIcons = {
        idp.onPreferencesChanged(context)
    }


    private fun pokeChange() {
        CoroutineScope(Dispatchers.Default).launch {
            _changePoker.emit(Random.nextInt())
        }
    }

    private val pokeChange = { pokeChange() }
    val updateBlur = { updateBlur() }
    val recreate = { recreate() }

    fun reloadApps() {
        val las = LauncherAppState.getInstance(context)
        val idp = las.invariantDeviceProfile
        idp.onPreferencesChanged(context)
    }

    private fun updateSmartSpaceProvider() {
        onChangeCallback?.updateSmartspaceProvider()
    }


    // Profile
    // TODO themeResetCustomIcons, themeIconShape, themeIconPackGlobal, themePrimaryColor (restore or revamp?)

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

    var profileAccentColor = ColorIntPref(
        dataStore = dataStore,
        titleId = R.string.title__theme_accent_color,
        key = PrefKey.PROFILE_ACCENT_COLOR,
        defaultValue = Themes.getAttrColor(context, R.attr.colorAccent),
        navRoute = Routes.COLOR_ACCENT
    )

    var profileIconPack = StringSelectionPref(
        titleId = R.string.title_theme_icon_packs,
        dataStore = dataStore,
        key = PrefKey.PROFILE_ICON_PACK,
        defaultValue = "",
        entries = IconPackProvider.INSTANCE.get(context)
            .getIconPackList()
            .associateBy(IconPackInfo::packageName, IconPackInfo::name),
        onChange = { reloadIcons }
    )

    var profileIconShape = NavigationPref(
        titleId = R.string.title__theme_icon_shape,
        dataStore = dataStore,
        key = PrefKey.PROFILE_ICON_SHAPE,
        defaultValue = "system",
        navRoute = Routes.ICON_SHAPE
    )

    var profileThemedIcons = BooleanPref(
        titleId = R.string.title__theme_blur,
        summaryId = R.string.summary__theme_blur,
        dataStore = dataStore,
        key = PrefKey.PROFILE_THEMED_ICONS,
        defaultValue = Utilities.ATLEAST_T
    )
    var profileTransparentBgIcons = BooleanPref(
        titleId = R.string.title__theme_blur,
        summaryId = R.string.summary__theme_blur,
        dataStore = dataStore,
        key = PrefKey.PROFILE_ICON_TRANSPARENT_BG,
        defaultValue = Utilities.ATLEAST_T
    )

    var profileBlurEnable = BooleanPref(
        titleId = R.string.title__theme_blur,
        summaryId = R.string.summary__theme_blur,
        dataStore = dataStore,
        key = PrefKey.PROFILE_BLUR_ENABLED,
        defaultValue = false,
        onChange = { updateBlur }
    )

    var profileBlurRadius = FloatPref(
        key = PrefKey.PROFILE_BLUR_RADIUS,
        titleId = R.string.title__theme_blur_radius,
        dataStore = dataStore,
        defaultValue = 0.75f,
        maxValue = 1.5f,
        minValue = 0.1f,
        steps = 27,
        specialOutputs = { "${(it * 100).roundToInt()}%" }
    )

    var profileIconColoredBackground = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.PROFILE_ICON_COLORED_BG,
        titleId = R.string.title_colored_backgrounds,
        summaryId = R.string.summary_colored_backgrounds,
        defaultValue = false,
        onChange = {
            legacyPrefs.savePreference(PrefKey.PROFILE_ICON_COLORED_BG.name, it)
        }
    )

    var profileIconAdaptify = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.PROFILE_ICON_ADAPTIFY,
        titleId = R.string.title_adaptify_pack,
        defaultValue = false,
        onChange = {
            legacyPrefs.savePreference(PrefKey.PROFILE_ICON_ADAPTIFY.name, it)
        }
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

    val profileAllowRotation = BooleanPref(
        titleId = R.string.allow_rotation_title,
        summaryId = R.string.allow_rotation_desc,
        dataStore = dataStore,
        key = PrefKey.PROFILE_ROTATION_ALLOW,
        defaultValue = false,
        onChange = {
            legacyPrefs.savePreference(key = "pref_allowRotation", value = it)
        }
    )

    val profileShowTopShadow = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.PROFILE_STATUSBAR_SHADOW,
        titleId = R.string.show_top_shadow,
        defaultValue = true,
    )
    val profileResetCustomIcons = DialogPref(
        titleId = R.string.reset_custom_icons,
        dataStore = dataStore,
        key = PrefKey.PROFILE_ICON_RESET_CUSTOM
    )

    // Desktop
    // TODO desktop_rows, desktop_columns,
    val desktopIconAddInstalled = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_ICON_ADD_INSTALLED,
        titleId = R.string.auto_add_shortcuts_label,
        summaryId = R.string.auto_add_shortcuts_description,
        defaultValue = false,
        onChange = {
            legacyPrefs.savePreference(PrefKey.DESKTOP_ICON_ADD_INSTALLED.name, it)
        }
    )

    var desktopIconScaleEnforce = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_ICON_SCALE_ENFORCE,
        titleId = R.string.title__dock_icon_size_enforce,
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
        withIcons = true,
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

    val desktopFolderBackgroundColor = ColorIntPref(
        titleId = R.string.folder_background,
        dataStore = dataStore,
        key = PrefKey.DESKTOP_FOLDER_BG_COLOR,
        defaultValue = Themes.getAttrColor(context, R.attr.colorSurface),
        navRoute = Routes.COLOR_BG_DESKTOP_FOLDER,
    )

    val desktopFolderStroke = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_FOLDER_STROKE,
        titleId = R.string.folder_draw_stroke,
        defaultValue = false
    )

    val desktopFolderStrokeColor = ColorIntPref(
        titleId = R.string.folder_stroke_color,
        dataStore = dataStore,
        key = PrefKey.DESKTOP_FOLDER_STROKE_COLOR,
        defaultValue = Themes.getAttrColor(context, R.attr.colorSurface),
        navRoute = Routes.COLOR_STROKE_FOLDER,
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

    var dockIconScaleEnforce = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DOCK_ICON_SCALE_ENFORCE,
        titleId = R.string.title__dock_icon_size_enforce,
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
        defaultValue = false,
        onChange = { pokeChange }
    )

    val dockBackgroundColor = ColorIntPref(
        dataStore = dataStore,
        key = PrefKey.DOCK_BG_COLOR,
        titleId = R.string.title_dock_background_color,
        defaultValue = (0xff101010).toInt(),
        navRoute = Routes.COLOR_BG_DOCK,
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

    var drawerHiddenAppSet = StringSetPref(
        key = PrefKey.DRAWER_HIDDEN_APPS_LIST,
        titleId = R.string.title__drawer_hide_apps,
        summaryId = R.string.summary__drawer_hide_apps,
        dataStore = dataStore,
        defaultValue = setOf(),
        navRoute = Routes.HIDDEN_APPS,
        onChange = { reloadApps() }
    )

    var drawerPopup = StringMultiSelectionPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_POPUP_OPTIONS,
        titleId = R.string.title__drawer_icon_popup_menu,
        defaultValue = setOf(PREFS_DRAWER_POPUP_EDIT),
        entries = drawerPopupOptions,
        withIcons = true,
    )

    // TODO Show lock screen when the app is enabled and is clicked
    var drawerEnableProtectedApps = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_PROTECTED_APPS_ENABLED,
        titleId = R.string.enable_protected_apps,
        defaultValue = false
    )

    var drawerIconScaleEnforce = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_ICON_SCALE_ENFORCE,
        titleId = R.string.title__dock_icon_size_enforce,
        defaultValue = false,
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
        onChange = { pokeChange }
    )

    val drawerBackgroundColor = ColorIntPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_BG_COLOR,
        titleId = R.string.title_dock_background_color,
        defaultValue = (0xff101010).toInt(),
        navRoute = Routes.COLOR_BG_DRAWER,
    )

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
    val notificationDots = IntentLauncherPref(
        dataStore = dataStore,
        key = PrefKey.NOTIFICATION_DOTS_COUNT,
        titleId = R.string.notification_dots_title,
        summaryId = run {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                PrefKey.NOTIFICATION_ENABLED_LISTENERS
            )
            val myListener = ComponentName(context, NotificationListener::class.java)
            val serviceEnabled = enabledListeners != null &&
                    (enabledListeners.contains(myListener.flattenToString()) ||
                            enabledListeners.contains(myListener.flattenToShortString()))
            if (serviceEnabled) R.string.notification_dots_disabled
            else R.string.notification_dots_missing_notification_access
        },
        positiveAnswerId = R.string.title_change_settings,
        defaultValue = false,
        getter = {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                PrefKey.NOTIFICATION_ENABLED_LISTENERS
            )
            val myListener = ComponentName(context, NotificationListener::class.java)
            val serviceEnabled = enabledListeners != null &&
                    (enabledListeners.contains(myListener.flattenToString()) ||
                            enabledListeners.contains(myListener.flattenToShortString()))
            serviceEnabled && SettingsCache.INSTANCE[context]
                .getValue(SettingsCache.NOTIFICATION_BADGING_URI)
        },
        intent = {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                PrefKey.NOTIFICATION_ENABLED_LISTENERS
            )
            val myListener = ComponentName(context, NotificationListener::class.java)
            val serviceEnabled = enabledListeners != null &&
                    (enabledListeners.contains(myListener.flattenToString()) ||
                            enabledListeners.contains(myListener.flattenToShortString()))
            if (serviceEnabled) {
                val extras = Bundle()
                extras.putString(SettingsActivity.EXTRA_FRAGMENT_ARG_KEY, "notification_badging")
                Intent("android.settings.NOTIFICATION_SETTINGS")
                    .putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_ARGS, extras)
            } else {
                val cn = ComponentName(context, NotificationListener::class.java)
                val showFragmentArgs = Bundle()
                showFragmentArgs.putString(
                    SettingsActivity.EXTRA_FRAGMENT_ARG_KEY,
                    cn.flattenToString()
                )
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(SettingsActivity.EXTRA_FRAGMENT_ARG_KEY, cn.flattenToString())
                    .putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_ARGS, showFragmentArgs)
            }
        }
    )

    val notificationCount = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.NOTIFICATION_DOTS_COUNT,
        titleId = R.string.title__notification_count,
        defaultValue = false,
    )

    val notificationCustomColor = BooleanPref(
        titleId = R.string.notification_custom_color,
        dataStore = dataStore,
        key = PrefKey.NOTIFICATION_DOTS_CUSTOM,
        defaultValue = false,
        onChange = { pokeChange }
    )

    val notificationBackground = ColorIntPref(
        dataStore = dataStore,
        key = PrefKey.NOTIFICATION_DOTS_COLOR,
        titleId = R.string.title__notification_background,
        defaultValue = 0xFFF32020.toInt(),
        navRoute = Routes.COLOR_DOTS_NOTIFICATION,
    )

    val smartspaceEnable = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_ENABLED,
        titleId = R.string.title_smartspace,
        defaultValue = false
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

    val smartspaceDate = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_DATE,
        titleId = R.string.title_smartspace_date,
        defaultValue = true,
        onChange = { pokeChange }
    )

    val smartspaceTime = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_TIME,
        titleId = R.string.title_smartspace_time,
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
        entries = temperatureUnitOptions
    )

    var smartspaceWeatherProvider = StringSelectionPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_WEATHER_PROVIDER,
        titleId = R.string.title_smartspace_widget_provider,
        defaultValue = OWMWeatherDataProvider::class.java.name,
        entries = Config.smartspaceWeatherProviders(context).associateBy(
            keySelector = { it },
            valueTransform = { OmegaSmartSpaceController.getDisplayName(context, it) }
        )
    ) {
        updateSmartSpaceProvider()
        pokeChange
    }

    var smartspaceEventProviders = StringMultiSelectionPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_EVENTS_PROVIDER,
        titleId = R.string.title_smartspace_event_providers,
        defaultValue = setOf(
            SmartSpaceDataWidget::class.java.name,
            NotificationUnreadProvider::class.java.name,
            NowPlayingProvider::class.java.name,
            BatteryStatusProvider::class.java.name,
            PersonalityProvider::class.java.name
        ),
        entries = Config.smartspaceEventProviders,
        withIcons = true,
        onChange = { updateSmartSpaceProvider() }
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

    var searchProvider = StringSelectionPref(
        dataStore = dataStore,
        titleId = R.string.title_search_provider,
        key = PrefKey.SEARCH_PROVIDER,
        defaultValue = "",
        entries = SearchProviderController.getSearchProvidersMap(context),
        onChange = { SearchProviderController.getInstance(context).onSearchProviderChanged() }
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

    /*var searchProvider = StringSelectionPref(
        dataStore = dataStore,
        key = PrefKey.SEARCH_PROVIDER,
        titleId = R.string.title_search_provider,
        defaultValue = "",
        entries = getSearchProvidersMap(context),
    )*/

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
    var gestureDoubleTap = GesturePref(
        titleId = R.string.gesture_double_tap,
        dataStore = dataStore,
        key = PrefKey.GESTURES_DOUBLE_TAP,
        defaultValue = BlankGestureHandler(context, null).toString(),
        navRoute = "${Routes.GESTURE_SELECTOR}/${PrefKey.GESTURES_DOUBLE_TAP.name}"
    )

    var gestureLongPress = GesturePref(
        titleId = R.string.gesture_long_press,
        dataStore = dataStore,
        key = PrefKey.GESTURES_LONG_TAP,
        defaultValue = OpenOverviewGestureHandler(context, null).toString(),
        navRoute = "${Routes.GESTURE_SELECTOR}/${PrefKey.GESTURES_LONG_TAP.name}"
    )

    var gestureHomePress = GesturePref(
        titleId = R.string.gesture_press_home,
        dataStore = dataStore,
        key = PrefKey.GESTURES_HOME_PRESS,
        defaultValue = BlankGestureHandler(context, null).toString(),
        navRoute = "${Routes.GESTURE_SELECTOR}/${PrefKey.GESTURES_HOME_PRESS.name}"
    )

    var gestureSwipeDown = GesturePref(
        titleId = R.string.title__gesture_swipe_down,
        key = PrefKey.GESTURES_SWIPE_DOWN,
        dataStore = dataStore,
        defaultValue = NotificationsOpenGestureHandler(context, null).toString(),
        navRoute = "${Routes.GESTURE_SELECTOR}/${PrefKey.GESTURES_SWIPE_DOWN.name}"
    )

    var gestureSwipeUp = GesturePref(
        titleId = R.string.gesture_swipe_up,
        key = PrefKey.GESTURES_SWIPE_UP,
        dataStore = dataStore,
        defaultValue = NotificationsOpenGestureHandler(context, null).toString(),
        navRoute = "${Routes.GESTURE_SELECTOR}/${PrefKey.GESTURES_SWIPE_UP.name}"
    )
    var gestureDockSwipeUp = GesturePref(
        titleId = R.string.gesture_dock_swipe_up,
        key = PrefKey.GESTURES_SWIPE_UP_DOCK,
        dataStore = dataStore,
        defaultValue = OpenDrawerGestureHandler(context, null).toString(),
        navRoute = "${Routes.GESTURE_SELECTOR}/${PrefKey.GESTURES_SWIPE_UP_DOCK.name}"
    )

    var gestureBackPress = GesturePref(
        titleId = R.string.gesture_press_back,
        dataStore = dataStore,
        key = PrefKey.GESTURES_BACK_PRESS,
        defaultValue = BlankGestureHandler(context, null).toString(),
        navRoute = "${Routes.GESTURE_SELECTOR}/${PrefKey.GESTURES_BACK_PRESS.name}"
    )

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
    var restartLauncher = StringPref(
        titleId = R.string.title__restart_launcher,
        summaryId = R.string.summary__dev_restart,
        dataStore = dataStore,
        key = PrefKey.KILL_LAUNCHER,
        onClick = { Utilities.killLauncher() }
    )
    var developerOptionsEnabled = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.SHOW_DEV_OPTIONS,
        titleId = R.string.title__dev_show_Dev,
        defaultValue = false
    )

    val showDebugInfo = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DEBUG_MODE,
        titleId = R.string.title__dev_show_debug_info,
        defaultValue = false
    )

    private val scope = MainScope()

    init {
        val iconShape = IconShape.fromString(context, profileIconShape.get().firstBlocking())
        initializeIconShape(iconShape)
        profileIconShape.get()
            .drop(1)
            .distinctUntilChanged()
            .onEach { shape ->
                initializeIconShape(IconShape.fromString(context, shape))
                L3IconShape.init(context)
                LauncherAppState.getInstance(context).refreshAndReloadLauncher()
            }
            .launchIn(scope)
    }

    fun registerCallback(callback: PreferencesChangeCallback) {
        onChangeCallback = callback
    }

    fun unregisterCallback() {
        onChangeCallback = null
    }

    fun recreate() {
        onChangeCallback?.recreate()
    }

    private fun updateBlur() {
        onChangeCallback?.updateBlur()
    }

    private fun initializeIconShape(shape: IconShape) {
        CustomAdaptiveIconDrawable.sInitialized = true
        CustomAdaptiveIconDrawable.sMaskId = shape.getHashString()
        CustomAdaptiveIconDrawable.sMask = shape.getMaskPath()
    }

    companion object {
        private val INSTANCE = MainThreadInitializedObject(::NLPrefs)

        @JvmStatic
        fun getInstance(context: Context) = INSTANCE.get(context)!!

    }
}