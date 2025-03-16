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

package com.neoapps.launcher.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.launcher3.util.ComponentKey
import com.neoapps.launcher.data.model.IconOverride
import kotlinx.coroutines.flow.Flow

@Dao
interface IconOverrideDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: IconOverride)

    @Query("DELETE FROM iconoverride WHERE target = :target")
    suspend fun delete(target: ComponentKey)

    @Query("SELECT * FROM iconoverride")
    fun observeAll(): Flow<List<IconOverride>>

    @Query("SELECT * FROM iconoverride")
    suspend fun getAll(): List<IconOverride>

    @Query("SELECT * FROM iconoverride WHERE target = :target")
    fun observeTarget(target: ComponentKey): Flow<IconOverride?>

    @Query("SELECT COUNT(target) FROM iconoverride")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM iconoverride")
    suspend fun deleteAll()
}