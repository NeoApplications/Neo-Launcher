package com.saggitt.omega.compose.navigation

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
import com.saggitt.omega.compose.pages.AppCategoriesPage
import com.saggitt.omega.compose.pages.EditIconPage
import com.saggitt.omega.compose.pages.preferences.EditDashPage
import com.saggitt.omega.compose.pages.preferences.MainPrefsPage
import com.saggitt.omega.compose.pages.preferences.SearchPrefsPage
import com.saggitt.omega.compose.pages.preferences.WidgetsPrefsPage
import com.saggitt.omega.util.getUserForProfileId

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
    preferenceGraph<NavRoute.Search>(deepLink = Routes.PREFS_SEARCH) { SearchPrefsPage() }
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