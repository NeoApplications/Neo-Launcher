/*
 * This file is part of Neo Launcher
 * Copyright (c) 2025   Neo Launcher Team
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

package com.neoapps.launcher.data.repository

import android.content.Context
import com.android.launcher3.R
import com.neoapps.launcher.data.NeoLauncherDb
import com.neoapps.launcher.data.model.SearchProvider
import com.neoapps.launcher.util.CoreUtils
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class SearchProviderRepository(context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("SearchProviderRepository")
    private val dao = NeoLauncherDb.INSTANCE.get(context).searchProviderDao()
    private val prefs = CoreUtils.getNeoPrefs()

    val allProviders = dao.allFlow.stateIn(
        scope,
        SharingStarted.Eagerly,
        emptyList()
    )
    val enabledProviders = dao.enabledFlow.stateIn(
        scope,
        SharingStarted.Eagerly,
        emptyList()
    )
    val disabledProviders = dao.disabledFlow.stateIn(
        scope,
        SharingStarted.Eagerly,
        emptyList()
    )
    val activeProviders = combine(allProviders, prefs.searchProviders.get()) { ps, pref ->
        ps.filter { pref.contains(it.id.toString()) }.takeIf { it.isNotEmpty() }
            ?: listOf(SearchProvider.offlineSearchProvider(context))
    }.stateIn(
        scope,
        SharingStarted.Eagerly,
        listOf(SearchProvider.offlineSearchProvider(context))
    )

    fun get(id: Long): SearchProvider? = dao.get(id)

    fun getFlow(id: Long): Flow<SearchProvider?> = dao.getFlow(id)

    fun insert(provider: SearchProvider) {
        scope.launch {
            dao.insert(provider)
        }
    }

    suspend fun insertNew(): Long = scope.async {
        dao.insert(
            SearchProvider(
                id = 0,
                name = "New provider",
                iconId = R.drawable.ic_search,
                searchUrl = "",
                suggestionUrl = null,
                enabled = false,
                order = -1,
            )
        )
    }.await()

    fun update(provider: SearchProvider?) = provider?.let {
        scope.launch { dao.upsert(it) }
    }

    fun updateProvidersOrder(providers: List<SearchProvider>) {
        scope.launch {
            providers.forEachIndexed { index, provider ->
                dao.upsert(provider.copy(order = index, enabled = true))
            }
        }
    }

    fun enableProvider(provider: SearchProvider) {
        scope.launch {
            val maxOrder = enabledProviders.value.maxOfOrNull { it.order } ?: -1
            dao.upsert(provider.copy(enabled = true, order = maxOrder + 1))
        }
    }

    fun disableProvider(provider: SearchProvider) {
        scope.launch {
            dao.upsert(provider.copy(enabled = false, order = -1))
        }
    }

    fun delete(id: Long?) = id?.let {
        scope.launch { dao.delete(it) }
    }

    fun delete(provider: SearchProvider) {
        scope.launch { dao.delete(provider) }
    }

    fun emptyTable() {
        scope.launch { dao.emptyTable() }
    }
}