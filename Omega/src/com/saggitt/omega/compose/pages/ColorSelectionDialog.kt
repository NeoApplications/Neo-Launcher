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

package com.saggitt.omega.compose.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.saggitt.omega.compose.components.DialogNegativeButton
import com.saggitt.omega.compose.components.HorizontalPagerNavBar
import com.saggitt.omega.compose.components.HorizontalPagerPage
import com.saggitt.omega.compose.components.TabItem
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.theme.AccentColorOption
import com.saggitt.omega.util.blockBorder
import com.saggitt.omega.util.dynamicColors
import com.saggitt.omega.util.staticColors

@Composable
fun ColorSelectionDialog(
    defaultColor: String,
    onCancel: (String) -> Unit,
    onSave: (String) -> Unit,
) {
    val currentColor = remember { mutableStateOf(defaultColor) }
    val dynamicColors = dynamicColors
    val presetColors = staticColors

    val tabs = listOf(
        TabItem(title = R.string.color_presets, icon = R.drawable.ic_setting) {
            PresetsPage(
                presetColors = presetColors,
                onSelectColor = { currentColor.value = it },
                isColorSelected = { it == currentColor.value }
            )
        },
        TabItem(title = R.string.custom, icon = R.drawable.ic_color_donut) {
            CustomPage(
                initialColor = Color(AccentColorOption.fromString(currentColor.value).accentColor),
                onSelectColor = {
                    currentColor.value = it
                }
            )
        },
        TabItem(title = R.string.color_dynamic, icon = R.drawable.ic_paint_bucket) {
            DynamicPage(
                dynamicColors = dynamicColors,
                onSelectColor = { currentColor.value = it },
                isColorSelected = { it == currentColor.value }
            )
        }
    )
    val defaultTabIndex = when {
        presetColors.any { it.toString() == currentColor.value } -> 0
        dynamicColors.any { it.toString() == currentColor.value } -> 2
        else -> 1
    }
    val pagerState = rememberPagerState(initialPage = defaultTabIndex, pageCount = { tabs.size })

    ViewWithActionBar(
        title = stringResource(R.string.tab_color),
        showBackButton = false,
        bottomBar = {
            Column {
                HorizontalPagerNavBar(tabs = tabs, pagerState = pagerState)
                BottomAppBar(
                    modifier = Modifier
                        .fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    DialogNegativeButton(
                        onClick = {
                            onCancel(currentColor.value)
                        }
                    )
                    Button(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .height(48.dp)
                            .fillMaxWidth(),
                        onClick = { onSave(currentColor.value) }
                    ) {
                        Text(
                            text = stringResource(id = R.string.button_apply),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        HorizontalPagerPage(
            pagerState = pagerState,
            tabs = tabs,
            modifier = Modifier
                .padding(paddingValues)
                .blockBorder()
                .fillMaxSize(),
        )
    }

    DisposableEffect(key1 = null) {
        onDispose {
            onCancel(currentColor.value)
        }
    }
}