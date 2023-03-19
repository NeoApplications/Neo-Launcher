/*
 * This file is part of Neo Launcher
 * Copyright (c) 2021   Neo Launcher Team
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

package com.saggitt.omega.util

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.text.TextUtils
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.preferences.NavigationPref
import com.saggitt.omega.smartspace.BlankDataProvider
import com.saggitt.omega.smartspace.SmartSpaceDataWidget
import com.saggitt.omega.smartspace.eventprovider.AlarmEventProvider
import com.saggitt.omega.smartspace.eventprovider.BatteryStatusProvider
import com.saggitt.omega.smartspace.eventprovider.CalendarEventProvider
import com.saggitt.omega.smartspace.eventprovider.NotificationUnreadProvider
import com.saggitt.omega.smartspace.eventprovider.NowPlayingProvider
import com.saggitt.omega.smartspace.eventprovider.PersonalityProvider
import com.saggitt.omega.smartspace.weather.OWMWeatherDataProvider
import com.saggitt.omega.smartspace.weather.PEWeatherDataProvider
import com.saggitt.omega.theme.ThemeOverride
import java.util.Locale

class Config(val context: Context) {

    //TODO: Use ContextWrapper instead of UpdateConfiguration
    fun setAppLanguage(languageCode: String) {
        val locale = getLocaleByAndroidCode(languageCode)
        val config = context.resources.configuration
        val mLocale =
            if (languageCode.isNotEmpty()) locale else Resources.getSystem().configuration.locales[0]
        config.setLocale(mLocale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getLocaleByAndroidCode(languageCode: String): Locale {
        return if (!TextUtils.isEmpty(languageCode)) {
            if (languageCode.contains("-r")) Locale(
                languageCode.substring(0, 2),
                languageCode.substring(4, 6)
            ) // de-rAt
            else Locale(languageCode) // de
        } else Resources.getSystem().configuration.locales[0]
    }

    companion object {
        //PERMISSION FLAGS
        const val REQUEST_PERMISSION_STORAGE_ACCESS = 666
        const val REQUEST_PERMISSION_LOCATION_ACCESS = 667
        const val REQUEST_PERMISSION_READ_CONTACTS = 668

        const val GOOGLE_QSB = "com.google.android.googlequicksearchbox"

        //APP DRAWER SORT MODE
        const val SORT_AZ = 0
        const val SORT_ZA = 1
        const val SORT_MOST_USED = 2
        const val SORT_BY_COLOR = 3
        const val SORT_BY_INSTALL_DATE = 4

        //COMPOSE THEME COLORS
        const val THEME_LIGHT = 0
        const val THEME_DARK = 1
        const val THEME_BLACK = 2

        val drawerSortOptions = mutableMapOf(
            SORT_AZ to R.string.title__sort_alphabetical_az,
            SORT_ZA to R.string.title__sort_alphabetical_za,
            SORT_MOST_USED to R.string.title__sort_most_used,
            SORT_BY_COLOR to R.string.title__sort_by_color,
            SORT_BY_INSTALL_DATE to R.string.title__sort_last_installed,
        )

        const val LAWNICONS_PACKAGE_NAME = "app.lawnchair.lawnicons"
        const val THEME_ICON_THEMED = "themed"

        val ICON_INTENTS = arrayOf(
            Intent("com.novalauncher.THEME"),
            Intent("org.adw.launcher.THEMES"),
            Intent("org.adw.launcher.icons.ACTION_PICK_ICON"),
            Intent("com.anddoes.launcher.THEME"),
            Intent("com.teslacoilsw.launcher.THEME"),
            Intent("com.fede.launcher.THEME_ICONPACK"),
            Intent("com.gau.go.launcherex.theme"),
            Intent("com.dlto.atom.launcher.THEME"),
        )

        val smartspaceEventProviders = mapOf(
            NotificationUnreadProvider::class.java.name to R.string.event_provider_unread_notifications,
            NowPlayingProvider::class.java.name to R.string.event_provider_now_playing,
            BatteryStatusProvider::class.java.name to R.string.battery_status,
            PersonalityProvider::class.java.name to R.string.personality_provider,
            CalendarEventProvider::class.java.name to R.string.smartspace_provider_calendar,
            SmartSpaceDataWidget::class.java.name to R.string.title_smartspace_widget_provider,
            AlarmEventProvider::class.java.name to R.string.name_provider_alarm_events
        )

        fun smartspaceWeatherProviders(context: Context) = listOfNotNull(
            BlankDataProvider::class.java.name,
            SmartSpaceDataWidget::class.java.name,
            OWMWeatherDataProvider::class.java.name,
            if (PEWeatherDataProvider.isAvailable(context)) PEWeatherDataProvider::class.java.name else null
        )

        fun calendarOptions(context: Context) = mapOf(
            context.resources.getString(R.string.smartspace_calendar_gregorian) to context.resources.getString(
                R.string.title_calendar_gregorian
            ),
            context.resources.getString(R.string.smartspace_calendar_persian) to context.resources.getString(
                R.string.title_calendar_persian
            )
        )

        fun getCurrentTheme(context: Context): Int {
            val themeSet = ThemeOverride.Settings()
            var currentTheme = THEME_LIGHT
            if (themeSet.getTheme(context) == R.style.SettingsTheme_Dark) {
                currentTheme = THEME_DARK
            } else if (themeSet.getTheme(context) == R.style.SettingsTheme_Black) {
                currentTheme = THEME_BLACK
            }
            return currentTheme
        }

        fun gesturePrefs(context: Context): List<NavigationPref> {
            val prefs = Utilities.getOmegaPrefs(context)
            return listOf(
                prefs.gestureDoubleTap,
                prefs.gestureLongPress,
                prefs.gestureSwipeDown,
                prefs.gestureSwipeUp,
                prefs.gestureDockSwipeUp,
                prefs.gestureHomePress,
                prefs.gestureBackPress,
            )
        }
    }
}