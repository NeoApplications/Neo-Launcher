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

package com.neoapps.neolauncher.data

import android.content.Context
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import com.android.launcher3.pm.UserCache
import com.android.launcher3.util.MainThreadInitializedObject
import com.neoapps.neolauncher.data.models.AppTracker
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class AppTrackerRepository(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("AppTrackerRepository")
    private val dao = NeoLauncherDb.INSTANCE.get(context).appTrackerDao()
    private val userCache = UserCache.INSTANCE.get(context)

    var appCountList: List<AppTracker> = listOf()

    init {
        scope.launch {
            appCountList = dao.getAppCount()
        }
    }

    fun getAppsCount(): List<AppTracker> {
        scope.launch {
            appCountList = dao.getAppCount()
        }
        return appCountList
    }

    fun getRecentApps(limit: Int): List<AppTracker> {
        return dao.getRecentApps(limit)
    }

    fun getAllRecentApps(): List<AppTracker> {
        return dao.getAllRecentApps()
    }

    fun updateAppCount(packageName: String, user: UserHandle = Process.myUserHandle()) {
        val userSerialNumber = userCache.getSerialNumberForUser(user)
        val timestamp = System.currentTimeMillis()
        //Check if the app is already in the database
        if (dao.appExist(packageName, userSerialNumber)) {
            //If it is, update the count
            val currentCount = dao.getAppCount(packageName, userSerialNumber)
            dao.update(AppTracker(packageName, userSerialNumber, currentCount + 1, timestamp))
        } else {
            dao.insert(AppTracker(packageName, userSerialNumber, 1, timestamp))
        }
    }

    fun deleteApp(packageName: String) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            scope.launch { dao.deleteApp(packageName) }
        } else {
            dao.deleteApp(packageName)
        }
    }

    fun deleteApp(packageName: String, userSerialNumber: Long) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            scope.launch { dao.deleteApp(packageName, userSerialNumber) }
        } else {
            dao.deleteApp(packageName, userSerialNumber)
        }
    }

    fun deleteApp(packageName: String, user: UserHandle = Process.myUserHandle()) {
        val userSerialNumber = userCache.getSerialNumberForUser(user)
        deleteApp(packageName, userSerialNumber)
    }

    fun deleteAppCount(packageName: String, user: UserHandle = Process.myUserHandle()) {
        deleteApp(packageName, user)
    }

    companion object {
        val INSTANCE = MainThreadInitializedObject(::AppTrackerRepository)
    }
}