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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.Utilities.makeComponentKey
import com.android.launcher3.graphics.ThemeManager
import com.android.launcher3.graphics.ThemeManager.Companion.KEY_THEMED_ICONS
import com.android.launcher3.notification.NotificationListener
import com.android.launcher3.settings.SettingsActivity
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.SettingsCache
import com.android.launcher3.util.Themes
import com.neoapps.neolauncher.compose.navigation.NavRoute
import com.neoapps.neolauncher.compose.views.IconShapeIcon
import com.neoapps.neolauncher.dash.actionprovider.DeviceSettings
import com.neoapps.neolauncher.dash.actionprovider.EditDash
import com.neoapps.neolauncher.dash.actionprovider.LaunchAssistant
import com.neoapps.neolauncher.dash.actionprovider.ManageVolume
import com.neoapps.neolauncher.dash.controlprovider.MobileData
import com.neoapps.neolauncher.dash.controlprovider.Wifi
import com.neoapps.neolauncher.dash.dashProviderOptions
import com.neoapps.neolauncher.gestures.BlankGestureHandler
import com.neoapps.neolauncher.gestures.handlers.NotificationsOpenGestureHandler
import com.neoapps.neolauncher.gestures.handlers.OpenDashGestureHandler
import com.neoapps.neolauncher.gestures.handlers.OpenDrawerGestureHandler
import com.neoapps.neolauncher.gestures.handlers.OpenOverviewGestureHandler
import com.neoapps.neolauncher.gestures.handlers.StartGlobalSearchGestureHandler
import com.neoapps.neolauncher.groups.AppGroupsManager
import com.neoapps.neolauncher.groups.category.DrawerTabs
import com.neoapps.neolauncher.iconpack.IconPackInfo
import com.neoapps.neolauncher.iconpack.IconPackProvider
import com.neoapps.neolauncher.icons.CustomAdaptiveIconDrawable
import com.neoapps.neolauncher.icons.IconShape
import com.neoapps.neolauncher.search.SearchProviderController
import com.neoapps.neolauncher.smartspace.provider.BatteryStatusProvider
import com.neoapps.neolauncher.smartspace.provider.NowPlayingProvider
import com.neoapps.neolauncher.smartspace.weather.GoogleWeatherProvider
import com.neoapps.neolauncher.smartspace.weather.OWMWeatherProvider
import com.neoapps.neolauncher.util.Config
import com.neoapps.neolauncher.util.CustomPreferencesMigration
import com.neoapps.neolauncher.util.getFeedProviders
import com.neoapps.neolauncher.util.languageOptions
import com.neoapps.neolauncher.widget.Temperature
import kotlinx.coroutines.CoroutineName
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
import kotlinx.coroutines.plus
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.getKoin
import kotlin.math.roundToInt
import kotlin.random.Random

class NeoPrefs private constructor(val context: Context) {
    private val scope = MainScope()
    val publicScope = CoroutineScope(Dispatchers.IO) + CoroutineName("NeoPrefs")

    private val dataStore: DataStore<Preferences> by getKoin().inject()
    val legacyPrefs = LegacyPreferences(context)

    private var onChangeCallback: PreferencesChangeCallback? = null

    private val _changePoker = MutableSharedFlow<Int>()
    val changePoker = _changePoker.asSharedFlow()

    // TODO add more differentiate callbacks
    val recreate = { onChangeCallback?.recreate() }
    val restart = { onChangeCallback?.restart() }
    val reloadModel = { onChangeCallback!!.reloadModel() }
    val reloadGrid = { onChangeCallback?.reloadGrid() }
    val reloadAll = { reloadModel(); reloadGrid() }
    val reloadTabs = { onChangeCallback?.reloadTabs() }

    inline fun withChangeCallback(
        crossinline callback: (PreferencesChangeCallback) -> Unit,
    ): () -> Unit {
        return { getOnChangeCallback()?.let { callback(it) } }
    }

    var onboardingBounceSeen = BooleanPref(
        titleId = R.string.onboarding_swipe_up,
        dataStore = dataStore,
        key = PrefKey.ONBOARDING_DOCK_BOUNCE_SEEN,
        defaultValue = false,
    )

