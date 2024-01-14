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

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.components.ListItemWithIcon
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.components.move
import com.saggitt.omega.dash.dashProviderOptions
import com.saggitt.omega.preferences.iconIds
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun EditDashPage() {
    val context = LocalContext.current
    val prefs = Utilities.getNeoPrefs(context)
    val iconList = iconIds

    val allItems = dashProviderOptions

    val enabled = allItems.filter {
        it.key in prefs.dashProvidersItems.getValue()
    }.map { DashItem(it.key, it.value) }.associateBy { it.key }

    val enabledSorted = prefs.dashProvidersItems.getValue().mapNotNull { enabled[it] }

    val disabled = allItems.filter {
        it.key !in prefs.dashProvidersItems.getValue()
    }.map { DashItem(it.key, it.value) }

    val enabledItems = remember { mutableStateListOf(*enabledSorted.toTypedArray()) }
    val disabledItems = remember { mutableStateOf(disabled) }

    val state = rememberReorderableLazyListState(onMove = { from, to ->
        enabledItems.move(
            (from.index - 2).coerceIn(0, enabledItems.size - 1),
            (to.index - 2).coerceIn(0, enabledItems.size - 1)
        )
    })

    ViewWithActionBar(
        title = stringResource(id = R.string.edit_dash),
        onBackAction = {
            val enabledKeys = enabledItems.map { it.key }
            prefs.dashProvidersItems.setAll(enabledKeys)
        }
    ) { paddingValues ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxSize()
                .reorderable(state)
                .detectReorderAfterLongPress(state)
                .padding(horizontal = 8.dp),
            contentPadding = paddingValues,
            state = state.listState
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.enabled_events),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }
            itemsIndexed(enabledItems, { _, it -> it.key }) { index, item ->
                ReorderableItem(state, key = item.key, index = index + 2) { isDragging ->
                    val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                    ListItemWithIcon(
                        title = stringResource(id = item.titleResId),
                        modifier = Modifier
                            .shadow(elevation.value)
                            .height(56.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = if (index == 0) 16.dp else 6.dp,
                                    topEnd = if (index == 0) 16.dp else 6.dp,
                                    bottomStart = if (index == enabledItems.size - 1) 16.dp else 6.dp,
                                    bottomEnd = if (index == enabledItems.size - 1) 16.dp else 6.dp
                                )
                            )
                            .background(
                                if (isDragging) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                enabledItems.remove(item)
                                val tempList = disabledItems.value.toMutableList()
                                tempList.add(0, item)
                                disabledItems.value = tempList
                            },
                        startIcon = {
                            Icon(
                                painter = painterResource(
                                    id = iconList[item.key] ?: R.drawable.ic_edit_dash
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp)
                            )
                        },
                        endCheckbox = {
                            IconButton(
                                modifier = Modifier.size(36.dp),
                                onClick = {
                                    enabledItems.remove(item)
                                    val tempList = disabledItems.value.toMutableList()
                                    tempList.add(0, item)
                                    disabledItems.value = tempList
                                }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_drag_handle),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        verticalPadding = 6.dp
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.tap_to_enable),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }
            itemsIndexed(disabledItems.value) { index, item ->
                ListItemWithIcon(
                    title = stringResource(id = item.titleResId),
                    modifier = Modifier
                        .height(56.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = if (index == 0) 16.dp else 6.dp,
                                topEnd = if (index == 0) 16.dp else 6.dp,
                                bottomStart = if (index == disabledItems.value.size - 1) 16.dp else 6.dp,
                                bottomEnd = if (index == disabledItems.value.size - 1) 16.dp else 6.dp
                            )
                        )
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable {
                            disabledItems.value = disabledItems.value - item
                            enabledItems.add(item)
                        },
                    startIcon = {
                        Image(
                            painter = painterResource(
                                id = iconList[item.key] ?: R.drawable.ic_edit_dash
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                    },
                    endCheckbox = {
                        Spacer(modifier = Modifier.height(32.dp))
                    },
                    verticalPadding = 6.dp
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    DisposableEffect(key1 = null) {
        onDispose {
            val enabledKeys = enabledItems.map { it.key }
            prefs.dashProvidersItems.setAll(enabledKeys)
        }
    }
}

data class DashItem(val key: String, val titleResId: Int)