/*
 * This file is part of Neo Launcher
 * Copyright (c) 2025   Neo Launcher Team
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

package com.neoapps.launcher.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.neoapps.launcher.views.preferences.MainPrefsPage

const val NAV_BASE = "nl-navigation://androidx.navigation/"

inline fun <reified T : Any> NavGraphBuilder.preferenceGraph(
    deepLink: String,
    crossinline root: @Composable (NavBackStackEntry) -> Unit,
) {
    composable<T>(
        deepLinks = listOf(navDeepLink {
            uriPattern = "$NAV_BASE$deepLink"
        })
    ) {
        root(it)
    }
}

fun NavGraphBuilder.prefsGraph() {
    preferenceGraph<NavRoute.Main>(deepLink = Routes.PREFS_MAIN) { MainPrefsPage() }
}