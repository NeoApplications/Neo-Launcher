/*
 * This file is part of Neo Launcher
 * Copyright (c) 2024   Neo Launcher Team
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
 */

package com.neoapps.neolauncher.util

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.android.launcher3.LauncherFiles
import kotlin.collections.iterator

class CustomPreferencesMigration(val context: Context) {
    private val sharedPreferencesName = LauncherFiles.SHARED_PREFERENCES_KEY

    private val oldPreferences = mapOf(
        "pref_language" to "profile_language",
        "pref_launcherTheme" to "profile_launcher_theme",
        "pref_accent_color" to "profile_accent_color",
        "pref_iconShape" to "profile_icon_shape",
        "pref_icon_pack_package" to "profile_icon_pack",
        "pref_force_shape_less" to "profile_icon_shape_less",
        "pref_enableBlur" to "profile_blur_enabled",
        "pref_blurRadius" to "profile_blur_radius",
        "pref_colored_background" to "profile_icon_colored_background",
        "pref_adaptive_icon_pack" to "profile_icon_adaptify",
        "pref_customWindowCornerRadius" to "profile_custom_window_corner_radius",
        "pref_home_icon_scale" to "desktop_icon_scale",
        "pref_apps_categorization" to "drawer_categorization",
        "pref_drawer_no_categorization" to "pref_drawer_no_categorization",
        "pref_drawer_tabs" to "pref_drawer_tabs",
        "pref_drawer_flowerpot" to "pref_drawer_flowerpot",
        "pref_drawer_folders" to "pref_drawer_folders"
    )

    fun preferencesMigration() = SharedPreferencesMigration(
        context = context,
        sharedPreferencesName = sharedPreferencesName,
        keysToMigrate = oldPreferences.keys,
        shouldRunMigration = getShouldRunMigration(),
        migrate = preferencesMigrationFunction(),
    )

    private fun getShouldRunMigration(): suspend (Preferences) -> Boolean = { preferences ->
        val allKeys = preferences.asMap().keys.map { it.name }
        oldPreferences.values.any { it !in allKeys }
    }

    private fun preferencesMigrationFunction(): suspend (SharedPreferencesView, Preferences) -> Preferences =
        { sharedPreferences: SharedPreferencesView, currentData: Preferences ->
            val currentKeys = currentData.asMap().keys.map { it.name }
            val migratedKeys =
                currentKeys.mapNotNull { currentKey -> oldPreferences.entries.find { entry -> entry.value == currentKey }?.key }
            val filteredSharedPreferences =
                sharedPreferences.getAll().filter { (key, _) -> key !in migratedKeys }
            val mutablePreferences = currentData.toMutablePreferences()

            for ((key, value) in filteredSharedPreferences) {
                val newKey = oldPreferences[key] ?: key
                when (value) {
                    is Boolean -> mutablePreferences[booleanPreferencesKey(newKey)] = value
                    is Float -> mutablePreferences[floatPreferencesKey(newKey)] = value
                    is Int -> mutablePreferences[intPreferencesKey(newKey)] = value
                    is Long -> mutablePreferences[longPreferencesKey(newKey)] = value
                    is String -> mutablePreferences[stringPreferencesKey(newKey)] = value
                    is Set<*> -> {
                        mutablePreferences[stringSetPreferencesKey(newKey)] = value as Set<String>
                    }
                }
            }

            mutablePreferences.toPreferences()
        }
}