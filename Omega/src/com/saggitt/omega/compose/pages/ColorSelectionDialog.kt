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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.android.launcher3.R
import com.saggitt.omega.compose.components.HorizontalPagerPage
import com.saggitt.omega.compose.components.TabItem
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.theme.AccentColorOption
import com.saggitt.omega.theme.OmegaAppTheme
import com.saggitt.omega.util.dynamicColors
import com.saggitt.omega.util.staticColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColorSelectionDialog(
    defaultColor: String,
    onClose: (String) -> Unit,
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

    OmegaAppTheme {
        ViewWithActionBar(
            title = stringResource(R.string.tab_color),
            onBackAction = {
                onClose(currentColor.value)
            }
        ) { paddingValues ->
            HorizontalPagerPage(
                pagerState = pagerState,
                tabs = tabs,
                paddingValues = paddingValues
            )
        }
    }

    DisposableEffect(key1 = null) {
        onDispose {
            onClose(currentColor.value)
        }
    }
}