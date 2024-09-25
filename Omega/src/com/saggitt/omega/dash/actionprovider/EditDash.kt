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
package com.saggitt.omega.dash.actionprovider

import android.content.Context
import com.android.launcher3.R
import com.saggitt.omega.compose.icons.Phosphor
import com.saggitt.omega.compose.icons.phosphor.PencilSimple
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.dash.DashActionProvider
import com.saggitt.omega.preferences.PreferenceActivity

class EditDash(context: Context) : DashActionProvider(context) {
    override val itemId = 5
    override val name = context.getString(R.string.edit_dash)
    override val description = context.getString(R.string.edit_dash_summary)
    override val icon = Phosphor.PencilSimple

    override fun runAction(context: Context) {
        context.startActivity(
            PreferenceActivity.navigateIntent(context, Routes.EDIT_DASH)
        )
    }
}