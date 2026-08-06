/*
 * This file is part of Neo Launcher
 * Copyright (c) 2026   Neo Launcher Team
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
import com.neoapps.neolauncher.compose.components.ViewWithActionBar
import com.neoapps.neolauncher.compose.components.preferences.PreferenceGroup
import com.neoapps.neolauncher.util.prefs

@Composable
fun FolderPrefsPage() {
    val context = LocalContext.current
    val prefs = context.prefs
    val openDialog = remember { mutableStateOf(false) }
    var dialogPref by remember { mutableStateOf<Any?>(null) }
    val onPrefDialog = { pref: Any ->
        dialogPref = pref
        openDialog.value = true
    }

    val folderIconPref = remember(prefs.changePoker.collectAsState(initial = 1).value) {
        mutableStateListOf(
            prefs.desktopFolderIconShape,
            prefs.desktopFolderOpacity
        )
    }

    val folderGeneralPref = remember(prefs.changePoker.collectAsState(initial = 1).value) {
        mutableStateListOf(
            *listOfNotNull(
                //prefs.desktopFolderFullScreen,
                prefs.desktopCustomFolderBackground,
                if (prefs.desktopCustomFolderBackground.getValue()) {
                    prefs.desktopFolderBackgroundColor
                } else null,
            ).toTypedArray()
        )
    }

    val folderGridPrefs = remember(prefs.changePoker.collectAsState(initial = 1).value) {
        mutableStateListOf(
            prefs.desktopFolderColumns,
            prefs.desktopFolderRows
        )
    }

    ViewWithActionBar(
        title = stringResource(R.string.pref_title_folder)
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
                    stringResource(id = R.string.pref_title_general),
                    prefs = folderGeneralPref,
                    onPrefDialog = onPrefDialog
                )
            }

            item {
                PreferenceGroup(
                    stringResource(id = R.string.cat_drawer_icons),
                    prefs = folderIconPref,
                    onPrefDialog = onPrefDialog
                )
            }

            item {
                PreferenceGroup(
                    stringResource(id = R.string.cat_desktop_grid),
                    prefs = folderGridPrefs,
                    onPrefDialog = onPrefDialog
                )
            }
        }
    }
}