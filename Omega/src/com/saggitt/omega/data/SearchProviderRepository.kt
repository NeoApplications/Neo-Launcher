/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
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
 *
 */

package com.saggitt.omega.data

import android.content.Context
import com.android.launcher3.util.MainThreadInitializedObject
import com.saggitt.omega.data.models.SearchProvider
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class SearchProviderRepository(context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("SearchProviderRepository")
    private val dao = NeoLauncherDb.INSTANCE.get(context).searchProviderDao()

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

    fun getLatest(): List<SearchProvider> {
        return allProviders.value
    }

    fun insert(provider: SearchProvider) {
        scope.launch {
            dao.insert(provider)
        }
    }

    fun setEnabled(provider: SearchProvider, enable: Boolean = true) {
        scope.launch {
            dao.insert(provider.copy(enabled = enable))
        }
    }

    fun setOrder(provider: SearchProvider, order: Int = -1) {
        scope.launch {
            dao.insert(provider.copy(order = order))
        }
    }

    fun delete(id: Int) {
        scope.launch { dao.delete(id) }
    }

    fun delete(provider: SearchProvider) {
        scope.launch { dao.delete(provider) }
    }

    companion object {
        val INSTANCE = MainThreadInitializedObject(::SearchProviderRepository)
    }
}