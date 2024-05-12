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

package com.saulhdev.neolauncher.search

import android.content.Context
import com.saggitt.omega.data.SearchProviderRepository
import com.saggitt.omega.data.models.SearchProvider
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.theme.ThemeOverride
import com.saggitt.omega.util.SingletonHolder
import com.saggitt.omega.util.ensureOnMainThread
import com.saggitt.omega.util.useApplicationContext
import com.saulhdev.neolauncher.search.providers.BaiduSearchProvider
import com.saulhdev.neolauncher.search.providers.BingSearchProvider
import com.saulhdev.neolauncher.search.providers.DuckDuckGoSearchProvider
import com.saulhdev.neolauncher.search.providers.EdgeSearchProvider
import com.saulhdev.neolauncher.search.providers.FirefoxSearchProvider
import com.saulhdev.neolauncher.search.providers.GoogleGoSearchProvider
import com.saulhdev.neolauncher.search.providers.SFinderSearchProvider
import kotlinx.coroutines.flow.StateFlow

class SearchProviderController(private val context: Context) {
    private val themeOverride = ThemeOverride(ThemeOverride.Launcher(), ThemeListener())

    private var themeRes: Int = 0
    init {
        ThemeManager.getInstance(context).addOverride(themeOverride)
    }

    val searchProviderState: StateFlow<SearchProvider>
        get() = SearchProviderRepository.INSTANCE.get(context).activeProvider
    val activeSearchProvider: SearchProvider // TODO add support for multiple providers
        get() = SearchProviderRepository.INSTANCE.get(context).activeProvider.value

    fun appSearchProvider(searchId: Int): AbstractSearchProvider {
        val result = getSearchProviders(context).first { it.id == searchId }
        return result
    }



    inner class ThemeListener : ThemeOverride.ThemeOverrideListener {

        override val isAlive = true

        override fun applyTheme(themeRes: Int) {
            this@SearchProviderController.themeRes = themeRes
        }

        override fun reloadTheme() {
            applyTheme(themeOverride.getTheme(context))
        }
    }

    companion object : SingletonHolder<SearchProviderController, Context>(
        ensureOnMainThread(
            useApplicationContext(::SearchProviderController)
        )
    ) {
        fun getSearchProvidersMap(context: Context): Map<Long, String> =
            SearchProviderRepository.INSTANCE.get(context)
                .allProviders
                .value
                .associate {
                    Pair(it.id, it.name)
                }

        fun getSearchProviders(context: Context): List<AbstractSearchProvider> {
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

        fun getProviderName(context: Context, provider: Int): String {
            if (provider > 1000) {
                val providers = getSearchProviders(context)
                val currentProvider = providers.filter { it.id == provider }
                return currentProvider.first().name
            } else {
                return SearchProviderRepository.INSTANCE.get(context).activeProvider.value.name
            }
        }
    }
}