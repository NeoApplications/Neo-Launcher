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
 */

package com.saggitt.omega.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.android.launcher3.util.MainThreadInitializedObject
import com.saggitt.omega.data.models.AppTracker
import com.saggitt.omega.data.models.GestureItemInfo
import com.saggitt.omega.data.models.IconOverride
import com.saggitt.omega.data.models.PeopleInfo
import com.saggitt.omega.data.models.SearchProvider
import com.saggitt.omega.data.models.SearchProvider.Companion.defaultProviders
import com.saggitt.omega.data.models.SearchProvider.Companion.offlineSearchProvider
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        IconOverride::class,
        AppTracker::class,
        PeopleInfo::class,
        GestureItemInfo::class,
        SearchProvider::class,
    ],
    version = 5,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(
            from = 4,
            to = 5,
        ),
    ]
)
@TypeConverters(Converters::class)
abstract class NeoLauncherDb : RoomDatabase() {

    abstract fun iconOverrideDao(): IconOverrideDao
    abstract fun appTrackerDao(): AppTrackerDao
    abstract fun peopleDao(): PeopleDao
    abstract fun gestureItemInfoDao(): GestureItemInfoDao
    abstract fun searchProviderDao(): SearchProviderDao

    companion object {

        @OptIn(DelicateCoroutinesApi::class)
        val INSTANCE = MainThreadInitializedObject { context ->
            Room.databaseBuilder(context, NeoLauncherDb::class.java, "NeoLauncher.db")
                .build()
                .apply {
                    GlobalScope.launch(Dispatchers.IO) {
                        if (searchProviderDao().getCount() == 0) {
                            searchProviderDao().insert(offlineSearchProvider(context))
                            defaultProviders.forEach(searchProviderDao()::insert)
                        }
                    }
                }
        }
    }
}