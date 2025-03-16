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
 */

package com.neoapps.launcher

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.neoapps.launcher.preferences.NeoPrefs
import com.neoapps.launcher.util.CoreUtils.Companion.minSDK
import org.chickenhook.restrictionbypass.Unseal
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androix.startup.KoinStartup
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module

@OptIn(KoinExperimentalAPI::class)
class NeoApp : Application(), KoinStartup {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "neo_launcher"
    )

    val prefsModule = module {
        single { NeoPrefs(get()) }
        single { androidContext().dataStore }
    }

    private val dataModule = module {
    }
    private val coreModule = module {
        single { NeoLauncher.getLauncher(get()) }
    }

    val appModule = module {

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        startKoin {
            androidLogger()
            androidContext(this@NeoApp)
            modules(coreModule, dataModule, appModule, prefsModule)
        }
        if (minSDK(Build.VERSION_CODES.P)) {
            try {
                Unseal.unseal()
                Log.i(TAG, "Unseal success!")
            } catch (e: Exception) {
                Log.e(TAG, "Unseal fail!")
                e.printStackTrace()
            }
        }
    }

    override fun onKoinStartup() = koinConfiguration {
        androidLogger()
        androidContext(this@NeoApp)
        modules(coreModule, dataModule, appModule, prefsModule)
    }

    override fun onTerminate() {
        super.onTerminate()
        GlobalContext.get().close()
    }

    companion object {
        @JvmStatic
        var instance: NeoApp? = null
            private set

        private const val TAG = "OmegaApp"
    }
}

val Context.neoApp get() = applicationContext as NeoApp