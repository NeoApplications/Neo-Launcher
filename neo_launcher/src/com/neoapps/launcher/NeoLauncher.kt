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

package com.neoapps.launcher

import android.content.Context
import android.content.ContextWrapper
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherAppState
import com.neoapps.launcher.preferences.NeoPrefs
import com.neoapps.launcher.util.CoreUtils


class NeoLauncher: Launcher() {
    val prefs: NeoPrefs by lazy { CoreUtils.getNeoPrefs() }

    companion object {
        @JvmStatic
        fun getLauncher(context: Context): NeoLauncher {
            return context as? NeoLauncher
                ?: (context as ContextWrapper).baseContext as? NeoLauncher
                ?: LauncherAppState.getInstance(context).launcher as NeoLauncher
        }
    }
}