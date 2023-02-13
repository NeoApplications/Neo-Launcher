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
package com.saggitt.omega

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import com.android.launcher3.BaseActivity
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherAppState
import com.android.launcher3.Utilities
import com.android.systemui.plugins.shared.LauncherOverlayManager
import com.saggitt.omega.preferences.NLPrefs
import com.saggitt.omega.util.Config

class OmegaLauncher : Launcher() {

    val prefs: NLPrefs by lazy { Utilities.getOmegaPrefs(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = Config(this)
        config.setAppLanguage(prefs.profileLanguage.getValue())
        mOverlayManager = defaultOverlay
    }

    override fun getDefaultOverlay(): LauncherOverlayManager {
        if (mOverlayManager == null) {
            mOverlayManager = OverlayCallbackImpl(this)
        }
        return mOverlayManager
    }

    companion object {
        var showFolderNotificationCount = false

        @JvmStatic
        fun getLauncher(context: Context): OmegaLauncher {
            return context as? OmegaLauncher
                ?: (context as ContextWrapper).baseContext as? OmegaLauncher
                ?: LauncherAppState.getInstance(context).launcher as OmegaLauncher
        }
    }
}

val Context.launcher: OmegaLauncher
    get() = BaseActivity.fromContext(this)