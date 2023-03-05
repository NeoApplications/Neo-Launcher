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

import android.util.Log
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TabRow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.shortcuts.ShortcutKey
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.saggitt.omega.compose.components.ExpandableListItem
import com.saggitt.omega.compose.components.ListItemWithIcon
import com.saggitt.omega.compose.components.TabItem
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.components.preferences.PreferenceGroup
import com.saggitt.omega.compose.navigation.preferenceGraph
import com.saggitt.omega.data.AppItemWithShortcuts
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.gestures.handlers.StartAppGestureHandler
import com.saggitt.omega.preferences.NavigationPref
import com.saggitt.omega.util.App
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.appsState
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.gesturesPageGraph(route: String) {
    preferenceGraph(route, { }) { subRoute ->
        composable(
            route = subRoute("{key}"),
            arguments = listOf(
                navArgument("key") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            val key = stringPreferencesKey(args.getString("key")!!)
            val prefs = Config.gesturePrefs(LocalContext.current)
            Log.d("GestureSelector", "key: $key")
            val gesture = prefs.first { it.key == key }
            GestureSelectorPage(prefs = gesture)
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun GestureSelectorPage(prefs: NavigationPref) {

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState()

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

    ViewWithActionBar(
        title = stringResource(prefs.titleId),
    ) {
        Column(
            modifier = Modifier
                .padding(
                    top = it.calculateTopPadding() - 1.dp,
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
                backgroundColor = MaterialTheme.colorScheme.background
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
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = tab.icon),
                                contentDescription = stringResource(id = tab.title),
                                tint = if (pagerState.currentPage == index)
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

@Composable
fun LauncherScreen(
    selectedOption: MutableState<String>,
    onSelect: (String) -> Unit
) {
    val context = LocalContext.current
    val launcherItems = GestureController.getGestureHandlers(context, true, true)

    val groupSize = launcherItems.size
    val colors = RadioButtonDefaults.colors(
        selectedColor = MaterialTheme.colorScheme.primary,
        unselectedColor = MaterialTheme.colorScheme.onPrimary
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 4.dp, end = 4.dp)
    ) {
        PreferenceGroup {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(launcherItems) { index, item ->
                    ListItemWithIcon(
                        title = item.displayName,
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = if (index == 0) 16.dp else 6.dp,
                                    topEnd = if (index == 0) 16.dp else 6.dp,
                                    bottomStart = if (index == groupSize - 1) 16.dp else 6.dp,
                                    bottomEnd = if (index == groupSize - 1) 16.dp else 6.dp
                                )
                            )
                            .background(
                                color = if (item.toString() == selectedOption.value)
                                    MaterialTheme.colorScheme.primary.copy(0.4f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                onSelect(item.toString())
                            },
                        summary = "",
                        startIcon = {
                            Icon(
                                painter = rememberDrawablePainter(drawable = item.icon),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(30.dp)
                                    .zIndex(1f),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        endCheckbox = {
                            RadioButton(
                                selected = (item.toString() == selectedOption.value),
                                onClick = {
                                    onSelect(item.toString())
                                },
                                colors = colors
                            )
                        },
                        horizontalPadding = 0.dp,
                        verticalPadding = 4.dp
                    )
                }
            }
        }
    }
}

@Composable
fun AppsScreen(
    apps: List<App>,
    selectedOption: MutableState<String>,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 4.dp, end = 4.dp)
    ) {
        val context = LocalContext.current
        val colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.onPrimary,
            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val groupSize = apps.size

        PreferenceGroup {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                        title = item.label,
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = if (index == 0) 16.dp else 6.dp,
                                    topEnd = if (index == 0) 16.dp else 6.dp,
                                    bottomStart = if (index == groupSize - 1) 16.dp else 6.dp,
                                    bottomEnd = if (index == groupSize - 1) 16.dp else 6.dp
                                )
                            )
                            .background(
                                color = if (appGestureHandler.toString() == selectedOption.value)
                                    MaterialTheme.colorScheme.primary.copy(0.4f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                onSelect(appGestureHandler.toString())
                            },
                        summary = "",
                        startIcon = {
                            Image(
                                painter = BitmapPainter(item.icon.asImageBitmap()),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                        },
                        endCheckbox = {
                            RadioButton(
                                selected = (appGestureHandler.toString() == selectedOption.value),
                                onClick = {
                                    onSelect(appGestureHandler.toString())
                                },
                                colors = colors
                            )
                        },
                        horizontalPadding = 0.dp,
                        verticalPadding = 4.dp
                    )
                }
            }
        }
    }
}

@Composable
fun ShortcutsScreen(
    apps: List<App>,
    selectedOption: MutableState<String>,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 4.dp, end = 4.dp)
    ) {
        val context = LocalContext.current
        var appsWithShortcuts by remember { mutableStateOf(emptyList<AppItemWithShortcuts>()) }

        if (apps.isNotEmpty()) {
            appsWithShortcuts = apps
                .map { AppItemWithShortcuts(context, it) }
                .filter { it.hasShortcuts }
        }

        val colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.onPrimary,
            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val appsSize = appsWithShortcuts.size
        PreferenceGroup {
            LazyColumn {
                itemsIndexed(appsWithShortcuts) { appIndex, app ->
                    var expanded by remember { mutableStateOf(false) }

                    val rank = (appIndex + 1f) / appsSize
                    val base = appIndex.toFloat() / appsSize
                    ExpandableListItem(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = if (base == 0f) 16.dp else 6.dp,
                                    topEnd = if (base == 0f) 16.dp else 6.dp,
                                    bottomStart = if (rank == 1f) 16.dp else 6.dp,
                                    bottomEnd = if (rank == 1f) 16.dp else 6.dp
                                )
                            )
                            .background(
                                if (expanded) MaterialTheme.colorScheme.background
                                else MaterialTheme.colorScheme.surface
                            ),
                        title = app.info.label,
                        icon = app.info.icon,
                        onClick = { expanded = !expanded }
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
                                title = it.label.toString(),
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = if (index == 0) 16.dp else 6.dp,
                                            topEnd = if (index == 0) 16.dp else 6.dp,
                                            bottomStart = if (index == groupSize - 1) 16.dp else 6.dp,
                                            bottomEnd = if (index == groupSize - 1) 16.dp else 6.dp
                                        )
                                    )
                                    .background(
                                        color = if (appGestureHandler.toString() == selectedOption.value)
                                            MaterialTheme.colorScheme.primary.copy(0.4f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable {
                                        onSelect(appGestureHandler.toString())
                                    },
                                summary = "",
                                startIcon = {
                                    Image(
                                        painter = BitmapPainter(
                                            it.iconDrawable.toBitmap(
                                                32,
                                                32,
                                                null
                                            ).asImageBitmap()
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp)
                                    )
                                },
                                horizontalPadding = 0.dp,
                                verticalPadding = 0.dp,
                                endCheckbox = {
                                    RadioButton(
                                        selected = (appGestureHandler.toString() == selectedOption.value),
                                        onClick = {
                                            onSelect(appGestureHandler.toString())
                                        },
                                        colors = colors
                                    )
                                }
                            )
                            if (index < groupSize - 1) Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                    if (appIndex < appsSize - 1) Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
@Preview
fun GestureSelectorPreview() {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    GestureSelectorPage(prefs.gestureDoubleTap)
}