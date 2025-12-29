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

package com.neoapps.neolauncher.compose.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
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
import androidx.datastore.preferences.core.Preferences
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.components.ColorItem
import com.neoapps.neolauncher.compose.components.DialogNegativeButton
import com.neoapps.neolauncher.compose.components.HorizontalPagerNavBar
import com.neoapps.neolauncher.compose.components.HorizontalPagerPage
import com.neoapps.neolauncher.compose.components.SingleSelectionListItem
import com.neoapps.neolauncher.compose.components.TabItem
import com.neoapps.neolauncher.compose.components.ViewWithActionBar
import com.neoapps.neolauncher.compose.navigation.LocalPaneNavigator
import com.neoapps.neolauncher.preferences.PrefKey
import com.neoapps.neolauncher.theme.AccentColorOption
import com.neoapps.neolauncher.util.blockBorder
import com.neoapps.neolauncher.util.dynamicColors
import com.neoapps.neolauncher.util.prefs
import com.neoapps.neolauncher.util.staticColors
import com.raedapps.alwan.rememberAlwanState
import com.raedapps.alwan.ui.Alwan
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ColorSelectionPage(prefKey: Preferences.Key<String>) {
    val prefs = LocalContext.current.prefs
    val pref = when (prefKey) {
        PrefKey.DESKTOP_FOLDER_BG_COLOR     -> prefs.desktopFolderBackgroundColor
        PrefKey.DESKTOP_FOLDER_STROKE_COLOR -> prefs.desktopFolderStrokeColor
        PrefKey.DOCK_BG_COLOR               -> prefs.dockBackgroundColor
        PrefKey.DRAWER_BG_COLOR             -> prefs.drawerBackgroundColor
        PrefKey.NOTIFICATION_DOTS_COLOR     -> prefs.notificationBackground
        else                                -> prefs.profileAccentColor
    }
    val paneNavigator = LocalPaneNavigator.current
    val currentAccentColor = remember { mutableStateOf(pref.getValue()) }
    val dynamicColors = dynamicColors
    val presetColors = staticColors
    val coroutineScope = rememberCoroutineScope()

    val tabs = listOf(
        TabItem(title = R.string.color_presets, icon = R.drawable.ic_setting) {
            PresetsPage(
                presetColors = presetColors,
                onSelectColor = { currentAccentColor.value = it },
                isColorSelected = { it == currentAccentColor.value }
            )
        },
        TabItem(title = R.string.custom, icon = R.drawable.ic_color_donut) {
            CustomPage(
                initialColor = Color(AccentColorOption.fromString(currentAccentColor.value).accentColor),
                onSelectColor = {
                    currentAccentColor.value = it
                }
            )
        },
        TabItem(title = R.string.color_dynamic, icon = R.drawable.ic_paint_bucket) {
            DynamicPage(
                dynamicColors = dynamicColors,
                onSelectColor = { currentAccentColor.value = it },
                isColorSelected = { it == currentAccentColor.value }
            )
        }
    )
    val defaultTabIndex = when {
        presetColors.any { it.toString() == currentAccentColor.value }  -> 0
        dynamicColors.any { it.toString() == currentAccentColor.value } -> 2
        else                                                            -> 1
    }
    val pagerState = rememberPagerState(initialPage = defaultTabIndex, pageCount = { tabs.size })

    ViewWithActionBar(
        title = stringResource(pref.titleId),
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
                            coroutineScope.launch {
                                paneNavigator.navigateBack(BackNavigationBehavior.PopLatest)

                            }
                        }
                    )
                    Button(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .height(48.dp)
                            .fillMaxWidth(),
                        onClick = {
                            pref.setValue(currentAccentColor.value)
                            coroutineScope.launch {
                                paneNavigator.navigateBack(BackNavigationBehavior.PopLatest)
                            }
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
}

@Composable
fun PresetsPage(
    presetColors: List<AccentColorOption>,
    onSelectColor: (String) -> Unit,
    isColorSelected: (String) -> Boolean,
) {
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.FixedSize(72.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        contentPadding = PaddingValues(12.dp)
    ) {
        itemsIndexed(presetColors) { _, colorOption ->
            ColorItem(
                color = colorOption.accentColor,
                selected = isColorSelected(colorOption.toString()),
                modifier = Modifier.widthIn(0.dp, 64.dp),
                onClick = { onSelectColor(colorOption.toString()) }
            )
        }
    }
}

@Composable
fun CustomPage(
    initialColor: Color,
    onSelectColor: (String) -> Unit,
) {
    val current = rememberAlwanState(initialColor = initialColor)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(12.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(current.color),
                contentAlignment = Alignment.Center
            ) {
                Text(text = Integer.toHexString(current.color.hashCode()), color = Color.White)
            }
        }
        item {
            Alwan(
                modifier = Modifier
                    .widthIn(min = 500.dp)
                    .padding(horizontal = 24.dp),
                onColorChanged = {
                    onSelectColor("custom|#${Integer.toHexString(current.color.hashCode())}")
                },
                state = current,
                showAlphaSlider = true,
            )
        }
    }
}

@Composable
fun DynamicPage(
    dynamicColors: List<AccentColorOption>,
    onSelectColor: (String) -> Unit,
    isColorSelected: (String) -> Boolean,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 8.dp)
    ) {
        itemsIndexed(dynamicColors) { _, option ->
            SingleSelectionListItem(
                text = stringResource(id = option.displayName),
                isSelected = isColorSelected(option.toString()),
                endWidget = {
                    ColorItem(
                        color = option.accentColor,
                        selected = false,
                        modifier = Modifier.widthIn(0.dp, 36.dp),
                        onClick = { onSelectColor(option.toString()) }
                    )
                },
                onClick = {
                    onSelectColor(option.toString())
                }
            )
        }
    }
}