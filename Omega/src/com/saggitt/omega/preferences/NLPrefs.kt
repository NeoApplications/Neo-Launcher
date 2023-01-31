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

package com.saggitt.omega.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.android.launcher3.R
import com.android.launcher3.util.MainThreadInitializedObject
import com.saggitt.omega.util.Config
import kotlin.math.roundToInt

private const val USER_PREFERENCES_NAME = "neo_launcher"

class NLPrefs private constructor(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES_NAME)
    private val dataStore: DataStore<Preferences> = context.dataStore

    // Profile
    var themeCornerRadius = FloatPref(
        titleId = R.string.title_override_corner_radius_value,
        dataStore = dataStore,
        key = PrefKey.PROFILE_WINDOW_CORNER_RADIUS,
        defaultValue = 8f,
        maxValue = 24f,
        minValue = -1f,
        steps = 24,
        specialOutputs = {
            when {
                it < 0f -> context.getString(R.string.automatic_short)
                else    -> "${it.roundToInt()}dp"
            }
        }
    )

    // Drawer
    var drawerSortMode = IntSelectionPref(
        titleId = R.string.title__sort_mode,
        dataStore = dataStore,
        key = PrefKey.DRAWER_SORT_MODE,
        defaultValue = Config.SORT_AZ,
        entries = Config.drawerSortOptions,
    )


    companion object {
        private val INSTANCE = MainThreadInitializedObject(::NLPrefs)

        @JvmStatic
        fun getInstance(context: Context) = INSTANCE.get(context)!!

    }
}