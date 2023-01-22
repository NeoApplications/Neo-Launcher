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

package com.saggitt.omega.util

import com.android.launcher3.R

class Config {
    companion object {

        //APP DRAWER SORT MODE
        const val SORT_AZ = 0
        const val SORT_ZA = 1
        const val SORT_MOST_USED = 2
        const val SORT_BY_COLOR = 3
        const val SORT_BY_INSTALL_DATE = 4

        val drawerSortOptions = mutableMapOf(
            Config.SORT_AZ to R.string.title__sort_alphabetical_az,
            Config.SORT_ZA to R.string.title__sort_alphabetical_za,
            Config.SORT_MOST_USED to R.string.title__sort_most_used,
            Config.SORT_BY_COLOR to R.string.title__sort_by_color,
            Config.SORT_BY_INSTALL_DATE to R.string.title__sort_last_installed,
        )
    }
}