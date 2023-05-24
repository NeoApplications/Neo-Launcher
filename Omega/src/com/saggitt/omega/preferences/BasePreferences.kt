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
import android.content.Intent
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.android.launcher3.InvariantDeviceProfile
import com.saggitt.omega.theme.AccentColorOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import kotlin.reflect.KProperty

open class BooleanPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<Boolean>,
    private val defaultValue: Boolean = false,
    val onChange: (Boolean) -> Unit = {}
) : PrefDelegate<Boolean>(titleId, summaryId, dataStore, key, defaultValue) {

    override fun get(): Flow<Boolean> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    override suspend fun set(value: Boolean) {
        onChange(value)
        dataStore.edit { it[key] = value }
    }
}

open class IntPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<Int>,
    val defaultValue: Int = -1,
    val minValue: Int = 0,
    val maxValue: Int = "FFFFFF".toInt(16),
    val steps: Int = 1,
    val specialOutputs: ((Int) -> String) = Int::toString,
) : PrefDelegate<Int>(titleId, summaryId, dataStore, key, defaultValue) {

    override fun get(): Flow<Int> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    override suspend fun set(value: Int) {
        dataStore.edit { it[key] = value }
    }
}

class IdpIntPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    val key: Preferences.Key<Int>,
    val selectDefaultValue: InvariantDeviceProfile.GridOption.() -> Int,
    val specialOutputs: ((Int) -> String) = Int::toString,
    val defaultValue: Int = 0,
    val minValue: Float,
    val maxValue: Float,
    val steps: Int,
    onChange: () -> Unit = {},
) : PrefDelegate<Int>(titleId, summaryId, dataStore, key, defaultValue) {

    override fun get(): Flow<Int> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    override suspend fun set(value: Int) {
        dataStore.edit { it[key] = value }
    }

    fun defaultValue(defaultGrid: InvariantDeviceProfile.GridOption): Int {
        return selectDefaultValue(defaultGrid)
    }

    fun get(defaultGrid: InvariantDeviceProfile.GridOption): Int {
        val value = getValue()
        return if (value == -1 || value == -0) {
            selectDefaultValue(defaultGrid)
        } else {
            value
        }
    }

    fun set(newValue: Int, defaultGrid: InvariantDeviceProfile.GridOption) {
        if (newValue == selectDefaultValue(defaultGrid)) {
            setValue(-1)
        } else {
            setValue(newValue)
        }
    }

}

open class IntentLauncherPref(
    private val dataStore: DataStore<Preferences>,
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val key: Preferences.Key<Boolean>,
    @StringRes val positiveAnswerId: Int = -1,
    val defaultValue: Boolean = false,
    val intent: () -> Intent,
    val getter: () -> Boolean,
) : PrefDelegate<Boolean>(titleId, summaryId, dataStore, key, defaultValue) {
    override fun get(): Flow<Boolean> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    override suspend fun set(value: Boolean) {
        dataStore.edit { it[key] = value }
    }
}

open class FloatPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<Float>,
    val defaultValue: Float = 0f,
    val minValue: Float,
    val maxValue: Float,
    val steps: Int,
    val specialOutputs: ((Float) -> String) = Float::toString,
) : PrefDelegate<Float>(titleId, summaryId, dataStore, key, defaultValue) {

    override fun get(): Flow<Float> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    override suspend fun set(value: Float) {
        dataStore.edit { it[key] = value }
    }
}

open class IntSelectionPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<Int>,
    val defaultValue: Int = 0,
    val entries: Map<Int, Int>,
) :
    PrefDelegate<Int>(titleId, summaryId, dataStore, key, defaultValue) {

    override fun get(): Flow<Int> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    override suspend fun set(value: Int) {
        dataStore.edit { it[key] = value }
    }
}

open class ColorIntPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    val defaultValue: String = "system_accent",
    private val key: Preferences.Key<String>,
    val navRoute: String = ""
) : PrefDelegate<String>(titleId, summaryId, dataStore, key, defaultValue) {
    override fun get(): Flow<String> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    override suspend fun set(value: String) {
        dataStore.edit { it[key] = value }
    }

    fun getColor(): Int {
        return runBlocking(Dispatchers.IO) {
            AccentColorOption.fromString(get().firstOrNull() ?: defaultValue).accentColor
        }
    }
}

open class NavigationPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    val key: Preferences.Key<String>,
    val defaultValue: String = "",
    val onClick: (() -> Unit)? = null,
    val navRoute: String = ""
) : PrefDelegate<String>(titleId, summaryId, dataStore, key, defaultValue) {
    override fun get(): Flow<String> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    override suspend fun set(value: String) {
        dataStore.edit { it[key] = value }
    }
}

class GesturePref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    dataStore: DataStore<Preferences>,
    key: Preferences.Key<String>,
    defaultValue: String = "",
    onClick: (() -> Unit)? = null,
    navRoute: String = ""
) : NavigationPref(
    titleId = titleId,
    summaryId = summaryId,
    dataStore = dataStore,
    key = key,
    defaultValue = defaultValue,
    onClick = onClick,
    navRoute = navRoute
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return getValue()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        setValue(value)
    }
}

