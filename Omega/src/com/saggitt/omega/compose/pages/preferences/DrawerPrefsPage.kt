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
import androidx.compose.foundation.layout.fillMaxSize
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
import com.android.launcher3.R
import com.saggitt.omega.compose.components.BaseDialog
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.components.preferences.GridSizePrefDialogUI
import com.saggitt.omega.compose.components.preferences.IntSelectionPrefDialogUI
import com.saggitt.omega.compose.components.preferences.StringMultiSelectionPrefDialogUI
import com.saggitt.omega.compose.components.preferences.StringSelectionPrefDialogUI
import com.saggitt.omega.preferences.GridSize
import com.saggitt.omega.preferences.IntSelectionPref
import com.saggitt.omega.preferences.StringMultiSelectionPref
import com.saggitt.omega.preferences.StringSelectionPref
import com.saggitt.omega.util.prefs

@Composable
fun DrawerPrefsPage() {
    val context = LocalContext.current
    val prefs = context.prefs
    val openDialog = remember { mutableStateOf(false) }
    var dialogPref by remember { mutableStateOf<Any?>(null) }
    val onPrefDialog = { pref: Any ->
        dialogPref = pref
        openDialog.value = true
    }

    val iconPrefs = listOf(
        prefs.drawerIconScale,
        prefs.drawerHideLabels,
        prefs.drawerMultilineLabel,
        prefs.drawerLabelScale,
        prefs.drawerPopup,
    )
    val gridPrefs = listOf(
        //prefs.drawerGridSize,
        prefs.drawerSortMode,
        prefs.drawerAppGroups,
        prefs.drawerSeparateWorkApps,
        prefs.drawerCellHeightMultiplier,
        prefs.drawerSaveScrollPosition,
        prefs.drawerHideScrollbar,
    )
    val otherPrefs = remember(prefs.changePoker.collectAsState(initial = 1).value) {
        mutableStateListOf(
            *listOfNotNull(
                prefs.drawerHiddenAppSet,
                prefs.drawerEnableProtectedApps,
                prefs.drawerProtectedAppsSet,
                prefs.drawerCustomBackground,
                if (prefs.drawerCustomBackground.getValue()) {
                    prefs.drawerBackgroundColor
                } else {
                    null
                },
                prefs.drawerBackgroundOpacity
            ).toTypedArray()
        )
    }

    ViewWithActionBar(
        title = stringResource(R.string.title__general_drawer)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            /*item {
                PreferenceGroup(
                    stringResource(id = R.string.cat_drawer_icons),
                    prefs = iconPrefs,
                    onPrefDialog = onPrefDialog
                )
                PreferenceGroup(
                    stringResource(id = R.string.cat_drawer_grid),
                    prefs = gridPrefs,
                    onPrefDialog = onPrefDialog
                )
                PreferenceGroup(
                    stringResource(id = R.string.pref_category__others),
                    prefs = otherPrefs,
                    onPrefDialog = onPrefDialog
                )
                Spacer(modifier = Modifier.height(8.dp))
            }*/
        }
    }
    if (openDialog.value) {
        BaseDialog(openDialogCustom = openDialog) {
            when (dialogPref) {
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

                is GridSize                 -> GridSizePrefDialogUI(
                    pref = dialogPref as GridSize,
                    openDialogCustom = openDialog
                )
            }
        }
    }
}
