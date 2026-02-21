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
package com.neoapps.neolauncher.dash.actionprovider

import android.content.Context
import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherState
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.icons.Phosphor
import com.neoapps.neolauncher.compose.icons.phosphor.DotsNine
import com.neoapps.neolauncher.dash.DashActionProvider

class AllAppsShortcut(context: Context) : DashActionProvider(context) {
    override val itemId = 1
    override val name = context.getString(R.string.dash_all_apps_title)
    override val description = context.getString(R.string.dash_all_apps_summary)
    override val icon = Phosphor.DotsNine

    override fun runAction(context: Context) {
        val launcher = Launcher.getLauncher(context)
        AbstractFloatingView.closeAllOpenViews(launcher)
        if (launcher.isInState(LauncherState.ALL_APPS)) return
        launcher.stateManager.goToState(LauncherState.ALL_APPS)
    }
}