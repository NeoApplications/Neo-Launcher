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

package com.saggitt.omega.search

import android.content.Context
import com.saggitt.omega.data.SearchProviderRepository
import com.saggitt.omega.data.models.SearchProvider
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.theme.ThemeOverride
import com.saggitt.omega.util.SingletonHolder
import com.saggitt.omega.util.ensureOnMainThread
import com.saggitt.omega.util.useApplicationContext
import kotlinx.coroutines.flow.StateFlow

class SearchProviderController(private val context: Context) {
    private val themeOverride = ThemeOverride(ThemeOverride.Launcher(), ThemeListener())
    private var themeRes: Int = 0

    init {
        ThemeManager.getInstance(context).addOverride(themeOverride)
    }

    val searchProviderState: StateFlow<SearchProvider>
        get() = SearchProviderRepository.INSTANCE.get(context).activeProvider
    val searchProvider: SearchProvider // TODO add support for multiple providers
        get() = SearchProviderRepository.INSTANCE.get(context).activeProvider.value

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
    }
}