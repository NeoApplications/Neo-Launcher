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
) : PrefDelegate<Int>(titleId, summaryId, dataStore, key, defaultValue) {

    override fun get(): Flow<Int> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    override suspend fun set(value: Int) {
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
    val specialOutputs: ((Float) -> String) = Float::toString
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
    val entries: Map<Int, Int>
) :
    PrefDelegate<Int>(titleId, summaryId, dataStore, key, defaultValue) {

    override fun get(): Flow<Int> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    fun getValue(): Int {
        var value = defaultValue
        /*CoroutineScope(Dispatchers.IO).launch {
            value = get().first()
        }*/
        runBlocking(Dispatchers.IO) {
            value = get().firstOrNull()!!
        }
        return value
    }

    override suspend fun set(value: Int) {
        dataStore.edit { it[key] = value }
    }
}

abstract class PrefDelegate<T : Any>(
    @StringRes var titleId: Int,
    @StringRes var summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<T>,
    private val defaultValue: T,
) {
    abstract fun get(): Flow<T>

    abstract suspend fun set(value: T)
}