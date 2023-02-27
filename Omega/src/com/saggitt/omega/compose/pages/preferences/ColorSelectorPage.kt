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

package com.saggitt.omega.compose.pages.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TabRow
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.raedapps.alwan.rememberAlwanState
import com.raedapps.alwan.ui.Alwan
import com.saggitt.omega.compose.components.ColorItem
import com.saggitt.omega.compose.components.SingleSelectionListItem
import com.saggitt.omega.compose.components.TabItem
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.navigation.LocalNavController
import com.saggitt.omega.theme.AccentColorOption
import com.saggitt.omega.theme.OmegaAppTheme
import com.saggitt.omega.util.dynamicColors
import com.saggitt.omega.util.staticColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ColorSelectorPage() {
    val coroutineScope = rememberCoroutineScope()
    val prefs = Utilities.getOmegaPrefs(LocalContext.current)
    val navController = LocalNavController.current
    val currentAccentColor = remember { mutableStateOf(prefs.profileAccentColor.getValue()) }
    val dynamicColors = dynamicColors
    val presetColors = staticColors

    val defaultTabIndex = when {
        presetColors.any { it.accentColor == currentAccentColor.value } -> 0
        dynamicColors.any { it.accentColor == currentAccentColor.value } -> 2
        else -> 1
    }
    val pagerState = rememberPagerState(defaultTabIndex)

    val tabs = listOf(
        TabItem(title = R.string.color_presets) {
            PresetsPage(
                presetColors = presetColors,
                onSelectColor = { currentAccentColor.value = it },
                isColorSelected = { it == currentAccentColor.value }
            )
        },
        TabItem(title = R.string.custom) {
            CustomPage(
                initialColor = Color(currentAccentColor.value),
                onSelectColor = { currentAccentColor.value = it }
            )
        },
        TabItem(title = R.string.color_dynamic) {
            DynamicPage(
                dynamicColors = dynamicColors,
                onSelectColor = { currentAccentColor.value = it },
                isColorSelected = { it == currentAccentColor.value }
            )
        }
    )

    OmegaAppTheme {
        ViewWithActionBar(
            title = stringResource(R.string.title__theme_accent_color),
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier
                        .fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.background
                ) {

                    Button(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp)
                            .fillMaxWidth(),
                        onClick = {
                            prefs.profileAccentColor.setValue(currentAccentColor.value)
                            navController.popBackStack()
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.button_apply),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
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
                            Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
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
                    HorizontalPager(state = pagerState, count = tabs.size) { page ->
                        tabs[page].screen()
                    }
                }
            }
        }
    }
}

@Composable
fun PresetsPage(
    presetColors: List<AccentColorOption>,
    onSelectColor: (Int) -> Unit,
    isColorSelected: (Int) -> Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(vertical = 16.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            contentPadding = PaddingValues(8.dp)
        ) {
            itemsIndexed(presetColors) { _, colorOption ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    ColorItem(
                        color = colorOption.accentColor,
                        selected = isColorSelected(colorOption.accentColor),
                        modifier = Modifier.widthIn(0.dp, 56.dp),
                        onClick = { onSelectColor(colorOption.accentColor) }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomPage(
    initialColor: Color,
    onSelectColor: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val current = rememberAlwanState(initialColor = initialColor)
        Box(
            modifier = Modifier
                .height(64.dp)
                .width(164.dp)
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(current.color),
            contentAlignment = Alignment.Center
        ) {
            Text(text = Integer.toHexString(current.color.hashCode()), color = Color.White)
        }

        Alwan(
            onColorChanged = {
                onSelectColor(current.color.hashCode())
            },
            modifier = Modifier.width(300.dp),
            state = current,
            showAlphaSlider = true,
        )
    }
}

@Composable
fun DynamicPage(
    dynamicColors: List<AccentColorOption>,
    onSelectColor: (Int) -> Unit,
    isColorSelected: (Int) -> Boolean
) {
    val groupSize = dynamicColors.size
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        dynamicColors.forEachIndexed { index, option ->
            val rank = (index + 1f) / groupSize
            val base = index.toFloat() / groupSize
            SingleSelectionListItem(
                modifier = Modifier
                    .height(56.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = if (base == 0f) 16.dp else 6.dp,
                            topEnd = if (base == 0f) 16.dp else 6.dp,
                            bottomStart = if (rank == 1f) 16.dp else 6.dp,
                            bottomEnd = if (rank == 1f) 16.dp else 6.dp
                        )
                    )
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation((rank * 24).dp)),
                text = stringResource(id = option.displayName), endWidget = {
                    ColorItem(
                        color = option.accentColor,
                        selected = false,
                        modifier = Modifier.widthIn(0.dp, 36.dp),
                        onClick = { onSelectColor(option.accentColor) }
                    )
                },
                isSelected = isColorSelected(option.accentColor)
            ) {
                onSelectColor(option.accentColor)
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}