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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.saggitt.omega.compose.components.ComposeSwitchView
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.components.move
import com.saggitt.omega.groups.AppGroups
import com.saggitt.omega.groups.AppGroupsManager
import com.saggitt.omega.groups.category.DrawerFolders
import com.saggitt.omega.groups.category.DrawerTabs
import com.saggitt.omega.groups.category.FlowerpotTabs
import com.saggitt.omega.groups.ui.CategorizationOption
import com.saggitt.omega.groups.ui.CreateGroupBottomSheet
import com.saggitt.omega.groups.ui.EditGroupBottomSheet
import com.saggitt.omega.groups.ui.GroupItem
import com.saggitt.omega.groups.ui.SelectTabBottomSheet
import com.saggitt.omega.preferences.NeoPrefs
import com.saggitt.omega.util.Config
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialApi::class)
@Composable
fun AppCategoriesPage() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = NeoPrefs.getInstance(context)
    val manager by lazy { prefs.drawerAppGroupsManager }
    val categoriesEnabled = remember { mutableStateOf(manager.categorizationEnabled.getValue())}

    var categoryTitle by remember { mutableStateOf("") }

    var radius = 16.dp
    if (prefs.profileWindowCornerRadius.getValue() > -1) {
        radius = prefs.profileWindowCornerRadius.getValue().dp
    }
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmValueChange = { it != ModalBottomSheetValue.HalfExpanded }
    )
    val hasWorkApps = Config.hasWorkApps(LocalContext.current)

    var selectedCategoryKey by remember { mutableStateOf(manager.categorizationType.getValue()) }
    var currentCategory by remember { mutableStateOf(AppGroupsManager.Category.TAB) }

    val groups = remember(currentCategory, categoriesEnabled) {
        mutableStateListOf(*loadAppGroups(manager, hasWorkApps))
    }

    val (openedOption, onOptionOpen) = remember(manager.getCurrentCategory(), categoriesEnabled) {
        mutableStateOf(manager.getCurrentCategory())
    }

    val editGroup = remember {
        mutableStateOf(groups.firstOrNull())
    }

    when (selectedCategoryKey) {
        AppGroupsManager.Category.TAB.key,
        AppGroupsManager.Category.FLOWERPOT.key,
        -> {
            categoryTitle = stringResource(id = R.string.app_categorization_tabs)
        }

        AppGroupsManager.Category.FOLDER.key -> {
            categoryTitle = stringResource(id = R.string.app_categorization_folders)
        }

        else -> {}
    }

    var sheetChanger by remember {
        mutableIntStateOf(Config.BS_SELECT_TAB_TYPE)
    }

    BackHandler(sheetState.isVisible) {
        coroutineScope.launch {
            sheetState.hide()
            sheetChanger = Config.BS_SELECT_TAB_TYPE
        }
    }
    if (sheetState.currentValue != ModalBottomSheetValue.Hidden) {
        DisposableEffect(Unit) {
            onDispose {
                sheetChanger = Config.BS_SELECT_TAB_TYPE
            }
        }
    }

    val state = rememberReorderableLazyListState(onMove = { from, to ->
        groups.move(from.index, to.index)
    })

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = radius, topEnd = radius),
        sheetElevation = 8.dp,
        sheetBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        sheetContent = {
            when (sheetChanger) {
                //Select tab type
                Config.BS_SELECT_TAB_TYPE -> {
                    if (currentCategory == AppGroupsManager.Category.TAB || currentCategory == AppGroupsManager.Category.FLOWERPOT)
                        SelectTabBottomSheet { changer, categorizationType ->
                            currentCategory = categorizationType
                            sheetChanger = changer
                            groups.clear()
                            groups.addAll(loadAppGroups(manager, hasWorkApps))
                        }
                }

                //Create tab or folder
                Config.BS_CREATE_GROUP -> {
                    CreateGroupBottomSheet(category = currentCategory) {
                        coroutineScope.launch {
                            sheetChanger = if (currentCategory == AppGroupsManager.Category.TAB
                                || currentCategory == AppGroupsManager.Category.FLOWERPOT
                            ) {
                                Config.BS_SELECT_TAB_TYPE
                            } else {
                                Config.BS_CREATE_GROUP
                            }
                            groups.clear()
                            groups.addAll(loadAppGroups(manager, hasWorkApps))
                            sheetState.hide()
                        }
                    }
                }

                //Edit group
                Config.BS_EDIT_GROUP -> {
                    editGroup.value?.let { editGroup ->
                        EditGroupBottomSheet(openedOption, editGroup) {
                            sheetChanger = it
                            coroutineScope.launch {
                                sheetState.hide()
                            }
                        }
                    }
                }
            }
        }
    ) {
        ViewWithActionBar(
            title = stringResource(id = R.string.title_app_categorize),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            if (sheetState.isVisible) {
                                sheetState.hide()
                            } else {
                                sheetState.show()
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
                    )
            ) {
                ComposeSwitchView(
                    modifier = Modifier
                        .background(
                            if (categoriesEnabled.value) MaterialTheme.colorScheme.primary.copy(0.6f)
                            else MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                            MaterialTheme.shapes.extraLarge
                        )
                        .clip(MaterialTheme.shapes.extraLarge),
                    title = stringResource(id = R.string.title_app_categorization_enable),
                    summary = stringResource(id = R.string.summary_app_categorization_enable),
                    verticalPadding = 16.dp,
                    horizontalPadding = 16.dp,
                    isChecked = categoriesEnabled.value,
                    onCheckedChange = {
                        categoriesEnabled.value = it
                        manager.categorizationEnabled.setValue(it)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        AppGroupsManager.Category.FOLDER,
                        AppGroupsManager.Category.TAB
                    ).forEach {
                        CategorizationOption(
                            category = it,
                            selected = selectedCategoryKey == it.key && categoriesEnabled.value
                        ) {
                            if (categoriesEnabled.value) {
                                manager.categorizationType.setValue(it.key)
                                selectedCategoryKey = it.key
                                currentCategory = it
                                sheetChanger = if (it == AppGroupsManager.Category.FOLDER) {
                                    Config.BS_CREATE_GROUP
                                } else {
                                    Config.BS_SELECT_TAB_TYPE
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = categoryTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .reorderable(state)
                        .detectReorderAfterLongPress(state),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    state = state.listState
                ) {
                    itemsIndexed(groups) { index, item ->
                        ReorderableItem(
                            reorderableState = state,
                            key = item.title,
                            index = index
                        ) { isDragging ->
                            val elevation = animateDpAsState(
                                if (isDragging) 24.dp else 0.dp,
                                label = ""
                            )
                            GroupItem(
                                title = item.title,
                                summary = item.summary,
                                modifier = Modifier
                                    .shadow(elevation.value)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = if (index == 0) 24.dp else 8.dp,
                                            topEnd = if (index == 0) 24.dp else 8.dp,
                                            bottomStart = if (index == groups.size - 1) 24.dp else 8.dp,
                                            bottomEnd = if (index == groups.size - 1) 24.dp else 8.dp
                                        )
                                    )
                                    .background(
                                        if (isDragging) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                removable = item.type in arrayOf(
                                    DrawerTabs.TYPE_CUSTOM,
                                    FlowerpotTabs.TYPE_FLOWERPOT,
                                    DrawerFolders.TYPE_CUSTOM
                                ),
                                index = index,
                                groupSize = groups.size,
                                onClick = {
                                    coroutineScope.launch {
                                        sheetChanger = Config.BS_EDIT_GROUP
                                        onOptionOpen(
                                            when (item.type) {
                                                DrawerTabs.TYPE_CUSTOM -> AppGroupsManager.Category.TAB
                                                FlowerpotTabs.TYPE_FLOWERPOT -> AppGroupsManager.Category.FLOWERPOT
                                                else -> AppGroupsManager.Category.FOLDER
                                            }
                                        )
                                        editGroup.value = item
                                        sheetState.show()
                                    }
                                }
                            ) {
                                groups.remove(item)
                                saveGroupPositions(manager, groups)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.pref_app_groups_edit_tip),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
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
            if (hasWorkApps) {
                manager.drawerTabs.getGroups()
                    .filter { it !is DrawerTabs.ProfileTab || !it.profile.matchesAll }
            } else {
                manager.drawerTabs.getGroups()
                    .filter { it !is DrawerTabs.ProfileTab || it.profile.matchesAll }
            }
        }

        AppGroupsManager.Category.FOLDER.key -> {
            manager.drawerFolders.getGroups()
        }

        else -> {
            emptyList()
        }
    }.toTypedArray()
}
