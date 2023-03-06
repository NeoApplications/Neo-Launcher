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
package com.saggitt.omega

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import com.saggitt.omega.blur.BlurWallpaperProvider
import com.saggitt.omega.flowerpot.Flowerpot
import com.saggitt.omega.smartspace.OmegaSmartSpaceController
import org.chickenhook.restrictionbypass.Unseal

class OmegaApp : Application() {
    private val TAG = "OmegaApp"
    val activityHandler = ActivityHandler()
    var accessibilityService: OmegaAccessibilityService? = null
    val smartspace by lazy { OmegaSmartSpaceController(this) }
    override fun onCreate() {
        super.onCreate()
        instance = this
        BlurWallpaperProvider.getInstance(this).updateAsync()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                Unseal.unseal()
                Log.i(TAG, "Unseal success!")
            } catch (e: Exception) {
                Log.e(TAG, "Unseal fail!")
                e.printStackTrace()
            }
        }
    }

    fun onLauncherAppStateCreated() {
        registerActivityLifecycleCallbacks(activityHandler)
        BlurWallpaperProvider.getInstance(this)
        Flowerpot.Manager.getInstance(this)
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

    class ActivityHandler : ActivityLifecycleCallbacks {

        val activities = HashSet<Activity>()
        var foregroundActivity: Activity? = null

        fun finishAll(recreateLauncher: Boolean = true) {
            HashSet(activities).forEach { if (recreateLauncher && it is OmegaLauncher) it.recreate() else it.finish() }
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
        var instance: OmegaApp? = null
            private set

        fun minSDK(sdk: Int): Boolean {
            return Build.VERSION.SDK_INT >= sdk
        }
    }
}

val Context.omegaApp get() = applicationContext as OmegaApp