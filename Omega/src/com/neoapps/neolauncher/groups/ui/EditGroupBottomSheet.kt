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

package com.neoapps.neolauncher.groups.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.android.launcher3.util.ComponentKey
import com.neoapps.neolauncher.compose.components.BaseDialog
import com.neoapps.neolauncher.compose.components.DialogNegativeButton
import com.neoapps.neolauncher.compose.components.DialogPositiveButton
import com.neoapps.neolauncher.compose.components.preferences.BasePreference
import com.neoapps.neolauncher.compose.pages.ColorSelectionDialog
import com.neoapps.neolauncher.flowerpot.Flowerpot
import com.neoapps.neolauncher.groups.AppGroups
import com.neoapps.neolauncher.groups.AppGroupsManager
import com.neoapps.neolauncher.groups.category.DrawerFolders
import com.neoapps.neolauncher.groups.category.DrawerTabs
import com.neoapps.neolauncher.groups.category.FlowerpotTabs.Companion.KEY_FLOWERPOT
import com.neoapps.neolauncher.groups.category.FlowerpotTabs.Companion.TYPE_FLOWERPOT
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.theme.AccentColorOption
import com.neoapps.neolauncher.util.Config
import com.neoapps.neolauncher.util.prefs

@Composable
fun EditGroupBottomSheet(
    category: AppGroupsManager.Category,
    group: AppGroups.Group,
    onClose: (Int) -> Unit,
) {
    val context = LocalContext.current
    val prefs = NeoPrefs.getInstance()
    val flowerpotManager = Flowerpot.Manager.getInstance(context)
    val config = group.customizations
    val keyboardController = LocalSoftwareKeyboardController.current
    val openDialog = remember { mutableStateOf(false) }

    var title by remember { mutableStateOf(group.title) }

    var cornerRadius = 16.dp
    if (prefs.profileWindowCornerRadius.getValue() > -1) {
        cornerRadius = prefs.profileWindowCornerRadius.getValue().dp
    }

    var isHidden by remember {
        mutableStateOf(
            (config[AppGroups.KEY_HIDE_FROM_ALL_APPS] as? AppGroups.BooleanCustomization)?.value != false
        )
    }

    val colorPicker = remember { mutableStateOf(false) }
    var selectedCategory by remember {
        mutableStateOf(
            (config[KEY_FLOWERPOT] as? AppGroups.StringCustomization)?.value
                ?: AppGroups.KEY_FLOWERPOT_DEFAULT
        )
    }
    val allAppsTab = "profile{\"matchesAll\":true}}"

    val apps: Array<ComponentKey> = if (group.type == allAppsTab) {
        prefs.drawerHiddenAppSet.getValue().map { ComponentKey.fromString(it)!! }.toTypedArray()
    } else {
        (config[AppGroups.KEY_ITEMS] as? AppGroups.ComponentsCustomization)?.value?.toTypedArray()
            ?: emptyArray()
    }

    val selectedApps = remember { mutableStateListOf(*apps) }
    var color by remember {
        mutableStateOf(
            (config[AppGroups.KEY_COLOR] as? AppGroups.StringCustomization)?.value
                ?: context.prefs.profileAccentColor.getValue()
        )
    }

    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier
                    .fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                }),
                shape = MaterialTheme.shapes.large,
                label = {
                    Text(
                        text = stringResource(id = R.string.name),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                isError = title.isEmpty()
            )
        }
        val summary = context.resources.getQuantityString(
            R.plurals.tab_apps_count,
            selectedApps.size,
            selectedApps.size
        )
        item {
            when (group.type) {
                allAppsTab -> {
                    BasePreference(
                        titleId = R.string.title__drawer_hide_apps,
                        summary = summary,
                        startWidget = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_apps),
                                contentDescription = null,
                            )
                        },
                        endWidget = {
                            Icon(
                                painter = painterResource(id = R.drawable.chevron_right),
                                contentDescription = null,
                            )
                        },
                        index = 0,
                        groupSize = 2
                    ) { openDialog.value = true }

                    if (openDialog.value) {
                        BaseDialog(openDialogCustom = openDialog) {
                            Card(
                                shape = MaterialTheme.shapes.extraLarge,
                                modifier = Modifier.padding(8.dp),
                                elevation = CardDefaults.elevatedCardElevation(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                GroupAppSelection(
                                    selectedApps = selectedApps.map { it.toString() }.toSet(),
                                ) {
                                    val componentsSet =
                                        it.mapNotNull { ck -> ComponentKey.fromString(ck) }
                                            .toMutableSet()
                                    selectedApps.clear()
                                    selectedApps.addAll(componentsSet)
                                    prefs.drawerHiddenAppSet.setValue(selectedApps.map { key -> key.toString() }
                                        .toSet())
                                    openDialog.value = false
                                }
                            }
                        }
                    }
                }

                DrawerTabs.TYPE_CUSTOM, DrawerFolders.TYPE_CUSTOM, TYPE_FLOWERPOT -> {
                    when {
                        category != AppGroupsManager.Category.FLOWERPOT -> {
                            BasePreference(
                                titleId = R.string.tab_manage_apps,
                                summary = summary,
                                startWidget = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_apps),
                                        contentDescription = null,
                                    )
                                },
                                endWidget = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.chevron_right),
                                        contentDescription = null,
                                    )
                                },
                                index = 0,
                                groupSize = 3
                            ) { openDialog.value = true }
                            Spacer(modifier = Modifier.height(4.dp))
                            BasePreference(
                                titleId = R.string.tab_hide_from_main,
                                startWidget = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.tab_hide_from_main),
                                        contentDescription = null,
                                    )
                                },
                                endWidget = {
                                    Switch(
                                        modifier = Modifier
                                            .height(24.dp),
                                        checked = isHidden,
                                        onCheckedChange = {
                                            isHidden = it
                                        }
                                    )
                                },
                                onClick = { isHidden = !isHidden },
                                index = 1,
                                groupSize = 3
                            )

                            if (openDialog.value) {
                                BaseDialog(openDialogCustom = openDialog) {
                                    Card(
                                        shape = MaterialTheme.shapes.extraLarge,
                                        modifier = Modifier.padding(8.dp),
                                        elevation = CardDefaults.elevatedCardElevation(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                                    ) {
                                        GroupAppSelection(
                                            selectedApps = selectedApps.map { it.toString() }
                                                .toSet(),
                                        ) {
                                            val componentsSet =
                                                it.mapNotNull { ck -> ComponentKey.fromString(ck) }
                                                    .toMutableSet()
                                            selectedApps.clear()
                                            selectedApps.addAll(componentsSet)
                                            (config[AppGroups.KEY_ITEMS] as? AppGroups.ComponentsCustomization)?.value =
                                                componentsSet
                                        }
                                    }
                                }
                            }
                        }

                        else                                            -> { // AppGroupsManager.Category.FLOWERPOT
                            BasePreference(
                                titleId = R.string.pref_appcategorization_flowerpot_title,
                                summary = flowerpotManager.getAllPots()
                                    .find { it.name == selectedCategory }!!.displayName,
                                startWidget = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_category),
                                        contentDescription = null,
                                    )
                                },
                                endWidget = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.chevron_right),
                                        contentDescription = null,
                                    )
                                },
                                index = 0,
                                groupSize = 2
                            ) { openDialog.value = true }

                            if (openDialog.value) {
                                BaseDialog(openDialogCustom = openDialog) {
                                    CategorySelectionDialogUI(selectedCategory = selectedCategory) {
                                        selectedCategory = it
                                        (config[KEY_FLOWERPOT] as? AppGroups.StringCustomization)?.value =
                                            it
                                        openDialog.value = false
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (group.type != DrawerFolders.TYPE_CUSTOM) {
                Spacer(modifier = Modifier.height(4.dp))
                BasePreference(
                    titleId = R.string.tab_color,
                    startWidget = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_color_donut),
                            contentDescription = "",
                            modifier = Modifier.size(24.dp),
                            tint = Color(AccentColorOption.fromString(color).accentColor)
                        )
                    },
                    index = 2,
                    groupSize = 3
                ) {
                    colorPicker.value = true
                }
                if (colorPicker.value) {
                    BaseDialog(openDialogCustom = colorPicker) {
                        Card(
                            shape = MaterialTheme.shapes.extraLarge,
                            modifier = Modifier.padding(8.dp),
                            elevation = CardDefaults.elevatedCardElevation(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            ColorSelectionDialog(
                                defaultColor = color,
                                onCancel = {
                                    colorPicker.value = false
                                },
                                onSave = {
                                    color = it
                                    colorPicker.value = false
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DialogNegativeButton(
                    cornerRadius = cornerRadius,
                    onClick = { onClose(Config.BS_SELECT_TAB_TYPE) }
                )
                DialogPositiveButton(
                    cornerRadius = cornerRadius,
                    textId = R.string.tab_bottom_sheet_save,
                    onClick = {
                        (config[AppGroups.KEY_TITLE] as? AppGroups.StringCustomization)?.value =
                            title
                        (config[AppGroups.KEY_HIDE_FROM_ALL_APPS] as? AppGroups.BooleanCustomization)?.value =
                            isHidden
                        (config[AppGroups.KEY_ITEMS] as? AppGroups.ComponentsCustomization)?.value =
                            selectedApps.toMutableSet()
                        if (category != AppGroupsManager.Category.FOLDER) {
                            (config[AppGroups.KEY_COLOR] as? AppGroups.StringCustomization)?.value =
                                color
                        }
                        group.customizations.applyFrom(config)

                        when (category) {
                            AppGroupsManager.Category.FOLDER -> {
                                prefs.drawerFolders.saveToJson()
                            }

                            AppGroupsManager.Category.TAB,
                            AppGroupsManager.Category.FLOWERPOT,
                                                             -> {
                                prefs.drawerTabs.saveToJson()
                                prefs.reloadTabs()
                            }

                            else                             -> {}
                        }
                        onClose(Config.BS_SELECT_TAB_TYPE)
                    }
                )
            }
        }
    }
}
