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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.neoapps.neolauncher.preferences

import android.content.Context
import android.content.SharedPreferences
import com.android.launcher3.LauncherFiles

class LegacyPreferences(context: Context) {
    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(LauncherFiles.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
    var editor = sharedPreferences.edit()

    fun getStringPreference(key: String, default: String): String? {
        return sharedPreferences.getString(key, default)
    }

    fun savePreference(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun savePreference(key: String, value: String) {
        editor.putString(key, value)
        editor.apply()
    }

    fun sharedPref(): SharedPreferences {
        return sharedPreferences
    }
}