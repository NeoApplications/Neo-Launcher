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
import com.neoapps.neolauncher.compose.components.BaseDialog
import com.neoapps.neolauncher.compose.components.ViewWithActionBar
import com.neoapps.neolauncher.compose.components.preferences.GridSizePrefDialogUI
import com.neoapps.neolauncher.compose.components.preferences.IntSelectionPrefDialogUI
import com.neoapps.neolauncher.compose.components.preferences.StringMultiSelectionPrefDialogUI
import com.neoapps.neolauncher.compose.components.preferences.StringSelectionPrefDialogUI
import com.neoapps.neolauncher.preferences.GridSize
import com.neoapps.neolauncher.preferences.IntSelectionPref
import com.neoapps.neolauncher.preferences.StringMultiSelectionPref
import com.neoapps.neolauncher.preferences.StringSelectionPref
import com.neoapps.neolauncher.util.prefs

@Composable
fun DesktopPrefsPage() {
    val context = LocalContext.current
    val prefs = context.prefs
    val openDialog = remember { mutableStateOf(false) }
    var dialogPref by remember { mutableStateOf<Any?>(null) }
    val onPrefDialog = { pref: Any ->
        dialogPref = pref
        openDialog.value = true
    }

    val iconPrefs = listOf(
        prefs.desktopIconScale,
        prefs.desktopHideAppLabels,
        prefs.desktopMultilineLabel,
        prefs.desktopLabelScale,
        prefs.desktopPopup,
    )
    /*
    val gridPrefs = listOf(
        prefs.desktopGridSize,
        prefs.desktopIconAddInstalled,
        prefs.desktopAllowFullWidthWidgets,
        prefs.desktopWidgetCornerRadius
    )*/
    val folderPrefs = remember(prefs.changePoker.collectAsState(initial = 1).value) {
        mutableStateListOf(
            *listOfNotNull(
                prefs.desktopCustomFolderBackground,
                if (prefs.desktopCustomFolderBackground.getValue()) {
                    prefs.desktopFolderBackgroundColor
                } else null,
                prefs.desktopFolderCornerRadius,
                prefs.desktopFolderOpacity,
                prefs.desktopFolderStroke,
                if (prefs.desktopFolderStroke.getValue()) {
                    prefs.desktopFolderStrokeColor
                } else null,
            ).toTypedArray()
        )
    }
    val otherPrefs = listOfNotNull(
        prefs.desktopHideStatusBar,
        prefs.desktopLock
    )

    ViewWithActionBar(
        title = stringResource(R.string.title__general_desktop)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            /*
            item {
                PreferenceGroup(
                    stringResource(id = R.string.cat_drawer_icons),
                    prefs = iconPrefs,
                    onPrefDialog = onPrefDialog
                )
            }
            item {
                PreferenceGroup(
                    stringResource(id = R.string.cat_desktop_grid),
                    prefs = gridPrefs,
                    onPrefDialog = onPrefDialog
                )
            }
            item {
                PreferenceGroup(
                    stringResource(id = R.string.app_categorization_folders),
                    prefs = folderPrefs,
                    onPrefDialog = onPrefDialog
                )
            }
            item {
                PreferenceGroup(
                    stringResource(id = R.string.pref_category__others),
                    prefs = otherPrefs,
                    onPrefDialog = onPrefDialog
                )
                Spacer(modifier = Modifier.height(8.dp))
            }*/
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
}