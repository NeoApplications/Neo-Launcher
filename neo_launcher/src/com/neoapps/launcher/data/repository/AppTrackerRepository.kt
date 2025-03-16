/*
 * This file is part of Neo Launcher
 * Copyright (c) 2025   Neo Launcher Team
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

package com.neoapps.launcher.data.repository


import android.content.Context
import com.android.launcher3.util.MainThreadInitializedObject
import com.android.launcher3.util.SafeCloseable
import com.neoapps.launcher.data.NeoLauncherDb
import com.neoapps.launcher.data.model.AppTracker
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class AppTrackerRepository(context: Context) : SafeCloseable {
    private val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("AppTrackerRepository")
    private val dao = NeoLauncherDb.INSTANCE.get(context).appTrackerDao()

    private var appCountList: List<AppTracker> = listOf()

    init {
        scope.launch {
            appCountList = dao.getAllApps()
        }
    }

    fun getAllApps(): List<AppTracker> {
        scope.launch {
            appCountList = dao.getAllApps()
        }
        return appCountList
    }

    fun updateAppCount(packageName: String) {
        scope.launch {
            //Check if the app is already in the database
            if (dao.appExist(packageName)) {
                //If it is, update the count
                val currentCount = dao.getAppCount(packageName)
                dao.update(AppTracker(packageName, currentCount + 1, System.currentTimeMillis()))
            } else {
                dao.insert(AppTracker(packageName, 1, System.currentTimeMillis()))
            }
        }
    }

    fun deleteApp(packageName: String) {
        scope.launch { dao.deleteAppCount(packageName) }
    }

    override fun close() {
        scope.coroutineContext.cancel()
    }

    companion object {
        val INSTANCE = MainThreadInitializedObject(::AppTrackerRepository)
    }
}