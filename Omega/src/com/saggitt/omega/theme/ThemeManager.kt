/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Saul Henriquez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.theme

import android.content.Context
import android.content.res.Configuration
import com.android.launcher3.R
import com.neoapps.neolauncher.neoApp
import com.saggitt.omega.preferences.THEME_DARK
import com.saggitt.omega.preferences.THEME_SYSTEM
import com.saggitt.omega.preferences.THEME_USE_BLACK
import com.saggitt.omega.preferences.THEME_WALLPAPER
import com.saggitt.omega.util.SingletonHolder
import com.saggitt.omega.util.ensureOnMainThread
import com.saggitt.omega.util.hasFlag
import com.saggitt.omega.util.prefs
import com.saggitt.omega.util.useApplicationContext
import com.saggitt.omega.util.usingNightMode
import com.saggitt.omega.wallpaper.WallpaperColorsCompat
import com.saggitt.omega.wallpaper.WallpaperManagerCompat
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.plus

class ThemeManager(val context: Context) : WallpaperManagerCompat.OnColorsChangedListenerCompat {

    private val app = context.neoApp
    private val cScope = MainScope() + CoroutineName("ThemeManager")
    private val wallpaperManager = WallpaperManagerCompat.INSTANCE.get(context)
    private val listeners = HashSet<ThemeOverride>()
    private val prefs = context.prefs
    private var themeFlags = 0
    private var usingNightMode = context.resources.configuration.usingNightMode
        set(value) {
            if (field != value) {
                field = value
                updateTheme()
            }
        }

    val displayName: String
        get() {
            val values = context.resources.getIntArray(R.array.themeValues)
            val strings = context.resources.getStringArray(R.array.themes)
            val index = values.indexOf(themeFlags)
            return strings.getOrNull(index) ?: context.resources.getString(R.string.theme_auto)
        }

    init {
        updateTheme()
        wallpaperManager.addOnChangeListener(this)
    }

    fun addOverride(themeOverride: ThemeOverride) {
        synchronized(listeners) {
            removeDeadListeners()
            listeners.add(themeOverride)
        }
        themeOverride.applyTheme(context)
    }

    private fun removeDeadListeners() {
        val it = listeners.iterator()
        while (it.hasNext()) {
            if (!it.next().isAlive) {
                it.remove()
            }
        }
    }

    override fun onColorsChanged(colors: WallpaperColorsCompat?, which: Int) {
        updateTheme()
    }

    private fun updateTheme(accentUpdated: Boolean = false) {
        val newTheme = prefs.profileTheme.getValue()
        if (newTheme == themeFlags && !accentUpdated) return
        themeFlags = newTheme
        // TODO no listeners are added for now, either we keep this logic and use it in all classes or just use reloadActivities
        reloadActivities(forceUpdate = accentUpdated)
        synchronized(listeners) {
            removeDeadListeners()
            listeners.forEach { it.onThemeChanged(themeFlags) }
        }
    }

    private fun reloadActivities(forceUpdate: Boolean) {
        HashSet(app.activityHandler.activities).forEach {
            if (it is ThemeableActivity) {
                it.onThemeChanged(forceUpdate)
            } else {
                it.recreate()
            }
        }
    }

    fun updateNightMode(newConfig: Configuration) {
        usingNightMode = newConfig.usingNightMode
    }

    // TODO make all activities (including the desktop one) apply the chosen theme
    interface ThemeableActivity {
        var currentTheme: Int
        var currentAccent: Int
        fun onThemeChanged(forceUpdate: Boolean = false)
    }

    val isDarkTheme
        get() = prefs.profileTheme.getValue().let {
            when {
                it.hasFlag(THEME_SYSTEM) -> usingNightMode
                it.hasFlag(THEME_WALLPAPER) -> wallpaperManager.supportsDarkTheme
                else -> it.hasFlag(THEME_DARK)
            }
        }

    companion object :
        SingletonHolder<ThemeManager, Context>(ensureOnMainThread(useApplicationContext(::ThemeManager)))
}

val Context.isBlackTheme: Boolean
    get() = prefs.profileTheme.getValue().hasFlag(THEME_USE_BLACK)
