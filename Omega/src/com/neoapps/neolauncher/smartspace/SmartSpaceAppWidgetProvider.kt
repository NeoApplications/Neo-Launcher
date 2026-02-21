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

package com.neoapps.neolauncher.smartspace

import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import com.android.launcher3.BuildConfig

class SmartSpaceAppWidgetProvider : AppWidgetProvider() {

    companion object {
        @JvmField
        val componentName =
            ComponentName(BuildConfig.APPLICATION_ID, SmartSpaceAppWidgetProvider::class.java.name)
    }
}
