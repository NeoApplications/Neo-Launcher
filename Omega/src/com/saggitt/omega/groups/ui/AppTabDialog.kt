/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
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

package com.saggitt.omega.groups.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.compose.components.DialogNegativeButton
import com.saggitt.omega.compose.components.DialogPositiveButton
import com.saggitt.omega.compose.components.MultiSelectionListItem
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.groups.category.DrawerTabs
import com.saggitt.omega.preferences.NeoPrefs
import com.saggitt.omega.preferences.PreferenceActivity
import com.saggitt.omega.util.addOrRemove
import com.saggitt.omega.util.blockShadow

@Composable
fun AppTabDialog(
    componentKey: ComponentKey,
    openDialogCustom: MutableState<Boolean>,
) {
    Dialog(
        onDismissRequest = { openDialogCustom.value = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AppTabDialogUI(
            componentKey = componentKey,
            openDialogCustom = openDialogCustom
        )
    }
}

@Composable
fun AppTabDialogUI(
    componentKey: ComponentKey,
    openDialogCustom: MutableState<Boolean>,
) {
    val context = LocalContext.current
    val prefs = NeoPrefs.getInstance()

    var radius = 16.dp
    if (prefs.profileWindowCornerRadius.getValue() > -1) {
        radius = prefs.profileWindowCornerRadius.getValue().dp
    }
    val cornerRadius by remember { mutableStateOf(radius) }

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs: List<DrawerTabs.Tab> =
                prefs.drawerTabs.getGroups()
            val selectedItems = tabs
                .map {
                    (it as? DrawerTabs.CustomTab)?.contents?.value()?.contains(componentKey)
                        ?: false
                }
                .toMutableList()

            LazyColumn(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .weight(1f, false)
                    .blockShadow()
            ) {
                itemsIndexed(tabs) { index, tab ->
                    var isSelected by rememberSaveable { mutableStateOf(selectedItems[index]) }

                    MultiSelectionListItem(
                        text = tab.title,
                        isChecked = isSelected,
                        isEnabled = tab is DrawerTabs.CustomTab,
                    ) {
                        isSelected = !isSelected
                        selectedItems[index] = isSelected
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) {
                DialogNegativeButton(
                    textId = R.string.tabs_manage,
                    cornerRadius = cornerRadius,
                    onClick = {
                        openDialogCustom.value = false
                        context.startActivity(
                            PreferenceActivity.navigateIntent(context, Routes.CATEGORIZE_APPS)
                        )
                    },
                )
                Spacer(Modifier.weight(1f))
                DialogNegativeButton(
                    modifier = Modifier.padding(start = 16.dp),
                    cornerRadius = cornerRadius,
                    onClick = {
                        openDialogCustom.value = false
                    }
                )
                DialogPositiveButton(
                    modifier = Modifier.padding(start = 16.dp),
                    cornerRadius = cornerRadius,
                    onClick = {
                        tabs
                            .forEachIndexed { index, tab ->
                                (tab as? DrawerTabs.CustomTab)?.contents?.value()
                                    ?.addOrRemove(componentKey, selectedItems[index])
                            }
                        tabs.hashCode()
                        prefs.drawerTabs.saveToJson()
                        prefs.reloadTabs()
                        openDialogCustom.value = false
                    }
                )
            }
        }
    }
}