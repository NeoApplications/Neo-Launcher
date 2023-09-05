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

package com.saggitt.omega.search

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import androidx.core.content.res.ResourcesCompat
import com.android.launcher3.LauncherState
import com.android.launcher3.R
import com.android.launcher3.anim.AnimatorListeners
import com.saggitt.omega.nLauncher
import com.saggitt.omega.util.prefs

@Keep
class AppsSearchProvider(context: Context) : SearchProvider(context) {

    override val displayName: String = context.getString(R.string.search_provider_appsearch)
    override val supportsVoiceSearch = false
    override val supportsAssistant = false
    override val supportsFeed = false
    override val packageName: String
        get() = "AppsSearchProvider"


    override fun startSearch(context: Context, callback: (intent: Intent) -> Unit) {
        val launcher = context.nLauncher
        launcher.stateManager.goToState(
            LauncherState.ALL_APPS,
            true,
            AnimatorListeners.forEndCallback(Runnable { launcher.appsView.searchUiManager.startSearch() })
        )
    }

    override val iconRes: Int
        get() = R.drawable.ic_search
    override val icon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, iconRes, null)!!
            .mutate().apply { setTint(context.prefs.profileAccentColor.getColor()) }
}