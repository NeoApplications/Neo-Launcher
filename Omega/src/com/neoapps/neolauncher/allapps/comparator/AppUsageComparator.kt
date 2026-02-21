/*
 * This file is part of Neo Launcher
 * Copyright (c) 2026   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.neoapps.neolauncher.allapps.comparator

import com.android.launcher3.model.data.AppInfo
import com.neoapps.neolauncher.data.models.AppTracker

class AppUsageComparator(private val mApps: List<AppTracker>) : Comparator<AppInfo> {
    override fun compare(app1: AppInfo, app2: AppInfo): Int {
        var item1 = 0
        var item2 = 0
        for (i in mApps.indices) {
            if (mApps[i].packageName == app1.componentName!!.packageName)
                item1 = mApps[i].count
            if (mApps[i].packageName == app2.componentName!!.packageName)
                item2 = mApps[i].count
        }
        return when {
            item1 < item2 -> 1
            item2 < item1 -> -1
            else -> 0
        }
    }
}