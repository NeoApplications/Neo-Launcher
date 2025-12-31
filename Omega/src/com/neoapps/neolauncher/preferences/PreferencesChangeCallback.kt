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

package com.neoapps.neolauncher.preferences

import com.android.launcher3.dagger.LauncherComponentProvider.appComponent
import com.neoapps.neolauncher.NeoLauncher
import com.neoapps.neolauncher.blur.BlurWallpaperProvider

class PreferencesChangeCallback(val launcher: NeoLauncher) {

    fun reloadGrid() {
        launcher.appComponent.idp.onPreferencesChanged()
    }

    fun reloadModel() {
        launcher.model.forceReload()
    }

    fun recreate() {
        launcher.recreateIfNotScheduled()
    }

    fun reloadTabs() {
        //launcher.appsView.reloadTabs()
    }

    fun updateBlur() {
        BlurWallpaperProvider.getInstance(launcher).updateAsync()
    }

    fun restart() {
        reloadGrid()
        recreate()
    }
}