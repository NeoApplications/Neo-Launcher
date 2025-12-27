/*
 * This file is part of Neo Launcher
 * Copyright (c) 2023   Neo Launcher Team
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

package com.saggitt.omega.folder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.model.data.FolderInfo
import com.saggitt.omega.compose.components.DialogNegativeButton
import com.saggitt.omega.compose.components.DialogPositiveButton
import com.saggitt.omega.compose.components.SingleSelectionListItem
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.gestures.GestureHandler
import com.saggitt.omega.preferences.NeoPrefs
import com.saggitt.omega.util.blockShadow

@Composable
fun FolderListDialog(
    folder: FolderInfo,
    openDialogCustom: MutableState<Boolean>,
    currentGesture: GestureHandler,
    onClose: (GestureHandler) -> Unit,
) {
    Dialog(
        onDismissRequest = { openDialogCustom.value = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        FolderListDialogUI(
            folder = folder,
            currentGesture = currentGesture,
            openDialogCustom = openDialogCustom,
            onClose = onClose
        )
    }
}

@Composable
fun FolderListDialogUI(
    folder: FolderInfo,
    currentGesture: GestureHandler,
    openDialogCustom: MutableState<Boolean>,
    onClose: (GestureHandler) -> Unit,
) {
    val context = LocalContext.current
    val prefs = NeoPrefs.getInstance()

    var radius = 16.dp
    if (prefs.profileWindowCornerRadius.getValue() > -1) {
        radius = prefs.profileWindowCornerRadius.getValue().dp
    }
    val cornerRadius by remember { mutableStateOf(radius) }
    val gestures = GestureController.getGestureHandlers(context, isSwipeUp = true, hasBlank = true)
    var selected by remember { mutableStateOf(currentGesture.javaClass.name.toString()) }
    var selectedGesture by remember { mutableStateOf(currentGesture) }

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(id = R.string.folder_swipe_up),
                style = MaterialTheme.typography.titleLarge
            )
            LazyColumn(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .weight(1f, false)
                    .blockShadow()
            ) {
                items(items = gestures) {
                    val isSelected = rememberSaveable(selected) {
                        mutableStateOf(selected == it.javaClass.name.toString())
                    }

                    SingleSelectionListItem(
                        text = it.displayName,
                        isSelected = isSelected.value
                    ) {
                        selected = it.javaClass.name.toString()
                        selectedGesture = it
                    }
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
                    //folder.setSwipeUpAction(context, selected)
                    onClose(selectedGesture)
                    openDialogCustom.value = false
                }
            )
        }
    }
}