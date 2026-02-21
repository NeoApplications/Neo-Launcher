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
package com.neoapps.neolauncher.compose.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

object Routes {
    const val PREFS_MAIN = "prefs_main"
    const val PREFS_WIDGETS = "prefs_widgets"
    const val PREFS_SEARCH = "prefs_search"
    const val EDIT_ICON = "edit_icon"
    const val CATEGORIZE_APPS = "categorize_apps"
    const val EDIT_DASH = "edit_dash"
}

val LocalNavController = staticCompositionLocalOf<NavController> {
    error("CompositionLocal LocalNavController not present")
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
val LocalPaneNavigator = staticCompositionLocalOf<ThreePaneScaffoldNavigator<Any>> {
    error("CompositionLocal LocalPaneNavigator not present")
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun PrefsComposeView(
    navController: NavHostController,
    paneNavigator: ThreePaneScaffoldNavigator<Any>,
) {
    CompositionLocalProvider(
        //LocalNavController provides navController,
        LocalPaneNavigator provides paneNavigator,
    ) {
        NavHost(
            navController = navController,
            startDestination = NavRoute.Main(),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start)
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start)
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End)
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End)
            },
            builder = {
                prefsGraph()
            }
        )
    }
}