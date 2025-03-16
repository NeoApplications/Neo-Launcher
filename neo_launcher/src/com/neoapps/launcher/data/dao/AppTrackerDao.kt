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
 */

package com.neoapps.launcher.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.neoapps.launcher.data.model.AppTracker

@Dao
interface AppTrackerDao {

    @Query("SELECT * FROM apptracker")
    fun getAllApps(): List<AppTracker>

    @Query("DELETE FROM apptracker WHERE packageName = :packageName")
    suspend fun deleteAppCount(packageName: String)

    @Query("SELECT count FROM apptracker WHERE packageName = :packageName")
    fun getAppCount(packageName: String): Int

    @Query("SELECT EXISTS(SELECT * FROM apptracker WHERE packageName = :packageName)")
    fun appExist(packageName: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(appTracker: AppTracker)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun update(appTracker: AppTracker)
}