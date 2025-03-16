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
 */

package com.neoapps.launcher.preferences

import android.content.Context
import com.android.launcher3.R
import com.neoapps.launcher.navigation.NavRoute
import com.neoapps.launcher.search.SearchProviderController
import com.neoapps.launcher.util.Config
import org.koin.java.KoinJavaComponent.getKoin

class NeoPrefs(context: Context) : NeoBasePreferences(context) {
    /*THEME Preferences*/
    val profileAccentColor = ColorIntPref(
        dataStore = dataStore,
        titleId = R.string.title__theme_accent_color,
        key = PrefKey.PROFILE_ACCENT_COLOR,
        defaultValue = "system_accent",
        navRoute = NavRoute.Profile.AccentColor(),
    )

    /*HOME Preferences*/
    /*DOCK Preferences*/

    /*DRAWER Preferences*/
    var drawerSortApps = IntSelectionPref(
        dataStore = dataStore,
        titleId = R.string.title__sort_mode,
        key = PrefKey.DRAWER_SORT_MODE,
        defaultValue = Config.SORT_AZ,
        entries = Config.drawerSortOptions,
        onChange = { reloadGrid() },
    )
    var drawerEnableProtectedApps = BooleanPref(
        dataStore = dataStore,
        key = PrefKey.DRAWER_PROTECTED_APPS_ENABLED,
        titleId = R.string.enable_protected_apps,
        defaultValue = false
    )

    var drawerProtectedAppsSet = StringSetPref(
        dataStore = dataStore,
        titleId = R.string.protected_apps,
        key = PrefKey.DRAWER_PROTECTED_APPS_LIST,
        navRoute = NavRoute.Drawer.ProtectedApps(),
        defaultValue = setOf(),
        onChange = { reloadGrid() }
    )

    /*WIDGET Preferences*/

    /*SEARCH Preferences*/
    var searchProviders = LongMultiSelectionPref(
        dataStore = dataStore,
        titleId = R.string.title_search_providers,
        key = PrefKey.SEARCH_PROVIDERS,
        defaultValue = setOf(1L),
        entries = { SearchProviderController.getSearchProvidersMap() },
    )


    /*GESTURE Preferences*/
    /*DEV Preferences*/
    /*ABOUT Preferences*/
    companion object {
        @JvmStatic
        fun getInstance(): NeoPrefs = getKoin().get()

    }
}