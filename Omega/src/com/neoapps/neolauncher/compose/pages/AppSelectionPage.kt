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

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.components.ListItemWithIcon
import com.neoapps.neolauncher.compose.components.OverflowMenu
import com.neoapps.neolauncher.compose.components.ViewWithActionBar
import com.neoapps.neolauncher.compose.components.preferences.PreferenceGroup
import com.neoapps.neolauncher.theme.GroupItemShape
import com.neoapps.neolauncher.util.appsState

@Composable
fun AppSelectionPage(
    pageTitle: String,
    selectedApps: Set<String>,
    pluralTitleId: Int,
    onSave: (Set<String>) -> Unit
) {
    var selected by remember { mutableStateOf(selectedApps) }
    var title by remember { mutableStateOf(pageTitle) }
    val allApps by appsState(comparator = hiddenAppsComparator(selectedApps))
    val pluralTitle = stringResource(id = pluralTitleId, selected.size)
    var appsSize by remember { mutableIntStateOf(1) }

    DisposableEffect(allApps.size) {
        appsSize = allApps.size
        onDispose { }
    }

    title = if (selected.isNotEmpty()) {
        pluralTitle
    } else {
        pageTitle
    }

    ViewWithActionBar(
        title = title,
        actions = {
            OverflowMenu {
                DropdownMenuItem(
                    onClick = {
                        selected = emptySet()
                        hideMenu()
                    },
                    text = { Text(text = stringResource(id = R.string.app_reset)) }
                )
            }
        },
        onBackAction = {
            onSave(selected)
        }
    ) { paddingValues ->
        if (allApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            PreferenceGroup {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentPadding = paddingValues,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(allApps) { index, app ->
                        val isSelected = rememberSaveable(selected) {
                            mutableStateOf(selected.contains(app.key.toString()))
                        }
                        ListItemWithIcon(
                            modifier = Modifier
                                .clip(GroupItemShape(index, appsSize - 1))
                                .clickable {
                                    selected =
                                        if (isSelected.value) selected.minus(app.key.toString())
                                        else selected.plus(app.key.toString())
                                },
                            title = app.label + if (app.key.user.hashCode() != 0) " \uD83D\uDCBC" else "",
                            startIcon = {
                                Image(
                                    painter = BitmapPainter(app.icon.asImageBitmap()),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp)
                                )
                            },
                            endCheckbox = {
                                Checkbox(
                                    checked = isSelected.value,
                                    onCheckedChange = {
                                        selected = if (it) selected.plus(app.key.toString())
                                        else selected.minus(app.key.toString())
                                    },

                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            index = index,
                            groupSize = appsSize
                        )
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
