package com.neoapps.neolauncher.compose.navigation

import android.content.ComponentName
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.android.launcher3.pm.UserCache
import com.android.launcher3.util.ComponentKey
import com.neoapps.neolauncher.compose.pages.AppCategoriesPage
import com.neoapps.neolauncher.compose.pages.EditIconPage
import com.neoapps.neolauncher.compose.pages.preferences.EditDashPage
import com.neoapps.neolauncher.compose.pages.preferences.MainPrefsPage
import com.neoapps.neolauncher.compose.pages.preferences.SearchProvidersPage
import com.neoapps.neolauncher.compose.pages.preferences.WidgetsPrefsPage
import com.neoapps.neolauncher.util.getUserForProfileId

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
    preferenceGraph<NavRoute.Widgets>(deepLink = Routes.PREFS_WIDGETS) { WidgetsPrefsPage() }
    preferenceGraph<NavRoute.Search.SearchProviders>(deepLink = Routes.PREFS_SEARCH) { SearchProvidersPage() }
    preferenceGraph<NavRoute.Gestures.EditDash>(deepLink = Routes.EDIT_DASH) { EditDashPage() }
    preferenceGraph<NavRoute.Drawer.Categorize>(deepLink = Routes.CATEGORIZE_APPS) { AppCategoriesPage() }
    preferenceGraph<NavRoute.EditIcon>(deepLink = "${Routes.EDIT_ICON}/{packageName}/{name}#{user}") {
        val args = it.toRoute<NavRoute.EditIcon>()
        val user = UserCache.INSTANCE.get(LocalContext.current)
            .getUserForProfileId(args.user)
        val key = ComponentKey(ComponentName(args.packageName, args.name), user)
        EditIconPage(key)
    }
}