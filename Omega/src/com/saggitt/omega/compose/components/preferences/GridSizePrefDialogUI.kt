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

package com.saggitt.omega.compose.components.preferences

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.components.DialogNegativeButton
import com.saggitt.omega.compose.components.DialogPositiveButton
import com.saggitt.omega.preferences.GridSize
import com.saggitt.omega.preferences.GridSize2D
import com.saggitt.omega.preferences.NeoPrefs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridSizePrefDialogUI(
    pref: GridSize,
    openDialogCustom: MutableState<Boolean>,
) {
    val context = LocalContext.current
    val prefs = NeoPrefs.getInstance()
    var numColumns by remember { mutableIntStateOf(pref.numColumnsPref.getValue()) }
    var numRows by remember { mutableIntStateOf(if (pref is GridSize2D) pref.numRowsPref.getValue() else 0) }

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
            Text(text = stringResource(pref.titleId), style = MaterialTheme.typography.titleLarge)
            LazyColumn(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .weight(1f, false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.height(40.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        var menuExpanded by remember { mutableStateOf(false) }
                        Icon(
                            modifier = Modifier
                                .size(32.dp)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        menuExpanded = !menuExpanded
                                    }
                                ),
                            painter = painterResource(id = R.drawable.ic_columns),
                            contentDescription = stringResource(id = R.string.title__drawer_columns)
                        )
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                onClick = {
                                    numColumns = pref.resetDefaultColumn()
                                    menuExpanded = false
                                },
                                text = { Text(text = stringResource(id = R.string.reset_to_default)) }
                            )
                        }
                        Text(
                            text = numColumns.toString(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.widthIn(min = 32.dp)
                        )
                        Slider(
                            modifier = Modifier
                                .requiredHeight(24.dp)
                                .weight(1f),
                            value = numColumns.toFloat(),
                            valueRange = pref.numColumnsPref.minValue..pref.numColumnsPref.maxValue,
                            onValueChange = { numColumns = it.toInt() },
                            steps = pref.numColumnsPref.steps
                        )
                    }
                }
                if (pref is GridSize2D) item {
                    Row(
                        modifier = Modifier.height(40.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        var menuExpanded by remember { mutableStateOf(false) }
                        Icon(
                            modifier = Modifier
                                .size(32.dp)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        menuExpanded = !menuExpanded
                                    }
                                ),
                            painter = painterResource(id = R.drawable.ic_rows),
                            contentDescription = stringResource(id = R.string.title__drawer_rows)
                        )
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                onClick = {
                                    numRows = pref.resetDefaultRow()
                                    menuExpanded = false
                                },
                                text = { Text(text = stringResource(id = R.string.reset_to_default)) }
                            )
                        }
                        Text(
                            text = numRows.toString(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.widthIn(min = 32.dp),
                        )
                        Slider(
                            modifier = Modifier
                                .requiredHeight(24.dp)
                                .weight(1f),
                            value = numRows.toFloat(),
                            valueRange = pref.numRowsPref.minValue..pref.numRowsPref.maxValue,
                            onValueChange = { numRows = it.toInt() },
                            steps = pref.numRowsPref.steps,
                        )
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DialogNegativeButton(
                    cornerRadius = cornerRadius,
                    onClick = { openDialogCustom.value = false }
                )
                DialogPositiveButton(
                    modifier = Modifier.padding(start = 16.dp),
                    cornerRadius = cornerRadius,
                    onClick = {
                        pref.numColumnsPref.setValue(numColumns)
                        if (pref is GridSize2D) pref.numRowsPref.setValue(numRows)
                        openDialogCustom.value = false
                    }
                )
            }
        }
    }
}
