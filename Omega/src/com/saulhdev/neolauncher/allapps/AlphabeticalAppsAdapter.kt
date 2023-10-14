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
package com.saulhdev.neolauncher.allapps

import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.allapps.AlphabeticalAppsList
import com.android.launcher3.allapps.BaseAllAppsAdapter
import com.android.launcher3.allapps.search.SearchAdapterProvider
import com.android.launcher3.views.ActivityContext

class AlphabeticalAppsAdapter<T>(
    activityContext: T,
    inflater: LayoutInflater,
    apps: AlphabeticalAppsList<T>,
    searchAdapterProvider: SearchAdapterProvider<T>
) : BaseAllAppsAdapter<T>(
    activityContext,
    inflater,
    apps,
    searchAdapterProvider
) where T : Context?, T : ActivityContext? {


    init {
        setAppsPerRow(activityContext!!.getDeviceProfile().numShownAllAppsColumns)
    }

    override fun setAppsPerRow(appsPerRow: Int) {
        mAppsPerRow = appsPerRow
        var totalSpans = mAppsPerRow
        for (itemPerRow in mAdapterProvider.supportedItemsPerRowArray) {
            if (totalSpans % itemPerRow != 0) {
                totalSpans *= itemPerRow
            }
        }
    }

    override fun getLayoutManager(): RecyclerView.LayoutManager? {
        return null
    }
}