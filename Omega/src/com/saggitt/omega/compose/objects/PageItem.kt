package com.saggitt.omega.compose.objects

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.launcher3.R
import com.saggitt.omega.compose.icons.Phosphor
import com.saggitt.omega.compose.icons.PhosphorCustom
import com.saggitt.omega.compose.icons.custom.DeviceMobileDock
import com.saggitt.omega.compose.icons.phosphor.BracketsCurly
import com.saggitt.omega.compose.icons.phosphor.ClockCounterClockwise
import com.saggitt.omega.compose.icons.phosphor.Copyleft
import com.saggitt.omega.compose.icons.phosphor.DotsNine
import com.saggitt.omega.compose.icons.phosphor.Info
import com.saggitt.omega.compose.icons.phosphor.ListDashes
import com.saggitt.omega.compose.icons.phosphor.MagnifyingGlass
import com.saggitt.omega.compose.icons.phosphor.Monitor
import com.saggitt.omega.compose.icons.phosphor.Palette
import com.saggitt.omega.compose.icons.phosphor.ScribbleLoop
import com.saggitt.omega.compose.icons.phosphor.SquaresFour
import com.saggitt.omega.compose.icons.phosphor.Translate
import com.saggitt.omega.compose.navigation.NavRoute

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