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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.components.BaseDialog
import com.neoapps.neolauncher.compose.components.ViewWithActionBar
import com.neoapps.neolauncher.compose.components.preferences.IntSelectionPrefDialogUI
import com.neoapps.neolauncher.compose.components.preferences.LongSelectionPrefDialogUI
import com.neoapps.neolauncher.compose.components.preferences.PreferenceGroup
import com.neoapps.neolauncher.compose.components.preferences.StringMultiSelectionPrefDialogUI
import com.neoapps.neolauncher.compose.components.preferences.StringSelectionPrefDialogUI
import com.neoapps.neolauncher.preferences.IntSelectionPref
import com.neoapps.neolauncher.preferences.LongSelectionPref
import com.neoapps.neolauncher.preferences.StringMultiSelectionPref
import com.neoapps.neolauncher.preferences.StringSelectionPref
import com.neoapps.neolauncher.util.prefs

@Composable
fun SearchPrefsPage() {
    val context = LocalContext.current
    val prefs = context.prefs

    val openDialog = remember { mutableStateOf(false) }
    var dialogPref by remember { mutableStateOf<Any?>(null) }
    val onPrefDialog = { pref: Any ->
        dialogPref = pref
        openDialog.value = true
    }

    val searchPrefs = listOf(
        prefs.searchDrawerEnabled,
        prefs.searchFuzzy,
        prefs.searchProvidersEdit,
        prefs.searchHiddenApps
    )
    val feedPrefs = listOf(
        prefs.feedProvider,
    )

    ViewWithActionBar(
        title = stringResource(R.string.title__general_search_feed)
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
                    stringResource(id = R.string.title_search_providers),
                    prefs = searchPrefs,
                    onPrefDialog = onPrefDialog
                )
            }
            /*item {
                PreferenceGroup(
                        stringResource(id = R.string.cat_dock_search),
                        prefs = showPrefs,
                        onPrefDialog = onPrefDialog
                )
            }*/
            item {
                PreferenceGroup(
                    stringResource(id = R.string.title_feed_provider),
                    prefs = feedPrefs,
                    onPrefDialog = onPrefDialog
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (openDialog.value) {
            BaseDialog(openDialogCustom = openDialog) {
                when (dialogPref) {
                    is IntSelectionPref         -> IntSelectionPrefDialogUI(
                        pref = dialogPref as IntSelectionPref,
                        openDialogCustom = openDialog
                    )

                    is LongSelectionPref        -> LongSelectionPrefDialogUI(
                        pref = dialogPref as LongSelectionPref,
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
                }
            }
        }
    }
}
