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

package com.neoapps.launcher.search

import android.content.Context
import com.neoapps.launcher.data.model.SearchProvider
import com.neoapps.launcher.data.repository.SearchProviderRepository
import com.neoapps.launcher.search.providers.BaiduSearchProvider
import com.neoapps.launcher.search.providers.BingSearchProvider
import com.neoapps.launcher.search.providers.DuckDuckGoSearchProvider
import com.neoapps.launcher.search.providers.EdgeSearchProvider
import com.neoapps.launcher.search.providers.FirefoxSearchProvider
import com.neoapps.launcher.search.providers.GoogleGoSearchProvider
import com.neoapps.launcher.search.providers.SFinderSearchProvider
import com.neoapps.launcher.util.SingletonHolder
import com.neoapps.launcher.util.ensureOnMainThread
import com.neoapps.launcher.util.useApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import org.koin.java.KoinJavaComponent.getKoin

class SearchProviderController(private val context: Context) {

    val searchProvidersState: StateFlow<List<SearchProvider>>
        get() = getKoin().get<SearchProviderRepository>().activeProviders
    private val _searchProviderSelector: MutableStateFlow<Int> = MutableStateFlow(0)
    val searchProviderSelector: Flow<Int> = _searchProviderSelector.map {
        it.coerceIn(0, searchProvidersState.value.size)
    }
    val activeSearchProvider: SearchProvider
        get() = searchProvidersState.value[_searchProviderSelector.value]

    fun changeSearchProvider() {
        _searchProviderSelector.tryEmit((_searchProviderSelector.value + 1) % searchProvidersState.value.size)
    }

    companion object : SingletonHolder<SearchProviderController, Context>(
        ensureOnMainThread(
            useApplicationContext(::SearchProviderController)
        )
    ) {
        fun getSearchProvidersMap(): Map<Long, String> =
            getKoin().get<SearchProviderRepository>()
                .allProviders
                .value
                .associate {
                    Pair(it.id, it.name)
                }

        fun getSearchProviders(): Map<Long, SearchProvider> =
            getKoin().get<SearchProviderRepository>()
                .allProviders
                .value
                .associateBy { it.id }

        fun getAppSearchProviders(context: Context): List<AbstractSearchProvider> {
            val list = listOf(
                BaiduSearchProvider(context),
                BingSearchProvider(context),
                DuckDuckGoSearchProvider(context),
                EdgeSearchProvider(context),
                FirefoxSearchProvider(context),
                GoogleGoSearchProvider(context),
                SFinderSearchProvider(context)
            )
            return list
        }
    }
}