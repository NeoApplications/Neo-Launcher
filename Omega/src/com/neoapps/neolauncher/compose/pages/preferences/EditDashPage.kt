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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.components.ListItemWithIcon
import com.neoapps.neolauncher.compose.components.ViewWithActionBar
import com.neoapps.neolauncher.compose.components.move
import com.neoapps.neolauncher.dash.dashProviderOptions
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.preferences.iconIds
import com.neoapps.neolauncher.theme.GroupItemShape
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditDashPage() {
    val context = LocalContext.current
    val dashProvidersItems = NeoPrefs.getInstance().dashProvidersItems
    val iconList = iconIds

    val allItems = dashProviderOptions

    val (enabled, disabled) = allItems
        .map { DashItem(it.key, it.value) }
        .partition {
            it.key in dashProvidersItems.getValue()
        }
    val enabledMap = enabled.associateBy { it.key }

    val enabledSorted = dashProvidersItems.getValue().mapNotNull { enabledMap[it] }

    val enabledItems = remember { enabledSorted.toMutableStateList() }
    val disabledItems = remember { disabled.toMutableStateList() }

    val lazyListState = rememberLazyListState()
    val reorderableListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromIndex = enabledItems.indexOfFirst { it.key == from.key }
        val toIndex = enabledItems.indexOfFirst { it.key == to.key }

        enabledItems.move(fromIndex, toIndex)
    }

    ViewWithActionBar(
        title = stringResource(id = R.string.edit_dash),
        onBackAction = {
            val enabledKeys = enabledItems.map { it.key }
            dashProvidersItems.setAll(enabledKeys)
        }
    ) { paddingValues ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 8.dp),
            state = lazyListState,
        ) {
            stickyHeader {
                Text(
                    text = stringResource(id = R.string.enabled_events),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }
            itemsIndexed(enabledItems, key = { _, it -> it.key }) { subIndex, item ->
                ReorderableItem(
                    reorderableListState,
                    key = item.key,
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

                    ListItemWithIcon(
                        modifier = Modifier
                            .longPressDraggableHandle()
                            .shadow(elevation)
                            .clip(GroupItemShape(subIndex, enabledItems.size - 1))
                            .clickable {
                                enabledItems.remove(item)
                                disabledItems.add(0, item)
                            },
                        containerColor = bgColor,
                        title = stringResource(id = item.titleResId),
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
                                    disabledItems.add(0, item)
                                }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_plus),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                    )
                }
            }

            stickyHeader {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.tap_to_enable),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }
            itemsIndexed(disabledItems, key = { _, it -> it.key }) { subIndex, item ->
                ListItemWithIcon(
                    modifier = Modifier
                        .clip(GroupItemShape(subIndex, disabledItems.size - 1))
                        .clickable {
                            disabledItems.remove(item)
                            enabledItems.add(item)
                        },
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    title = stringResource(id = item.titleResId),
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
            dashProvidersItems.setAll(enabledKeys)
        }
    }
}

data class DashItem(val key: String, val titleResId: Int)