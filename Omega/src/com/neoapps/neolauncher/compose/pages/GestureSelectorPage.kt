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

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import com.android.launcher3.R
import com.android.launcher3.shortcuts.ShortcutKey
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.neoapps.neolauncher.compose.components.ExpandableListItem
import com.neoapps.neolauncher.compose.components.HorizontalPagerNavBar
import com.neoapps.neolauncher.compose.components.HorizontalPagerPage
import com.neoapps.neolauncher.compose.components.ListItemWithIcon
import com.neoapps.neolauncher.compose.components.TabItem
import com.neoapps.neolauncher.compose.components.ViewWithActionBar
import com.neoapps.neolauncher.compose.components.preferences.PreferenceGroup
import com.neoapps.neolauncher.data.AppItemWithShortcuts
import com.neoapps.neolauncher.gestures.GestureController
import com.neoapps.neolauncher.gestures.handlers.StartAppGestureHandler
import com.neoapps.neolauncher.preferences.NavigationPref
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.theme.GroupItemShape
import com.neoapps.neolauncher.util.App
import com.neoapps.neolauncher.util.appsState
import com.neoapps.neolauncher.util.blockBorder
import org.json.JSONObject

@Composable
fun GestureSelectorPage(prefs: NavigationPref) {

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val currentOption = remember { mutableStateOf(prefs.getValue()) }
    val apps = appsState().value
    val tabs = listOf(
        TabItem(R.drawable.ic_assistant, R.string.tab_launcher) {
            LauncherScreen(selectedOption = currentOption, onSelect = {
                prefs.setValue(it)
                backDispatcher?.onBackPressed()
            })
        },
        TabItem(R.drawable.ic_apps, R.string.apps_label) {
            AppsScreen(
                apps = apps,
                selectedOption = currentOption,
                onSelect = {
                    prefs.setValue(it)
                    backDispatcher?.onBackPressed()
                }
            )
        },
        TabItem(R.drawable.ic_edit_dash, R.string.tab_shortcuts) {
            ShortcutsScreen(
                apps = apps,
                selectedOption = currentOption,
                onSelect = {
                    prefs.setValue(it)
                    backDispatcher?.onBackPressed()
                }
            )
        }
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    ViewWithActionBar(
        title = stringResource(prefs.titleId),
        bottomBar = {
            HorizontalPagerNavBar(tabs = tabs, pagerState = pagerState)
        }
    ) { paddingValues ->
        HorizontalPagerPage(
            pagerState = pagerState,
            tabs = tabs,
            modifier = Modifier
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(),
                    start = 8.dp,
                    end = 8.dp)
                .blockBorder()
                .fillMaxSize(),
        )
    }
}

