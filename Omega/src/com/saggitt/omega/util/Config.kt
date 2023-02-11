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
import android.content.res.Resources
import android.text.TextUtils
import com.android.launcher3.R
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

    }
}