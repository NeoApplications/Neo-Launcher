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

package com.saggitt.omega.compose.screens

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.shortcuts.ShortcutKey
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.saggitt.omega.compose.components.ExpandableListItem
import com.saggitt.omega.compose.components.ListItemWithIcon
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.components.preferences.PreferenceGroup
import com.saggitt.omega.compose.navigation.preferenceGraph
import com.saggitt.omega.data.AppItemWithShortcuts
import com.saggitt.omega.gestures.GestureController
import com.saggitt.omega.gestures.handlers.StartAppGestureHandler
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.util.appsList
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.gesturesPageGraph(route: String) {
    preferenceGraph(route, { }) { subRoute ->
        composable(
            route = subRoute("{titleId}/{key}/{default}"),
            arguments = listOf(
                navArgument("titleId") { type = NavType.IntType },
                navArgument("key") { type = NavType.StringType },
                navArgument("default") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            val title = args.getInt("titleId")
            val key = args.getString("key") ?: ""
            val default = args.getString("default") ?: ""
            GestureSelector(titleId = title, key = key, default = default)
        }
    }
}

@Composable
fun GestureSelector(titleId: Int, key: String, default: String) {
    ViewWithActionBar(
        title = stringResource(titleId),
    ) {
        MainGesturesScreen(key, default)
    }
}

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainGesturesScreen(key: String, default: String) {
    val pagerState = rememberPagerState()
    val prefs = Utilities.getOmegaPrefs(LocalContext.current)
    val selectedOption = remember {
        mutableStateOf(
            prefs.sharedPrefs.getString(key, default)
        )
    }

    val tabs = listOf(
        TabItem(R.drawable.ic_assistant, R.string.tab_launcher) {
            LauncherScreen(
                prefs,
                selectedOption,
                key
            )
        },
        TabItem(R.drawable.ic_apps, R.string.apps_label) { AppsScreen(prefs, selectedOption, key) },
        TabItem(R.drawable.ic_edit_dash, R.string.tab_shortcuts) {
            ShortcutsScreen(
                prefs,
                selectedOption,
                key
            )
        }
    )

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.app_name), fontSize = 18.sp) }
                )
                Tabs(tabs = tabs, pagerState = pagerState)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabsContent(tabs = tabs, pagerState = pagerState)
        }
    }
}

@Composable
fun LauncherScreen(prefs: OmegaPreferences, selectedOption: MutableState<String?>, key: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        val context = LocalContext.current
        val launcherItems = GestureController.getGestureHandlers(context, true, true)
        val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        val colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.onPrimary,
            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val groupSize = launcherItems.size

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
                                selectedOption.value = item.toString()
                                prefs.sharedPrefs
                                    .edit()
                                    .putString(key, selectedOption.value)
                                    .apply()
                                backDispatcher?.onBackPressed()
                            },
                        summary = "",
                        startIcon = {
                            val bitmap = item.icon?.toBitmap(32, 32, null)
                            if (bitmap != null) Icon(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                            )
                            else Spacer(modifier = Modifier.size(32.dp))
                        },
                        endCheckbox = {
                            RadioButton(
                                selected = (item.toString() == selectedOption.value),
                                onClick = {
                                    selectedOption.value = item.toString()
                                    prefs.sharedPrefs.edit()
                                        .putString(key, selectedOption.value)
                                        .apply()
                                    backDispatcher?.onBackPressed()
                                },
                                colors = colors
                            )
                        },
                        verticalPadding = 4.dp
                    )
                }
            }
        }
    }
}

@Composable
fun AppsScreen(prefs: OmegaPreferences, selectedHandler: MutableState<String?>, key: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        val context = LocalContext.current
        val apps = appsList().value
        val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        val colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.onPrimary,
            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val selectedOption = remember {
            mutableStateOf(selectedHandler.value)
        }
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
                                selectedOption.value = appGestureHandler.toString()
                                prefs.sharedPrefs
                                    .edit()
                                    .putString(key, selectedOption.value)
                                    .apply()
                                backDispatcher?.onBackPressed()
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
                                    selectedOption.value = appGestureHandler.toString()
                                    prefs.sharedPrefs.edit()
                                        .putString(key, selectedOption.value)
                                        .apply()
                                    backDispatcher?.onBackPressed()
                                },
                                colors = colors
                            )
                        },
                        verticalPadding = 4.dp
                    )
                }
            }
        }
    }
}

@Composable
fun ShortcutsScreen(prefs: OmegaPreferences, selectedHandler: MutableState<String?>, key: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        val context = LocalContext.current
        val apps = appsList().value
            .sortedBy { it.label.toString() }
            .map { AppItemWithShortcuts(context, it) }
        val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

        val colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.onPrimary,
            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val selectedOption = remember {
            mutableStateOf(selectedHandler.value)
        }
        val appsNum = apps.size

        PreferenceGroup {
            LazyColumn {
                itemsIndexed(apps) { index, app ->
                    if (app.hasShortcuts) {
                        var expanded by remember { mutableStateOf(false) }

                        ExpandableListItem(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = if (index == 0) 16.dp else 6.dp,
                                        topEnd = if (index == 0) 16.dp else 6.dp,
                                        bottomStart = if (index == appsNum - 1) 16.dp else 6.dp,
                                        bottomEnd = if (index == appsNum - 1) 16.dp else 6.dp
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

                            app.shortcuts.forEachIndexed { iIndex, it ->

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
                                                topStart = if (iIndex == 0) 16.dp else 6.dp,
                                                topEnd = if (iIndex == 0) 16.dp else 6.dp,
                                                bottomStart = if (iIndex == groupSize - 1) 16.dp else 6.dp,
                                                bottomEnd = if (iIndex == groupSize - 1) 16.dp else 6.dp
                                            )
                                        )
                                        .background(
                                            color = if (appGestureHandler.toString() == selectedOption.value)
                                                MaterialTheme.colorScheme.primary.copy(0.4f)
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .clickable {
                                            selectedOption.value = appGestureHandler.toString()
                                            prefs.sharedPrefs
                                                .edit()
                                                .putString(key, selectedOption.value)
                                                .apply()
                                            backDispatcher?.onBackPressed()
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
                                    verticalPadding = 4.dp,
                                    horizontalPadding = 8.dp,
                                    endCheckbox = {
                                        RadioButton(
                                            selected = (appGestureHandler.toString() == selectedOption.value),
                                            onClick = {
                                                selectedOption.value = appGestureHandler.toString()
                                                prefs.sharedPrefs.edit()
                                                    .putString(key, selectedOption.value)
                                                    .apply()
                                                backDispatcher?.onBackPressed()
                                            },
                                            colors = colors
                                        )
                                    }
                                )
                                if (iIndex < groupSize - 1) Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                        if (index < appsNum - 1) Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Tabs(tabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = Color.White,
        indicator = {},
        divider = {}
    ) {
        tabs.forEachIndexed { index, tab ->
            NavigationRailItem(
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = tab.icon),
                        contentDescription = ""
                    )
                },
                label = {
                    Text(text = stringResource(id = tab.title))
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                    unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabsContent(tabs: List<TabItem>, pagerState: PagerState) {
    HorizontalPager(state = pagerState, count = tabs.size) { page ->
        tabs[page].screen()
    }
}

typealias ComposableFun = @Composable () -> Unit

class TabItem(var icon: Int, var title: Int, var screen: ComposableFun)