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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.launcher3.util.ComponentKey
import com.neoapps.neolauncher.data.models.GestureItemInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface GestureItemInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: GestureItemInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: GestureItemInfo)

    @Query("SELECT * FROM GestureItemInfo WHERE packageName = :packageName")
    fun find(packageName: ComponentKey): Flow<GestureItemInfo?>
}