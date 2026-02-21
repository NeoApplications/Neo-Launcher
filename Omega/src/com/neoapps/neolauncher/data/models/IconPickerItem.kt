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

package com.neoapps.neolauncher.data.models

import android.os.Parcelable
import com.neoapps.neolauncher.iconpack.IconEntry
import com.neoapps.neolauncher.iconpack.IconType
import kotlinx.parcelize.Parcelize

@Parcelize
data class IconPickerItem(
    val packPackageName: String,
    val drawableName: String,
    val label: String,
    val type: IconType,
) : Parcelable {
    fun toIconEntry(): IconEntry {
        return IconEntry(
            packPackageName = packPackageName,
            name = drawableName,
            type = type
        )
    }
}
