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
 */

package com.neoapps.launcher.preferences

import com.android.launcher3.InvariantDeviceProfile
import com.neoapps.launcher.NeoLauncher

class PreferencesChangeCallback(val launcher: NeoLauncher) {
    private val idp: InvariantDeviceProfile
        get() = InvariantDeviceProfile.INSTANCE.get(launcher)

    fun reloadGrid() {
        idp.onPreferencesChanged(launcher)
    }

}