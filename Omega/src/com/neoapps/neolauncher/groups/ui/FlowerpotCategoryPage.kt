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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.components.SingleSelectionListItem
import com.neoapps.neolauncher.compose.components.ViewWithActionBar
import com.neoapps.neolauncher.compose.components.preferences.PreferenceGroup
import com.neoapps.neolauncher.flowerpot.Flowerpot
import com.neoapps.neolauncher.theme.GroupItemShape

@Composable
fun FlowerpotCategoryPage(
    selectedCategory: String,
    onSave: (String) -> Unit,
) {
    val context = LocalContext.current
    val flowerpotManager = Flowerpot.Manager.getInstance(context)
    val categories = flowerpotManager.getAllPots().toList()
    var categoriesSize by remember { mutableIntStateOf(1) }
    var selected by remember { mutableStateOf(selectedCategory) }
    DisposableEffect(categories.size) {
        categoriesSize = categories.size
        onDispose { }
    }

    ViewWithActionBar(
        title = stringResource(id = R.string.pref_appcategorization_flowerpot_title),
        actions = {},
        onBackAction = {
            onSave(selected)
        }
    ) { paddingValues ->
        PreferenceGroup {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                itemsIndexed(categories) { index, it ->
                    val isSelected = rememberSaveable(selected) {
                        mutableStateOf(selected == it.name)
                    }
                    SingleSelectionListItem(
                        modifier = Modifier
                            .clip(GroupItemShape(index, categoriesSize - 1)),
                        title = it.displayName,
                        isSelected = isSelected.value,
                        index = index,
                        groupSize = categoriesSize
                    ) {
                        selected = it.name
                        onSave(selected)
                    }
                }
            }
        }

    }

    DisposableEffect(key1 = null) {
        onDispose {
            onSave(selected)
        }
    }
}