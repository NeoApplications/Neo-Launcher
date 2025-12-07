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

package com.saggitt.omega.allapps

import android.content.ComponentName
import android.content.Context
import android.os.UserHandle
import com.android.launcher3.AppFilter
import com.android.launcher3.Utilities
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.preferences.NeoPrefs

class CustomAppFilter(private val mContext: Context) : AppFilter(mContext) {
    fun shouldShowApp(componentName: ComponentName?, user: UserHandle?): Boolean {
        return super.shouldShowApp(componentName)
                && !isHiddenApp(mContext, ComponentKey(componentName, user))
    }

    companion object {
        fun setComponentNameState(context: Context, comp: String, hidden: Boolean) {
            val hiddenApps = getHiddenApps(context)
            while (hiddenApps.contains(comp)) {
                hiddenApps.remove(comp)
            }
            if (hidden) {
                hiddenApps.add(comp)
            }
            setHiddenApps(context, hiddenApps)
        }

        fun isHiddenApp(context: Context, key: ComponentKey?): Boolean {
            return getHiddenApps(context).contains(key.toString())
        }

        private fun getHiddenApps(context: Context): MutableSet<String> {
            return HashSet(NeoPrefs.getInstance().drawerHiddenAppSet.getValue())
        }

        private fun setHiddenApps(context: Context, hiddenApps: Set<String>?) {
            NeoPrefs.getInstance().drawerHiddenAppSet.setValue(hiddenApps!!)
        }
    }
}