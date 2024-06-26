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

package com.saggitt.omega.compose.pages

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.launcher3.R
import com.saggitt.omega.compose.components.BaseDialog
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.components.move
import com.saggitt.omega.compose.components.preferences.PreferenceBuilder
import com.saggitt.omega.compose.components.preferences.StringSelectionPrefDialogUI
import com.saggitt.omega.groups.AppGroups
import com.saggitt.omega.groups.AppGroupsManager
import com.saggitt.omega.groups.category.DrawerFolders
import com.saggitt.omega.groups.category.DrawerTabs
import com.saggitt.omega.groups.category.FlowerpotTabs
import com.saggitt.omega.groups.ui.CreateGroupBottomSheet
import com.saggitt.omega.groups.ui.EditGroupBottomSheet
import com.saggitt.omega.groups.ui.GroupItem
import com.saggitt.omega.groups.ui.SelectTabBottomSheet
import com.saggitt.omega.preferences.NeoPrefs
import com.saggitt.omega.preferences.StringSelectionPref
import com.saggitt.omega.theme.GroupItemShape
import com.saggitt.omega.util.Config
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
)
@Composable
fun AppCategoriesPage() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = NeoPrefs.getInstance()
    val manager by lazy { prefs.drawerAppGroupsManager }
    val categoriesEnabled by manager.categorizationEnabled.collectAsState(false)

    var categoryTitle by remember { mutableStateOf("") }

    var radius = 16.dp
    if (prefs.profileWindowCornerRadius.getValue() > -1) {
        radius = prefs.profileWindowCornerRadius.getValue().dp
    }
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        //confirmValueChange = { it != SheetValue.PartiallyExpanded },
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(sheetState)
    val hasWorkApps = Config.hasWorkApps(LocalContext.current)

    val openDialog = remember { mutableStateOf(false) }
    var dialogPref by remember { mutableStateOf<Any?>(null) }

    val selectedCategorizationKey by manager.categorizationType.getState()

    val groups = remember(selectedCategorizationKey, scaffoldState.bottomSheetState) {
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

    when (selectedCategorizationKey) {
        AppGroupsManager.Category.TAB.key,
        AppGroupsManager.Category.FLOWERPOT.key,
                                             -> {
            categoryTitle = stringResource(id = R.string.app_categorization_tabs)
        }

        AppGroupsManager.Category.FOLDER.key -> {
            categoryTitle = stringResource(id = R.string.app_categorization_folders)
        }
    }

    var sheetChanger by rememberSaveable {
        mutableIntStateOf(Config.BS_NONE)
    }

    BackHandler(scaffoldState.bottomSheetState.isVisible) {
        coroutineScope.launch {
            scaffoldState.bottomSheetState.hide()
            sheetChanger = Config.BS_NONE
        }
    }

    if (scaffoldState.bottomSheetState.currentValue != SheetValue.Hidden) {
        DisposableEffect(Unit) {
            onDispose {
                sheetChanger = Config.BS_NONE
            }
        }
    }

    val lazyListState = rememberLazyListState()
    val reorderableListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        groups.move(from.index, to.index)
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetShape = RoundedCornerShape(topStart = radius, topEnd = radius),
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

                //Create tab or folder
                Config.BS_CREATE_GROUP    -> {
                    CreateGroupBottomSheet(category = openedOption) {
                        sheetChanger =
                            if (openedOption == AppGroupsManager.Category.TAB
                                || openedOption == AppGroupsManager.Category.FLOWERPOT
                            ) Config.BS_SELECT_TAB_TYPE
                            else Config.BS_CREATE_GROUP
                        coroutineScope.launch {
                            scaffoldState.bottomSheetState.hide()
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
                                scaffoldState.bottomSheetState.hide()
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
            title = stringResource(id = R.string.title_app_categorize),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        sheetChanger =
                            when (selectedCategorizationKey) {
                                AppGroupsManager.Category.FOLDER.key
                                     -> Config.BS_CREATE_GROUP

                                AppGroupsManager.Category.TAB.key,
                                AppGroupsManager.Category.FLOWERPOT.key,
                                     -> Config.BS_SELECT_TAB_TYPE

                                else -> Config.BS_NONE
                            }
                        coroutineScope.launch {
                            if (scaffoldState.bottomSheetState.isVisible) {
                                scaffoldState.bottomSheetState.hide()
                            } else {
                                scaffoldState.bottomSheetState.show()
                                scaffoldState.bottomSheetState.expand()
                            }
                        }
                    },
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary.copy(0.65f),
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.title_create),
                        tint = MaterialTheme.colorScheme.onPrimary
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
                    manager.categorizationType,
                    { pref: Any ->
                        dialogPref = pref
                        openDialog.value = true
                    },
                    0, 1
                )
                if (categoriesEnabled) {
                    Text(
                        text = categoryTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp)
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
                            key = { _, item -> item.customizations.toString() }) { index, item ->
                            ReorderableItem(
                                state = reorderableListState,
                                key = item.customizations.toString(),
                            ) { isDragging ->
                                val elevation by animateDpAsState(
                                    if (isDragging) 16.dp else 0.dp,
                                    label = "elevation",
                                )
                                val bgColor by animateColorAsState(
                                    if (isDragging) MaterialTheme.colorScheme.surfaceContainerHighest
                                    else MaterialTheme.colorScheme.surfaceContainer,
                                    label = "bgColor",
                                )

                                GroupItem(
                                    title = item.title,
                                    summary = item.summary,
                                    modifier = Modifier
                                        .longPressDraggableHandle {
                                            saveGroupPositions(manager, groups)
                                        }
                                        .shadow(elevation)
                                        .clip(GroupItemShape(index, groups.size - 1)),
                                    containerColor = bgColor,
                                    removable = item.type in arrayOf(
                                        DrawerTabs.TYPE_CUSTOM,
                                        FlowerpotTabs.TYPE_FLOWERPOT,
                                        DrawerFolders.TYPE_CUSTOM
                                    ),
                                    onClick = {
                                        coroutineScope.launch {
                                            sheetChanger = Config.BS_EDIT_GROUP
                                            onOptionOpen(
                                                when (item.type) {
                                                    DrawerTabs.TYPE_CUSTOM       -> AppGroupsManager.Category.TAB
                                                    FlowerpotTabs.TYPE_FLOWERPOT -> AppGroupsManager.Category.FLOWERPOT
                                                    else                         -> AppGroupsManager.Category.FOLDER
                                                }
                                            )
                                            editGroup.value = item
                                            scaffoldState.bottomSheetState.show()
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

        AppGroupsManager.Category.FOLDER.key -> {
            manager.drawerFolders.setGroups(groups as List<DrawerFolders.Folder>)
            manager.drawerFolders.saveToJson()
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

        AppGroupsManager.Category.FOLDER.key -> {
            manager.drawerFolders.getGroups()
        }

        else                                 -> {
            emptyList()
        }
    }.toTypedArray()
}
