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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.saggitt.omega.data.models.SearchProvider
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchProviderDao {

    @get:Query("SELECT * FROM searchprovider")
    val all: List<SearchProvider>

    @get:Query("SELECT * FROM searchprovider")
    val allFlow: Flow<List<SearchProvider>>

    @get:Query("SELECT * FROM searchprovider WHERE enabled = 1")
    val enabled: List<SearchProvider>

    @get:Query("SELECT * FROM searchprovider WHERE enabled = 1")
    val enabledFlow: Flow<List<SearchProvider>>

    @get:Query("SELECT * FROM searchprovider WHERE enabled = 0")
    val disabled: List<SearchProvider>

    @get:Query("SELECT * FROM searchprovider WHERE enabled = 0")
    val disabledFlow: Flow<List<SearchProvider>>

    @Query("SELECT * FROM searchprovider WHERE id = :id")
    fun get(id: String): SearchProvider

    @Query("SELECT * FROM searchprovider WHERE id = :id")
    fun getFlow(id: String): Flow<SearchProvider>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(appTracker: SearchProvider)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(appTracker: SearchProvider)

    @Delete
    fun delete(appTracker: SearchProvider)

    @Query("DELETE FROM searchprovider WHERE id = :id")
    suspend fun delete(id: Int)
}