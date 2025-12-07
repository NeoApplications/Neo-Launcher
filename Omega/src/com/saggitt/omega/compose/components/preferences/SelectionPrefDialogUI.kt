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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.components.DialogNegativeButton
import com.saggitt.omega.compose.components.DialogPositiveButton
import com.saggitt.omega.compose.components.MultiSelectionListItem
import com.saggitt.omega.compose.components.SingleSelectionListItem
import com.saggitt.omega.data.IconOverrideRepository
import com.saggitt.omega.preferences.DialogPref
import com.saggitt.omega.preferences.IntSelectionPref
import com.saggitt.omega.preferences.LongSelectionPref
import com.saggitt.omega.preferences.NeoPrefs
import com.saggitt.omega.preferences.StringMultiSelectionPref
import com.saggitt.omega.preferences.StringSelectionPref
import com.saggitt.omega.util.blockShadow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun IntSelectionPrefDialogUI(
    pref: IntSelectionPref,
    openDialogCustom: MutableState<Boolean>,
) {
    val context = LocalContext.current
    val prefs = NeoPrefs.getInstance()
    val entryPairs = pref.entries.toList()
    val coroutineScope = rememberCoroutineScope()
    var selected by remember { mutableIntStateOf(-1) }
    var themeCornerRadius by remember { mutableFloatStateOf(-1f) }
    SideEffect {
        coroutineScope.launch {
            selected = pref.get().first()
            themeCornerRadius = prefs.profileWindowCornerRadius.get().first()
        }
    }

    var radius = 16.dp
    if (themeCornerRadius > -1) {
        radius = themeCornerRadius.dp
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
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .weight(1f, false)
                    .blockShadow()
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(items = entryPairs) {
                    val isSelected = rememberSaveable(selected) {
                        mutableStateOf(selected == it.first)
                    }
                    SingleSelectionListItem(
                        text = stringResource(id = it.second),
                        isSelected = isSelected.value
                    ) {
                        selected = it.first
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(4.dp))
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
                        coroutineScope.launch { pref.setValue(selected) }
                        openDialogCustom.value = false
                    }
                )
            }
        }
    }
}

@Composable
fun LongSelectionPrefDialogUI(
    pref: LongSelectionPref,
    openDialogCustom: MutableState<Boolean>,
) {
    val context = LocalContext.current
    val prefs = NeoPrefs.getInstance()
    val entryPairs = pref.entries().toList()
    val coroutineScope = rememberCoroutineScope()
    var selected by remember { mutableLongStateOf(-1L) }
    var themeCornerRadius by remember { mutableFloatStateOf(-1f) }
    SideEffect {
        coroutineScope.launch {
            selected = pref.get().first()
            themeCornerRadius = prefs.profileWindowCornerRadius.get().first()
        }
    }

    var radius = 16.dp
    if (themeCornerRadius > -1) {
        radius = themeCornerRadius.dp
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
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .weight(1f, false)
                    .blockShadow(),
            ) {
                items(items = entryPairs) {
                    val isSelected = rememberSaveable(selected) {
                        mutableStateOf(selected == it.first)
                    }
                    SingleSelectionListItem(
                        text = it.second,
                        isSelected = isSelected.value
                    ) {
                        selected = it.first
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
                    cornerRadius = cornerRadius,
                    onClick = {
                        coroutineScope.launch { pref.setValue(selected) }
                        openDialogCustom.value = false
                    }
                )
            }
        }
    }
}

@Composable
fun StringSelectionPrefDialogUI(
    pref: StringSelectionPref,
    openDialogCustom: MutableState<Boolean>,
) {
    val prefs = NeoPrefs.getInstance()
    var selected by remember { mutableStateOf(pref.getValue()) }
    val entryPairs = pref.entries.toList()

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
                    .padding(top = 16.dp, bottom = 8.dp)
                    .weight(1f, false)
                    .blockShadow()
            ) {
                items(items = entryPairs) {
                    val isSelected = rememberSaveable(selected) {
                        mutableStateOf(selected == it.first)
                    }
                    SingleSelectionListItem(
                        text = it.second,
                        isSelected = isSelected.value
                    ) {
                        selected = it.first
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
                    cornerRadius = cornerRadius,
                    onClick = {
                        pref.setValue(selected)
                        openDialogCustom.value = false
                    }
                )
            }
        }
    }
}

@Composable
fun StringMultiSelectionPrefDialogUI(
    pref: StringMultiSelectionPref,
    openDialogCustom: MutableState<Boolean>,
) {
    val prefs = NeoPrefs.getInstance()
    var selected by remember { mutableStateOf(pref.getValue()) }
    val entryPairs = pref.entries.toList()

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
                    .padding(top = 16.dp, bottom = 8.dp)
                    .weight(1f, false)
                    .blockShadow()
            ) {
                items(items = entryPairs) { item ->
                    val isSelected = rememberSaveable(selected) {
                        mutableStateOf(selected.contains(item.first))
                    }

                    MultiSelectionListItem(
                        text = stringResource(id = item.second),
                        isChecked = isSelected.value,
                        withIcon = pref.withIcons,
                        iconId = item.first
                    ) {
                        selected = if (it) selected.plus(item.first)
                        else selected.minus(item.first)
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
                    cornerRadius = cornerRadius,
                    onClick = {
                        pref.setValue(selected)
                        openDialogCustom.value = false
                    }
                )
            }
        }
    }
}

@Composable
fun ResetCustomIconsDialog(
    pref: DialogPref,
    openDialogCustom: MutableState<Boolean>,
) {
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
            Text(text = stringResource(pref.titleId), style = MaterialTheme.typography.titleLarge)
            Text(
                text = stringResource(R.string.reset_custom_icons_confirmation),
                style = MaterialTheme.typography.titleMedium
            )
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
                    cornerRadius = cornerRadius,
                    onClick = {
                        val overrideRepo = IconOverrideRepository.INSTANCE.get(context)
                        MainScope().launch {
                            overrideRepo.deleteAll()
                        }
                        openDialogCustom.value = false
                    }
                )
            }
        }
    }
}