    var profileLanguage = StringSelectionPref(
        dataStore = dataStore,
        key = PrefKey.PROFILE_GLOBAL_LANGUAGE,
        titleId = R.string.title__advanced_language,
        defaultValue = "",
        entries = context.languageOptions(),
        onChange = { recreate() }
    )

    var profileTheme = IntSelectionPref(
        dataStore = dataStore,
        key = PrefKey.PROFILE_GLOBAL_THEME,
        titleId = R.string.title__general_theme,
        defaultValue = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) THEME_SYSTEM else THEME_WALLPAPER,
        entries = themeItems,
    )

    val profileAccentColor = ColorIntPref(
        dataStore = dataStore,
        titleId = R.string.title__theme_accent_color,
        key = PrefKey.PROFILE_ACCENT_COLOR,
        defaultValue = "system_accent",
        navRoute = NavRoute.Profile.AccentColor(),
    )

    var profileIconShape = NavigationPref(
        titleId = R.string.title__theme_icon_shape,
        dataStore = dataStore,
        key = PrefKey.PROFILE_ICON_SHAPE,
        defaultValue = "system",
        navRoute = NavRoute.Profile.IconShape(),
        endIcon = {
            IconShapeIcon(
                iconShape = IconShape.fromString(context, it)
            )
        },
        onChange = {
            legacyPrefs.savePreference("icon_shape_model", it)
        }
    )

    var profileIconPack = StringSelectionPref(
        titleId = R.string.title_theme_icon_packs,
        dataStore = dataStore,
        key = PrefKey.PROFILE_ICON_PACK,
        defaultValue = "",
        entries = IconPackProvider.INSTANCE.get(context)
            .getIconPackList()
            .associateBy(IconPackInfo::packageName, IconPackInfo::name),
        onChange = {
            legacyPrefs.savePreference(
                KEY_THEMED_ICONS,
                it == context.getString(R.string.icon_packs_intent_name)
            )
            reloadAll()
        }
    )

    var profileThemedIcons = BooleanPref(
        titleId = R.string.title_themed_icons,
        dataStore = dataStore,
        key = PrefKey.PROFILE_THEMED_ICONS,
        defaultValue = Utilities.ATLEAST_T,
        onChange = {
            legacyPrefs.savePreference(KEY_THEMED_ICONS, it)
        }
    )

    var profileTransparentBgIcons = BooleanPref(
        titleId = R.string.title_themed_background,
        dataStore = dataStore,
        key = PrefKey.PROFILE_ICON_TRANSPARENT_BG,
        defaultValue = false
    )

    var profileShapeLessIcon = BooleanPref(
        titleId = R.string.title_force_shapeless,
        summaryId = R.string.summary_force_shapeless,
        dataStore = dataStore,
        key = PrefKey.PROFILE_ICON_SHAPE_LESS,
        defaultValue = false,
        onChange = { reloadModel() }
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

    var profileWindowCornerRadius = FloatPref(
        dataStore = dataStore,
        titleId = R.string.title_override_corner_radius_value,
        key = PrefKey.PROFILE_WINDOW_CORNER_RADIUS,
        defaultValue = 24f,
        maxValue = 48f,
        minValue = -1f,
        steps = 50,
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

    // TODO fix this
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
    val desktopIconAddInstalled = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_ICON_ADD_INSTALLED,
        titleId = R.string.auto_add_shortcuts_label,
        summaryId = R.string.auto_add_shortcuts_description,
        defaultValue = true,
        onChange = {
            legacyPrefs.savePreference(PrefKey.DESKTOP_ICON_ADD_INSTALLED.name, it)
        }
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
        onChange = {
            reloadGrid()
        }
    )

    // TODO fix this
    val desktopLock = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_LOCK_CHANGES,
        titleId = R.string.title_desktop_lock_desktop,
        defaultValue = false,
    )

    // TODO test on device
    val desktopHideStatusBar = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_STATUS_BAR_HIDE,
        titleId = R.string.title_desktop_hide_statusbar,
        defaultValue = false,
    )

    // TODO fix this
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
        onChange = { reloadGrid() }
    )

    val desktopLabelScale = FloatPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_LABELS_SCALE,
        titleId = R.string.title_desktop_text_size,
        defaultValue = 1f,
        maxValue = 2f,
        minValue = 0.5f,
        steps = 150,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
        onChange = { reloadGrid() }
    )

    // TODO fix this
    val desktopAllowFullWidthWidgets = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_WIDGETS_FULL_WIDTH,
        titleId = R.string.title_desktop_full_width_widgets,
        summaryId = R.string.summary_full_width_widgets,
        defaultValue = false,
    )

    var desktopPopup = StringMultiSelectionPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_POPUP_OPTIONS,
        titleId = R.string.title_desktop_icon_popup_menu,
        defaultValue = setOf(PREFS_DESKTOP_POPUP_EDIT),
        entries = desktopPopupOptions,
        withIcons = true,
    )

    val desktopPopupEdit: Boolean
        get() = desktopPopup.getValue().contains(PREFS_DESKTOP_POPUP_EDIT)
    val desktopPopupUninstall: Boolean
        get() = desktopPopup.getValue().contains(PREFS_DESKTOP_POPUP_UNINSTALL)

    /*private var desktopGridSizeDelegate = ResettableLazy {
        GridSize2D(
            titleId = R.string.title__desktop_grid_size,
            numColumnsPref = desktopGridColumns,
            numRowsPref = desktopGridRows,
            columnsKey = "numColumns",
            rowsKey = "numRows",
            targetObject = LauncherAppState.getIDP(context),
            onChangeListener = { reloadGrid() }
        )
    }

    val desktopGridSize by desktopGridSizeDelegate
    val desktopGridColumns = IdpIntPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_GRID_COLUMNS,
        titleId = R.string.title__drawer_columns,
        selectDefaultValue = { numColumns },
        defaultValue = 4,
        minValue = 2f,
        maxValue = 16f,
        steps = 13,
        onChange = { reloadGrid() },
    )

    val desktopGridRows = IdpIntPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_GRID_ROWS,
        titleId = R.string.title__drawer_rows,
        selectDefaultValue = { numRows },
        defaultValue = 5,
        minValue = 2f,
        maxValue = 16f,
        steps = 13,
        onChange = { reloadGrid() },
    )*/

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
        onChange = { reloadGrid() },
    )

    val desktopCustomFolderBackground = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_FOLDER_BG_CUSTOM,
        titleId = R.string.folder_custom_background,
        defaultValue = false,
        onChange = { reloadGrid() },
    )

    val desktopFolderBackgroundColor = ColorIntPref(
        titleId = R.string.folder_background,
        dataStore = dataStore,
        key = PrefKey.DESKTOP_FOLDER_BG_COLOR,
        defaultValue = "custom|#${
            Themes.getAttrColor(
                context,
                com.android.internal.R.attr.colorSurface
            )
        }",
        navRoute = NavRoute.Desktop.FolderBG(),
        onChange = { reloadGrid() },
    )

    val desktopFolderStroke = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_FOLDER_STROKE,
        titleId = R.string.folder_draw_stroke,
        defaultValue = false,
        onChange = { reloadGrid() },
    )

    val desktopFolderStrokeColor = ColorIntPref(
        titleId = R.string.folder_stroke_color,
        dataStore = dataStore,
        key = PrefKey.DESKTOP_FOLDER_STROKE_COLOR,
        defaultValue = "custom|#${
            Themes.getAttrColor(
                context,
                com.google.android.material.R.attr.colorSurface
            )
        }",
        navRoute = NavRoute.Desktop.FolderStroke(),
        onChange = { reloadGrid() },
    )
    /*
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
        )*/

    val desktopFolderOpacity = FloatPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_FOLDER_BG_OPACITY,
        titleId = R.string.folder_opacity,
        defaultValue = 1f,
        maxValue = 1f,
        minValue = 0f,
        steps = 10,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
        onChange = { reloadGrid() },
    )

    // TODO add different settings for Folder desktop vs. open background

    val desktopMultilineLabel = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_LABELS_MULTILINE,
        titleId = R.string.title__multiline_labels,
        defaultValue = false,
    )
    val desktopLabelRows get() = if (desktopMultilineLabel.getValue()) 2 else 1

    val desktopFreeScrolling = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DESKTOP_FREE_SCROLLING,
        titleId = R.string.title_desktop_free_scrolling,
        defaultValue = false,
        onChange = { recreate() },
    )

    // Dock
    var dockHide = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DOCK_ENABLED,
        titleId = R.string.title__dock_hide,
        defaultValue = false,
        onChange = { reloadGrid() }
    )
    var dockExpandable = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DOCK_EXPANDABLE,
        titleId = R.string.title_expandable_dock,
        defaultValue = false,
        onChange = { recreate() }
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
        onChange = { reloadGrid() }
    )

    val dockCustomBackground = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DOCK_BG_CUSTOM,
        titleId = R.string.title_dock_fill,
        defaultValue = false
    ) {
        pokeChange()
    }

    val dockBackgroundColor = ColorIntPref(
        dataStore = dataStore,
        key = PrefKey.DOCK_BG_COLOR,
        titleId = R.string.title_dock_background_color,
        defaultValue = "custom|#ff101010",
        navRoute = NavRoute.Dock.BG(),
    )

    val dockShowPageIndicator = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DOCK_PAGE_INDICATOR,
        titleId = R.string.hotseat_show_page_indicator,
        defaultValue = true,
    )

    val dockDotsPageIndicator = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DOCK_PAGE_INDICATOR_DOT,
        titleId = R.string.hotseat_show_dots_page_indicator,
        defaultValue = true,
        onChange = { recreate() },
    )
    /*

    private val dockGridSizeDelegate = ResettableLazy {
        GridSize(
            titleId = R.string.title__dock_hotseat_icons,
            numColumnsPref = dockNumIcons,
            columnsKey = "numHotseatIcons",
            targetObject = LauncherAppState.getIDP(context),
            onChangeListener = { reloadGrid() }
        )
    }
    val dockGridSize by dockGridSizeDelegate

    val dockNumIcons = IdpIntPref(
        dataStore = dataStore,
        key = PrefKey.DOCK_COLUMNS,
        titleId = R.string.num_hotseat_icons_pref_title,
        selectDefaultValue = { numHotseatIcons },
        defaultValue = 4,
        minValue = 2f,
        maxValue = 16f,
        steps = 13,
        onChange = { reloadGrid() },
    )
    val dockNumRows = IntPref(
        dataStore = dataStore,
        key = PrefKey.DOCK_ROWS,
        titleId = R.string.num_hotseat_icons_pref_title,
        defaultValue = 2,
        minValue = 2,
        maxValue = 3,
        steps = 3,
        onChange = { reloadGrid() },
    )*/

    // Drawer
    var drawerSortMode = IntSelectionPref(
        dataStore = dataStore,
        titleId = R.string.title__sort_mode,
        key = PrefKey.DRAWER_SORT_MODE,
        defaultValue = Config.SORT_AZ,
        entries = Config.drawerSortOptions,
        onChange = { reloadGrid() },
    )

    var drawerHiddenAppSet = StringSetPref(
        dataStore = dataStore,
        titleId = R.string.title__drawer_hide_apps,
        key = PrefKey.DRAWER_HIDDEN_APPS_LIST,
        navRoute = NavRoute.Drawer.HiddenApps(),
        defaultValue = setOf(),
        summaryId = R.string.summary__drawer_hide_apps,
        onChange = { reloadTabs() }
    )

    var drawerProtectedAppsSet = StringSetPref(
        dataStore = dataStore,
        titleId = R.string.protected_apps,
        key = PrefKey.DRAWER_PROTECTED_APPS_LIST,
        navRoute = NavRoute.Drawer.ProtectedApps(),
        defaultValue = setOf()
    ) {
        reloadGrid()
    }

    var drawerEnableProtectedApps = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_PROTECTED_APPS_ENABLED,
        titleId = R.string.enable_protected_apps,
        defaultValue = false
    ) {
        pokeChange()
    }

    /*
    private val drawerGridSizeDelegate = ResettableLazy {
        GridSize(
            titleId = R.string.title__drawer_columns,
            numColumnsPref = drawerGridColumns,
            columnsKey = "numAllAppsColumns",
            targetObject = LauncherAppState.getIDP(context),
            onChangeListener = { reloadGrid() }
        )
    }
    val drawerGridSize by drawerGridSizeDelegate

    val drawerGridColumns = IdpIntPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_GRID_COLUMNS,
        titleId = R.string.title__drawer_columns,
        selectDefaultValue = { numAllAppsColumns },
        minValue = 2f,
        maxValue = 16f,
        steps = 13,
        onChange = { reloadGrid() }
    )
*/
    var drawerPopup = StringMultiSelectionPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_POPUP_OPTIONS,
        titleId = R.string.title__drawer_icon_popup_menu,
        defaultValue = setOf(PREFS_DRAWER_POPUP_EDIT),
        entries = drawerPopupOptions,
        withIcons = true,
    )
    val drawerPopupUninstall: Boolean
        get() = drawerPopup.getValue().contains(PREFS_DRAWER_POPUP_UNINSTALL)
    val drawerPopupEdit: Boolean
        get() = drawerPopup.getValue().contains(PREFS_DRAWER_POPUP_EDIT)

    var drawerIconScale = FloatPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_ICON_SCALE,
        titleId = R.string.title__drawer_icon_size,
        defaultValue = 1f,
        maxValue = 2f,
        minValue = 0.5f,
        steps = 150,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
        onChange = { reloadGrid() }
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
        onChange = { reloadGrid() }
    )

    val drawerHideLabels = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_LABELS_HIDE,
        titleId = R.string.title__drawer_hide_icon_labels,
        defaultValue = false,
        onChange = { reloadGrid() }
    )

    val drawerMultilineLabel = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_LABELS_MULTILINE,
        titleId = R.string.title__multiline_labels,
        defaultValue = false,
        onChange = {
            legacyPrefs.savePreference("pref_enable_two_line_toggle", it)
        }
    )

    val drawerCellHeightMultiplier = FloatPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_HEIGHT_MULTIPLIER,
        titleId = R.string.title_drawer_row_height,
        defaultValue = 1f,
        maxValue = 2f,
        minValue = 0.5f,
        steps = 150,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
        onChange = { reloadGrid() }
    )

    val drawerSeparateWorkApps = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_WORK_APPS_SEPARATE,
        titleId = R.string.title_separate_work_apps,
        defaultValue = false,
        onChange = { reloadGrid() }
    )

    val drawerAppGroupsManager by lazy { AppGroupsManager(this, dataStore) }
    val drawerGroupsType get() = drawerAppGroupsManager.getEnabledType()
    val drawerTabs get() = drawerAppGroupsManager.drawerTabs
    val drawerFolders get() = drawerAppGroupsManager.drawerFolders
    val drawerEnabledGroupsModel
        get() = drawerAppGroupsManager.getEnabledModel() as? DrawerTabs
            ?: drawerTabs

    val drawerSaveScrollPosition = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_SCROLL_POSITION_SAVE,
        titleId = R.string.title_all_apps_keep_scroll_state,
        defaultValue = false,
    )

    val drawerHideScrollbar = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_SCROLLBAR_HIDDEN,
        titleId = R.string.title_all_apps_hide_scrollbar,
        defaultValue = false,
        onChange = { reloadTabs() }
    )

    val drawerCustomBackground = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_BG_CUSTOM,
        titleId = R.string.title_drawer_enable_background,
        defaultValue = false
    ) {
        pokeChange()
    }

    val drawerBackgroundColor = ColorIntPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_BG_COLOR,
        titleId = R.string.title_dock_background_color,
        defaultValue = "custom|#ff101010",
        navRoute = NavRoute.Drawer.BG(),
    )

    val drawerBackgroundOpacity = FloatPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_BG_OPACITY,
        titleId = R.string.title_opacity,
        defaultValue = 1f,
        maxValue = 1f,
        minValue = 0f,
        steps = 100,
        specialOutputs = { "${(it * 100).roundToInt()}%" }
    )

    var drawerAppGroups = NavigationPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_CATEGORIZATION,
        titleId = R.string.title_manage_tabs,
        summaryId = R.string.summary_manage_tabs,
        navRoute = NavRoute.Drawer.Categorize(),
    )

    val drawerLayout = IntSelectionPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_LAYOUT,
        titleId = R.string.title_drawer_layout,
        defaultValue = LAYOUT_VERTICAL,
        entries = drawerLayoutOptions,
        onChange = {
            pokeChange()
            reloadGrid()
        }
    )

    var categoriesLayout = StringMultiSelectionPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_LAYOUT_CATEGORIES,
        titleId = R.string.title_drawer_layout_categories,
        defaultValue = Config.activeCategories(context).keys.toSet(),
        entries = Config.activeCategories(context),
        onChange = {
            recreate()
        }
    )

    var drawerTabManager = NavigationPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_CATEGORIZATION,
        titleId = R.string.title_manage_tabs,
        summaryId = R.string.summary_manage_tabs,
        navRoute = NavRoute.Drawer.Categorize(),
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
        onChange = { pokeChange() }
    )

    val notificationBackground = ColorIntPref(
        dataStore = dataStore,
        key = PrefKey.NOTIFICATION_DOTS_COLOR,
        titleId = R.string.title__notification_background,
        defaultValue = "custom|#FFF32020",
        navRoute = NavRoute.Widgets.NotificationDots(),
    )

    val smartspaceEnable = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_ENABLED,
        titleId = R.string.title_smartspace,
        defaultValue = false,
        onChange = { reloadModel() }
    )

    val smartspaceBackground = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_BACKGROUND,
        titleId = R.string.title_smartspace_background,
        defaultValue = false,
    )

    val smartspaceDate = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_DATE,
        titleId = R.string.title_smartspace_date,
        defaultValue = true,
        onChange = { pokeChange() }
    )

    val smartspaceCalendar = StringSelectionPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_CALENDAR,
        titleId = R.string.title_smartspace_calendar,
        defaultValue = context.getString(R.string.smartspace_calendar_gregorian),
        entries = Config.calendarOptions(context)
    )

    val smartspaceTime = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_TIME,
        titleId = R.string.title_smartspace_time,
        defaultValue = true,
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
        defaultValue = OWMWeatherProvider::class.java.name,
        entries = Config.smartspaceWeatherProviders(context).filter { it.key != "none" },
        onChange = {
            pokeChange()
            recreate()
        }
    )


    var smartspaceEventProviders = StringMultiSelectionPref(
        dataStore = dataStore,
        key = PrefKey.WIDGETS_SMARTSPACE_EVENTS_PROVIDER,
        titleId = R.string.title_smartspace_event_providers,
        defaultValue = setOf(
            GoogleWeatherProvider::class.java.name,
            BatteryStatusProvider::class.java.name,
            NowPlayingProvider::class.java.name
        ),
        entries = Config.smartspaceEventProviders,
        withIcons = true,
        onChange = { pokeChange() }
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
        onChange = { recreate() }
    )

    val searchDockEnabled = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.SEARCH_DOCK_ENABLED,
        titleId = R.string.title_all_apps_search,
        defaultValue = false,
    )

    var searchProvidersEdit = NavigationPref(
        dataStore = dataStore,
        key = PrefKey.SEARCH_PROVIDERS_EDIT,
        titleId = R.string.title_search_providers,
        summaryId = R.string.edit_search_summary,
        navRoute = NavRoute.Search.SearchProviders(),
    )

    var searchProviders = LongMultiSelectionPref(
        dataStore = dataStore,
        titleId = R.string.title_search_providers,
        key = PrefKey.SEARCH_PROVIDERS,
        defaultValue = setOf(1L),
        entries = { SearchProviderController.getSearchProvidersMap() },
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
        onChange = { reloadModel() }
    )

    // GESTURES & Dash
    var gestureDoubleTap = GesturePref(
        titleId = R.string.gesture_double_tap,
        dataStore = dataStore,
        key = PrefKey.GESTURES_DOUBLE_TAP,
        defaultValue = OpenDashGestureHandler(context, null).toString(),
    )

    var gestureLongPress = GesturePref(
        titleId = R.string.gesture_long_press,
        dataStore = dataStore,
        key = PrefKey.GESTURES_LONG_TAP,
        defaultValue = OpenOverviewGestureHandler(context, null).toString(),
    )

    var gestureHomePress = GesturePref(
        titleId = R.string.gesture_press_home,
        dataStore = dataStore,
        key = PrefKey.GESTURES_HOME_PRESS,
        defaultValue = BlankGestureHandler(context, null).toString(),
    )

    var gestureSwipeDown = GesturePref(
        titleId = R.string.title__gesture_swipe_down,
        key = PrefKey.GESTURES_SWIPE_DOWN,
        dataStore = dataStore,
        defaultValue = NotificationsOpenGestureHandler(context, null).toString(),
    )

    var gestureSwipeUp = GesturePref(
        titleId = R.string.gesture_swipe_up,
        key = PrefKey.GESTURES_SWIPE_UP,
        dataStore = dataStore,
        defaultValue = OpenDrawerGestureHandler(context, null).toString(),
    )

    var gestureDockSwipeUp = GesturePref(
        titleId = R.string.gesture_dock_swipe_up,
        key = PrefKey.GESTURES_SWIPE_UP_DOCK,
        dataStore = dataStore,
        defaultValue = StartGlobalSearchGestureHandler(context, null).toString(),
    )

    var gestureBackPress = GesturePref(
        titleId = R.string.gesture_press_back,
        dataStore = dataStore,
        key = PrefKey.GESTURES_BACK_PRESS,
        defaultValue = BlankGestureHandler(context, null).toString(),
    )

    var gestureLaunchAssistant = GesturePref(
        titleId = R.string.gesture_launch_assistant,
        dataStore = dataStore,
        key = PrefKey.GESTURES_LAUNCH_ASSISTANT,
        defaultValue = BlankGestureHandler(context, null).toString(),
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

    var dashProvidersItems = StringMultiSelectionPref(
        dataStore = dataStore,
        key = PrefKey.DASH_PROVIDERS,
        titleId = R.string.edit_dash,
        summaryId = R.string.edit_dash_summary,
        defaultValue = setOf(
            Wifi::class.java.name,
            MobileData::class.java.name,
            DeviceSettings::class.java.name,
            LaunchAssistant::class.java.name,
            ManageVolume::class.java.name,
            EditDash::class.java.name,
        ),
        entries = dashProviderOptions,
        withIcons = true,
    )

    var dashEdit = NavigationPref(
        dataStore = dataStore,
        key = PrefKey.DASH_EDIT,
        titleId = R.string.edit_dash,
        summaryId = R.string.edit_dash_summary,
        navRoute = NavRoute.Gestures.EditDash(),
    )

    var dashTorchState = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DASH_TORCH_STATE,
        titleId = R.string.dash_torch,
        defaultValue = false,
    )


    //Misc
    val customAppName =
        object : MutableMapPref<ComponentKey, String>(context, "app_name_map", { reloadModel() }) {
            override fun flattenKey(key: ComponentKey) = key.toString()
            override fun unflattenKey(key: String) = makeComponentKey(context, key)
            override fun flattenValue(value: String) = value
            override fun unflattenValue(value: String) = value
        }

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

    init {
        val iconShape = IconShape.fromString(context, profileIconShape.getValue())
        initializeIconShape(iconShape)
        profileIconShape.get()
            .drop(1)
            .distinctUntilChanged()
            .onEach { shape ->
                initializeIconShape(IconShape.fromString(context, shape))
                ThemeManager.INSTANCE.get(context)
                LauncherAppState.getInstance(context).model.reloadIfActive()
            }
            .launchIn(scope)
    }

    fun registerCallback(callback: PreferencesChangeCallback) {
        onChangeCallback = callback
    }

    fun unregisterCallback() {
        onChangeCallback = null
    }

    private fun pokeChange() {
        CoroutineScope(Dispatchers.IO).launch {
            _changePoker.emit(Random.nextInt())
        }
    }

    fun getOnChangeCallback() = onChangeCallback

    private fun initializeIconShape(shape: IconShape) {
        CustomAdaptiveIconDrawable.sInitialized = true
        CustomAdaptiveIconDrawable.sMaskId = shape.getHashString()
        CustomAdaptiveIconDrawable.sMask = shape.getMaskPath()
    }

    companion object {
        val prefsModule = module {
            single { NeoPrefs(get()) }
            single { provideDataStore(get()) }
        }

        private fun provideDataStore(context: Context): DataStore<Preferences> {
            return PreferenceDataStoreFactory.create(
                produceFile = {
                    context.preferencesDataStoreFile("neo_launcher")
                },
                migrations = listOf(CustomPreferencesMigration(context).preferencesMigration())
            )
        }

        @JvmStatic
        fun getInstance(): NeoPrefs = getKoin().get()

    }
}