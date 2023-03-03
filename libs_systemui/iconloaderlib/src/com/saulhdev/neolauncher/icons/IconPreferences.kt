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

package com.saulhdev.neolauncher.icons

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey

class IconPreferences(context: Context) {
    //private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "neo_launcher")
    //private val dataStore: DataStore<Preferences> = context.dataStore

    fun shouldTransparentBGIcons(): Boolean {
        return false;
    }

    /*fun shouldTransparentBGIcons(): Boolean {
        return runBlocking(Dispatchers.IO) {
            shouldTransparentIcons().firstOrNull() ?: false
        }
    }

    private fun shouldTransparentIcons(): Flow<Boolean> {
        return dataStore.data.map { it[TRANSPARENT_ICON] ?: false }
    }*/

    fun getThemedIcons(): Boolean {
        return true
    }

    /*fun getThemedIcons(): Boolean {
        return runBlocking(Dispatchers.IO) {
            getEnableThemedIcons().firstOrNull() ?: false
        }
    }

    private fun getEnableThemedIcons(): Flow<Boolean> {
        return dataStore.data.map { it[THEMED_ICON] ?: false }
    }*/

    companion object {
        val THEMED_ICON = booleanPreferencesKey("profile_themed_icons")
        val TRANSPARENT_ICON = booleanPreferencesKey("profile_transparent_icon")
    }
}
