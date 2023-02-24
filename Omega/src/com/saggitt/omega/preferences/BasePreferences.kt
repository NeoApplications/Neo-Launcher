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

import android.content.Intent
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

open class BooleanPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<Boolean>,
    private val defaultValue: Boolean = false,
    onChange: () -> Unit = {}
) : PrefDelegate<Boolean>(titleId, summaryId, dataStore, key, defaultValue) {

    override fun get(): Flow<Boolean> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    override suspend fun set(value: Boolean) {
        dataStore.edit { it[key] = value }
    }
}

open class IntPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<Int>,
    private val defaultValue: Int = -1,
    minValue: Int = 0,
    maxValue: Int = "FFFFFF".toInt(16),
    steps: Int = 1,
    specialOutputs: ((Int) -> String) = Int::toString,
) : PrefDelegate<Int>(titleId, summaryId, dataStore, key, defaultValue) {

    override fun get(): Flow<Int> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    override suspend fun set(value: Int) {
        dataStore.edit { it[key] = value }
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

open class StringTextPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<String>,
    val defaultValue: String = "",
    val predicate: (String) -> Boolean = { true },
) :
    PrefDelegate<String>(titleId, summaryId, dataStore, key, defaultValue) {

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
    onChange: () -> Unit = { }
) :
    PrefDelegate<String>(titleId, summaryId, dataStore, key, defaultValue) {

    override fun get(): Flow<String> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    override suspend fun set(value: String) {
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
    onChange: () -> Unit = {}
) : PrefDelegate<Set<String>>(titleId, summaryId, dataStore, key, defaultValue) {
    private val valueList = arrayListOf<String>()
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