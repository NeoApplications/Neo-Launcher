package com.neoapps.neolauncher.compose.objects

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.icons.Phosphor
import com.neoapps.neolauncher.compose.icons.PhosphorCustom
import com.neoapps.neolauncher.compose.icons.custom.DeviceMobileDock
import com.neoapps.neolauncher.compose.icons.phosphor.BracketsCurly
import com.neoapps.neolauncher.compose.icons.phosphor.ClockCounterClockwise
import com.neoapps.neolauncher.compose.icons.phosphor.Copyleft
import com.neoapps.neolauncher.compose.icons.phosphor.DotsNine
import com.neoapps.neolauncher.compose.icons.phosphor.Info
import com.neoapps.neolauncher.compose.icons.phosphor.ListDashes
import com.neoapps.neolauncher.compose.icons.phosphor.MagnifyingGlass
import com.neoapps.neolauncher.compose.icons.phosphor.Monitor
import com.neoapps.neolauncher.compose.icons.phosphor.Palette
import com.neoapps.neolauncher.compose.icons.phosphor.ScribbleLoop
import com.neoapps.neolauncher.compose.icons.phosphor.SquaresFour
import com.neoapps.neolauncher.compose.icons.phosphor.Translate
import com.neoapps.neolauncher.compose.navigation.NavRoute

open class PageItem(
    @StringRes val titleId: Int,
    val icon: ImageVector? = null,
    val route: NavRoute,
) {
    companion object {
        val PrefsProfile = PageItem(
            titleId = R.string.title__general_profile,
            icon = Phosphor.Palette,
            route = NavRoute.Profile()
        )
        val PrefsDesktop = PageItem(
            titleId = R.string.title__general_desktop,
            icon = Phosphor.Monitor,
            route = NavRoute.Desktop()
        )
        val PrefsDock = PageItem(
            titleId = R.string.title__general_dock,
            icon = PhosphorCustom.DeviceMobileDock,
            route = NavRoute.Dock()
        )
        val PrefsDrawer = PageItem(
            titleId = R.string.title__general_drawer,
            icon = Phosphor.DotsNine,
            route = NavRoute.Drawer()
        )
        val PrefsWidgetsNotifications = PageItem(
            titleId = R.string.title__general_widgets_notifications,
            icon = Phosphor.SquaresFour,
            route = NavRoute.Widgets()
        )
        val PrefsSearchFeed = PageItem(
            titleId = R.string.title__general_search_feed,
            icon = Phosphor.MagnifyingGlass,
            route = NavRoute.Search()
        )
        val PrefsGesturesDash = PageItem(
            titleId = R.string.title__general_gestures_dash,
            icon = Phosphor.ScribbleLoop,
            route = NavRoute.Gestures()
        )
        val PrefsBackup = PageItem(
            titleId = R.string.backups,
            icon = Phosphor.ClockCounterClockwise,
            route = NavRoute.Backup()
        )
        val PrefsDeveloper = PageItem(
            titleId = R.string.developer_options_title,
            icon = Phosphor.BracketsCurly,
            route = NavRoute.Dev()
        )
        val PrefsAbout = PageItem(
            titleId = R.string.title__general_about,
            icon = Phosphor.Info,
            route = NavRoute.About()
        )
        val AboutTranslators = PageItem(
            titleId = R.string.about_translators,
            icon = Phosphor.Translate,
            route = NavRoute.About.Translators()
        )
        val AboutLicense = PageItem(
            titleId = R.string.category__about_licenses,
            icon = Phosphor.Copyleft,
            route = NavRoute.About.License()
        )
        val AboutChangelog = PageItem(
            titleId = R.string.title__about_changelog,
            icon = Phosphor.ListDashes,
            route = NavRoute.About.Changelog()
        )
    }
}