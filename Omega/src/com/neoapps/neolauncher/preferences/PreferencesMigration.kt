package com.neoapps.neolauncher.preferences

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

//TODO: create all key pairs when the migration to android 13 is complete
class PreferencesMigration(val context: Context) {
    private val sharedPref = "com.saggitt.omega._preferences.xml"

    private val keys = mapOf(
            "pref_home_icon_scale" to "desktop_icon_scale",
            "pref_hide_app_label" to "desktop_labels_hide"
    )

    fun produceMigration() = SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = sharedPref,
            keysToMigrate = keys.keys,
            shouldRunMigration = getShouldRunMigration(),
            migrate = migrationWorker(),
    )

    private fun getShouldRunMigration(): suspend (Preferences) -> Boolean = { preferences ->
        val allKeys = preferences.asMap().keys.map { it.name }
        keys.values.any { it !in allKeys }
    }

    private fun migrationWorker(): suspend (SharedPreferencesView, Preferences) -> Preferences =
            { sharedPreferences: SharedPreferencesView, currentData: Preferences ->
                val currentKeys = currentData.asMap().keys.map { it.name }
                val migratedKeys = currentKeys.mapNotNull { currentKey -> keys.entries.find { entry -> entry.value == currentKey }?.key }
                val filteredSharedPreferences = sharedPreferences.getAll().filter { (key, _) -> key !in migratedKeys }
                val mutablePreferences = currentData.toMutablePreferences()

                for ((key, value) in filteredSharedPreferences) {
                    val newKey = keys[key] ?: key
                    when (value) {
                        is Boolean -> mutablePreferences[booleanPreferencesKey(newKey)] = value
                        is Float -> mutablePreferences[floatPreferencesKey(newKey)] = value
                        is Int -> mutablePreferences[intPreferencesKey(newKey)] = value
                        is String -> mutablePreferences[stringPreferencesKey(newKey)] = value
                        is Set<*> -> {
                            mutablePreferences[stringSetPreferencesKey(newKey)] = value as Set<String>
                        }
                    }
                }

                mutablePreferences.toPreferences()
            }
}