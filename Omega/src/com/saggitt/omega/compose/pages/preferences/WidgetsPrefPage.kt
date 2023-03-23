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

package com.saggitt.omega.compose.pages.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import com.android.launcher3.R
import com.saggitt.omega.compose.components.BaseDialog
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.components.preferences.IntSelectionPrefDialogUI
import com.saggitt.omega.compose.components.preferences.IntentLauncherDialogUI
import com.saggitt.omega.compose.components.preferences.PreferenceGroup
import com.saggitt.omega.compose.components.preferences.StringMultiSelectionPrefDialogUI
import com.saggitt.omega.compose.components.preferences.StringSelectionPrefDialogUI
import com.saggitt.omega.compose.components.preferences.StringTextPrefDialogUI
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.compose.navigation.preferenceGraph
import com.saggitt.omega.compose.pages.ColorSelectorPage
import com.saggitt.omega.preferences.IntSelectionPref
import com.saggitt.omega.preferences.IntentLauncherPref
import com.saggitt.omega.preferences.PrefKey
import com.saggitt.omega.preferences.StringMultiSelectionPref
import com.saggitt.omega.preferences.StringSelectionPref
import com.saggitt.omega.preferences.StringTextPref
import com.saggitt.omega.smartspace.weather.OWMWeatherDataProvider
import com.saggitt.omega.theme.OmegaAppTheme
import com.saggitt.omega.util.firstBlocking
import com.saggitt.omega.util.prefs

@Composable
fun WidgetsPrefsPage() {
    val context = LocalContext.current
    val prefs = context.prefs
    val openDialog = remember { mutableStateOf(false) }
    var dialogPref by remember { mutableStateOf<Any?>(null) }
    val onPrefDialog = { pref: Any ->
        dialogPref = pref
        openDialog.value = true
    }
    val smartspacePrefs = remember(prefs.changePoker.collectAsState(initial = false).value) {
        mutableStateListOf(
            *listOfNotNull(
                prefs.smartspaceEnable,
                prefs.smartspaceDate,
                prefs.smartspaceTime,
                prefs.smartspaceTimeLarge,
                prefs.smartspaceTime24H,
                prefs.smartspaceUsePillQsb,
                prefs.smartspaceWeatherProvider,
                if (prefs.smartspaceWeatherProvider.getValue() == OWMWeatherDataProvider::class.java.name) {
                    prefs.smartspaceWeatherApiKey
                    prefs.smartspaceWeatherCity
                } else null,
                prefs.smartspaceWeatherUnit,
                prefs.smartspaceEventProviders
            ).toTypedArray()
        )
    }

    val notificationsPrefs = remember(prefs.changePoker.collectAsState(initial = false).value) {
        mutableStateListOf(
            *listOfNotNull(
                prefs.notificationDots,
                prefs.notificationCustomColor,
                if (prefs.notificationCustomColor.get().firstBlocking()) {
                    prefs.notificationBackground
                } else {
                    null
                },
                prefs.notificationCount
            ).toTypedArray()
        )
    }

    OmegaAppTheme {
        ViewWithActionBar(
            title = stringResource(R.string.title__general_widgets_notifications)
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    PreferenceGroup(
                        stringResource(id = R.string.title__general_smartspace),
                        prefs = smartspacePrefs,
                        onPrefDialog = onPrefDialog
                    )
                }
                item {
                    PreferenceGroup(
                        stringResource(id = R.string.pref_category__notifications),
                        prefs = notificationsPrefs,
                        onPrefDialog = onPrefDialog
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (openDialog.value) {
                BaseDialog(openDialogCustom = openDialog) {
                    when (dialogPref) {
                        is IntentLauncherPref       -> IntentLauncherDialogUI(
                            pref = dialogPref as IntentLauncherPref,
                            openDialogCustom = openDialog
                        )

                        is IntSelectionPref         -> IntSelectionPrefDialogUI(
                            pref = dialogPref as IntSelectionPref,
                            openDialogCustom = openDialog
                        )

                        is StringSelectionPref      -> StringSelectionPrefDialogUI(
                            pref = dialogPref as StringSelectionPref,
                            openDialogCustom = openDialog
                        )

                        is StringMultiSelectionPref -> StringMultiSelectionPrefDialogUI(
                            pref = dialogPref as StringMultiSelectionPref,
                            openDialogCustom = openDialog
                        )

                        is StringTextPref           -> StringTextPrefDialogUI(
                            pref = dialogPref as StringTextPref,
                            openDialogCustom = openDialog
                        )
                    }
                }
            }
        }
    }
}

fun NavGraphBuilder.widgetsPrefsGraph(route: String) {
    preferenceGraph(route, { WidgetsPrefsPage() }) { subRoute ->
        preferenceGraph(
            route = subRoute(Routes.COLOR_DOTS_NOTIFICATION),
            { ColorSelectorPage(PrefKey.NOTIFICATION_DOTS_COLOR) })
    }
}