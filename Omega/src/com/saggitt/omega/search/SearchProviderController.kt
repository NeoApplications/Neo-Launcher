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
import android.view.ContextThemeWrapper
import com.android.launcher3.Utilities
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.theme.ThemeOverride
import com.saggitt.omega.util.SingletonHolder
import com.saggitt.omega.util.ensureOnMainThread
import com.saggitt.omega.util.useApplicationContext

class SearchProviderController(private val context: Context) {
    private val prefs by lazy { Utilities.getOmegaPrefs(context) }
    private var cache: SearchProvider? = null
    private var cached: String = ""

    private val themeOverride = ThemeOverride(ThemeOverride.Launcher(), ThemeListener())
    private var themeRes: Int = 0

    private val listeners = HashSet<OnProviderChangeListener>()

    val isGoogle get() = false//searchProvider is GoogleSearchProvider

    init {
        ThemeManager.getInstance(context).addOverride(themeOverride)
    }

    fun addOnProviderChangeListener(listener: OnProviderChangeListener) {
        listeners.add(listener)
    }

    fun removeOnProviderChangeListener(listener: OnProviderChangeListener) {
        listeners.remove(listener)
    }

    fun onSearchProviderChanged() {
        cache = null
        notifyProviderChanged()
    }

    private fun notifyProviderChanged() {
        HashSet(listeners).forEach(OnProviderChangeListener::onSearchProviderChanged)
    }

    val searchProvider: SearchProvider
        get() {
            val curr = prefs.searchProvider.getValue()
            if (cache == null || cached != curr) {
                cache = createProvider(prefs.searchProvider.getValue()) {
                    AppsSearchProvider(context)
                }
                cached = cache!!::class.java.name
                if (prefs.searchProvider.getValue() != cached) {
                    prefs.searchProvider.setValue(cached)
                }
                notifyProviderChanged()
            }
            return cache!!
        }

    private fun createProvider(
        providerName: String,
        fallback: () -> SearchProvider
    ): SearchProvider {
        try {
            val constructor = Class.forName(providerName).getConstructor(Context::class.java)
            val themedContext = ContextThemeWrapper(context, themeRes)
            val prov = constructor.newInstance(themedContext) as SearchProvider
            if (prov.isAvailable) {
                return prov
            }
        } catch (ignored: Exception) {
        }
        return fallback()
    }

    inner class ThemeListener : ThemeOverride.ThemeOverrideListener {

        override val isAlive = true

        override fun applyTheme(themeRes: Int) {
            this@SearchProviderController.themeRes = themeRes
        }

        override fun reloadTheme() {
            cache = null
            applyTheme(themeOverride.getTheme(context))
            onSearchProviderChanged()
        }
    }

    interface OnProviderChangeListener {
        fun onSearchProviderChanged()
    }

    companion object : SingletonHolder<SearchProviderController, Context>(
        ensureOnMainThread(
            useApplicationContext(::SearchProviderController)
        )
    ) {
        fun getSearchProvidersMap(context: Context): Map<String, String> {
            val providers = listOf(
                AppsSearchProvider(context)
            ).filter { it.isAvailable }

            val entries = providers.map { it.name }.toTypedArray()
            val entryValues = providers.map { it::class.java.name }.toTypedArray()
            return entryValues.zip(entries).toMap()
        }
    }
}