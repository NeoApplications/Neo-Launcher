/*
 * This file is part of Neo Launcher
 * Copyright (c) 2026   Neo Launcher Team
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

package com.neoapps.neolauncher.compose.components.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neoapps.neolauncher.compose.components.BaseDialog
import com.neoapps.neolauncher.compose.components.DialogNegativeButton
import com.neoapps.neolauncher.compose.components.DialogPositiveButton
import com.neoapps.neolauncher.compose.components.SingleSelectionListItem
import com.neoapps.neolauncher.compose.navigation.LocalPaneNavigator
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.preferences.TwoStatePref
import com.neoapps.neolauncher.theme.GroupItemShape
import com.neoapps.neolauncher.util.addIf
import com.neoapps.neolauncher.util.blockShadow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TwoStatePreference(
    modifier: Modifier = Modifier,
    pref: TwoStatePref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onCheckedChange: ((Boolean) -> Unit) = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val paneNavigator = LocalPaneNavigator.current
    val (checked, check) = remember(pref) { mutableStateOf(pref.getValue()) }
    val openDialog = remember { mutableStateOf(false) }
    val selectedValue2 by pref.getStringState()
    var selectedKeyOverride by remember(pref) { mutableStateOf<String?>(null) }
    val currentSelectedKey = selectedKeyOverride ?: selectedValue2
    val summaryText = pref.entries[currentSelectedKey]
        ?: (if (pref.entries.isNotEmpty()) pref.entries.values.firstOrNull() else null)
        ?: ""
    val prefState by pref.getState()
    LaunchedEffect(prefState) {
        check(prefState)
    }
    LaunchedEffect(selectedValue2) {
        selectedKeyOverride = null
    }

    val onToggle = { newValue: Boolean ->
        val update = {
            onCheckedChange(newValue)
            check(newValue)
            coroutineScope.launch { pref.setValue(newValue) }
            Unit
        }
        if (pref.confirmAction != null) {
            pref.confirmAction(context, newValue, update)
        } else {
            update()
        }

    }
    val onClick = {

        if (pref.navRoute != null) {
            val navigateTo = {
                coroutineScope.launch {
                    paneNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail, pref.navRoute)
                }
                Unit
            }

            if (pref.confirmAction != null) {
                pref.confirmAction(context, true, navigateTo)
            } else {
                navigateTo()
            }
        }
        if (isEnabled && pref.entries.isNotEmpty()) {
            openDialog.value = true
        }
    }

    TwoStatePreference(
        title = pref.titleId,
        modifier = modifier,
        isEnabled = isEnabled,
        isChecked = checked,
        summary = pref.summaryId,
        summaryText = summaryText,
        index = index,
        groupSize = groupSize,
        onclick = {
            onClick()
        },
        onValueChange = {
            onToggle(!checked)
        }
    )

    if (openDialog.value) {
        BaseDialog(openDialogCustom = openDialog) {
            TwoStatePrefDialogUI(
                titleId = pref.titleId,
                entries = pref.entries,
                selectedValue = currentSelectedKey,
                openDialogCustom = openDialog,
                onConfirm = { selectedKey ->
                    selectedKeyOverride = selectedKey
                    coroutineScope.launch {
                        pref.setStringValue(selectedKey)
                    }
                }
            )
        }
    }
}

@Composable
fun TwoStatePreference(
    title: Int,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    isChecked: Boolean,
    summary: Int = -1,
    summaryText: String = "",
    iconId: Int = 0,
    index: Int = 0,
    groupSize: Int = 1,
    onclick: () -> Unit = {},
    onValueChange: (Boolean) -> Unit = {}
) {
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(id = title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        supportingContent = {
            if (summary != -1 || summaryText.isNotEmpty()) {
                Text(
                    modifier = Modifier
                        .addIf(!isEnabled) {
                            alpha(0.3f)
                        },
                    text = if (summary != -1) {
                        if (summaryText.isNotEmpty()) {
                            stringResource(id = summary, summaryText)
                        } else {
                            stringResource(id = summary)
                        }
                    } else {
                        summaryText
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        leadingContent = iconId.takeIf { it != 0 }?.let {
            {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = "",
                    tint = Color(NeoPrefs.getInstance().profileAccentColor.getColor()),
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12F))

                )
            }
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                VerticalDivider(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    thickness = 1.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    modifier = Modifier
                        .height(24.dp),
                    checked = isChecked,
                    onCheckedChange = {
                        onValueChange(it)
                    },
                    enabled = isEnabled,
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled) {
                onclick()
            }
            .clip(
                GroupItemShape(index, groupSize - 1)
            ),
        colors = ListItemDefaults.colors(
            containerColor = if (isChecked) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ),
    )
}


@Composable
fun TwoStatePrefDialogUI(
    titleId: Int,
    entries: Map<String, String>,
    selectedValue: String,
    openDialogCustom: MutableState<Boolean>,
    onConfirm: (String) -> Unit
) {
    var selected by remember(selectedValue, entries) {
        mutableStateOf(
            if (entries.containsKey(selectedValue)) selectedValue
            else entries.keys.firstOrNull() ?: ""
        )
    }
    val entryPairs = remember(entries) { entries.toList() }
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
            Text(
                text = stringResource(id = titleId),
                style = MaterialTheme.typography.titleLarge
            )
            LazyColumn(
                modifier = Modifier
                    .blockShadow()
                    .padding(all = 8.dp)
                    .weight(1f, false),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val groupSize = entryPairs.size
                items(items = entryPairs, key = { it.first }) { item ->
                    SingleSelectionListItem(
                        title = item.second,
                        isSelected = selected == item.first,
                        index = entryPairs.indexOf(item),
                        groupSize = groupSize
                    ) {
                        selected = item.first
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                DialogNegativeButton(
                    onClick = { openDialogCustom.value = false }
                )
                Spacer(Modifier.weight(1f))
                DialogPositiveButton(
                    modifier = Modifier.padding(start = 16.dp),
                    onClick = {
                        openDialogCustom.value = false
                        onConfirm(selected)
                    }
                )
            }
        }
    }
}
