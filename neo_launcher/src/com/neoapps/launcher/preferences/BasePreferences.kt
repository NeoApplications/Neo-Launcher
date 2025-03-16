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
 */

package com.neoapps.launcher.preferences

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.neoapps.launcher.navigation.NavRoute
import com.neoapps.launcher.theme.AccentColorOption
import com.neoapps.launcher.util.runOnMainThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

open class BooleanPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    dataStore: DataStore<Preferences>,
    key: Preferences.Key<Boolean>,
    defaultValue: Boolean = false,
    onChange: (Boolean) -> Unit = {}
) : BasePreference<Boolean>(titleId, summaryId, dataStore, key, defaultValue, onChange)

open class IntSelectionPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    dataStore: DataStore<Preferences>,
    key: Preferences.Key<Int>,
    val defaultValue: Int = 0,
    val entries: Map<Int, Int>,
    onChange: (Int) -> Unit = {}
) : BasePreference<Int>(titleId, summaryId, dataStore, key, defaultValue, onChange)

open class LongMultiSelectionPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<Set<String>>,
    val defaultValue: Set<Long> = emptySet(),
    val entries: () -> Map<Long, String>,
    onChange: (Set<Long>) -> Unit = { }
) : BasePreference<Set<String>>(
    titleId,
    summaryId,
    dataStore,
    key,
    defaultValue.mapTo(mutableSetOf()) { it.toString() },
    { set -> onChange(set.mapTo(mutableSetOf()) { it.toLong() }) }
) {
    private val valueList = arrayListOf<String>()

    init {
        runBlocking(Dispatchers.IO) {
            valueList.addAll(getValue())
        }
    }

    fun getAll(): List<Long> = valueList.map { it.toLong() }

    fun setAll(value: List<Long>) {
        valueList.clear()
        valueList.addAll(value.map { it.toString() })
        return runBlocking(Dispatchers.IO) {
            saveChanges()
        }
    }

    private suspend fun saveChanges() {
        dataStore.edit { it[key] = valueList.toSet() }
    }
}

open class ColorIntPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    dataStore: DataStore<Preferences>,
    val defaultValue: String = "system_accent",
    key: Preferences.Key<String>,
    val navRoute: NavRoute,
    onChange: (String) -> Unit = {}
) : BasePreference<String>(titleId, summaryId, dataStore, key, defaultValue, onChange) {

    fun getColor(): Int {
        return runBlocking(Dispatchers.IO) {
            AccentColorOption.fromString(getValue()).accentColor
        }
    }

    @Composable
    fun getColorFromState(): Int {
        return AccentColorOption.fromString(getState().value).accentColor
    }
}

open class StringSetPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    dataStore: DataStore<Preferences>,
    key: Preferences.Key<Set<String>>,
    val navRoute: NavRoute,
    val defaultValue: Set<String> = emptySet(),
    onChange: (Set<String>) -> Unit = {}
) : BasePreference<Set<String>>(titleId, summaryId, dataStore, key, defaultValue, onChange)

abstract class BasePreference<T : Any>(
    @StringRes var titleId: Int,
    @StringRes var summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<T>,
    private val defaultValue: T,
    val onChange: (T) -> Unit
) {
    fun getValue(): T {
        return runBlocking(Dispatchers.IO) {
            get().firstOrNull() ?: defaultValue
        }
    }

    @Composable
    fun getState(): State<T> {
        return get().collectAsState(initial = defaultValue)
    }

    fun setValue(value: T) {
        return runBlocking(Dispatchers.IO) {
            set(value)
        }
    }

    open fun get(): Flow<T> {
        return dataStore.data.map { prefs -> prefs[key] ?: defaultValue }
    }

    protected open suspend fun set(value: T) {
        if (getValue() == value) return
        dataStore.edit { prefs -> prefs[key] = value }
        runOnMainThread {
            onChange(value)
        }
    }
}