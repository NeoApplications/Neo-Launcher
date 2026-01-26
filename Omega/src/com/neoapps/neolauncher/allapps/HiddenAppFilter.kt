/*
 * This file is part of Neo Launcher
 * Copyright (c) 2021   Neo Launcher Team
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

package com.neoapps.neolauncher.allapps

import android.content.ComponentName
import android.content.Context
import android.os.Process
import com.android.launcher3.AppFilter
import com.android.launcher3.dagger.ApplicationContext
import com.android.launcher3.util.ComponentKey
import com.neoapps.neolauncher.preferences.NeoPrefs
import javax.inject.Inject

class HiddenAppFilter @Inject constructor(@ApplicationContext context: Context) :
    AppFilter(context) {

    override fun shouldShowApp(componentName: ComponentName): Boolean {
        val key = ComponentKey(componentName, Process.myUserHandle())
        return super.shouldShowApp(componentName) && !isHiddenApp(key)
    }

    companion object {
        private fun isHiddenApp(key: ComponentKey?): Boolean {
            return getHiddenAppList().contains(key.toString())
        }

        private fun getHiddenAppList(): MutableSet<String> {
            return HashSet(NeoPrefs.getInstance().drawerHiddenAppSet.getValue())
        }
    }
}