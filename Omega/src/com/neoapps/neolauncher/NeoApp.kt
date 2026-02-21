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
package com.neoapps.neolauncher

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.Utilities
import com.neoapps.neolauncher.data.reposModule
import com.neoapps.neolauncher.flowerpot.Flowerpot
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.preferences.NeoPrefs.Companion.prefsModule
import com.neoapps.neolauncher.theme.ThemeManager
import com.neoapps.neolauncher.util.minSDK
//import org.chickenhook.restrictionbypass.Unseal
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import java.io.File

class NeoApp : Application() {
    val activityHandler = ActivityHandler()
    var accessibilityService: OmegaAccessibilityService? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        startKoin {
            androidLogger()
            androidContext(this@NeoApp)
            modules(
                prefsModule,
                reposModule,
            )
        }
        if (minSDK(Build.VERSION_CODES.P)) {
            try {
                //Unseal.unseal()
                Log.i(TAG, "Unseal success!")
            } catch (e: Exception) {
                Log.e(TAG, "Unseal fail!")
                e.printStackTrace()
            }
        }
    }

    fun onLauncherAppStateCreated() {
        registerActivityLifecycleCallbacks(activityHandler)
        Flowerpot.Manager.getInstance(this)
    }

    fun restart(recreateLauncher: Boolean = true) {
        if (recreateLauncher) {
            activityHandler.finishAll(recreateLauncher)
        } else {
            Utilities.restartLauncher(this)
        }
    }

    fun performGlobalAction(action: Int): Boolean {
        return if (accessibilityService != null) {
            accessibilityService!!.performGlobalAction(action)
        } else {
            startActivity(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            false
        }
    }

    fun renameRestoredDb(dbName: String) {
        //val restoredDbFile = getDatabasePath(OmegaBackup.RESTORED_DB_FILE_NAME)
        val restoredDbFile = getDatabasePath("restored.db")
        if (!restoredDbFile.exists()) return
        val dbFile = getDatabasePath(dbName)
        restoredDbFile.renameTo(dbFile)
    }

    fun migrateDbName(dbName: String) {
        val dbFile = getDatabasePath(dbName)
        if (dbFile.exists()) return
        val prefs = NeoPrefs.getInstance()
        val dbJournalFile = getJournalFile(dbFile)
        val oldDbSlot = prefs.legacyPrefs.getStringPreference("pref_currentDbSlot", "a")
        val oldDbName = if (oldDbSlot == "a") "launcher.db" else "launcher.db_b"
        val oldDbFile = getDatabasePath(oldDbName)
        val oldDbJournalFile = getJournalFile(oldDbFile)
        if (oldDbFile.exists()) {
            oldDbFile.copyTo(dbFile)
            oldDbJournalFile.copyTo(dbJournalFile)
            oldDbFile.delete()
            oldDbJournalFile.delete()
        }
    }

    fun cleanUpDatabases() {
        val idp = InvariantDeviceProfile.INSTANCE.get(this)
        val dbName = idp.dbFile
        val dbFile = getDatabasePath(dbName)
        dbFile?.parentFile?.listFiles()?.forEach { file ->
            val name = file.name
            if (name.startsWith("launcher") && !name.startsWith(dbName)) {
                file.delete()
            }
        }
    }

    private fun getJournalFile(file: File): File =
        File(file.parentFile, "${file.name}-journal")

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ThemeManager.getInstance(this).updateNightMode(newConfig)
    }

    class ActivityHandler : ActivityLifecycleCallbacks {

        val activities = HashSet<Activity>()
        var foregroundActivity: Activity? = null

        fun finishAll(recreateLauncher: Boolean = true) {
            HashSet(activities).forEach { if (recreateLauncher && it is NeoLauncher) it.recreate() else it.finish() }
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
            foregroundActivity = activity
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activity == foregroundActivity)
                foregroundActivity = null
            activities.remove(activity)
        }

        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            activities.add(activity)
        }
    }

    companion object {
        @JvmStatic
        var instance: NeoApp? = null
            private set

        private const val TAG = "LauncherApp"
    }
}

val Context.neoApp get() = applicationContext as NeoApp