@Composable
fun LauncherScreen(
    selectedOption: MutableState<String>,
    onSelect: (String) -> Unit,
) {
    val context = LocalContext.current
    val launcherItems = GestureController.getGestureHandlers(
        context = context,
        isSwipeUp = true,
        hasBlank = true
    )

    val groupSize = launcherItems.size
    val colors = RadioButtonDefaults.colors(
        selectedColor = MaterialTheme.colorScheme.primary,
        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        PreferenceGroup {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = 0.dp,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 0.dp
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(launcherItems) { index, item ->
                    ListItemWithIcon(
                        modifier = Modifier
                            .clip(
                                GroupItemShape(index, groupSize - 1)
                            )
                            .clickable {
                                onSelect(item.toString())
                            },
                        title = item.displayName,
                        startIcon = {
                            Icon(
                                painter = BitmapPainter(
                                    image = item.icon?.toBitmap()!!.asImageBitmap(),
                                ),
                                contentDescription = item.displayName,
                                modifier = Modifier.size(24.dp),
                                tint = Color.Black
                            )
                        },
                        endCheckbox = {
                            RadioButton(
                                selected = (item.toString() == selectedOption.value),
                                onClick = {
                                    onSelect(item.toString())
                                },
                                colors = colors,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        index = index,
                        groupSize = groupSize
                    )
                }
                item{
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun AppsScreen(
    apps: List<App>,
    selectedOption: MutableState<String>,
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        val context = LocalContext.current
        val colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.primary,
            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        var appsSize by remember { mutableIntStateOf(apps.size) }
        PreferenceGroup {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = 0.dp,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 0.dp
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                item{
                    Spacer(Modifier.height(4.dp))
                }

                itemsIndexed(apps) { index, item ->
                    val config = JSONObject("{}")
                    config.apply {
                        put("appName", item.label)
                        put("packageName", item.packageName)
                        put("target", item.key)
                        put("type", "app")
                    }

                    val appGestureHandler = StartAppGestureHandler(context, config)
                    appGestureHandler.apply {
                        appName = item.label
                    }

                    ListItemWithIcon(
                        modifier = Modifier
                            .clip(GroupItemShape(index, appsSize - 1))
                            .clickable {
                                onSelect(appGestureHandler.toString())
                            },
                        title = item.label + if (item.key.user.hashCode() != 0) " \uD83D\uDCBC" else "",
                        startIcon = {
                            Image(
                                painter = BitmapPainter(item.icon.asImageBitmap()),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                        },
                        endCheckbox = {
                            RadioButton(
                                selected = (appGestureHandler.toString() == selectedOption.value),
                                onClick = {
                                    onSelect(appGestureHandler.toString())
                                },
                                colors = colors,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        index = index,
                        groupSize = appsSize
                    )
                }
                item{
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ShortcutsScreen(
    apps: List<App>,
    selectedOption: MutableState<String>,
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        val context = LocalContext.current
        var appsWithShortcuts by remember { mutableStateOf(emptyList<AppItemWithShortcuts>()) }

        if (apps.isNotEmpty()) {
            appsWithShortcuts = apps
                .map { AppItemWithShortcuts(context, it) }
                .filter { it.hasShortcuts }
        }

        val colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.primary,
            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        var appsSize by remember { mutableIntStateOf(appsWithShortcuts.size) }
        PreferenceGroup {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = 0.dp,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 0.dp
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(appsWithShortcuts) { appIndex, app ->
                    var expanded by remember { mutableStateOf(false) }

                    ExpandableListItem(
                        modifier = Modifier
                            .clip(
                                GroupItemShape(appIndex, appsSize - 1)
                            )
                            .background(
                                if (expanded) MaterialTheme.colorScheme.background
                                else MaterialTheme.colorScheme.surface
                            ),
                        title = app.info.label,
                        icon = app.info.icon,
                        onClick = { expanded = !expanded },
                        index = appIndex,
                        groupSize = appsSize
                    ) {
                        val groupSize = app.shortcuts.size

                        app.shortcuts.forEachIndexed { index, it ->

                            val config = JSONObject("{}")
                            config.apply {
                                put("appName", it.label.toString())
                                put("packageName", it.info.`package`)
                                put("intent", ShortcutKey.makeIntent(it.info).toUri(0))
                                put("user", 0)
                                put("id", it.info.id)
                                put("type", "shortcut")
                            }
                            val appGestureHandler = StartAppGestureHandler(context, config)
                            appGestureHandler.apply {
                                appName = it.label.toString()
                            }
                            ListItemWithIcon(
                                modifier = Modifier
                                    .clip(
                                        GroupItemShape(index, groupSize - 1)
                                    )
                                    .clickable {
                                        onSelect(appGestureHandler.toString())
                                    },
                                title = it.label.toString(),
                                startIcon = {
                                    Image(
                                        painter = if (it.iconDrawable != null){
                                            BitmapPainter(it.iconDrawable.toBitmap().asImageBitmap())
                                        }
                                         else painterResource(id = R.drawable.ic_widget),
                                        contentScale = ContentScale.FillBounds,
                                        contentDescription = it.label.toString(),
                                        modifier = Modifier.size(40.dp),
                                    )
                                },

                                endCheckbox = {
                                    RadioButton(
                                        selected = (appGestureHandler.toString() == selectedOption.value),
                                        onClick = {
                                            onSelect(appGestureHandler.toString())
                                        },
                                        colors = colors,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                index = index,
                                groupSize = appsSize
                            )
                            if (index < groupSize - 1) Spacer(modifier = Modifier.height(4.dp))
                        }

                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun GestureSelectorPreview() {
    val prefs = NeoPrefs.getInstance()
    GestureSelectorPage(prefs.gestureDoubleTap)
}