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

package com.saggitt.omega.smartspace

import android.content.IntentFilter
import com.saggitt.omega.util.Config

class ActionIntentFilter {

    companion object {
        fun googleInstance(vararg array: String?): IntentFilter {
            return newInstance(Config.GOOGLE_QSB, *array)
        }

        fun newInstance(s: String?, vararg array: String?): IntentFilter {
            val intentFilter = IntentFilter()
            val length = array.size
            var i = 0
            while (i < length) {
                intentFilter.addAction(array[i])
                ++i
            }
            intentFilter.addDataScheme("package")
            intentFilter.addDataSchemeSpecificPart(s, 0)
            return intentFilter
        }
    }

}