open class StringTextPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<String>,
    val defaultValue: String = "",
    val predicate: (String) -> Boolean = { true },
) : PrefDelegate<String>(titleId, summaryId, dataStore, key, defaultValue) {

    override fun get(): Flow<String> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    override suspend fun set(value: String) {
        dataStore.edit { it[key] = value }
    }
}

open class StringSelectionPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<String>,
    val defaultValue: String = "",
    val entries: Map<String, String>,
    val onChange: () -> Unit = { }
) : PrefDelegate<String>(titleId, summaryId, dataStore, key, defaultValue) {

    override fun get(): Flow<String> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    override suspend fun set(value: String) {
        onChange()
        dataStore.edit { it[key] = value }
    }
}

open class StringSetPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<Set<String>>,
    val defaultValue: Set<String> = emptySet(),
    val navRoute: String = "",
    val onChange: () -> Unit = {}
) : PrefDelegate<Set<String>>(titleId, summaryId, dataStore, key, defaultValue) {
    private val valueList = arrayListOf<String>()
    override fun get(): Flow<Set<String>> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    override suspend fun set(value: Set<String>) {
        onChange()
        dataStore.edit { it[key] = value }
    }

    fun getAll(): List<String> = valueList

    fun setAll(value: List<String>) {
        valueList.clear()
        valueList.addAll(value)
        return runBlocking(Dispatchers.IO) {
            saveChanges()
        }
    }

    private suspend fun saveChanges() {
        dataStore.edit { it[key] = valueList.toSet() }
    }
}

open class StringMultiSelectionPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<Set<String>>,
    val defaultValue: Set<String> = emptySet(),
    val withIcons: Boolean = false,
    val entries: Map<String, Int>,
    onChange: () -> Unit = { }
) : PrefDelegate<Set<String>>(titleId, summaryId, dataStore, key, defaultValue) {

    private val valueList = arrayListOf<String>()

    init {
        runBlocking(Dispatchers.IO) {
            valueList.addAll(get().firstOrNull() ?: defaultValue)
        }
    }

    override fun get(): Flow<Set<String>> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    override suspend fun set(value: Set<String>) {
        dataStore.edit { it[key] = value }
    }

    fun getAll(): List<String> = valueList

    fun setAll(value: List<String>) {
        valueList.clear()
        valueList.addAll(value)
        return runBlocking(Dispatchers.IO) {
            saveChanges()
        }
    }

    private suspend fun saveChanges() {
        dataStore.edit { it[key] = valueList.toSet() }
    }
}

open class DialogPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<String>,
    val defaultValue: String = "",
) : PrefDelegate<String>(titleId, summaryId, dataStore, key, defaultValue) {

    override fun get(): Flow<String> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    override suspend fun set(value: String) {
        dataStore.edit { it[key] = value }
    }
}

open class StringPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<String>,
    val defaultValue: String = "",
    val onClick: (() -> Unit)? = null,
    val onChange: (String) -> Unit = {},
) : PrefDelegate<String>(titleId, summaryId, dataStore, key, defaultValue) {
    override fun get(): Flow<String> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    override suspend fun set(value: String) {
        dataStore.edit { it[key] = value }
        onChange(value)
    }
}

abstract class MutableMapPref<K, V>(
    context: Context,
    private val prefKey: String,
    onChange: () -> Unit = {}
) {
    private val valueMap = HashMap<K, V>()
    private val legacyPreferences = LegacyPreferences(context)

    private val onChangeMap: MutableMap<String, () -> Unit> = HashMap()

    init {
        val obj = JSONObject(legacyPreferences.sharedPref().getString(prefKey, "{}"))
        obj.keys().forEach {
            valueMap[unflattenKey(it)] = unflattenValue(obj.getString(it))
        }
        if (onChange !== {}) {
            onChangeMap[prefKey] = onChange
        }
    }

    fun toMap() = HashMap<K, V>(valueMap)

    open fun flattenKey(key: K) = key.toString()

    abstract fun unflattenKey(key: String): K

    open fun flattenValue(value: V) = value.toString()

    abstract fun unflattenValue(value: String): V

    operator fun set(key: K, value: V?) {
        if (value != null) {
            valueMap[key] = value
        } else {
            valueMap.remove(key)
        }
        saveChanges()
    }

    private fun saveChanges() {
        val obj = JSONObject()
        valueMap.entries.forEach { obj.put(flattenKey(it.key), flattenValue(it.value)) }
        legacyPreferences.savePreference(prefKey, obj.toString())
    }

    operator fun get(key: K): V? {
        return valueMap[key]
    }

    fun clear() {
        valueMap.clear()
        saveChanges()
    }
}

class ResettableLazy<out T : Any>(private val create: () -> T) {
    private var initialized = false
    private var currentValue: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (!initialized) {
            currentValue = create()
            initialized = true
        }
        return currentValue!!
    }

    fun resetValue() {
        initialized = false
        currentValue = null
    }
}

abstract class PrefDelegate<T : Any>(
    @StringRes var titleId: Int,
    @StringRes var summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<T>,
    private val defaultValue: T
) {
    fun getValue(): T {
        return runBlocking(Dispatchers.IO) {
            get().firstOrNull() ?: defaultValue
        }
    }

    fun setValue(value: T) {
        return runBlocking(Dispatchers.IO) {
            set(value)
        }
    }

    abstract fun get(): Flow<T>

    abstract suspend fun set(value: T)
}