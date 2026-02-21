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

package com.neoapps.neolauncher.compose.pages

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.components.BaseDialog
import com.neoapps.neolauncher.compose.components.ViewWithActionBar
import com.neoapps.neolauncher.compose.components.move
import com.neoapps.neolauncher.compose.components.preferences.PreferenceBuilder
import com.neoapps.neolauncher.compose.components.preferences.StringSelectionPrefDialogUI
import com.neoapps.neolauncher.groups.AppGroups
import com.neoapps.neolauncher.groups.AppGroupsManager
import com.neoapps.neolauncher.groups.category.DrawerTabs
import com.neoapps.neolauncher.groups.category.FlowerpotTabs
import com.neoapps.neolauncher.groups.ui.CreateGroupBottomSheet
import com.neoapps.neolauncher.groups.ui.EditGroupBottomSheet
import com.neoapps.neolauncher.groups.ui.GroupItem
import com.neoapps.neolauncher.groups.ui.SelectTabBottomSheet
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.preferences.StringSelectionPref
import com.neoapps.neolauncher.theme.GroupItemShape
import com.neoapps.neolauncher.util.Config
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
)
@Composable
fun AppCategoriesPage() {
    val coroutineScope = rememberCoroutineScope()
    val prefs = NeoPrefs.getInstance()
    val manager by lazy { prefs.drawerAppGroupsManager }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val hasWorkApps = Config.hasWorkApps(LocalContext.current)

    val openDialog = remember { mutableStateOf(false) }
    var dialogPref by remember { mutableStateOf<Any?>(null) }

    val selectedCategorizationKey by manager.categorizationType.getState()

    val groups = remember(selectedCategorizationKey, scaffoldState.bottomSheetState.currentValue) {
        mutableStateListOf(*loadAppGroups(manager, hasWorkApps))
    }

    val (openedOption, onOptionOpen) = remember(
        manager.getCurrentCategory(),
        selectedCategorizationKey
    ) {
        mutableStateOf(manager.getCurrentCategory())
    }

    val editGroup = remember {
        mutableStateOf(groups.firstOrNull())
    }

    var sheetChanger by rememberSaveable {
        mutableIntStateOf(Config.BS_NONE)
    }

    BackHandler(scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        coroutineScope.launch {
            scaffoldState.bottomSheetState.partialExpand()
            sheetChanger = Config.BS_NONE
        }
    }

    if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        DisposableEffect(Unit) {
            onDispose {
                sheetChanger = Config.BS_NONE
            }
        }
    }

    val lazyListState = rememberLazyListState()
    val reorderableListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        groups.move(from.index, to.index)
        saveGroupPositions(manager, groups)
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            when (sheetChanger) {
                //Select tab type
                Config.BS_SELECT_TAB_TYPE -> {
                    if (selectedCategorizationKey == AppGroupsManager.Category.TAB.key || selectedCategorizationKey == AppGroupsManager.Category.FLOWERPOT.key)
                        SelectTabBottomSheet { changer, categorizationType ->
                            onOptionOpen(categorizationType)
                            sheetChanger = changer
                            groups.clear()
                            groups.addAll(loadAppGroups(manager, hasWorkApps))
                        }
                }

                Config.BS_CREATE_GROUP    -> {
                    CreateGroupBottomSheet(category = openedOption) {
                        sheetChanger =
                            if (openedOption == AppGroupsManager.Category.TAB
                                || openedOption == AppGroupsManager.Category.FLOWERPOT
                            ) Config.BS_SELECT_TAB_TYPE
                            else Config.BS_CREATE_GROUP
                        coroutineScope.launch {
                            scaffoldState.bottomSheetState.partialExpand()
                        }
                        groups.clear()
                        groups.addAll(loadAppGroups(manager, hasWorkApps))
                    }
                }

                //Edit group
                Config.BS_EDIT_GROUP      -> {
                    editGroup.value?.let { editGroup ->
                        EditGroupBottomSheet(openedOption, editGroup) {
                            sheetChanger = it
                            coroutineScope.launch {
                                scaffoldState.bottomSheetState.partialExpand()
                            }
                            groups.clear()
                            groups.addAll(loadAppGroups(manager, hasWorkApps))
                        }
                    }
                }
            }
        }
    ) {
        ViewWithActionBar(
            modifier = Modifier.padding(it),
            title = stringResource(id = R.string.title_manage_tabs),
            floatingActionButton = {
                // TODO fix
                FloatingActionButton(
                    onClick = {
                        sheetChanger = Config.BS_SELECT_TAB_TYPE
                        coroutineScope.launch {
                            if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                scaffoldState.bottomSheetState.partialExpand()
                            } else {
                                scaffoldState.bottomSheetState.expand()
                            }
                        }
                    },
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.title_create),
                    )
                }
            },
            onBackAction = {
                saveGroupPositions(manager, groups)
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding(),
                        start = 8.dp,
                        end = 8.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PreferenceBuilder(
                    prefs.drawerSeparateWorkApps,
                    { pref: Any ->
                        dialogPref = pref
                        openDialog.value = true
                    },
                    0, 1
                )

                    Text(
                        text = stringResource(id = R.string.pref_app_groups_edit_tip),
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        state = lazyListState,
                    ) {
                        itemsIndexed(
                            groups,
                            key = { _, item -> item.customizations.toString() }
                        ) { index, item ->
                            ReorderableItem(
                                state = reorderableListState,
                                key = item.customizations.toString(),
                            ) { isDragging ->
                                val elevation by animateDpAsState(
                                    if (isDragging) 16.dp else 0.dp,
                                    label = "elevation",
                                )
                                val bgColor by animateColorAsState(
                                    if (isDragging) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainer,
                                    label = "bgColor",
                                )

                                GroupItem(
                                    title = item.title,
                                    summary = item.summary,
                                    modifier = Modifier
                                        .longPressDraggableHandle()
                                        .shadow(elevation)
                                        .clip(GroupItemShape(index, groups.size - 1)),
                                    containerColor = bgColor,
                                    removable = item.type in arrayOf(
                                        DrawerTabs.TYPE_CUSTOM,
                                        FlowerpotTabs.TYPE_FLOWERPOT
                                    ),
                                    onClick = {
                                        coroutineScope.launch {
                                            sheetChanger = Config.BS_EDIT_GROUP
                                            onOptionOpen(
                                                when (item.type) {
                                                    DrawerTabs.TYPE_CUSTOM       -> AppGroupsManager.Category.TAB
                                                    else -> AppGroupsManager.Category.FLOWERPOT
                                                }
                                            )
                                            editGroup.value = item
                                            scaffoldState.bottomSheetState.expand()
                                        }
                                    },
                                    onRemoveClick = {
                                        groups.remove(item)
                                        saveGroupPositions(manager, groups)
                                    },
                                )
                            }
                        }
                    }


            }

            if (openDialog.value) {
                BaseDialog(openDialogCustom = openDialog) {
                    when (dialogPref) {
                        is StringSelectionPref -> StringSelectionPrefDialogUI(
                            pref = dialogPref as StringSelectionPref,
                            openDialogCustom = openDialog
                        )

                        else                   -> {}
                    }
                }
            }
        }

        DisposableEffect(key1 = null) {
            onDispose {
                prefs.reloadTabs()
            }
        }
    }
}

fun saveGroupPositions(manager: AppGroupsManager, groups: List<AppGroups.Group>) {
    when (manager.categorizationType.getValue()) {
        AppGroupsManager.Category.TAB.key,
        AppGroupsManager.Category.FLOWERPOT.key,
                                             -> {
            manager.drawerTabs.setGroups(groups as List<DrawerTabs.Tab>)
            manager.drawerTabs.saveToJson()
        }
    }
}

fun loadAppGroups(manager: AppGroupsManager, hasWorkApps: Boolean): Array<AppGroups.Group> {
    return when (manager.categorizationType.getValue()) {
        AppGroupsManager.Category.TAB.key,
        AppGroupsManager.Category.FLOWERPOT.key,
                                             -> {
            if (hasWorkApps) manager.drawerTabs.getGroups()
                .filter { it !is DrawerTabs.ProfileTab || !it.profile.matchesAll }
            else manager.drawerTabs.getGroups()
                .filter { it !is DrawerTabs.ProfileTab || it.profile.matchesAll }

        }
        else                                 -> {
            emptyList()
        }
    }.toTypedArray()
}
