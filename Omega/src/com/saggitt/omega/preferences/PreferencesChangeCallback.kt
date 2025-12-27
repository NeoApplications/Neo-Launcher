/*
 * This file is part of Neo Launcher
 * Copyright (c) 2021   Neo Launcher Team
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

package com.saggitt.omega.preferences

import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import com.neoapps.neolauncher.NeoLauncher
import com.saggitt.omega.blur.BlurWallpaperProvider

class PreferencesChangeCallback(val launcher: NeoLauncher) {

    private val idp: InvariantDeviceProfile
        get() = InvariantDeviceProfile.INSTANCE.get(launcher)

    fun reloadGrid() {
        //idp.onPreferencesChanged(launcher)
    }

    fun reloadModel() {
        launcher.model.forceReload()
    }

    fun recreate() {
        MAIN_EXECUTOR.execute {
            launcher.recreateIfNotScheduled()
        }
    }

    fun scheduleRecreate() {
        MAIN_EXECUTOR.execute {
            launcher.scheduleRecreate()
        }
    }

    fun reloadTabs() {
        //launcher.appsView.reloadTabs()
    }

    fun updateBlur() {
        BlurWallpaperProvider.getInstance(launcher).updateAsync()
    }

    fun restart() {
        launcher.scheduleRestart()
    }
}