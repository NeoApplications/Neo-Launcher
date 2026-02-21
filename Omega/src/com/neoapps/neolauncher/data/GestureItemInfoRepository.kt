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

package com.neoapps.neolauncher.data

import android.content.Context
import com.android.launcher3.util.ComponentKey
import com.neoapps.neolauncher.data.models.GestureItemInfo
import com.neoapps.neolauncher.util.firstBlocking
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class GestureItemInfoRepository(context: Context) {
    private val scope = MainScope() + CoroutineName("GestureItemInfoRepository")
    private val dao = NeoLauncherDb.INSTANCE.get(context).gestureItemInfoDao()

    fun insert(target: GestureItemInfo) {
        scope.launch {
            dao.insert(target)
        }
    }

    fun update(target: GestureItemInfo) {
        scope.launch {
            dao.update(target)
        }
    }

    fun insertOrUpdate(target: GestureItemInfo) {
        scope.launch {
            if (find(target.packageName)?.packageName == target.packageName) {
                update(target)
            } else {
                insert(target)
            }
        }

    }

    //get item by
    fun find(target: ComponentKey): GestureItemInfo? {
        return dao.find(target).firstBlocking()
    }
}