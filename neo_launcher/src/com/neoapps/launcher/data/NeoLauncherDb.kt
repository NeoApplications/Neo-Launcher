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

package com.neoapps.launcher.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import com.android.launcher3.util.MainThreadInitializedObject
import com.android.launcher3.util.SafeCloseable
import com.neoapps.launcher.data.dao.AppTrackerDao
import com.neoapps.launcher.data.dao.GestureItemInfoDao
import com.neoapps.launcher.data.dao.IconOverrideDao
import com.neoapps.launcher.data.dao.PeopleDao
import com.neoapps.launcher.data.dao.SearchProviderDao
import com.neoapps.launcher.data.model.AppTracker
import com.neoapps.launcher.data.model.GestureItemInfo
import com.neoapps.launcher.data.model.IconOverride
import com.neoapps.launcher.data.model.PeopleInfo
import com.neoapps.launcher.data.model.SearchProvider
import com.neoapps.launcher.data.model.SearchProvider.Companion.addedProvidersV6
import com.neoapps.launcher.data.model.SearchProvider.Companion.defaultProviders
import com.neoapps.launcher.data.model.SearchProvider.Companion.offlineSearchProvider
import com.neoapps.launcher.data.repository.SearchProviderRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin

@Database(
    entities = [
        IconOverride::class,
        AppTracker::class,
        PeopleInfo::class,
        GestureItemInfo::class,
        SearchProvider::class,
    ],
    version = 7,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(
            from = 4,
            to = 5,
        ),
        AutoMigration(
            from = 5,
            to = 6,
            spec = NeoLauncherDb.Companion.MigrationSpec5to6::class
        ),
        AutoMigration(
            from = 6,
            to = 7,
            spec = NeoLauncherDb.Companion.MigrationSpec6to7::class
        )
    ]
)

@TypeConverters(Converters::class)
abstract class NeoLauncherDb : RoomDatabase(), SafeCloseable {

    abstract fun iconOverrideDao(): IconOverrideDao
    abstract fun appTrackerDao(): AppTrackerDao
    abstract fun peopleDao(): PeopleDao
    abstract fun gestureItemInfoDao(): GestureItemInfoDao
    abstract fun searchProviderDao(): SearchProviderDao

    override fun close() {

    }

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

        class MigrationSpec5to6 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(5, 6)
            }
        }

        class MigrationSpec6to7 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(6, 7)
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        fun onPostMigrate(from: Int, to: Int) {
            val preRepos = mutableListOf<SearchProvider>()
            if (from == 6) preRepos.addAll(addedProvidersV6)
            GlobalScope.launch(Dispatchers.IO) {
                preRepos.forEach {
                    getKoin().get<SearchProviderRepository>().insert(it)
                }
                if (to == 7) GlobalScope.launch(Dispatchers.IO) {
                    getKoin().get<SearchProviderRepository>().emptyTable()
                    defaultProviders.forEach {
                        getKoin().get<SearchProviderRepository>().insert(it)
                    }
                }
            }
        }
    }
}