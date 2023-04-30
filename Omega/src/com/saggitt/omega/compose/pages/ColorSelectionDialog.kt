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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.TabRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.saggitt.omega.compose.components.TabItem
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.theme.OmegaAppTheme
import com.saggitt.omega.util.dynamicColors
import com.saggitt.omega.util.staticColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalPagerApi::class)
@Composable
fun ColorSelectionDialog(
    defaultColor: Int,
    onClose: (Int) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val currentColor = remember { mutableStateOf(defaultColor) }
    val dynamicColors = dynamicColors
    val presetColors = staticColors

    val defaultTabIndex = when {
        presetColors.any { it.accentColor == currentColor.value } -> 0
        dynamicColors.any { it.accentColor == currentColor.value } -> 2
        else -> 1
    }
    val pagerState = rememberPagerState(defaultTabIndex)

    val tabs = listOf(
        TabItem(title = R.string.color_presets) {
            PresetsPage(
                presetColors = presetColors,
                onSelectColor = { currentColor.value = it },
                isColorSelected = { it == currentColor.value }
            )
        },
        TabItem(title = R.string.custom) {
            CustomPage(
                initialColor = Color(currentColor.value),
                onSelectColor = {
                    currentColor.value = it
                }
            )
        },
        TabItem(title = R.string.color_dynamic) {
            DynamicPage(
                dynamicColors = dynamicColors,
                onSelectColor = { currentColor.value = it },
                isColorSelected = { it == currentColor.value }
            )
        }
    )

    OmegaAppTheme {
        ViewWithActionBar(
            title = stringResource(R.string.tab_color),
            onBackAction = {
                onClose(currentColor.value)
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        top = it.calculateTopPadding(),
                        bottom = 16.dp,
                        start = 8.dp,
                        end = 8.dp
                    )
            ) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    backgroundColor = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Text(
                                    text = stringResource(id = tab.title),
                                    color = if (pagerState.currentPage == index)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    HorizontalPager(state = pagerState, pageCount = tabs.size) { page ->
                        tabs[page].screen()
                    }
                }
            }
        }
    }

    DisposableEffect(key1 = null) {
        onDispose {
            onClose(currentColor.value)
        }
    }
}