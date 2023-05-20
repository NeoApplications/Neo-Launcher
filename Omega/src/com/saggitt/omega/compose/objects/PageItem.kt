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
import com.saggitt.omega.compose.navigation.Routes

open class PageItem(
    @StringRes val titleId: Int,
    val icon: ImageVector? = null,
    val route: String,
) {
    companion object {
        val PrefsProfile = PageItem(
            R.string.title__general_profile,
            Phosphor.Palette,
            Routes.PREFS_PROFILE
        )
        val PrefsDesktop = PageItem(
            R.string.title__general_desktop,
            Phosphor.Monitor,
            Routes.PREFS_DESKTOP
        )
        val PrefsDock = PageItem(
            R.string.title__general_dock,
            PhosphorCustom.DeviceMobileDock,
            Routes.PREFS_DOCK
        )

        val PrefsDrawer = PageItem(
            R.string.title__general_drawer,
            Phosphor.DotsNine,
            Routes.PREFS_DRAWER
        )

        val PrefsWidgetsNotifications = PageItem(
            R.string.title__general_widgets_notifications,
            Phosphor.SquaresFour,
            Routes.PREFS_WIDGETS
        )
        val PrefsSearchFeed = PageItem(
            R.string.title__general_search_feed,
            Phosphor.MagnifyingGlass,
            Routes.PREFS_SEARCH
        )
        val PrefsGesturesDash = PageItem(
            R.string.title__general_gestures_dash,
            Phosphor.ScribbleLoop,
            Routes.PREFS_GESTURES
        )

        val PrefsBackup = PageItem(
            R.string.backups,
            Phosphor.ClockCounterClockwise,
            Routes.PREFS_BACKUPS
        )
        val PrefsDesktopMode = PageItem(
            R.string.pref_desktop_mode,
            Phosphor.Monitor,
            Routes.PREFS_DM
        )
        val PrefsDeveloper = PageItem(
            R.string.developer_options_title,
            Phosphor.BracketsCurly,
            Routes.PREFS_DEV
        )
        val PrefsAbout = PageItem(
            R.string.title__general_about,
            Phosphor.Info,
            Routes.ABOUT
        )
        val AboutTranslators = PageItem(
            titleId = R.string.about_translators,
            icon = Phosphor.Translate,
            route = Routes.TRANSLATORS,
        )
        val AboutLicense = PageItem(
            titleId = R.string.category__about_licenses,
            icon = Phosphor.Copyleft,
            route = Routes.LICENSE,
        )
        val AboutChangelog = PageItem(
            titleId = R.string.title__about_changelog,
            icon = Phosphor.ListDashes,
            route = Routes.CHANGELOG,
        )
    }
}