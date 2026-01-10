/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
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

package com.neoapps.neolauncher.compose.pages.preferences

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.neoapps.neolauncher.changeDefaultHome
import com.neoapps.neolauncher.compose.components.OverflowMenu
import com.neoapps.neolauncher.compose.components.ViewWithActionBar
import com.neoapps.neolauncher.compose.components.plus
import com.neoapps.neolauncher.compose.components.preferences.PreferenceGroup
import com.neoapps.neolauncher.compose.navigation.LocalPaneNavigator
import com.neoapps.neolauncher.compose.navigation.NavRoute
import com.neoapps.neolauncher.compose.objects.PageItem
import com.neoapps.neolauncher.compose.pages.AppCategoriesPage
import com.neoapps.neolauncher.compose.pages.ColorSelectionPage
import com.neoapps.neolauncher.compose.pages.GestureSelectorPage
import com.neoapps.neolauncher.compose.pages.HiddenAppsPage
import com.neoapps.neolauncher.compose.pages.ProtectedAppsPage
import com.neoapps.neolauncher.compose.pages.ProtectedAppsView
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.preferences.PrefKey
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainPrefsPage() {
    val context = LocalContext.current
    val paneNavigator = LocalPaneNavigator.current
    val prefs = NeoPrefs.getInstance()
    val showDev by prefs.developerOptionsEnabled.get().collectAsState(false)
    val scope = rememberCoroutineScope()

    val uiPrefs = persistentListOf(
        PageItem.PrefsProfile,
        //PageItem.PrefsDesktop,
        PageItem.PrefsDock,
        PageItem.PrefsDrawer
    )
    val featuresPrefs = persistentListOf(
        PageItem.PrefsWidgetsNotifications,
        PageItem.PrefsSearchFeed,
        //PageItem.PrefsGesturesDash
    )
    val otherPrefs: List<PageItem> = listOfNotNull(
        if (showDev) PageItem.PrefsDeveloper
        else null,
        PageItem.PrefsAbout
    )
    val gesturesMap = listOf(
        prefs.gestureDoubleTap,
        prefs.gestureLongPress,
        prefs.gestureSwipeDown,
        prefs.gestureSwipeUp,
        prefs.gestureDockSwipeUp,
        prefs.gestureHomePress,
        prefs.gestureBackPress,
        prefs.gestureLaunchAssistant,
    ).associateBy { it.key.name }
    val pageData: MutableState<NavRoute?> = remember { mutableStateOf(null) }

    fun resolveDefaultHome(): String? {
        val homeIntent: Intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
        val info: ResolveInfo? = context.packageManager
            .resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        return if (info?.activityInfo != null) {
            info.activityInfo.packageName
        } else {
            null
        }
    }

    NavigableListDetailPaneScaffold(
        navigator = paneNavigator,
        defaultBackBehavior = BackNavigationBehavior.PopLatest,
        listPane = {
            ViewWithActionBar(
                title = stringResource(R.string.settings_button_text),
                showBackButton = false,
                actions = {
                    OverflowMenu {
                        if (BuildConfig.APPLICATION_ID != resolveDefaultHome()) {
                            DropdownMenuItem(
                                onClick = {
                                    changeDefaultHome(context)
                                    hideMenu()
                                },
                                text = { Text(text = stringResource(id = R.string.change_default_home)) }
                            )
                        }
                        DropdownMenuItem(
                            onClick = {
                                Utilities.killLauncher()
                                hideMenu()
                            },
                            text = { Text(text = stringResource(id = R.string.title__restart_launcher)) }
                        )
                        DropdownMenuItem(
                            onClick = {
                                hideMenu()
                                scope.launch {
                                    paneNavigator.navigateTo(
                                        ListDetailPaneScaffoldRole.Detail,
                                        NavRoute.Dev()
                                    )
                                }
                            },
                            text = { Text(text = stringResource(id = R.string.developer_options_title)) }
                        )
                    }
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize()
                        .padding(paddingValues + PaddingValues(8.dp)),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PreferenceGroup(
                        heading = stringResource(id = R.string.pref_category__interfaces),
                        prefs = uiPrefs
                    )

                    PreferenceGroup(
                        heading = stringResource(id = R.string.pref_category__features),
                        prefs = featuresPrefs
                    )
                    PreferenceGroup(
                        heading = stringResource(id = R.string.pref_category__others),
                        prefs = otherPrefs
                    )
                }
            }
        },
        detailPane = {
            pageData.value = paneNavigator.currentDestination
                ?.takeIf { it.pane == this.paneRole }?.contentKey
                ?.let { it as? NavRoute }

            pageData.value?.let {
                AnimatedPane {
                    when (it) {
                        is NavRoute.Profile.IconShape
                            -> IconShapePage()

                        is NavRoute.Profile.AccentColor
                            -> ColorSelectionPage(PrefKey.PROFILE_ACCENT_COLOR)

                        is NavRoute.Profile
                            -> ProfilePrefsPage()

                        is NavRoute.Desktop.FolderBG
                            -> ColorSelectionPage(PrefKey.DESKTOP_FOLDER_BG_COLOR)

                        is NavRoute.Desktop.FolderStroke
                            -> ColorSelectionPage(PrefKey.DESKTOP_FOLDER_STROKE_COLOR)

                        is NavRoute.Desktop
                            -> DesktopPrefsPage()

                        is NavRoute.Dock.BG
                            -> ColorSelectionPage(PrefKey.DOCK_BG_COLOR)

                        is NavRoute.Dock
                            -> DockPrefsPage()

                        is NavRoute.Drawer.BG
                            -> ColorSelectionPage(PrefKey.DRAWER_BG_COLOR)

                        is NavRoute.Drawer.Categorize
                            -> AppCategoriesPage()

                        is NavRoute.Drawer.HiddenApps
                            -> HiddenAppsPage()

                        is NavRoute.Drawer.ProtectedApps
                            -> ProtectedAppsPage()

                        is NavRoute.Drawer.ProtectedAppsView
                            -> ProtectedAppsView()

                        is NavRoute.Drawer
                            -> DrawerPrefsPage()

                        is NavRoute.Widgets.NotificationDots
                            -> ColorSelectionPage(PrefKey.NOTIFICATION_DOTS_COLOR)

                        is NavRoute.Widgets
                            -> WidgetsPrefsPage()

                        is NavRoute.Search.SearchProviders
                            -> SearchProvidersPage()

                        is NavRoute.Search
                            -> SearchPrefsPage()

                        is NavRoute.Gestures.EditDash
                            -> EditDashPage()

                        is NavRoute.Gestures.Gesture
                            -> GestureSelectorPage(gesturesMap[it.key]!!)

                        is NavRoute.Gestures
                            -> GesturesPrefsPage()

                        is NavRoute.Backup
                            -> BackupsPrefsPage()

                        is NavRoute.Dev.Categorize
                            -> AppCategoriesPage()

                        is NavRoute.Dev
                            -> DevPrefsPage()

                        is NavRoute.About.License
                            -> LicenseScreen()

                        is NavRoute.About.Translators
                            -> TranslatorsScreen()

                        is NavRoute.About.Changelog
                            -> ChangelogScreen()
                        is NavRoute.About.Acknowledgement
                            -> AcknowledgementScreen()

                        is NavRoute.About
                            -> AboutPrefPage()

                        else -> {}
                    }
                }
            }
        }
    )
}